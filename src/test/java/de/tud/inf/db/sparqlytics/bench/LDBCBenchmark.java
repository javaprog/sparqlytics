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

import com.codahale.metrics.Slf4jReporter;
import de.tud.inf.db.sparqlytics.model.Cube;
import de.tud.inf.db.sparqlytics.model.Session;
import de.tud.inf.db.sparqlytics.parser.SPARQLyticsParser;
import de.tud.inf.db.sparqlytics.repository.Repository;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.jena.util.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 * Benchmarks the execution of SPARQLytics using the LDBC Social Network
 * Benchmark dataset.
 *
 * @author Michael Rudolf
 */
@RunWith(BenchmarkRunner.class) @FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LDBCBenchmark extends RandomBenchmarkBase  {
    private final Map<String, String> globalVariables;
    
    public LDBCBenchmark() {
        globalVariables = new HashMap<>();
        Properties systemProperties = System.getProperties();
        for (String name : systemProperties.stringPropertyNames()) {
            globalVariables.put(name, systemProperties.getProperty(name));
        }
    }
    
    @BeforeClass
    public static void bindRepository() throws Exception {
        SPARQLyticsParser parser = new SPARQLyticsParser(new InputStreamReader(
                LDBCBenchmark.class.getResourceAsStream(
                        "ldbc-snb-bi-repository.sparqlytics"), StandardCharsets.UTF_8));
        parser.Start();
        Repository repository = parser.getRepository();
        Slf4jReporter.forRegistry(repository.getStatistics())
                .withLoggingLevel(Slf4jReporter.LoggingLevel.INFO)
                .prefixedWith("before:")
                .build().report();
        //Set system properties Context.INITIAL_CONTEXT_FACTORY and Context.PROVIDER_URL in pom.xml
        new InitialContext(new Hashtable<Object, Object>()).bind("ldbc", repository);
    }
    
    @AfterClass
    public static void unbindRepository() throws NamingException {
        InitialContext ctx = new InitialContext();
        Repository repository = (Repository)ctx.lookup("ldbc");
        Slf4jReporter.forRegistry(repository.getStatistics())
                .withLoggingLevel(Slf4jReporter.LoggingLevel.INFO)
                .prefixedWith("after:")
                .build().report();
        ctx.unbind("ldbc");
    }
    
    @Test
    public void testQ01() throws Exception {
        test("01", Collections.singletonMap("date", "2014-01-01"));
    }
    
    @Test
    public void testQ02() throws Exception {
        Map<String, String> variables = new HashMap<>();
        variables.put("country1", "http://dbpedia.org/resources/Germany");
        variables.put("country2", "http://dbpedia.org/resources/Italy");
        test("02", variables);
    }
    
    @Test
    public void testQ03() throws Exception {
        Map<String, String> variables = new HashMap<>();
        variables.put("year", "2012");
        variables.put("month", "5");
        test("03", variables);
    }
    
    @Test
    public void testQ04() throws Exception {
        Map<String, String> variables = new HashMap<>();
        variables.put("tagClass", "http://dbpedia.org/resource/Company");
        variables.put("country", "http://dbpedia.org/resource/Germany");
        test("04", variables);
    }
    
    @Test
    public void testQ05() throws Exception {
        test("05", Collections.singletonMap("country",
                "http://dbpedia.org/resource/Germany"));
    }
    
    @Test
    public void testQ08() throws Exception {
        test("08", Collections.singletonMap("tag",
                "http://dbpedia.org/resource/Germany"));
    }
    
    @Test
    public void testQ11() throws Exception {
        Map<String, String> variables = new HashMap<>();
        variables.put("country", "http://dbpedia.org/resource/Germany");
        variables.put("blacklist", "idiot|loser");
        test("11", variables);
    }
    
    @Test
    public void testQ12() throws Exception {
        test("12", Collections.singletonMap("date", "2013-01-01"));
    }
    
    @Test
    public void testQ19() throws Exception {
        Map<String, String> variables = new HashMap<>();
        variables.put("date", "1983-05-11");
        variables.put("tagClass1", "http://dbpedia.org/resource/Film");
        variables.put("tagClass2", "http://dbpedia.org/resource/Album");
        test("19", variables);
    }
    
    @Test
    public void testQ20() throws Exception {
        test("20", Collections.EMPTY_MAP);
    }
    
    @Test
    public void testQ23() throws Exception {
        test("23", Collections.singletonMap("country",
                "http://dbpedia.org/resource/Germany"));
    }
    
    @Test
    public void testQ24() throws Exception {
        test("24", Collections.singletonMap("tagClass",
                "http://dbpedia.org/resource/Company"));
    }
    
    protected void test(String number, Map<String, String> variables)
            throws Exception {
        String contents = FileUtils.readWholeFileAsUTF8(
                LDBCBenchmark.class.getResourceAsStream("ldbc-snb-bi-q" +
                        number + ".sparqlytics"));
        try {
            variables.putAll(globalVariables);
        } catch (UnsupportedOperationException ex) {
            variables = new HashMap<>(variables);
            variables.putAll(globalVariables);
        }
        SPARQLyticsParser parser = new SPARQLyticsParser(new StringReader(
                StrSubstitutor.replace(contents, variables)));
        configureSession(parser.getSession());
        parser.Start();
    }
    
    @Test
    public void testQAllInSuccession() throws Exception {
        String contents = FileUtils.readWholeFileAsUTF8(
                LDBCBenchmark.class.getResourceAsStream("ldbc-snb-bi-all.sparqlytics"));
        SPARQLyticsParser parser = new SPARQLyticsParser(new StringReader(
                StrSubstitutor.replace(contents, globalVariables)));
        configureSession(parser.getSession());
        parser.Start();
    }
    
    @Test
    public void testQAllCubesRandomly() throws NamingException {
        Session session = createSession();
        Repository repository = (Repository)InitialContext.doLookup("ldbc");
        for (Cube cube : repository.getCubes()) {
            session.setCube(cube);
            testCube(session);
        }
    }
}
