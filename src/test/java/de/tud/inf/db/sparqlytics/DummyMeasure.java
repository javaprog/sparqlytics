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

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import de.tud.inf.db.sparqlytics.model.Measure;
import java.util.Collections;

/**
 * Helper class for tests. All properties except for the name are set to dummy
 * values.
 *
 * @author Michael Rudolf
 */
public class DummyMeasure extends Measure {
    public DummyMeasure(String name) {
        super(name, new ElementTriplesBlock(BasicPattern.wrap(
            Collections.singletonList(Triple.createMatch(
                    NodeFactory.createVariable("test"), null, null)))),
                NodeValue.TRUE, "COUNT");
    }
}
