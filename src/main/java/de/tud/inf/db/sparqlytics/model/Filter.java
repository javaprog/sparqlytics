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

package de.tud.inf.db.sparqlytics.model;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;

/**
 * Represents a filter for facts that is based on the level member.
 *
 * @author Michael Rudolf
 */
public class Filter {
    /**
     * The variable used in the filter predicate.
     */
    private final Var variable;

    /**
     * The predicate to apply to the level member.
     */
    private final Expr predicate;

    /**
     * Creates a new filter using the given variable in the given predicate.
     *
     * @param variable  the variable used in the given predicate
     * @param predicate the predicate to apply to the level member
     *
     * @throws NullPointerException if any argument is {@code null}
     */
    public Filter(final Var variable, final Expr predicate) {
        if (variable == null || predicate == null) {
            throw new NullPointerException();
        }
        this.variable = variable;
        this.predicate = predicate;
    }

    /**
     * Returns the variable used in the filter predicate.
     *
     * @return the variable
     */
    public Var getVariable() {
        return variable;
    }

    /**
     * Returns the predicate to apply to the level member.
     *
     * @return the predicate to apply to the level member
     */
    public Expr getPredicate() {
        return predicate;
    }
}
