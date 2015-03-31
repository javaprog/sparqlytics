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

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the drill-down operation.
 *
 * @author Michael Rudolf
 */
public class DrillDownTest {
    @Test(expected = NullPointerException.class)
    public void testInstantiateWithNullName() {
        new DrillDown(null, 1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testInstantiateWithNegativeSteps() {
        new DrillDown("Test", -1);
    }
    
    @Test
    public void testRun() {
        DrillDown instance = new DrillDown("dim1", 1);
        Session session = new Session();
        Dimension dim1 = new Dimension("dim1", new ElementGroup(), Arrays.asList(
                new Level("lev1", NodeValue.TRUE),
                new Level("lev2", NodeValue.TRUE),
                new Level("lev3", NodeValue.TRUE)
        ));
        session.addDimension(dim1);
        session.setGranularity(dim1, 2);
        Assert.assertEquals(2, session.getGranularity(dim1));
        session.execute(instance);
        Assert.assertEquals(1, session.getGranularity(dim1));
    }
}
