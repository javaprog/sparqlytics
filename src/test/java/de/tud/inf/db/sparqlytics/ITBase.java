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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

/**
 * Base class for integration tests.
 *
 * @author Michael Rudolf
 */
public abstract class ITBase {
    /**
     * The test fixture to prepend to every test input.
     */
    private final URL fixture;

    /**
     * Creates a new test case for the given test fixture.
     *
     * @param fixture the test fixture to prepend to every test input
     * @throws NullPointerException if the argument is {@code null}
     */
    protected ITBase(URL fixture) {
        if (fixture == null) {
            throw new NullPointerException();
        }
        this.fixture = fixture;
    }

    /**
     * Helper method that derives the name of the test case input and expected
     * output from the given name prefix by appending the respective file
     * extensions. The file extension for test inputs is
     * {@code .test.sparqlytics} and for expected output it is
     * {@code .expected.n3}.
     *
     * @param lang      the language to use for formatting the models in the
     *                  error message
     * @param prefix    the prefix of the names for the test input and expected
     *                  output files
     * @throws IOException  if an exception occurs when reading the test input,
     *                      the test fixture or the expected output or when
     *                      writing the test output
     * @throws NullPointerException if any of the arguments is {@code null}
     */
    protected void testIsomorphism(Lang lang, String prefix) throws IOException {
        testIsomorphism(lang, prefix.concat(".test.sparqlytics"),
                prefix.concat(".expected.n3"));
    }

    /**
     * Tests whether the given test input prepended with the test fixture
     * produces an output that is isomorphic to the given output. The test input
     * and expected output are resolved to URIs relative to the current class.
     *
     * @param lang      the language to use for formatting the models in the
     *                  error message
     * @param testCase  the test input to use (after prepending it with the test
     *                  fixture)
     * @param expected  the expected test output, to which the actual output has
     *                  to be isomorphic
     * @throws IOException  if an exception occurs when reading the test input,
     *                      the test fixture or the expected output or when
     *                      writing the test output
     * @throws NullPointerException if any of the arguments is {@code null}
     */
    protected void testIsomorphism(Lang lang, String testCase, String expected)
            throws IOException {
        testIsomorphism(lang, getClass().getResource(testCase),
                getClass().getResource(expected));
    }

    /**
     * Tests whether the given test input prepended with the test fixture
     * produces an output that is isomorphic to the given output.
     *
     * @param lang      the language to use for formatting the models in the
     *                  error message
     * @param testCase  the test input to use (after prepending it with the test
     *                  fixture)
     * @param expected  the expected test output, to which the actual output has
     *                  to be isomorphic
     * @throws IOException  if an exception occurs when reading the test input,
     *                      the test fixture or the expected output or when
     *                      writing the test output
     * @throws NullPointerException if any of the arguments is {@code null}
     */
    protected void testIsomorphism(Lang lang, URL testCase, URL expected)
            throws IOException {
        try (TemporaryFile output = createTempFile("sparqlytics-output-",
                    "." + lang.getFileExtensions().get(0));
                TemporaryFile input = createTempFile("sparqlytics-input-", ".sparqlytics")) {
            try (InputStream in = new SequenceInputStream(
                    fixture.openStream(), testCase.openStream())) {
                Files.copy(in, input.getPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            Main.main(new String[]{
                "-input", input.getPath().toString(),
                "-output", output.getPath().toString(), "-debug"
            });
            try (ModelGuard expectedModel = loadModel(expected.getPath());
                    ModelGuard resultModel = loadModel(output.getPath().toString())) {
                assertIsomorphic(lang, expectedModel.getModel(), resultModel.getModel());
            }
        }
    }

    /**
     * Asserts that two models are isomorphic. If they are not isomorphic, an
     * {@link AssertionError} is thrown.
     *
     * @param lang      the language to use for formatting the models in the
     *                  error message
     * @param expected  the expected model
     * @param actual    the model to compare to {@code expected}
     * @throws NullPointerException if any of the arguments is {@code null}
     */
    public static void assertIsomorphic(Lang lang, Model expected, Model actual) {
        if (!expected.isIsomorphicWith(actual)) {
            Model missing = expected.difference(actual);
            Model unexpected = actual.difference(expected);
            StringWriter message = new StringWriter();
            message.append("Result does not match expectations. ");
            if (missing.size() < expected.size() &&
                    unexpected.size() < actual.size()) {
                message.append("The missing triples are:\n");
                missing.write(message, lang.getLabel());
                message.append("\n\nThe unexpected triples are:\n");
                unexpected.write(message, lang.getLabel());
            } else {
                message.append("Expected:\n");
                expected.write(message, lang.getLabel());
                message.append("\n\n But was:\n");
                actual.write(message, lang.getLabel());
            }
            throw new AssertionError(message.toString());
        }
    }

    /**
     * Creates a closeable handle for a temporary file, which is created in the
     * system's temporary directory and uses the given prefix and suffix as well
     * as the given optional file attributes.
     *
     * @see Files#createTemporaryFile(String, String, FileAttribute&lt;?&gt;[])
     * @param prefix {@inheritDoc}
     * @param suffix {@inheritDoc}
     * @param attrs  {@inheritDoc}
     * @return a closeable handle to be used in a try-with-resources block
     * @throws IllegalArgumentException         {@inheritDoc}
     * @throws UnsupportedOperationException    {@inheritDoc}
     * @throws IOException                      {@inheritDoc}
     * @throws SecurityException                {@inheritDoc}
     */
    public static TemporaryFile createTempFile(String prefix, String suffix,
            FileAttribute<?>... attrs) throws IOException {
        return new TemporaryFile(Files.createTempFile(prefix, suffix, attrs));
    }

    /**
     * A closeable handle for temporary files. When used in a try-with-resources
     * block, the file will be deleted when control flow leaves the block.
     */
    public static class TemporaryFile implements Closeable {
        /**
         * The represented temporary file.
         */
        private final Path path;

        /**
         * Creates a new closeable handle to the given temporary file.
         *
         * @param file the file to delete
         * @throws NullPointerException if the argument is {@code null}
         */
        public TemporaryFile(Path file) {
            if (file == null) {
                throw new NullPointerException();
            }
            this.path = file;
        }

        /**
         * Returns the represented temporary file.
         *
         * @return the file to delete
         */
        public Path getPath() {
            return path;
        }

        @Override
        public void close() throws IOException {
            Files.deleteIfExists(path);
        }
    }

    /**
     * Creates an auto-closeable handle for the RDF model at the given URI.
     *
     * @param uri the URI to load the RDF model from
     * @return an auto-closeable handle to be used in a try-with-resources block
     */
    public static ModelGuard loadModel(String uri) {
        return new ModelGuard(RDFDataMgr.loadModel(uri));
    }

    /**
     * An auto-closeable handle for RDF models. When used in a try-with-resources
     * block, the model will be closed when control flow leaves the block.
     */
    public static class ModelGuard implements AutoCloseable {
        /**
         * The wrapped RDF model.
         */
        private final Model model;

        /**
         * Creates a new auto-closeable handle for the given RDF model.
         *
         * @param model the model to close
         * @throws NullPointerException if the argument is {@code null}
         */
        public ModelGuard(Model model) {
            if (model == null) {
                throw new NullPointerException();
            }
            this.model = model;
        }

        /**
         * Returns the wrapped RDF model.
         *
         * @return the model to close
         */
        public Model getModel() {
            return model;
        }

        @Override
        public void close() {
            model.close();
        }
    }
}
