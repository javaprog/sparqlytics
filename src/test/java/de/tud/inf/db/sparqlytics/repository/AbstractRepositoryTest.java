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

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import de.tud.inf.db.sparqlytics.DummyDimension;
import de.tud.inf.db.sparqlytics.DummyMeasure;
import de.tud.inf.db.sparqlytics.model.Cube;
import de.tud.inf.db.sparqlytics.model.Dimension;
import de.tud.inf.db.sparqlytics.model.Measure;
import de.tud.inf.db.sparqlytics.parser.CubeBuilder;
import java.util.Collections;
import java.util.NoSuchElementException;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests the find operations of the abstract repository implementation.
 *
 * @author Michael Rudolf
 */
public class AbstractRepositoryTest {
    @Test
    public void testFindCube() {
        CubeBuilder builder = new CubeBuilder("test", new ElementTriplesBlock(
                BasicPattern.wrap(Collections.singletonList(Triple.createMatch(
                        NodeFactory.createVariable("test"), null, null))))).
                addMeasure(new DummyMeasure("mes1")).
                addDimension(new DummyDimension("dim1"));
        Cube cube = builder.build();
        FixedRepository instance = new FixedRepository(
                Collections.singleton(cube),
                Collections.<Dimension>emptySet(),
                Collections.<Measure>emptySet());
        assertSame(cube, instance.findCube("test"));
    }

    @Test(expected = NoSuchElementException.class)
    public void testFindNonexistingCube() {
        FixedRepository instance = new FixedRepository();
        instance.findCube("test");
    }

    @Test
    public void testFindDimension() {
        Dimension dim1 = new DummyDimension("dim1");
        FixedRepository instance = new FixedRepository(
                Collections.<Cube>emptySet(),
                Collections.singleton(dim1),
                Collections.<Measure>emptySet());
        assertSame(dim1, instance.findDimension("dim1"));
    }

    @Test(expected = NoSuchElementException.class)
    public void testFindNonexistingDimension() {
        FixedRepository instance = new FixedRepository();
        instance.findDimension("test");
    }

    @Test
    public void testFindMeasure() {
        Measure mes1 = new DummyMeasure("mes1");
        FixedRepository instance = new FixedRepository(
                Collections.<Cube>emptySet(),
                Collections.<Dimension>emptySet(),
                Collections.singleton(mes1));
        assertSame(mes1, instance.findMeasure("mes1"));
    }

    @Test(expected = NoSuchElementException.class)
    public void testFindNonexistingMeasure() {
        FixedRepository instance = new FixedRepository();
        instance.findMeasure("test");
    }
}
