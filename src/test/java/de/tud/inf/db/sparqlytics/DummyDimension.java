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

import de.tud.inf.db.sparqlytics.model.Dimension;
import de.tud.inf.db.sparqlytics.model.Level;
import java.util.Arrays;
import java.util.Collections;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;

/**
 * Helper class for tests. All properties except for the name are set to dummy
 * values. The dimension contains three dummy levels.
 *
 * @author Michael Rudolf
 */
public class DummyDimension extends Dimension {
    public DummyDimension(String name) {
        super(name, new ElementTriplesBlock(BasicPattern.wrap(
                Collections.singletonList(Triple.createMatch(
                    NodeFactory.createVariable("test"), null, null)))),
            Arrays.asList(
                new Level("lev1", NodeValue.TRUE),
                new Level("lev2", NodeValue.TRUE),
                new Level("lev3", NodeValue.TRUE)
        ));
    }
}
