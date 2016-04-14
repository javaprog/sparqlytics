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

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SlidingWindowReservoir;
import de.tud.inf.db.sparqlytics.model.Cube;
import de.tud.inf.db.sparqlytics.model.Dimension;
import de.tud.inf.db.sparqlytics.model.Measure;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Set;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

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
    
    @Override
    public MetricRegistry getStatistics() {
        MetricRegistry registry = new MetricRegistry();
        final Set<Cube> cubes = getCubes();
        registry.register("#cubes", new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return cubes.size();
            }
        });
        final Set<Dimension> allDimensions = getDimensions();
        registry.register("#dimensions", new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return allDimensions.size();
            }
        });
        final Set<Measure> allMeasures = getMeasures();
        registry.register("#measures", new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return allMeasures.size();
            }
        });
        Histogram levelsPerDimension = registry.register("levels/dimension",
                new Histogram(new SlidingWindowReservoir(cubes.size())));
        for (Dimension dimension : getDimensions()) {
            levelsPerDimension.update(dimension.getLevels().size() - 1);
        }
        Histogram dimensionsPerCube = registry.register("dimensions/cube",
                new Histogram(new SlidingWindowReservoir(cubes.size())));
        Histogram measuresPerCube = registry.register("measures/cube",
                new Histogram(new SlidingWindowReservoir(cubes.size())));
        MultiValuedMap<Dimension, Cube> dimensionMap = new ArrayListValuedHashMap<>();
        MultiValuedMap<Measure, Cube> measureMap = new ArrayListValuedHashMap<>();
        for (Cube cube : cubes) {
            Set<Dimension> dimensions = cube.getDimensions();
            dimensionsPerCube.update(dimensions.size());
            for (Dimension dimension : dimensions) {
                dimensionMap.put(dimension, cube);
            }
            Set<Measure> measures = cube.getMeasures();
            measuresPerCube.update(measures.size());
            for (Measure measure : measures) {
                measureMap.put(measure, cube);
            }
        }
        Histogram cubesPerDimension = registry.register("cubes/dimension",
                new Histogram(new SlidingWindowReservoir(cubes.size())));
        for (Collection<Cube> values : dimensionMap.asMap().values()) {
            cubesPerDimension.update(values.size());
        }
        Histogram cubesPerMeasure = registry.register("cubes/measure",
                new Histogram(new SlidingWindowReservoir(cubes.size())));
        for (Collection<Cube> values : measureMap.asMap().values()) {
            cubesPerMeasure.update(values.size());
        }
        return registry;
    }
}
