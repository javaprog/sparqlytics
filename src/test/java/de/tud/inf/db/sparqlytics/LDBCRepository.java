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

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.aggregate.AggregatorFactory;
import com.hp.hpl.jena.sparql.lang.arq.ARQParser;
import com.hp.hpl.jena.sparql.lang.arq.ARQParserTokenManager;
import com.hp.hpl.jena.sparql.lang.arq.ParseException;
import com.hp.hpl.jena.sparql.lang.arq.Token;
import com.hp.hpl.jena.sparql.lang.arq.TokenMgrError;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
import com.hp.hpl.jena.sparql.util.ExprUtils;
import com.hp.hpl.jena.vocabulary.RDF;
import de.tud.inf.db.sparqlytics.model.Cube;
import de.tud.inf.db.sparqlytics.model.Dimension;
import de.tud.inf.db.sparqlytics.model.Level;
import de.tud.inf.db.sparqlytics.model.Measure;
import de.tud.inf.db.sparqlytics.repository.DefaultRepository;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains cube definitions for the LDBC Social Network Benchmark.
 *
 * @author Michael Rudolf
 */
public final class LDBCRepository extends DefaultRepository {
    /**
     * The prefix mapping used in the definition of dimensions and measures.
     */
    private final PrefixMapping prefixes = PrefixMapping.Factory.create().
            setNsPrefix("rdf", RDF.getURI()).
            setNsPrefix("snvoc", "http://www.ldbc.eu/ldbc_socialnet/1.0/vocabulary/").
            setNsPrefix("dbpedia", "http://dbpedia.org/resource/");
    
    public LDBCRepository() {
        final Dimension person_isLocatedIn = addDimension("Person::isLocatedIn",
                "?person  snvoc:isLocatedIn ?city . " +
                "?city    snvoc:isPartOf    ?country . " +
                "?country snvoc:isPartOf    ?continent",
                new Level("City", new ExprVar("city")),
                new Level("Country", new ExprVar("country")),
                new Level("Continent", new ExprVar("continent"))
        );

        final Dimension university_isLocatedIn = addDimension("University::isLocatedIn",
                "?university snvoc:isLocatedIn ?city . " +
                "?city       snvoc:isPartOf    ?country . " +
                "?country    snvoc:isPartOf    ?continent",
                person_isLocatedIn.getLevels().subList(0, 3).toArray(new Level[0])
        );

        final Dimension company_isLocatedIn = addDimension("Company::isLocatedIn",
                "?company snvoc:isLocatedIn ?country . " +
                "?country snvoc:isPartOf    ?continent",
                new Level("Country", new ExprVar("country")),
                new Level("Continent", new ExprVar("continent"))
        );

        final Dimension message_isLocatedIn = addDimension("Message::isLocatedIn",
                "?message snvoc:isLocatedIn ?country . " +
                "?country snvoc:isPartOf    ?continent",
                company_isLocatedIn.getLevels().subList(0, 2).toArray(new Level[0])
        );
        
        final Dimension person_gender = addDimension("Person::gender",
                "?person snvoc:gender ?gender",
                new Level("Gender", new ExprVar("gender")));
        
        final Dimension person_birthday = addDimension("Person::birthday",
                "?person snvoc:birthday ?date",
                new Level("Day", ExprUtils.parse("DAY($date)")),
                new Level("Month", ExprUtils.parse("MONTH($date)")),
                new Level("Year", ExprUtils.parse("YEAR($date)")));
        
        Query subqueryEmails = new Query();
        subqueryEmails.setQuerySelectType();
        subqueryEmails.setQueryPattern(parseElement("?person snvoc:email ?email"));
        subqueryEmails.addResultVar("person");
        subqueryEmails.addResultVar("emails", new ExprAggregator(Var.alloc("email"),
                AggregatorFactory.createCount(false)));
        subqueryEmails.addGroupBy("person");
        final Dimension person_emails = new Dimension("Person::emails",
                new ElementSubQuery(subqueryEmails), Collections.singletonList(
                        new Level("Emails", new ExprVar("emails"))
                ));
        addDimension(person_emails);
        
        Query subqueryLanguages = new Query();
        subqueryLanguages.setQuerySelectType();
        subqueryLanguages.setQueryPattern(parseElement("?person snvoc:speaks ?language"));
        subqueryLanguages.addResultVar("person");
        subqueryLanguages.addResultVar("languages", new ExprAggregator(Var.alloc("language"),
                AggregatorFactory.createCount(false)));
        subqueryLanguages.addGroupBy("person");
        final Dimension person_language = new Dimension("Person::languages",
                new ElementSubQuery(subqueryLanguages), Collections.singletonList(
                        new Level("Languages", new ExprVar("languages"))
                ));
        addDimension(person_language);
        
        Query subqueryInterest = new Query();
        subqueryInterest.setQuerySelectType();
        subqueryInterest.setQueryPattern(parseElement("?person snvoc:hasInterest ?tag"));
        subqueryInterest.addResultVar("person");
        subqueryInterest.addResultVar("tags", new ExprAggregator(Var.alloc("tag"),
                AggregatorFactory.createCount(false)));
        subqueryInterest.addGroupBy("person");
        final Dimension person_interest = new Dimension("Person::interest",
                new ElementSubQuery(subqueryInterest), Collections.singletonList(
                        new Level("Interests", new ExprVar("tags"))
                ));
        addDimension(person_interest);
        
        final Dimension person_browserUsed = addDimension("Person::browserUsed",
                "?person snvoc:browserUsed ?browser",
                new Level("Browser", ExprUtils.parse("$browser")));
        
        final Measure person_age_avg = addMeasure("Person::age::avg",
                "?person snvoc:birthday ?birthday",
                "YEAR(NOW()) - YEAR(?birthday)", "AVG");
        
        final Measure person_languages_avg = addMeasure("Person::languages::avg",
                "?person snvoc:speaks ?language", "COUNT(DISTINCT ?language)", "AVG");
        
        final Measure person_interest_avg = addMeasure("Person::languages::avg",
                "?person snvoc:hasInterest ?tag", "COUNT(DISTINCT ?tag)", "AVG");
        
        addCube("Person", "?person rdf:type snvoc:Person",
                Arrays.asList(
                        person_isLocatedIn, person_gender, person_birthday,
                        person_emails, person_interest, person_browserUsed,
                        person_interest),
                Arrays.asList(
                        person_age_avg, person_languages_avg,
                        person_interest_avg));
        
        final Dimension message_browserUsed = addDimension("Message::browserUsed",
                "?message snvoc:browserUsed ?browser",
                new Level("Browser", ExprUtils.parse("$browser")));
        
        final Dimension post_containedIn = addDimension("Post::containedIn",
                "?forum snvoc:containerOf ?message",
                new Level("Forum", new ExprVar("forum")));
        
        final Dimension post_language = addDimension("Post::language",
                "?message snvoc:language ?language",
                new Level("Language", new ExprVar("language")));
        
        final Measure post_languages_avg = addMeasure("Post::languages::avg",
                "?message snvoc:language ?language", "COUNT(DISTINCT ?language)", "AVG");
        
        addCube("Post", "?message rdf:type snvoc:Post",
                Arrays.asList(
                        message_browserUsed, post_containedIn, post_language),
                Arrays.asList(post_languages_avg));
        
        final Dimension comment_replyOf = addDimension("Comment::replyOf",
                "?message snvoc:replyOf ?origMessage",
                new Level("Reply of", new ExprVar("origMessage")));
        
        final Measure comment_length_avg = addMeasure("Comment::length::avg",
                "?message snvoc:length ?length", "COUNT(DISTINCT ?length)", "AVG");
        
        addCube("Comment", "?message rdf:type snvoc:Comment",
                Arrays.asList(
                        message_browserUsed, comment_replyOf),
                Arrays.asList(comment_length_avg));
        
        final Dimension tag_isSubclassOf = addDimension("Tag::isSubclassOf",
                "?tag snvoc:isSubclassOf ?parent",
                new Level("Parent", new ExprVar("parent")));
    }
    
    /**
     * Helper method for adding dimensions to this repository.
     * 
     * @param name      the dimension's name
     * @param bgp       the dimension's seed pattern
     * @param levels    the levels in the dimension
     * @return the created dimension
     * 
     * @throws NullPointerException if any argument is {@code null}
     */
    private Dimension addDimension(String name, String bgp, Level... levels) {
        Dimension dimension = new Dimension(name, parseElement(bgp),
                Arrays.asList(levels));
        addDimension(dimension);
        return dimension;
    }
    
    /**
     * Helper method for adding measures to this repository.
     * 
     * @param name                  the measure's name
     * @param bgp                   the measure's seed pattern
     * @param expr                  the expression for deriving the numeric
     *                              value
     * @param aggregationFunction   the aggregation function to use
     * @return the created measure
     * 
     * @throws NullPointerException if any argument is {@code null}
     */
    private Measure addMeasure(String name, String bgp, String expr,
            String aggregationFunction) {
        Measure measure = new Measure(name, parseElement(bgp), parseExpr(expr),
                aggregationFunction);
        addMeasure(measure);
        return measure;
    }
    
    /**
     * Helper method for adding cubes to this repository.
     * 
     * @param name          the cube's name
     * @param bgp           the cube's fact pattern
     * @param dimensions    the dimensions in the cube
     * @param measures      the measures in the cube
     * 
     * @throws NullPointerException if any argument is {@code null}
     */
    private void addCube(String name, String bgp,
            Collection<Dimension> dimensions, Collection<Measure> measures) {
        addCube(new Cube(name, parseElement(bgp),
                dimensions instanceof Set ?
                        (Set<Dimension>) dimensions : new HashSet<>(dimensions),
                measures instanceof Set ?
                        (Set<Measure>) measures : new HashSet<>(measures)
        ));
    }
    
    /**
     * Helper method that parses the given expression.
     * 
     * @param expr the expression to parse
     * @return the parsed expression
     */
    private Expr parseExpr(String expr) {
        ARQParser parser = createParser(expr);
        try {
            Expr expression = parser.Expression();
            Token t = parser.getNextToken();
            if (t.kind != ARQParserTokenManager.EOF) {
                throw new QueryParseException("Extra tokens beginning \"" +
                        t.image + "\" starting line " + t.beginLine +
                        ", column " + t.beginColumn, t.beginLine, t.beginColumn);
            }
            return expression;
        } catch (ParseException ex) {
            throw new QueryParseException(ex.getMessage(),
                    ex.currentToken.beginLine, ex.currentToken.beginLine);
        } catch (TokenMgrError tErr) {
            throw new QueryParseException(tErr.getMessage(), -1, -1) ;
        } catch (Error err) {
            // The token stream can throw java.lang.Error's 
            String tmp = err.getMessage() ;
            if (tmp == null) {
                throw new QueryParseException(err, -1, -1);
            }
            throw new QueryParseException(tmp, -1, -1);
        }
    }
    
    /**
     * Helper method that parses the given basic graph pattern.
     * 
     * @param bgp the basic graph pattern to parse
     * @return the parsed basic graph pattern
     */
    private Element parseElement(String bgp) {
        ARQParser parser = createParser(bgp);
        try {
            Element element = parser.GroupGraphPatternSub();
            Token t = parser.getNextToken();
            if (t.kind != ARQParserTokenManager.EOF) {
                throw new QueryParseException("Extra tokens beginning \"" +
                        t.image + "\" starting line " + t.beginLine +
                        ", column " + t.beginColumn, t.beginLine, t.beginColumn);
            }
            return element;
        } catch (ParseException ex) {
            throw new QueryParseException(ex.getMessage(),
                    ex.currentToken.beginLine, ex.currentToken.beginLine);
        } catch (TokenMgrError tErr) {
            throw new QueryParseException(tErr.getMessage(), -1, -1) ;
        } catch (Error err) {
            // The token stream can throw java.lang.Error's 
            String tmp = err.getMessage() ;
            if (tmp == null) {
                throw new QueryParseException(err, -1, -1);
            }
            throw new QueryParseException(tmp, -1, -1);
        }
    }
    
    /**
     * Helper method that creates a parser for the given string. The parser is
     * configured to allow aggregates in expressions.
     * 
     * @param s the string to parse
     * @return the new parser
     */
    private ARQParser createParser(String s) {
        Query query = new Query() ;
        query.setPrefixMapping(prefixes) ;
        ARQParser parser = new ARQParser(new StringReader(s));
        parser.setQuery(query);
        try {
            Field allowAggregatesInExpressions =
                    parser.getClass().getDeclaredField("allowAggregatesInExpressions");
            allowAggregatesInExpressions.setAccessible(true);
            allowAggregatesInExpressions.setBoolean(parser, true);
        } catch (NoSuchFieldException ex) {
            throw new AssertionError(ex);
        } catch (IllegalAccessException ex) {
            throw new AssertionError(ex);
        }
        return parser;
    }
    
    /**
     * Returns the prefix mapping used in the definition of dimensions and
     * measures.
     * 
     * @return the prefix mapping
     */
    public PrefixMapping getPrefixMapping() {
        return prefixes;
    }
}
