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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.ElementGroup;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests the dimension class.
 *
 * @author Michael Rudolf
 */
public class DimensionTest {
    @Test(expected = NullPointerException.class)
    public void testInstantiateWithNullName() {
        new Dimension(null, new ElementGroup(), Arrays.asList(
                new Level("lev1", NodeValue.TRUE)
        ));
    }

    @Test(expected = NullPointerException.class)
    public void testInstantiateWithNullPattern() {
        new Dimension("test", null, Arrays.asList(
                new Level("lev1", NodeValue.TRUE)
        ));
    }

    @Test(expected = NullPointerException.class)
    public void testInstantiateWithNullLevels() {
        new Dimension("test", new ElementGroup(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInstantiateWithEmptyLevels() {
        new Dimension("test", new ElementGroup(), Collections.<Level>emptyList());
    }

    @Test
    public void testGetLevels() {
        Level lev1 = new Level("lev1", NodeValue.TRUE);
        Dimension instance = new Dimension("test", new ElementGroup(),
                Arrays.asList(lev1));
        List<Level> levels = instance.getLevels();
        assertEquals(2, levels.size());
        assertSame(lev1, levels.get(0));
        assertSame(Level.ALL, levels.get(1));
    }

    @Test
    public void testFindLevel() {
        Level lev1 = new Level("lev1", NodeValue.TRUE);
        Dimension instance = new Dimension("test", new ElementGroup(),
                Arrays.asList(lev1));
        assertSame(lev1, instance.findLevel("lev1"));
    }
}
