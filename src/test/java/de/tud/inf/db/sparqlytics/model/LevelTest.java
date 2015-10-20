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

import com.hp.hpl.jena.sparql.expr.NodeValue;
import org.junit.Test;

/**
 * Tests the level class.
 *
 * @author Michael Rudolf
 */
public class LevelTest {
    @Test(expected = NullPointerException.class)
    public void testInstantiateWithNullName() {
        new Level(null, NodeValue.TRUE);
    }

    @Test(expected = NullPointerException.class)
    public void testInstantiateWithNullExpression() {
        new Level("test", null);
    }
}
