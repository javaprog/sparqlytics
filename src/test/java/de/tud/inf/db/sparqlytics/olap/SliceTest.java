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
import de.tud.inf.db.sparqlytics.model.Filter;
import de.tud.inf.db.sparqlytics.model.Level;
import de.tud.inf.db.sparqlytics.DummyDimension;
import de.tud.inf.db.sparqlytics.DummyMeasure;
import de.tud.inf.db.sparqlytics.parser.CubeBuilder;
import java.util.Collections;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the slice operation.
 *
 * @author Michael Rudolf
 */
public class SliceTest {
    @Test(expected = NullPointerException.class)
    public void testInstantiateWithNullDimension() {
        new Slice(null, "Test", NodeValue.TRUE);
    }

    @Test(expected = NullPointerException.class)
    public void testInstantiateWithNullLevel() {
        new Slice("Test", null, NodeValue.TRUE);
    }

    @Test(expected = NullPointerException.class)
    public void testInstantiateWithNullExpression() {
        new Slice("Test", "Test", null);
    }

    @Test
    public void testRun() {
        Expr value = NodeValue.TRUE;
        Slice instance = new Slice("dim1", "lev1", value);
        Session session = new Session();
        CubeBuilder builder = new CubeBuilder(new ElementTriplesBlock(
                BasicPattern.wrap(Collections.singletonList(Triple.createMatch(
                        NodeFactory.createVariable("test"), null, null))))).
                addMeasure(new DummyMeasure("mes1"));
        Dimension dim1 = new DummyDimension("dim1");
        Level lev1 = dim1.getLevels().get(0);
        session.setCube(builder.addDimension(dim1).build(""));
        session.execute(instance);
        Map<Pair<Dimension, Level>, Filter> filters = session.getFilters();
        Assert.assertEquals(1, filters.size());
        Map.Entry<Pair<Dimension, Level>, Filter> entry =
                filters.entrySet().iterator().next();
        Pair<Dimension, Level> key = entry.getKey();
        Assert.assertSame(dim1, key.getLeft());
        Assert.assertSame(lev1, key.getRight());
        Expr predicate = entry.getValue().getPredicate();
        Assert.assertTrue(predicate instanceof E_Equals);
        Assert.assertSame(value, ((E_Equals)predicate).getArg2());
    }
}
