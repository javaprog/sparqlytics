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

package de.tud.inf.db.sparqlytics;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import java.util.Arrays;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the dice operation.
 *
 * @author Michael Rudolf
 */
public class DiceTest {
    @Test(expected = NullPointerException.class)
    public void testInstantiateWithNullDimension() {
        new Dice(null, "Test");
    }
    
    @Test(expected = NullPointerException.class)
    public void testInstantiateWithNullLevel() {
        new Dice("Test", null);
    }
    
    @Test(expected = NullPointerException.class)
    public void testInstantiateWithNullFilter() {
        new Dice("Test", "Test", null);
    }
    
    @Test
    public void testRun() {
        Filter filter = new Filter(Var.alloc("test"), NodeValue.TRUE);
        Dice instance = new Dice("dim1", "lev1", filter);
        Session session = new Session();
        Level lev1 = new Level("lev1", NodeValue.TRUE);
        Dimension dim1 = new Dimension("dim1", new ElementGroup(), Arrays.asList(
                lev1,
                new Level("lev2", NodeValue.TRUE),
                new Level("lev3", NodeValue.TRUE)
        ));
        session.addDimension(dim1);
        session.execute(instance);
        Map<Pair<Dimension, Level>, Filter> filters = session.getFilters();
        Assert.assertEquals(1, filters.size());
        Map.Entry<Pair<Dimension, Level>, Filter> entry = 
                filters.entrySet().iterator().next();
        Pair<Dimension, Level> key = entry.getKey();
        Assert.assertSame(dim1, key.getLeft());
        Assert.assertSame(lev1, key.getRight());
    }
}
