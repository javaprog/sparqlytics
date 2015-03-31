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

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVars;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.PatternVars;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents a measure.
 *
 * @author Michael Rudolf
 */
public class Measure extends NamedObject {
    /**
     * The aggregation functions supported by SPARQL.
     */
    private static final String[] AGGREGATION_FUNCTIONS = new String[]{
        "COUNT", "SUM", "MIN", "MAX", "AVG", "SAMPLE"
    };
    
    static {
        Arrays.sort(AGGREGATION_FUNCTIONS);
    }
    
    /**
     * The seed pattern connecting the numeric expression with the facts.
     */
    private final Element seedPattern;
    
    /**
     * The function to apply when aggregating numeric values.
     */
    private final String aggregationFunction;
    
    /**
     * The numeric expression to apply to the facts.
     */
    private final Expr expression;
    
    /**
     * Creates a new measure with the given name, seed pattern, numeric 
     * expression and aggregation function.
     * 
     * @param name                  the name of the measure
     * @param seedPattern           the seed pattern connecting the numeric 
     *                              expression with the facts
     * @param expression            the expression to derive numeric values from 
     *                              the facts
     * @param aggregationFunction   the aggregation function to apply to the 
     *                              numeric values
     * 
     * @throws NullPointerException if any argument is <code>null</code>
     * @throws IllegalArgumentException if the numeric expression references a 
     *                                  variable not present in the seed pattern
     *                                  or if the aggregation function is 
     *                                  unsupported
     */
    public Measure(String name, Element seedPattern, Expr expression, 
            String aggregationFunction) {
        super(name);
        if (aggregationFunction == null) {
            throw new NullPointerException();
        } else {
            this.aggregationFunction = aggregationFunction.toUpperCase();
            if (Arrays.binarySearch(AGGREGATION_FUNCTIONS, this.aggregationFunction) < 0) {
                throw new IllegalArgumentException(
                        "Unsupported aggregation function: " + aggregationFunction);
            }
        }
        List<Var> vars = new LinkedList<Var>();
        ExprVars.varsMentioned(vars, expression);
        vars.removeAll(PatternVars.vars(seedPattern));
        if (!vars.isEmpty()) {
            throw new IllegalArgumentException(
                    "Expression references unknown variable(s): " + 
                    StringUtils.join(vars, ", "));
        }
        this.seedPattern = seedPattern;
        this.expression = expression;
    }
    
    /**
     * Returns the seed pattern that connects the numeric expression with the
     * facts.
     * 
     * @return the seed pattern
     */
    public Element getSeedPattern() {
        return seedPattern;
    }
    
    /**
     * Returns the numeric expression to apply to the facts.
     * 
     * @return the numeric expression
     */
    public Expr getExpression() {
        return expression;
    }
    
    /**
     * Returns the aggregation function to be used when aggregating numeric 
     * values for groups of facts.
     * 
     * @return the aggregation function
     */
    public String getAggregationFunction() {
        return aggregationFunction;
    }
}
