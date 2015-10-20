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

import de.tud.inf.db.sparqlytics.model.Filter;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;

/**
 * A slice operation filters facts with a specific value in a certain dimension.
 *
 * @author Michael Rudolf
 */
public class Slice extends Dice {
    /**
     * Creates a new slice operation for filtering facts based on their values
     * in the level and dimension with the given names.
     *
     * @param dimension the name of the dimension to apply the filter to
     * @param level     the name of the level to apply the filter to
     * @param value     the value of the facts on the given level of the given
     *                  dimension
     *
     * @throws NullPointerException if any argument is {@code null}
     */
    public Slice(final String dimension, final String level, final Expr value) {
        super(dimension, level);
        if (value == null) {
            throw new NullPointerException();
        }
        Var var = FILTER_VAR_ALLOC.allocVar();
        filter = new Filter(var, new E_Equals(new ExprVar(var), value));
    }
}
