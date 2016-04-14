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

package de.tud.inf.db.sparqlytics.olap;

import de.tud.inf.db.sparqlytics.model.Session;
import de.tud.inf.db.sparqlytics.model.Dimension;
import de.tud.inf.db.sparqlytics.DummyDimension;
import de.tud.inf.db.sparqlytics.DummyMeasure;
import de.tud.inf.db.sparqlytics.parser.CubeBuilder;
import java.util.Collections;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the roll-up operation.
 *
 * @author Michael Rudolf
 */
public class RollUpTest {
    @Test(expected = NullPointerException.class)
    public void testInstantiateWithNullName() {
        new RollUp(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInstantiateWithNegativeSteps() {
        new RollUp("Test", -1);
    }

    @Test
    public void testRun() {
        RollUp instance = new RollUp("dim1", 1);
        Session session = new Session();
        CubeBuilder builder = new CubeBuilder(new ElementTriplesBlock(
                BasicPattern.wrap(Collections.singletonList(Triple.createMatch(
                        NodeFactory.createVariable("test"), null, null))))).
                addMeasure(new DummyMeasure("mes1"));
        Dimension dim1 = new DummyDimension("dim1");
        session.setCube(builder.addDimension(dim1).build(""));
        Assert.assertEquals(0, session.getGranularity(dim1));
        session.execute(instance);
        Assert.assertEquals(1, session.getGranularity(dim1));
    }
}
