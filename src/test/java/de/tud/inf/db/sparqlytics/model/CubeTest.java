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

package de.tud.inf.db.sparqlytics.model;

import de.tud.inf.db.sparqlytics.DummyDimension;
import de.tud.inf.db.sparqlytics.DummyMeasure;
import de.tud.inf.db.sparqlytics.parser.CubeBuilder;
import java.util.Collections;
import java.util.NoSuchElementException;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import static org.junit.Assert.assertSame;
import org.junit.Test;

/**
 * Tests the cube class.
 *
 * @author Michael Rudolf
 */
public class CubeTest {
    @Test(expected = NullPointerException.class)
    public void testInstantiateWithNullName() {
        new Cube(null, new ElementGroup(),
                Collections.<Dimension>singleton(new DummyDimension("dim1")),
                Collections.<Measure>singleton(new DummyMeasure("mes1")));
    }

    @Test(expected = NullPointerException.class)
    public void testInstantiateWithNullPattern() {
        new Cube("test", null,
                Collections.<Dimension>singleton(new DummyDimension("dim1")),
                Collections.<Measure>singleton(new DummyMeasure("mes1")));
    }

    @Test(expected = NullPointerException.class)
    public void testInstantiateWithNullDimensions() {
        new Cube("test", new ElementGroup(), null,
                Collections.<Measure>singleton(new DummyMeasure("mes1")));
    }

    @Test(expected = NullPointerException.class)
    public void testInstantiateWithNullMeasures() {
        new Cube("test", new ElementGroup(),
                Collections.<Dimension>singleton(new DummyDimension("dim1")),
                null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInstantiateWithEmptyDimensions() {
        new Cube("test", new ElementGroup(), Collections.<Dimension>emptySet(),
                Collections.<Measure>singleton(new DummyMeasure("mes1")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInstantiateWithEmptyMeasures() {
        new Cube("test", new ElementGroup(),
                Collections.<Dimension>singleton(new DummyDimension("dim1")),
                Collections.<Measure>emptySet());
    }

    @Test
    public void testFindDimension() {
        CubeBuilder builder = new CubeBuilder(new ElementTriplesBlock(
                BasicPattern.wrap(Collections.singletonList(Triple.createMatch(
                        NodeFactory.createVariable("test"), null, null))))).
                addMeasure(new DummyMeasure("mes1"));
        Dimension dim1 = new DummyDimension("dim1");
        builder.addDimension(dim1);
        assertSame(dim1, builder.build("test").findDimension("dim1"));
    }

    @Test(expected = NoSuchElementException.class)
    public void testFindNonexistingDimension() {
        CubeBuilder builder = new CubeBuilder(new ElementTriplesBlock(
                BasicPattern.wrap(Collections.singletonList(Triple.createMatch(
                        NodeFactory.createVariable("test"), null, null))))).
                addMeasure(new DummyMeasure("mes1")).
                addDimension(new DummyDimension("dim1"));
        builder.build("test").findDimension("test");
    }

    @Test
    public void testFindMeasure() {
        CubeBuilder builder = new CubeBuilder(new ElementTriplesBlock(
                BasicPattern.wrap(Collections.singletonList(Triple.createMatch(
                        NodeFactory.createVariable("test"), null, null))))).
                addDimension(new DummyDimension("dim1"));
        Measure mes1 = new DummyMeasure("mes1");
        builder.addMeasure(mes1);
        assertSame(mes1, builder.build("test").findMeasure("mes1"));
    }

    @Test(expected = NoSuchElementException.class)
    public void testFindNonexistingMeasure() {
        CubeBuilder builder = new CubeBuilder(new ElementTriplesBlock(
                BasicPattern.wrap(Collections.singletonList(Triple.createMatch(
                        NodeFactory.createVariable("test"), null, null))))).
                addMeasure(new DummyMeasure("mes1")).
                addDimension(new DummyDimension("dim1"));
        builder.build("test").findMeasure("test");
    }
}
