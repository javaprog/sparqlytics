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

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.NodeValue;

/**
 * Represents a level in a dimension.
 *
 * @author Michael Rudolf
 */
public class Level extends NamedObject {
    /**
     * Special level above all user-defined levels that assembles all facts
     * under a common item. This level is automatically added to every dimension.
     */
    public static final Level ALL = new Level("ALL", NodeValue.makeString("ALL"));

    /**
     * The expression used to derive the level member from a fact.
     */
    private final Expr expression;

    /**
     * Creates a new level with the given name and member expression.
     *
     * @param name          the name of the dimension
     * @param expression    the expression to derive the level member from a fact
     *
     * @throws NullPointerException if any argument is {@code null}
     */
    public Level(final String name, final Expr expression) {
        super(name);
        if (expression == null) {
            throw new NullPointerException();
        }
        this.expression = expression;
    }

    /**
     * Returns the expression used to derive the level member from a fact.
     *
     * @return the expression for the level member
     */
    public Expr getExpression() {
        return expression;
    }
}
