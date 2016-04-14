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

import de.tud.inf.db.sparqlytics.DummyDimension;
import de.tud.inf.db.sparqlytics.DummyMeasure;
import de.tud.inf.db.sparqlytics.model.Cube;
import de.tud.inf.db.sparqlytics.model.Dimension;
import de.tud.inf.db.sparqlytics.model.Measure;
import de.tud.inf.db.sparqlytics.parser.CubeBuilder;
import java.util.Collections;
import java.util.Set;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests the getter operations of the fixed repository implementation.
 *
 * @author Michael Rudolf
 */
public class FixedRepositoryTest {
    @Test
    public void testGetCubes() {
        CubeBuilder builder = new CubeBuilder(new ElementTriplesBlock(
                BasicPattern.wrap(Collections.singletonList(Triple.createMatch(
                        NodeFactory.createVariable("test"), null, null))))).
                addMeasure(new DummyMeasure("mes1")).
                addDimension(new DummyDimension("dim1"));
        Set<Cube> cubes = Collections.singleton(builder.build("test"));
        FixedRepository instance = new FixedRepository(
                cubes,
                Collections.<Dimension>emptySet(),
                Collections.<Measure>emptySet());
        assertEquals(cubes, instance.getCubes());
    }

    @Test
    public void testGetCubesEmpty() {
        FixedRepository instance = new FixedRepository();
        assertTrue(instance.getCubes().isEmpty());
    }

    @Test
    public void testGetDimensions() {
        Dimension dim1 = new DummyDimension("dim1");
        Set<Dimension> dimensions = Collections.singleton(dim1);
        FixedRepository instance = new FixedRepository(
                Collections.<Cube>emptySet(),
                dimensions,
                Collections.<Measure>emptySet());
        assertEquals(dimensions, instance.getDimensions());
    }

    @Test
    public void testGetDimensionsEmpty() {
        FixedRepository instance = new FixedRepository();
        assertTrue(instance.getDimensions().isEmpty());
    }

    @Test
    public void testGetMeasures() {
        Measure mes1 = new DummyMeasure("mes1");
        Set<Measure> measures = Collections.singleton(mes1);
        FixedRepository instance = new FixedRepository(
                Collections.<Cube>emptySet(),
                Collections.<Dimension>emptySet(),
                measures);
        assertEquals(measures, instance.getMeasures());
    }

    @Test
    public void testGetMeasuresEmpty() {
        FixedRepository instance = new FixedRepository();
        assertTrue(instance.getMeasures().isEmpty());
    }
}
