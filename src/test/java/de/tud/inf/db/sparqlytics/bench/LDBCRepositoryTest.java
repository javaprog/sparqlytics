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

package de.tud.inf.db.sparqlytics.bench;

import de.tud.inf.db.sparqlytics.parser.SPARQLyticsParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

/**
 * Tests whether the SPARQLYtics repository file for the LDBC SNB parses.
 *
 * @author Michael Rudolf
 */
public class LDBCRepositoryTest {
    @Test
    public void test() throws Exception {
        SPARQLyticsParser parser = new SPARQLyticsParser(new InputStreamReader(
                LDBCBenchmark.class.getResourceAsStream(
                        "ldbc-snb-bi-repository.sparqlytics"), StandardCharsets.UTF_8));
        parser.Start();
    }
}
