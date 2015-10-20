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
import java.util.NoSuchElementException;

/**
 * Abstract superclass for repository implementations.
 *
 * @author Michael Rudolf
 */
public abstract class AbstractRepository implements Repository {
    @Override
    public Cube findCube(final String name) {
        for (Cube cube : getCubes()) {
            if (cube.getName().equals(name)) {
                return cube;
            }
        }
        throw new NoSuchElementException(name);
    }

    @Override
    public Dimension findDimension(final String name) {
        for (Dimension dimension : getDimensions()) {
            if (dimension.getName().equals(name)) {
                return dimension;
            }
        }
        throw new NoSuchElementException(name);
    }

    @Override
    public Measure findMeasure(final String name) {
        for (Measure measure : getMeasures()) {
            if (measure.getName().equals(name)) {
                return measure;
            }
        }
        throw new NoSuchElementException(name);
    }
}
