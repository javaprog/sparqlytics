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

/**
 * An operation for resetting filters created by a slice or a dice operation.
 *
 * @author Michael Rudolf
 */
public class ResetFilter implements Operation {
    /**
     * The name of the dimension to reset the filter for.
     */
    private final String dimension;

    /**
     * The name of the level to reset the filter for.
     */
    private final String level;

    /**
     * Creates a new reset filter operation for the dimension and level with the
     * given names.
     *
     * @param dimension the name of the dimension to reset the filter for
     * @param level     the name of the level to reset the filter for
     *
     * @throws NullPointerException if any argument is {@code null}
     */
    public ResetFilter(final String dimension, final String level) {
        if (dimension == null || level == null) {
            throw new NullPointerException();
        }
        this.dimension = dimension;
        this.level = level;
    }

    @Override
    public void run(final Session session) {
        Dimension temp = session.getCube().findDimension(dimension);
        session.removeFilter(temp, temp.findLevel(level));
    }
}
