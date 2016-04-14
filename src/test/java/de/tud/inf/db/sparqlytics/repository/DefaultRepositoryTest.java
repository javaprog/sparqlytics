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
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests the mutation methods of the default repository implementation.
 *
 * @author Michael Rudolf
 */
public class DefaultRepositoryTest {
    @Test
    public void testAddRemoveCube() {
        CubeBuilder builder = new CubeBuilder(new ElementTriplesBlock(
                BasicPattern.wrap(Collections.singletonList(Triple.createMatch(
                        NodeFactory.createVariable("test"), null, null))))).
                addMeasure(new DummyMeasure("mes1")).
                addDimension(new DummyDimension("dim1"));
        Cube cube = builder.build("test");
        DefaultRepository instance = new DefaultRepository();
        assertTrue(instance.addCube(cube));
        assertEquals(cube, instance.findCube("test"));
        assertEquals(Collections.singleton(cube), instance.getCubes());
        assertTrue(instance.removeCube(cube));
        assertTrue(instance.getCubes().isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void testAddCubeNull() {
        DefaultRepository instance = new DefaultRepository();
        instance.addCube(null);
    }

    @Test
    public void testRemoveCubeNull() {
        DefaultRepository instance = new DefaultRepository();
        assertFalse(instance.removeCube(null));
    }

    @Test
    public void testAddRemoveDimension() {
        Dimension dim1 = new DummyDimension("dim1");
        DefaultRepository instance = new DefaultRepository();
        assertTrue(instance.addDimension(dim1));
        assertEquals(dim1, instance.findDimension("dim1"));
        assertEquals(Collections.singleton(dim1), instance.getDimensions());
        assertTrue(instance.removeDimension(dim1));
        assertTrue(instance.getDimensions().isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void testAddDimensionNull() {
        DefaultRepository instance = new DefaultRepository();
        instance.addDimension(null);
    }

    @Test
    public void testRemoveDimensionNull() {
        DefaultRepository instance = new DefaultRepository();
        assertFalse(instance.removeDimension(null));
    }

    @Test
    public void testAddRemoveMeasure() {
        Measure mes1 = new DummyMeasure("mes1");
        DefaultRepository instance = new DefaultRepository();
        assertTrue(instance.addMeasure(mes1));
        assertEquals(mes1, instance.findMeasure("mes1"));
        assertEquals(Collections.singleton(mes1), instance.getMeasures());
        assertTrue(instance.removeMeasure(mes1));
        assertTrue(instance.getMeasures().isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void testAddMeasureNull() {
        DefaultRepository instance = new DefaultRepository();
        instance.addMeasure(null);
    }

    @Test
    public void testRemoveMeasureNull() {
        DefaultRepository instance = new DefaultRepository();
        assertFalse(instance.removeMeasure(null));
    }
}
