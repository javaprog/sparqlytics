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

package de.tud.inf.db.sparqlytics.model;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.Syntax;
import de.tud.inf.db.sparqlytics.olap.Operation;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.riot.Lang;

/**
 * Represents an analytical session, which encapsulates a cube and its filters
 * and has a granularity.
 *
 * @author Michael Rudolf
 */
public class Session {
    /**
     * Contains prologue and dataset information.
     */
    private final Query query = new Query();

    /**
     * The file or folder serving as output or {@code null}.
     */
    private File sink;

    /**
     * The date format used when creating new output files.
     */
    private final DateFormat dateFormat = new SimpleDateFormat("MMddyyyy-HHmmss.SSS");

    /**
     * The output format to use for the computed measures. {@code null}
     * implies &quot;RDF/XML&quot;.
     */
    private Lang outputFormat;
    
    /**
     * The cube to use for OLAP operations.
     */
    private Cube cube;

    /**
     * The URL of the SPARQL endpoint to use when computing measures.
     */
    private String sparqlEndpointURL;

    /**
     * The granularity for aggregating facts.
     */
    private final Map<Dimension, Integer> granularity = new HashMap<>();

    /**
     * The filters in this session.
     */
    private final Map<Pair<Dimension, Level>, Filter> filters = new HashMap<>();

    /**
     * Creates a new session.
     */
    public Session() {
        query.setSyntax(Syntax.syntaxSPARQL_11);
    }

    /**
     * Returns an object with prologue and dataset information.
     *
     * @return an object containing prologue and dataset information
     */
    public Query getQuery() {
        return query;
    }

    /**
     * Sets the sink for creating output writers. If the given file denotes a
     * directory, then every output request will result in a new file to be
     * created in that directory. If the given file denotes a regular file, then
     * all output requests will append to that file. If the given argument is
     * {@code null}, output will be appended to the standard output stream.
     *
     * @param sink the sink for creating output writers or {@code null}
     *
     * @see #getOutput
     */
    public void setSink(final File sink) {
        this.sink = sink;
    }

    /**
     * Creates a new output writer.
     *
     * @return a new output writer
     * @throws IOException if an error occurs
     *
     * @see #setSink
     */
    public Writer getOutput() throws IOException {
        if (sink == null) {
            //System.out must not be closed
            return new PrintWriter(System.out) {
                @Override
                public void close() {}
            };
        } else if (sink.isFile()) {
            return new BufferedWriter(new FileWriter(sink, true));
        } else {
            Lang format = getOutputFormat();
            if (format == null) {
                format = Lang.RDFXML;
            }
            List<String> extensions = format.getFileExtensions();
            StringBuilder name = new StringBuilder(
                    dateFormat.format(new Date()));
            if (!extensions.isEmpty()) {
                name.append('.').append(extensions.get(0));
            }
            return new BufferedWriter(new FileWriter(
                    new File(sink, name.toString())));
        }
    }

    /**
     * Returns the output format to use for the computed measures.
     * {@code null} implies &quot;RDF/XML&quot;.
     *
     * @return the output format
     *
     * @see #setOutputFormat
     */
    public Lang getOutputFormat() {
        return outputFormat;
    }

    /**
     * Sets the output format to use for the computed measures.
     * {@code null} implies &quot;RDF/XML&quot;.
     *
     * @param outputFormat the output format to use
     *
     * @see #getOutputFormat
     */
    public void setOutputFormat(final Lang outputFormat) {
        this.outputFormat = outputFormat;
    }
    
    /**
     * Returns the cube to use for OLAP operations.
     * 
     * @return the cube
     * 
     * @see #setCube
     */
    public Cube getCube() {
        return cube;
    }
    
    /**
     * Sets the cube to use for OLAP operations.
     * 
     * @param cube the cube
     * 
     * @see #getCube
     */
    public void setCube(final Cube cube) {
        this.cube = cube;
        granularity.clear();
        if (cube != null) {
            for (Dimension dimension : cube.getDimensions()) {
                granularity.put(dimension, 0);
            }
        }
    }

    /**
     * Returns the URL of the SPARQL endpoint to use when computing measures.
     *
     * @return the URL of the SPARQL endpoint
     *
     * @see #setSPARQLEndpointURL
     */
    public String getSPARQLEndpointURL() {
        return sparqlEndpointURL;
    }

    /**
     * Sets the URL of the SPARQL endpoint to use when computing measures.
     *
     * @param url the URL of the SPARQL endpoint
     *
     * @see #getSPARQLEndpointURL
     */
    public void setSPARQLEndpointURL(final String url) {
        this.sparqlEndpointURL = url;
    }

    /**
     * Sets the granularity of the given dimension to the given level.
     *
     * @param dimension the dimension for which the aggregation level should be
     *                  changed
     * @param level     the new aggregation level of the given dimension
     * @throw IllegalArgumentException  if the given dimension is not part of
     *                                  the session's cube or if the given level
     *                                  is less than zero
     *
     * @see #getGranularity
     */
    public void setGranularity(final Dimension dimension, final int level) {
        if (level < 0) {
            throw new IllegalArgumentException();
        } else if (granularity.put(dimension, level) == null) {
            granularity.remove(dimension);
            throw new IllegalArgumentException();
        }
    }

    /**
     * Returns the granularity for the given dimension.
     *
     * @param dimension the dimension in question
     * @return the level of aggregation in the given dimension
     * @throws IllegalArgumentException if the given dimension is not part of
     *                                  the session's cube
     *
     * @see #setGranularity
     */
    public int getGranularity(final Dimension dimension) {
        Integer level = granularity.get(dimension);
        if (level == null) {
            throw new IllegalArgumentException();
        }
        return level;
    }

    /**
     * Adds the given filter for the given level in the given dimension.
     *
     * @param dimension the dimension to add the filter for
     * @param level     the level in the given dimension
     * @param filter    the filter to add
     */
    public void addFilter(final Dimension dimension, final Level level,
            final Filter filter) {
        filters.put(Pair.of(dimension, level), filter);
    }

    /**
     * Removes the filter for the given level of the specified dimension.
     *
     * @param dimension the dimension of the given level
     * @param level     the level in the given dimension for which the filter
     *                  should be removed
     * @return whether the filter was removed
     *
     * @see #addFilter
     */
    public boolean removeFilter(final Dimension dimension, final Level level) {
        return filters.remove(Pair.of(dimension, level)) != null;
    }

    /**
     * Returns a mapping of dimension-level pairs to filters.
     *
     * @return  a mapping of dimension-level pairs to filters
     */
    public Map<Pair<Dimension, Level>, Filter> getFilters() {
        return Collections.unmodifiableMap(filters);
    }

    /**
     * Executes the given operation in the context of this session.
     *
     * @param operation the operation to execute
     * @throws NullPointerException     if the argument is {@code null}
     * @throws IllegalStateException    if no cube has been set
     */
    public void execute(final Operation operation) {
        if (cube == null) {
            throw new IllegalStateException();
        }
        operation.run(this);
    }
}
