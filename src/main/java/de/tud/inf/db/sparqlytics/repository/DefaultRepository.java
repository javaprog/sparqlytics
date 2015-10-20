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

/**
 * Default mutable repository implementation.
 *
 * @author Michael Rudolf
 */
public class DefaultRepository extends FixedRepository
        implements MutableRepository {
    @Override
    public boolean addCube(final Cube cube) {
        if (cube == null) {
            throw new NullPointerException();
        }
        return cubes.add(cube);
    }

    @Override
    public boolean removeCube(final Cube cube) {
        return cubes.remove(cube);
    }

    @Override
    public boolean addDimension(final Dimension dimension) {
        if (dimension == null) {
            throw new NullPointerException();
        }
        return dimensions.add(dimension);
    }

    @Override
    public boolean removeDimension(final Dimension dimension) {
        return dimensions.remove(dimension);
    }

    @Override
    public boolean addMeasure(final Measure measure) {
        if (measure == null) {
            throw new NullPointerException();
        }
        return measures.add(measure);
    }

    @Override
    public boolean removeMeasure(final Measure measure) {
        return measures.remove(measure);
    }
}
