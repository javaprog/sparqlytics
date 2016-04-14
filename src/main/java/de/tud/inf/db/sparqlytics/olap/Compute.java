// SPARQLytics: Multidimensional Analytics for RDF Data.
// Copyright (C) 2015  Michael Rudolf
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package de.tud.inf.db.sparqlytics.olap;

import de.tud.inf.db.sparqlytics.Main;
import de.tud.inf.db.sparqlytics.model.Measure;
import de.tud.inf.db.sparqlytics.model.Level;
import de.tud.inf.db.sparqlytics.model.Session;
import de.tud.inf.db.sparqlytics.model.Dimension;
import de.tud.inf.db.sparqlytics.model.Filter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.riot.Lang;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarAlloc;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.ExprVisitorBase;
import org.apache.jena.sparql.expr.ExprWalker;
import org.apache.jena.sparql.expr.aggregate.Aggregator;
import org.apache.jena.sparql.expr.aggregate.AggregatorFactory;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.apache.jena.sparql.syntax.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A compute operation computes the values of measures.
 *
 * @author Michael Rudolf
 */
public class Compute implements Operation {
    /**
     * Logs the generated SPARQL queries.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Compute.class);
    
    /**
     * Represents a sort condition consisting of a dimension or measure name and
     * an order.
     */
    public static class SortCondition {
        /**
         * The name of the dimension or measure according to which should be
         * sorted.
         */
        private final String name;
        
        /**
         * The order that should be applied to this sort condition.
         */
        private final int direction;
        
        /**
         * Creates a new sort condition for the given name of the dimension or
         * measure and the ordering that should be applied to it.
         * 
         * @param name      the name of the dimension or measure according to
         *                  which should be sorted
         * @param direction the order that should be applied to this sort
         *                  condition
         */
        public SortCondition(final String name, final int direction) {
            this.name = name;
            this.direction = direction;
        }
        
        /**
         * Returns the name of the dimension or measure according to which
         * should be sorted.
         * 
         * @return the name of the dimension or measure
         */
        public String getName() {
            return name;
        }
        
        /**
         * Returns the order that should be applied to this sort condition.
         * 
         * @return  the order, one of {@link Query#ORDER_ASCENDING},
         *          {@link Query#ORDER_ASCENDING}, and {@link Query#ORDER_DEFAULT}
         */
        public int getDirection() {
            return direction;
        }
    }

    /**
     * The measures to compute.
     */
    private final List<Measure> measures;
    
    /**
     * The conditions for ordering the computed measures.
     */
    private final List<SortCondition> sortConditions;
    
    /**
     * The limit until which to compute measures. A limit is only meaningful in
     * combination with sort conditions.
     */
    private final Long limit;
    
    /**
     * The offset from which to start computing measures. An offset is only
     * meaningful in combination with sort conditions.
     */
    private final Long offset;

    /**
     * Creates a new compute operation for the given measures.
     *
     * @param measures          the measures to compute
     * @param sortConditions    the ordering to apply, may be empty
     * @param limit             the limit until which to compute measures, may
     *                          be {@code null}
     * @param offset            the offset from which to compute measures, may
     *                          be {@code null}
     *
     * @throws NullPointerException if either argument {@code measures} or
     *                              {@code sortConditions} is {@code null}
     */
    public Compute(final List<Measure> measures,
            final List<SortCondition> sortConditions,
            final Long limit, final Long offset) {
        this.measures = new ArrayList<>(measures);
        this.sortConditions = new ArrayList<>(sortConditions);
        this.limit = limit;
        this.offset = offset;
    }

    @Override
    public void run(final Session session) {
        ResultsFormat resultsFormat = session.getResultsFormat();
        if (resultsFormat == null) {
            resultsFormat = ResultsFormat.FMT_RDF_XML;
        }
        
        //Create SPARQL query and measure elapsed time
        Timer createQuery = Main.METRICS.timer(
                MetricRegistry.name(Compute.class, "createQuery"));
        long creationTime;
        Query query;
        Timer.Context time = createQuery.time();
        try {
            query = createQuery(session,
                    resultsFormat != ResultsFormat.FMT_RS_CSV &&
                    resultsFormat != ResultsFormat.FMT_RS_TSV);
        } finally {
            creationTime = time.stop();
        }
        createQuery.update(creationTime, TimeUnit.NANOSECONDS);

        //Measure query length
        String queryString = query.toString();
        Histogram plainQueryLength = Main.METRICS.histogram(
                MetricRegistry.name(Compute.class, "plainQueryLength"));
        plainQueryLength.update(queryString.length());
        IndentedLineBuffer buffer = new IndentedLineBuffer();
        query.serialize(buffer);
        String indentedQueryString = buffer.toString();
        Histogram indentedQueryLength = Main.METRICS.histogram(
                MetricRegistry.name(Compute.class, "indentedQueryLength"));
        indentedQueryLength.update(buffer.getRow() - 1);

        //Execute SPARQL query and measure elapsed time and result size
        Timer executeQuery = Main.METRICS.timer(
                MetricRegistry.name(Compute.class, "executeQuery"));
        long executionTime;
        Histogram resultSize = Main.METRICS.histogram(
                MetricRegistry.name(Compute.class, "resultSize"));
        QueryEngineHTTP exec = (QueryEngineHTTP)QueryExecutionFactory.sparqlService(
                session.getSPARQLEndpointURL(), queryString);
        exec.setModelContentType(WebContent.contentTypeRDFXML);
        if (query.isConstructType()) {
            Model model;
            time = executeQuery.time();
            try {
                model = exec.execConstruct();
            } catch (RuntimeException ex) {
                throw extendRuntimeException(ex, indentedQueryString);
            } finally {
                executionTime = time.stop();
                exec.close();
            }

            try {
                resultSize.update(model.size());

                //Possibly output result
                if (resultsFormat != ResultsFormat.FMT_NONE) {
                    Lang lang = ResultsFormat.convert(resultsFormat);
                    if (lang == null) {
                        lang = RDFLanguages.contentTypeToLang(resultsFormat.getSymbol());
                    }
                    try (OutputStream output = session.getOutput()) {
                        model.write(output, lang == null ? null : lang.getLabel(), null);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            } finally {
                model.close();
            }
        } else {
            ResultSet result;
            time = executeQuery.time();
            try {
                result = exec.execSelect();
            } catch (RuntimeException ex) {
                throw extendRuntimeException(ex, indentedQueryString);
            } finally {
                executionTime = time.stop();
            }

            //Possibly output result
            try {
                if (resultsFormat != ResultsFormat.FMT_NONE) {
                    try (OutputStream output = session.getOutput()) {
                        ResultSetFormatter.output(output, result, resultsFormat);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    resultSize.update(result.getRowNumber());
                } else {
                    resultSize.update(ResultSetFormatter.consume(result));
                }
            } finally {
                exec.close();
            }
        }
        executeQuery.update(executionTime, TimeUnit.NANOSECONDS);
        LOG.debug("{}\n\nCreation {} us, Execution {} us", indentedQueryString,
                TimeUnit.NANOSECONDS.toMicros(creationTime),
                TimeUnit.NANOSECONDS.toMicros(executionTime));
        
        if (Main.getInstance().isDebug()) {
            System.err.print(indentedQueryString);
        }
    }

    /**
     * Extends the given runtime exception with the given query string.
     * 
     * @param ex    the exception to extend
     * @param query the query to add to the exception message
     * @return the extended exception
     */
    protected RuntimeException extendRuntimeException(RuntimeException ex,
            String query) {
        StringBuilder builder = new StringBuilder();
        String message = ex.getMessage();
        if (message != null) {
            builder.append(message).append(System.lineSeparator());
        }
        builder.append(ex.getClass().getSimpleName()).
                append(" caused by query:").append(System.lineSeparator());
        builder.append(query);
        RuntimeException extended = new RuntimeException(builder.toString(),
                ex.getCause());
        extended.setStackTrace(ex.getStackTrace());
        return extended;
    }

    /**
     * Creates a new SPARQL query for computing the measures in the given
     * session.
     *
     * @param session   the session to compute the measures in
     * @param construct whether to create a construct instead of a select query
     * @return the SPARQL query to use for computing
     *
     * @throws NullPointerException if the argument {@code session} is
     *                              {@code null}
     */
    private Query createQuery(final Session session, boolean construct) {
        //Allocate uniquely named variables for dimensions and measures
        Map<Dimension, Var> dimensionVariables = new HashMap<>();
        VarAlloc dimensionVarAlloc = new VarAlloc(
                "_dimension");
        for (Dimension dimension : session.getCube().getDimensions()) {
            dimensionVariables.put(dimension, dimensionVarAlloc.allocVar());
        }
        Map<Measure, Pair<Var, Var>> measureVariables = new HashMap<>();
        VarAlloc aggregatedMeasureVarAlloc = new VarAlloc(
                "_aggregatedmeasure");
        VarAlloc measureVarAlloc = new VarAlloc(
                "_measure");
        for (Measure measure : measures) {
            measureVariables.put(measure, Pair.of(measureVarAlloc.allocVar(),
                    aggregatedMeasureVarAlloc.allocVar()));
        }

        //Create query and fill in prologue and (named) graph URIs
        Query temp = session.getQuery();
        Query query = construct ?
                createConstructQuery(session, dimensionVariables, measureVariables) :
                createSelectQuery(session, dimensionVariables, measureVariables);
        query.setResolver(temp.getResolver());
        query.setPrefixMapping(temp.getPrefixMapping());
        query.setPrefix("sl", "http://tu-dresden.de/sparqlytics/");
        for (String uri : temp.getGraphURIs()) {
            query.addGraphURI(uri);
        }
        for (String uri : temp.getNamedGraphURIs()) {
            query.addNamedGraphURI(uri);
        }
        return query;
    }

    /**
     * Helper method for creating the CONSTRUCT SPARQL query body for computing
     * the measures in the given session.
     * 
     * @param session               the session to compute the measures in
     * @param dimensionVariables    the allocated dimension level variables
     * @param measureVariables      the allocated aggregated measure variables
     * @return the created CONSTRUCT SPARQL query body
     */
    protected Query createConstructQuery(final Session session,
            final Map<Dimension, Var> dimensionVariables,
            final Map<Measure, Pair<Var, Var>> measureVariables) {
        //CONSTRUCT part
        BasicPattern bp = new BasicPattern();
        for (Measure measure : measures) {
            Node measureNode = NodeFactory.createBlankNode();
            bp.add(new Triple(measureNode,
                    NodeFactory.createURI("sl:measureName"),
                    NodeFactory.createLiteral(measure.getName())));
            bp.add(new Triple(measureNode,
                    NodeFactory.createURI("sl:measureValue"),
                    measureVariables.get(measure).getRight()));
            for (Dimension dimension : session.getCube().getDimensions()) {
                Node levelNode = NodeFactory.createBlankNode();
                bp.add(new Triple(measureNode,
                        NodeFactory.createURI("sl:inLevel"), levelNode));
                bp.add(new Triple(levelNode,
                        NodeFactory.createURI("sl:levelMember"),
                        dimensionVariables.get(dimension)));
            }
        }
        Query constructQuery = new Query();
        constructQuery.setConstructTemplate(new Template(bp));
        constructQuery.setQueryConstructType();

        //Insert aggregation query as outer WHERE part into construct query
        constructQuery.setQueryPattern(new ElementSubQuery(createSelectQuery(
                session, dimensionVariables, measureVariables)));
        return constructQuery;
    }
    
    /**
     * Helper method for creating the SELECT SPARQL query body for computing
     * the measures in the given session.
     * 
     * @param session               the session to compute the measures in
     * @param dimensionVariables    the allocated dimension level variables
     * @param measureVariables      the allocated aggregated measure variables
     * @return the created SELECT SPARQL query body
     */
    protected Query createSelectQuery(final Session session,
            final Map<Dimension, Var> dimensionVariables,
            final Map<Measure, Pair<Var, Var>> measureVariables) {
        //Outer SELECT subquery for aggregating computed measure values
        Query aggregateQuery = new Query();
        aggregateQuery.setSyntax(Syntax.syntaxSPARQL_11);
        aggregateQuery.setQuerySelectType();
        for (Measure measure : measures) {
            ExprVar expr = new ExprVar(measureVariables.get(measure).getLeft());
            Aggregator aggregator = createAggregator(
                    measure.getAggregationFunction(), expr);
            Var variable = measureVariables.get(measure).getRight();
            aggregateQuery.addResultVar(variable,
                    new ExprAggregator(variable, aggregator));
        }
        for (Dimension dimension : session.getCube().getDimensions()) {
            aggregateQuery.addResultVar(dimensionVariables.get(dimension));
        }

        //Inner SELECT subquery for computing measure values
        Query computeQuery = new Query();
        computeQuery.setSyntax(Syntax.syntaxSPARQL_11);
        computeQuery.setQuerySelectType();
        Element factPattern = session.getCube().getFactPattern();
        Collection<Var> factPatternVars = PatternVars.vars(factPattern);
        for (Var var : factPatternVars) {
            if (var.isNamedVar()) {
                computeQuery.addResultVar(var);
            }
        }
        AggregationDetector detector = new AggregationDetector();
        for (Dimension dimension : session.getCube().getDimensions()) {
            Var variable = dimensionVariables.get(dimension);
            Level level = dimension.getLevels().get(
                    session.getGranularity(dimension));
            if (detector.isAggregating(level.getExpression())) {
                computeQuery.addResultVar(variable, level.getExpression());
            } else {
                computeQuery.addResultVar(variable);
            }
        }
        for (Measure measure : measures) {
            Var variable = measureVariables.get(measure).getLeft();
            if (detector.isAggregating(measure.getExpression())) {
                computeQuery.addResultVar(variable, measure.getExpression());
            } else {
                computeQuery.addResultVar(variable);
            }
        }
        for (Map.Entry<Pair<Dimension, Level>, Filter> filter : session.getFilters().entrySet()) {
            Var variable = filter.getValue().getVariable();
            Level level = filter.getKey().getRight();
            if (detector.isAggregating(level.getExpression())) {
                computeQuery.addResultVar(variable, level.getExpression());
            } else {
                computeQuery.addResultVar(variable);
            }
        }
        ElementGroup computeGroup = new ElementGroup();
        computeGroup.addElement(factPattern);
        for (Dimension dimension : session.getCube().getDimensions()) {
            Level level = dimension.getLevels().get(
                    session.getGranularity(dimension));
            if (detector.isAggregating(level.getExpression())) {
                computeGroup.addElement(
                        new ElementOptional(dimension.getSeedPattern()));
            } else {
                ElementGroup dimensionGroup = new ElementGroup();
                dimensionGroup.addElement(dimension.getSeedPattern());
                dimensionGroup.addElement(new ElementBind(
                        dimensionVariables.get(dimension),
                        level.getExpression()));
                computeGroup.addElement(new ElementOptional(dimensionGroup));
            }
        }
        for (Measure measure : measures) {
            computeGroup.addElement(measure.getSeedPattern());
            if (!detector.isAggregating(measure.getExpression())) {
                computeGroup.addElement(new ElementBind(
                        measureVariables.get(measure).getLeft(),
                        measure.getExpression()));
            }
        }
        for (Map.Entry<Pair<Dimension, Level>, Filter> filter : session.getFilters().entrySet()) {
            Level level = filter.getKey().getRight();
            if (!detector.isAggregating(level.getExpression())) {
                Var variable = filter.getValue().getVariable();
                computeGroup.addElement(
                        new ElementBind(variable, level.getExpression()));
            }
        }
        computeQuery.setQueryPattern(computeGroup);

        //Inner GROUP BY part
        for (Var var : factPatternVars) {
            if (var.isNamedVar()) {
                computeQuery.addGroupBy(var);
            }
        }
        for (Dimension dimension : session.getCube().getDimensions()) {
            Level level = dimension.getLevels().get(
                    session.getGranularity(dimension));
            if (!detector.isAggregating(level.getExpression())) {
                computeQuery.addGroupBy(dimensionVariables.get(dimension));
            }
        }
        for (Measure measure : measures) {
            if (!detector.isAggregating(measure.getExpression())) {
                computeQuery.addGroupBy(measureVariables.get(measure).getLeft());
            }
        }
        for (Map.Entry<Pair<Dimension, Level>, Filter> filter : session.getFilters().entrySet()) {
            Level level = filter.getKey().getRight();
            if (!detector.isAggregating(level.getExpression())) {
                computeQuery.addGroupBy(filter.getValue().getVariable());
            }
        }

        //Filter in aggregation query
        ElementGroup aggregateGroup = new ElementGroup();
        for (Map.Entry<Pair<Dimension, Level>, Filter> filter : session.getFilters().entrySet()) {
            aggregateGroup.addElementFilter(
                    new ElementFilter(filter.getValue().getPredicate()));
        }

        //Insert compute query as inner WHERE part into aggregation query
        aggregateGroup.addElement(new ElementSubQuery(computeQuery));
        aggregateQuery.setQueryPattern(aggregateGroup);

        //Outer GROUP BY part
        for (Dimension dimension : session.getCube().getDimensions()) {
            aggregateQuery.addGroupBy(dimensionVariables.get(dimension));
        }
        
        //Sort order and limits
        for (SortCondition sortCondition : sortConditions) {
            Var var;
            try {
                Dimension dimension = session.getCube().findDimension(
                        sortCondition.getName());
                var = dimensionVariables.get(dimension);
            } catch (NoSuchElementException ex) {
                Measure measure = session.getCube().findMeasure(
                        sortCondition.getName());
                var = measureVariables.get(measure).getRight();
            }
            aggregateQuery.addOrderBy(var, sortCondition.getDirection());
        }
        if (limit != null) {
            aggregateQuery.setLimit(limit);
        }
        if (offset != null) {
            aggregateQuery.setOffset(offset);
        }
        return aggregateQuery;
    }

    /**
     * Creates an aggregator implementing the given aggregation function over
     * the given expression.
     *
     * @param aggregationFunction   the aggregation function to create an
     *                              aggregator for
     * @param expr                  the expression to aggregate over
     * @return the produced aggregator
     *
     * @throws IllegalArgumentException if the aggregation function is not
     *                                  supported
     */
    private Aggregator createAggregator(final String aggregationFunction,
            final Expr expr) {
        switch (aggregationFunction) {
            case "COUNT":
                return AggregatorFactory.createCountExpr(false, expr);
            case "SUM":
                return AggregatorFactory.createSum(false, expr);
            case "MIN":
                return AggregatorFactory.createMin(false, expr);
            case "MAX":
                return AggregatorFactory.createMax(false, expr);
            case "AVG":
                return AggregatorFactory.createAvg(false, expr);
            case "SAMPLE":
                return AggregatorFactory.createSample(false, expr);
            default:
                throw new IllegalArgumentException(
                        "Unsupported aggregation function: " + aggregationFunction);
        }
    }

    /**
     * Helper class for detecting whether an expression contains an aggregation.
     */
    private static class AggregationDetector extends ExprVisitorBase {
        /**
         * Caches the results of detection runs.
         */
        private final Map<Expr, Boolean> cache = new HashMap<>();

        /**
         * Indicates whether an aggregation was detected.
         */
        private boolean aggregation = false;

        @Override
        public void visit(final ExprAggregator eAgg) {
            aggregation = true;
        }

        /**
         * Returns whether the given expression performs an aggregation.
         *
         * @param expr the expression to test for an aggregation operation
         * @return whether an aggregation was detected in the expression
         */
        public boolean isAggregating(final Expr expr) {
            Boolean result = cache.get(expr);
            if (result == null) {
                aggregation = false;
                ExprWalker.walk(this, expr);
                result = aggregation;
                cache.put(expr, result);
            }
            return result;
        }
    }
}
