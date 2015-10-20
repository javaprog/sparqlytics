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

import de.tud.inf.db.sparqlytics.model.Cube;
import de.tud.inf.db.sparqlytics.model.Dimension;
import de.tud.inf.db.sparqlytics.model.Measure;
import de.tud.inf.db.sparqlytics.model.Session;
import de.tud.inf.db.sparqlytics.olap.Compute;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.commons.io.FileUtils;
import org.apache.jena.riot.Lang;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Benchmarks the execution of SPARQLytics using the LDBC Social Network
 * Benchmark dataset.
 *
 * @author Michael Rudolf
 */
@RunWith(BenchmarkRunner.class)
public class LDBCBenchmark {
    /**
     * The name of the system property denoting the SPARQL endpoint to use.
     */
    private static final String SPARQL_ENDPOINT_PROPERTY = "sparql.endpoint";
    
    /**
     * The name of the system property denoting the whitespace-separated list of
     * default graph IRIs to use in SPARQL queries.
     */
    private static final String SPARQL_DEFAULT_GRAPH_PROPERTY =
            "sparql.defaultGraph";
    
    /**
     * The name of the system property denoting the whitespace-separated list of
     * named graph IRIs to use in SPARQL queries.
     */
    private static final String SPARQL_NAMED_GRAPHS_PROPERTY =
            "sparql.namedGraphs";
    
    /**
     * The output directory to use for computed measures.
     */
    private static Path sink;
    
    @BeforeClass
    public static void setUp() throws IOException {
        sink = Files.createTempDirectory("sparqlytics-bench-ldbc-");
    }
    
    @AfterClass
    public static void tearDown() throws IOException {
        FileUtils.deleteDirectory(sink.toFile());
    }
    
    /**
     * The random number generator to use for picking measures.
     */
    private final Random random = new Random();
    
    @Test
    public void testAllCubes() {
        Session session = createSession();
        LDBCRepository repository = new LDBCRepository();
        for (Cube cube : repository.getCubes()) {
            session.setCube(cube);
            testCube(session);
        }
    }
    
    /**
     * Creates a SPARQLytics session to use for the tests.
     * 
     * @return a new session
     */
    protected Session createSession() {
        Session session = new Session();
        session.setOutputFormat(Lang.N3);
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
        return session;
    }
    
    /**
     * Helper method to benchmark OLAP operations on the cube that is configured
     * in the given session.
     * 
     * @param session the configured session
     * @throws NullPointerException if the argument is {@code null}
     */
    protected void testCube(Session session) {
        Cube cube = session.getCube();
        Set<Measure> measures = cube.getMeasures();
        Set<Dimension> dimensions = cube.getDimensions();
        for (int i = 0; i < measures.size()*dimensions.size(); i++) {
            for (Dimension dimension : pickRandom(dimensions)) {
                session.setGranularity(dimension, 
                        random.nextInt(dimension.getLevels().size()));
            }
            session.execute(new Compute(pickRandom(measures)));
        }
    }
    
    /**
     * Randomly picks a random positive number of elements from the given
     * collection.
     * 
     * @param <T>           the type of elements
     * @param collection    the input from which to pick elements randomly
     * @return a random number of elements randomly picked from the input
     * @throws NullPointerException if the argument is {@code null}
     * @throws IllegalArgumentException if the input is empty
     */
    private <T> List<T> pickRandom(Collection<T> collection) {
        if (collection.isEmpty()) {
            throw new IllegalArgumentException();
        }
        int size = collection.size();
        return size == 1 ? new ArrayList<>(collection) :
                pickNRandom(collection, random.nextInt(size - 1) + 1);
    }
    
    /**
     * Randomly picks {@code n} elements from the given collection.
     * 
     * @param <T>           the type of elements
     * @param collection    the input from which to pick elements randomly
     * @param n             the number of elements to pick from the input
     * @return the specified number of elements randomly picked from the input
     * @throws IndexOutOfBoundsException    if {@code n} is not a positive
     *                                      number smaller than the size of the
     *                                      input
     */
    private <T> List<T> pickNRandom(Collection<T> collection, int n) {
        List<T> list = new ArrayList<>(collection);
        Collections.shuffle(list, random);
        return list.subList(0, n);
    }
}
