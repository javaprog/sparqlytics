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

package de.tud.inf.db.sparqlytics.parser;

import com.hp.hpl.jena.sparql.syntax.Element;
import de.tud.inf.db.sparqlytics.model.Cube;
import de.tud.inf.db.sparqlytics.model.Dimension;
import de.tud.inf.db.sparqlytics.model.Measure;
import java.util.HashSet;
import java.util.Set;

/**
 * Helper class for composing cubes.
 *
 * @author Michael Rudolf
 */
public class CubeBuilder {
    /**
     * The name to use for the cube.
     */
    private final String name;

    /**
     * The pattern to use for selecting facts.
     */
    private final Element factPattern;

    /**
     * The dimensions to use for the cube.
     */
    private final Set<Dimension> dimensions = new HashSet<>();

    /**
     * The measures to use for the cube.
     */
    private final Set<Measure> measures = new HashSet<>();

    /**
     * Creates a new cube builder for the given fact selection pattern.
     *
     * @param name          the name to use for the cube
     * @param factPattern   the pattern to use for selecting facts
     * @throws NullPointerException if the argument is {@code null}
     */
    public CubeBuilder(final String name, final Element factPattern) {
        if (name == null || factPattern == null) {
            throw new NullPointerException();
        }
        this.name = name;
        this.factPattern = factPattern;
    }

    /**
     * Adds the given dimension to this builder and returns this object. If a
     * dimension with the same name has been added before, an exception is
     * thrown.
     *
     * @param dimension the dimension to add
     * @return this object
     * @throws IllegalArgumentException if a measure with the same name has
     *                                  already been added
     */
    public CubeBuilder addDimension(final Dimension dimension) {
        if (!dimensions.add(dimension)) {
            throw new IllegalArgumentException();
        }
        return this;
    }

    /**
     * Adds the given measure to this builder and returns this object. If a
     * measure with the same name has been added before, an exception is thrown.
     *
     * @param measure the measure to add
     * @return this object
     * @throws IllegalArgumentException if a measure with the same name has
     *                                  already been added
     */
    public CubeBuilder addMeasure(Measure measure) {
        if (!measures.add(measure)) {
            throw new IllegalArgumentException();
        }
        return this;
    }

    /**
     * Creates a new cube from the configured fact selection pattern and the
     * dimensions and measures previously added.
     *
     * @return a new cube
     * @throws IllegalStateException    if no dimensions or measures have been
     *                                  added
     */
    public Cube build() {
        if (dimensions.isEmpty() || measures.isEmpty()) {
            throw new IllegalStateException();
        }
        return new Cube(name, factPattern, dimensions, measures);
    }
}
