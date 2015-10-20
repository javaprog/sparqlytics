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
 * Interface for mutable repositories.
 *
 * @author Michael Rudolf
 */
public interface MutableRepository extends Repository {
    /**
     * Adds the given cube to this repository, provided that no other
     * cube with the same name is already part of this repository.
     *
     * @param cube the cube to add
     * @return whether the cube was added
     *
     * @see #removeCube
     */
    boolean addCube(Cube cube);

    /**
     * Removes the given cube from this session, if it is part of this
     * repository.
     *
     * @param cube the cube to remove
     * @return whether the cube was removed
     *
     * @see #addCube
     */
    boolean removeCube(Cube cube);

    /**
     * Adds the given dimension to this repository, provided that no other
     * dimension with the same name is already part of this repository.
     *
     * @param dimension the dimension to add
     * @return whether the dimension was added
     *
     * @see #removeDimension
     */
    boolean addDimension(Dimension dimension);

    /**
     * Removes the given dimension from this session, if it is part of this
     * repository.
     *
     * @param dimension the dimension to remove
     * @return whether the dimension was removed
     *
     * @see #addDimension
     */
    boolean removeDimension(Dimension dimension);

    /**
     * Adds the given measure to this repository, provided that no other measure
     * with the same name is already part of this repository.
     *
     * @param measure the measure to add
     * @return whether the measure was added
     *
     * @see #removeMeasure
     */
    boolean addMeasure(Measure measure);

    /**
     * Removes the given measure from this session, if it is part of this
     * repository.
     *
     * @param measure the measure to remove
     * @return whether the measure was removed
     *
     * @see #addMeasure
     */
    boolean removeMeasure(Measure measure);
}
