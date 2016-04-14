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

package de.tud.inf.db.sparqlytics.bench;

import de.tud.inf.db.sparqlytics.model.Cube;
import de.tud.inf.db.sparqlytics.model.Dimension;
import de.tud.inf.db.sparqlytics.model.Measure;
import de.tud.inf.db.sparqlytics.model.Session;
import de.tud.inf.db.sparqlytics.olap.Compute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Base class for random benchmarks.
 *
 * @author Michael Rudolf
 */
public abstract class RandomBenchmarkBase extends BenchmarkBase {
    /**
     * The random number generator to use for picking measures.
     */
    protected final Random random = new Random();

    /**
     * Helper method to benchmark OLAP operations on the cube that is configured
     * in the given session.
     *
     * @param session the configured session
     * @throws NullPointerException if the argument is {@code null}
     */
    protected void testCube(Session session) {
        Cube cube = session.getCube();
        Set<Measure> measures = cube.getMeasures();
        Set<Dimension> dimensions = cube.getDimensions();
        for (int i = 0; i < measures.size() * dimensions.size(); i++) {
            for (Dimension dimension : pickRandom(dimensions)) {
                session.setGranularity(dimension,
                        random.nextInt(dimension.getLevels().size()));
            }
            session.execute(new Compute(pickRandom(measures),
                    Collections.EMPTY_LIST, null, null));
        }
    }

    /**
     * Randomly picks a random positive number of elements from the given
     * collection.
     *
     * @param <T>           the type of elements
     * @param collection    the input from which to pick elements randomly
     * @return a random number of elements randomly picked from the input
     * @throws NullPointerException if the argument is {@code null}
     * @throws IllegalArgumentException if the input is empty
     */
    protected <T> List<T> pickRandom(Collection<T> collection) {
        if (collection.isEmpty()) {
            throw new IllegalArgumentException();
        }
        int size = collection.size();
        return size == 1 ? new ArrayList<>(collection) :
                pickNRandom(collection, random.nextInt(size - 1) + 1);
    }

    /**
     * Randomly picks {@code n} elements from the given collection.
     *
     * @param <T>           the type of elements
     * @param collection    the input from which to pick elements randomly
     * @param n             the number of elements to pick from the input
     * @return the specified number of elements randomly picked from the input
     * @throws IndexOutOfBoundsException    if {@code n} is not a positive
     *                                      number smaller than the size of the
     *                                      input
     */
    protected <T> List<T> pickNRandom(Collection<T> collection, int n) {
        List<T> list = new ArrayList<>(collection);
        Collections.shuffle(list, random);
        return list.subList(0, n);
    }
}
