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

package de.tud.inf.db.sparqlytics.repository;

import de.tud.inf.db.sparqlytics.model.Cube;
import de.tud.inf.db.sparqlytics.model.Dimension;
import de.tud.inf.db.sparqlytics.model.Measure;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Fixed repository implementation.
 *
 * @author Michael Rudolf
 */
public class FixedRepository extends AbstractRepository {
    /**
     * The cubes in this repository.
     */
    protected final Set<Cube> cubes;

    /**
     * The dimensions in this repository.
     */
    protected final Set<Dimension> dimensions;

    /**
     * The measures in this repository.
     */
    protected final Set<Measure> measures;

    /**
     * Creates a new empty repository.
     */
    protected FixedRepository() {
        this.cubes = new HashSet<>();
        this.dimensions = new HashSet<>();
        this.measures = new HashSet<>();
    }

    /**
     * Creates a new repository with the given cubes, dimensions and measures.
     *
     * @param cubes         the cubes in this repository
     * @param dimensions    the dimensions in this repository
     * @param measures      the measures in this repository
     * @throws NullPointerException if any argument is {@code null}
     */
    public FixedRepository(final Set<Cube> cubes,
            final Set<Dimension> dimensions,
            final Set<Measure> measures) {
        this.cubes = new HashSet<>(cubes);
        this.dimensions = new HashSet<>(dimensions);
        this.measures = new HashSet<>(measures);
    }

    @Override
    public Set<Cube> getCubes() {
        return Collections.unmodifiableSet(cubes);
    }

    @Override
    public Set<Dimension> getDimensions() {
        return Collections.unmodifiableSet(dimensions);
    }

    @Override
    public Set<Measure> getMeasures() {
        return Collections.unmodifiableSet(measures);
    }
}
