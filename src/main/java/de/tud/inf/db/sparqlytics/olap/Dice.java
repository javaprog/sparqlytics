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

import de.tud.inf.db.sparqlytics.model.Session;
import de.tud.inf.db.sparqlytics.model.Dimension;
import de.tud.inf.db.sparqlytics.model.Filter;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarAlloc;
import org.apache.jena.sparql.engine.binding.BindingFactory;

/**
 * A dice operation applies filter constraints to values of facts in a dimension.
 *
 * @author Michael Rudolf
 */
public class Dice implements Operation {
    /**
     * Allocates variables to use in filters.
     */
    protected static final VarAlloc FILTER_VAR_ALLOC = new VarAlloc("_filter");

    /**
     * The name of the dimension to apply the filter to.
     */
    private final String dimension;

    /**
     * The name of the level to apply the filter to.
     */
    private final String level;

    /**
     * The filter to apply to the facts.
     */
    protected Filter filter;

    /**
     * Creates a new dice operation for filtering facts based on their values in
     * the level and dimension with the given names.
     *
     * @param dimension the name of the dimension to apply the filter to
     * @param level     the name of the level to apply the filter to
     * @param filter    the filter to apply to the facts
     *
     * @throws NullPointerException if any argument is {@code null}
     */
    public Dice(final String dimension, final String level, final Filter filter) {
        this(dimension, level);
        Var var = FILTER_VAR_ALLOC.allocVar();
        this.filter = new Filter(var, filter.getPredicate().copySubstitute(
                BindingFactory.binding(filter.getVariable(), var)));
    }

    /**
     * Helper constructor for subclasses that construct a filter separately.
     *
     * @param dimension the name of the dimension to apply the filter to
     * @param level     the name of the level to apply the filter to
     *
     * @throws NullPointerException if any argument is {@code null}
     */
    protected Dice(final String dimension, final String level) {
        if (dimension == null || level == null) {
            throw new NullPointerException();
        }
        this.dimension = dimension;
        this.level = level;
    }

    @Override
    public void run(final Session session) {
        Dimension dim = session.getCube().findDimension(dimension);
        session.addFilter(dim, dim.findLevel(level), filter);
    }
}
