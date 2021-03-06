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

import java.io.IOException;
import org.apache.jena.riot.Lang;
import org.junit.Test;

/**
 * Integration test.
 *
 * @author Michael Rudolf
 */
public class IT extends ITBase {
    public IT() {
        super(IT.class.getResource("fixture.sparqlytics"));
    }

    @Test
    public void test1Measure() throws IOException {
        testIsomorphism(Lang.N3, "1measure");
    }

    @Test
    public void testSliceDiceRollup1Measure() throws IOException {
        testIsomorphism(Lang.N3, "slice-dice-rollup-1measure");
    }

    @Test
    public void testSlice2Measures() throws IOException {
        testIsomorphism(Lang.N3, "slice-2measures");
    }

    @Test
    public void testSliceDiceRollup2Measures() throws IOException {
        testIsomorphism(Lang.N3, "slice-dice-rollup-2measures");
    }
}
