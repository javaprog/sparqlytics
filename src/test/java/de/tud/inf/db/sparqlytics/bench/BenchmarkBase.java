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

import de.tud.inf.db.sparqlytics.model.Session;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.StringTokenizer;
import static org.junit.Assert.assertNotNull;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.junit.BeforeClass;

/**
 * Base class for benchmarks.
 *
 * @author Michael Rudolf
 */
public abstract class BenchmarkBase {
    /**
     * The name of the system property denoting the SPARQL endpoint to use.
     */
    protected static final String SPARQL_ENDPOINT_PROPERTY = "sparql.endpoint";
    
    /**
     * The name of the system property denoting the whitespace-separated list of
     * default graph IRIs to use in SPARQL queries.
     */
    protected static final String SPARQL_DEFAULT_GRAPH_PROPERTY =
            "sparql.defaultGraph";
    
    /**
     * The name of the system property denoting the whitespace-separated list of
     * named graph IRIs to use in SPARQL queries.
     */
    protected static final String SPARQL_NAMED_GRAPHS_PROPERTY =
            "sparql.namedGraphs";
    
    /**
     * The name of the system property denoting the output format for the
     * results of the SPARQL queries.
     */
    protected static final String SPARQLYTICS_OUTPUT_FORMAT_PROPERTY =
            "sparqlytics.outputFormat";

    /**
     * The output directory to use for computed measures.
     */
    protected static Path sink;

    @BeforeClass
    public static void setUp() throws IOException {
        sink = Files.createTempDirectory("sparqlytics-bench-ldbc-");
    }

    /**
     * Creates a SPARQLytics session to use for the tests.
     *
     * @return a new session
     */
    protected Session createSession() {
        Session session = new Session();
        configureSession(session);
        return session;
    }
    
    /**
     * Configures the given SPARQLytics session to use for the tests.
     *
     * @param session the session to configure
     * @throws NullPointerException if the parameter is {@code null}
     */
    protected static void configureSession(Session session) {
        String format = System.getProperty(SPARQLYTICS_OUTPUT_FORMAT_PROPERTY);
        if (format != null) {
            ResultsFormat resultsFormat = ResultsFormat.lookup(format);
            assertNotNull("Unsupported output format set in the \"" +
                    SPARQLYTICS_OUTPUT_FORMAT_PROPERTY + "\" system property:\"" +
                    format + "\"", resultsFormat);
            session.setResultsFormat(resultsFormat);
        }
        String endpoint = System.getProperty(SPARQL_ENDPOINT_PROPERTY);
        assertNotNull("SPARQL endpoint has not been configured. Set the \"" +
                SPARQL_ENDPOINT_PROPERTY + "\" system property.", endpoint);
        session.setSPARQLEndpointURL(endpoint);
        String defaultGraph = System.getProperty(SPARQL_DEFAULT_GRAPH_PROPERTY);
        if (defaultGraph != null) {
            StringTokenizer tokenizer = new StringTokenizer(defaultGraph);
            while (tokenizer.hasMoreTokens()) {
                session.getQuery().addGraphURI(tokenizer.nextToken());
            }
        }
        String namedGraphs = System.getProperty(SPARQL_NAMED_GRAPHS_PROPERTY);
        if (namedGraphs != null) {
            StringTokenizer tokenizer = new StringTokenizer(namedGraphs);
            while (tokenizer.hasMoreTokens()) {
                session.getQuery().addNamedGraphURI(tokenizer.nextToken());
            }
        }
        session.setSink(sink.toFile());
    }
}
