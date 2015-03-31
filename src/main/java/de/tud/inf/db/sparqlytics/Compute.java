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

package de.tud.inf.db.sparqlytics;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarAlloc;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.ExprVisitorBase;
import com.hp.hpl.jena.sparql.expr.ExprWalker;
import com.hp.hpl.jena.sparql.expr.aggregate.Aggregator;
import com.hp.hpl.jena.sparql.expr.aggregate.AggregatorFactory;
import com.hp.hpl.jena.sparql.syntax.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.riot.Lang;

/**
 * A compute operation computes the values of measures.
 *
 * @author Michael Rudolf
 */
public class Compute implements Operation {

    /**
     * The measures to compute.
     */
    private final List<Measure> measures;

    /**
     * Creates a new compute operation for the given measures.
     *
     * @param measures the measures to compute
     *
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public Compute(List<Measure> measures) {
        this.measures = new ArrayList<Measure>(measures);
    }

    @Override
    public void run(Session session) {
        Query query = createQuery(session);
        if (Main.getInstance().isDebug()) {
            query.serialize(new IndentedWriter(System.err));
        }
        QueryExecution exec = QueryExecutionFactory.sparqlService(
                session.getSPARQLEndpointURL(), query);
        Model model = exec.execConstruct();
        Lang outputFormat = session.getOutputFormat();
        if (outputFormat == null) {
            outputFormat = Lang.RDFXML;
        }
        if (!outputFormat.equals(Lang.RDFNULL)) {
            try {
                model.write(session.getOutput(), outputFormat.getLabel(), null);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    
    /**
     * Creates a new SPARQL query for computing the measures in the given 
     * session.
     * 
     * @param session the session to compute the measures in
     * @return the SPARQL query to use for computing
     * 
     * @throws NullPointerException if the argument is <code>null</code>
     */
    private Query createQuery(Session session) {
        //Allocate uniquely named variables for dimensions and measures
        Map<Dimension, Var> dimensionVariables = new HashMap<Dimension, Var>();
        VarAlloc dimensionVarAlloc = new VarAlloc(
                "_dimension");
        for (Dimension dimension : session.getDimensions()) {
            dimensionVariables.put(dimension, dimensionVarAlloc.allocVar());
        }
        Map<Measure, Pair<Var, Var>> measureVariables = 
                new HashMap<Measure, Pair<Var, Var>>();
        VarAlloc aggregatedMeasureVarAlloc = new VarAlloc(
                "_aggregatedmeasure");
        VarAlloc measureVarAlloc = new VarAlloc(
                "_measure");
        for (Measure measure : measures) {
            measureVariables.put(measure, Pair.of(measureVarAlloc.allocVar(), 
                    aggregatedMeasureVarAlloc.allocVar()));
        }
        
        //Prepare query with prologue and (named) graph URIs
        Query temp = session.getQuery();
        Query constructQuery = new Query();
        constructQuery.setResolver(temp.getResolver());
        constructQuery.setPrefixMapping(temp.getPrefixMapping());
        constructQuery.setPrefix("sl", "http://tu-dresden.de/sparqlytics/");
        for (String uri : temp.getGraphURIs()) {
            constructQuery.addGraphURI(uri);
        }
        for (String uri : temp.getNamedGraphURIs()) {
            constructQuery.addNamedGraphURI(uri);
        }
        
        //CONSTRUCT part
        BasicPattern bp = new BasicPattern();
        for (Measure measure : measures) {
            Node measureNode = NodeFactory.createAnon();
            bp.add(new Triple(measureNode, 
                    NodeFactory.createURI("sl:measureName"), 
                    NodeFactory.createLiteral(measure.getName())));
            bp.add(new Triple(measureNode, 
                    NodeFactory.createURI("sl:measureValue"), 
                    measureVariables.get(measure).getRight()));
            for (Dimension dimension : session.getDimensions()) {
                Node levelNode = NodeFactory.createAnon();
                bp.add(new Triple(measureNode, 
                        NodeFactory.createURI("sl:inLevel"), levelNode));
                bp.add(new Triple(levelNode, 
                        NodeFactory.createURI("sl:levelMember"), 
                        dimensionVariables.get(dimension)));
            }
        }
        Template template = new Template(bp);
        constructQuery.setConstructTemplate(template);
        constructQuery.setQueryConstructType();
        
        //Outer SELECT subquery for aggregating computed measure values
        Query aggregateQuery = new Query();
        aggregateQuery.setSyntax(Syntax.syntaxSPARQL_11);
        aggregateQuery.setQuerySelectType();
        for (Measure measure : measures) {
            ExprVar expr = new ExprVar(measureVariables.get(measure).getLeft());
            Aggregator aggregator = createAggregator(
                    measure.getAggregationFunction(), expr);
            Var variable = measureVariables.get(measure).getRight();
            aggregateQuery.addResultVar(variable, new ExprAggregator(variable, aggregator));
        }
        for (Dimension dimension : session.getDimensions()) {
            aggregateQuery.addResultVar(dimensionVariables.get(dimension));
        }
        
        //Inner SELECT subquery for computing measure values
        Query computeQuery = new Query();
        computeQuery.setSyntax(Syntax.syntaxSPARQL_11);
        computeQuery.setQuerySelectType();
        Element factPattern = session.getFactPattern();
        Collection<Var> factPatternVars = PatternVars.vars(factPattern);
        for (Var var : factPatternVars) {
            computeQuery.addResultVar(var);
        }
        AggregationDetector detector = new AggregationDetector();
        for (Dimension dimension : session.getDimensions()) {
            Var variable = dimensionVariables.get(dimension);
            Level level = dimension.getLevels().get(session.getGranularity(dimension));
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
        for (Dimension dimension : session.getDimensions()) {
            Level level = dimension.getLevels().get(session.getGranularity(dimension));
            if (detector.isAggregating(level.getExpression())) {
                computeGroup.addElement(new ElementOptional(dimension.getSeedPattern()));
            } else {
                ElementGroup dimensionGroup = new ElementGroup();
                dimensionGroup.addElement(dimension.getSeedPattern());
                dimensionGroup.addElement(new ElementBind(
                        dimensionVariables.get(dimension), level.getExpression()));
                computeGroup.addElement(new ElementOptional(dimensionGroup));
            }
        }
        for (Measure measure : measures) {
            computeGroup.addElement(measure.getSeedPattern());
            if (!detector.isAggregating(measure.getExpression())) {
                computeGroup.addElement(new ElementBind(
                        measureVariables.get(measure).getLeft(), measure.getExpression()));
            }
        }
        for (Map.Entry<Pair<Dimension, Level>, Filter> filter : session.getFilters().entrySet()) {
            Level level = filter.getKey().getRight();
            if (!detector.isAggregating(level.getExpression())) {
                Var variable = filter.getValue().getVariable();
                computeGroup.addElement(new ElementBind(variable, level.getExpression()));
            }
        }
        computeQuery.setQueryPattern(computeGroup);
        
        //Inner GROUP BY part
        for (Var var : factPatternVars) {
            computeQuery.addGroupBy(var);
        }
        for (Dimension dimension : session.getDimensions()) {
            Level level = dimension.getLevels().get(session.getGranularity(dimension));
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
            aggregateGroup.addElementFilter(new ElementFilter(filter.getValue().getPredicate()));
        }
        
        //Insert compute query as inner WHERE part into aggregation query
        aggregateGroup.addElement(new ElementSubQuery(computeQuery));
        aggregateQuery.setQueryPattern(aggregateGroup);

        //Outer GROUP BY part
        for (Dimension dimension : session.getDimensions()) {
            aggregateQuery.addGroupBy(dimensionVariables.get(dimension));
        }
        
        //Insert aggregation query as outer WHERE part into construct query
        constructQuery.setQueryPattern(new ElementSubQuery(aggregateQuery));
        return constructQuery;
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
    private Aggregator createAggregator(String aggregationFunction, Expr expr) {
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
        private final Map<Expr, Boolean> cache = new HashMap<Expr, Boolean>();
        
        /**
         * Indicates whether an aggregation was detected.
         */
        private boolean aggregation = false;

        @Override
        public void visit(ExprAggregator eAgg) {
            aggregation = true;
        }
        
        /**
         * Returns whether the given expression performs an aggregation.
         * 
         * @return whether an aggregation was detected in the expression
         */
        public boolean isAggregating(Expr expr) {
            Boolean result = cache.get(expr);
            if (result == null) {
                aggregation = false;
                ExprWalker.walk(this, expr);
                result = Boolean.valueOf(aggregation);
                cache.put(expr, result);
            }
            return result.booleanValue();
        }
    }
}
