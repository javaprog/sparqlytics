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

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import java.io.File;
import java.io.IOException;
import org.apache.jena.fuseki.EmbeddedFusekiServer;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Integration test.
 *
 * @author Michael Rudolf
 */
public class IT {
    /**
     * The Fuseki server instance to use as SPARQL endpoint.
     */
    private static EmbeddedFusekiServer server;
    
    @BeforeClass
    public static void beforeClass() {
        DatasetGraph datasetGraph = RDFDataMgr.loadDatasetGraph(
                IT.class.getResource("test.ttl").getPath());
        server = EmbeddedFusekiServer.create(3030, datasetGraph, "ds");
        server.start();
    }
    
    @AfterClass
    public static void afterClass() {
        if (server != null) {
            server.stop();
        }
    }
    
    @Test
    public void test() throws IOException {
        File tmp = File.createTempFile("sparqlytics", ".n3");
        tmp.deleteOnExit();
        Main.main(new String[]{
            "-input", getClass().getResource("test.sparqlytics").getPath(), 
            "-output", tmp.getAbsolutePath(), "-debug"
        });
        Graph expected = RDFDataMgr.loadGraph(
                getClass().getResource("expected.n3").getPath());
        Graph result = RDFDataMgr.loadGraph(tmp.getAbsolutePath());
        assertTrue("Result does not match expectations", 
                expected.isIsomorphicWith(result));
    }
}
