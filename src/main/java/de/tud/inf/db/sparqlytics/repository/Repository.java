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

import com.codahale.metrics.MetricRegistry;
import de.tud.inf.db.sparqlytics.model.Cube;
import de.tud.inf.db.sparqlytics.model.Dimension;
import de.tud.inf.db.sparqlytics.model.Measure;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Interface for repositories of dimensions, measures and cubes.
 *
 * @author Michael Rudolf
 */
public interface Repository {
    /**
     * Returns an unmodifiable set of all cubes in this repository.
     *
     * @return all cubes
     */
    Set<Cube> getCubes();

    /**
     * Returns the cubes with the given name.
     *
     * @param name the name of the cube to be looked up
     * @return the cube with the given name
     * @throws NoSuchElementException   if this repository does not contain a
     *                                  cube with the given name
     */
    Cube findCube(String name) throws NoSuchElementException;

    /**
     * Returns an unmodifiable set of all dimensions in this repository.
     *
     * @return all dimensions
     */
    Set<Dimension> getDimensions();

    /**
     * Returns the dimension with the given name.
     *
     * @param name the name of the dimension to be looked up
     * @return the dimension with the given name
     * @throws NoSuchElementException   if this repository does not contain a
     *                                  dimension with the given name
     */
    Dimension findDimension(String name) throws NoSuchElementException;

    /**
     * Returns an unmodifiable set of all measures in this repository.
     *
     * @return all measures
     */
    Set<Measure> getMeasures();

    /**
     * Returns the measure with the given name.
     *
     * @param name the name of the measure to be looked up
     * @return the measure with the given name
     * @throws NoSuchElementException   if this repository does not contain a
     *                                  measure with the given name
     */
    Measure findMeasure(String name) throws NoSuchElementException;
    
    /**
     * Returns statistics about this repository. They include the following
     * gauges:
     * <ul>
     *   <li>number of cubes,</li>
     *   <li>number of dimensions, and</li>
     *   <li>number of measures.</li>
     * </ul>
     * Furthermore, they include the following histograms:
     * <ul>
     *   <li>number of dimensions per cube,</li>
     *   <li>number of levels per dimension (without {@literal "ALL"}),</li>
     *   <li>number of measures per cube,</li>
     *   <li>number of cubes per dimension, and</li>
     *   <li>number of cubes per measure.</li>
     * </ul>
     * The statistics are a snapshot, they are not updates as the repository is
     * modified.
     * 
     * @return statistics about this repository
     */
    MetricRegistry getStatistics();
}
