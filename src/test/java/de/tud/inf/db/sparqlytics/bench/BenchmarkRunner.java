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

import com.codahale.metrics.CsvReporter;
import de.tud.inf.db.sparqlytics.Main;
import java.io.File;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * Runs JUnit tests as benchmarks. Use with the {@link org.junit.runner.RunWith}
 * annotation.
 *
 * @author Michael Rudolf
 */
public class BenchmarkRunner extends BlockJUnit4ClassRunner {
    /**
     * Constructs a new instance of the benchmark runner for running the tests
     * in the given class.
     *
     * @param klass the class that should be benchmarked
     * @throws InitializationError  if there is an error initializing the
     *                              benchmark
     */
    public BenchmarkRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected Statement withAfterClasses(Statement statement) {
        return new RunBenchmarks(super.withAfterClasses(statement));
    }

    /**
     * Wrapper for executing all tests in a benchmark with the metrics reported
     * to the test report directory.
     */
    private static class RunBenchmarks extends Statement {
        private final Statement next;

        public RunBenchmarks(Statement next) {
            this.next = next;
        }

        @Override
        public void evaluate() throws Throwable {
            File directory = new File("target/surefire-reports");
            directory.mkdirs();
            CsvReporter reporter = CsvReporter.forRegistry(Main.METRICS).build(
                    directory);
            next.evaluate();
            reporter.report();
        }
    }
}
