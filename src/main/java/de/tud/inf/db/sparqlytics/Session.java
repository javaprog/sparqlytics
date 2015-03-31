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

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.syntax.Element;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.riot.Lang;

/**
 * Represents an analytical session, which encapsulates dimensions, measures, 
 * and filters and has a granularity.
 *
 * @author Michael Rudolf
 */
public class Session {
    /**
     * Contains prologue and dataset information.
     */
    private final Query query = new Query();
    
    /**
     * The file or folder serving as output or <code>null</code>.
     */
    private File sink;
    
    /**
     * The date format used when creating new output files.
     */
    private final DateFormat dateFormat = new SimpleDateFormat("MMddyyyy-HHmmss.SSS");
    
    /**
     * The output format to use for the computed measures. <code>null</code> 
     * implies &quot;RDF/XML&quot;.
     */
    private Lang outputFormat;
    
    /**
     * The pattern to use for selecting facts.
     */
    private Element factPattern;
    
    /**
     * The dimensions in this session.
     */
    private final Set<Dimension> dimensions = new HashSet<Dimension>();
    
    /**
     * The measures in this session.
     */
    private final Set<Measure> measures = new HashSet<Measure>();
    
    /**
     * The URL of the SPARQL endpoint to use when computing measures.
     */
    private String sparqlEndpointURL;
    
    /**
     * The granularity for aggregating facts.
     */
    private final Map<Dimension, Integer> granularity = 
            new HashMap<Dimension, Integer>();
    
    /**
     * The filters in this session.
     */
    private final Map<Pair<Dimension, Level>, Filter> filters = 
            new HashMap<Pair<Dimension, Level>, Filter>();
    
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
     * <code>null</code>, output will be appended to the standard output stream.
     * 
     * @param sink the sink for creating output writers or <code>null</code>
     * 
     * @see #getOutput
     */
    public void setSink(File sink) {
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
            return new OutputStreamWriter(System.out);
        } else if (sink.isFile()) {
            return new BufferedWriter(new FileWriter(sink, true));
        } else {
            Lang format = getOutputFormat();
            if (format == null) {
                format = Lang.RDFXML;
            }
            List<String> extensions = format.getFileExtensions();
            StringBuilder name = new StringBuilder(dateFormat.format(new Date()));
            if (!extensions.isEmpty()) {
                name.append('.').append(extensions.get(0));
            }
            return new BufferedWriter(new FileWriter(new File(sink, name.toString())));
        }
    }
    
    /**
     * Returns the output format to use for the computed measures. 
     * <code>null</code> implies &quot;RDF/XML&quot;.
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
     * <code>null</code> implies &quot;RDF/XML&quot;.
     * 
     * @param outputFormat the output format to use
     * 
     * @see #getOutputFormat
     */
    public void setOutputFormat(Lang outputFormat) {
        this.outputFormat = outputFormat;
    }
    
    /**
     * Returns the pattern used to select facts.
     * 
     * @return the pattern for selecting facts
     * 
     * @see #setFactPattern
     */
    public Element getFactPattern() {
        return factPattern;
    }
    
    /**
     * Sets the fact pattern used to select facts.
     * 
     * @param factPattern   the pattern to use for selecting facts
     * 
     * @throws NullPointerException if the argument is <code>null</code>
     * 
     * @see #getFactPattern
     */
    public void setFactPattern(Element factPattern) {
        if (factPattern == null) {
            throw new NullPointerException();
        }
        this.factPattern = factPattern;
    }
    
    /**
     * Adds the given measure to this session, provided that no other measure 
     * with the same name is already part of this session.
     * 
     * @param dimension the dimension to add
     * @return whether the dimension was added
     * 
     * @see #removeDimension
     */
    public boolean addDimension(Dimension dimension) {
        if (dimensions.add(dimension)) {
            granularity.put(dimension, 0);
            return true;
        }
        return false;
    }
    
    /**
     * Removes the given dimension from this session, if it is part of this
     * session.
     * 
     * @param dimension the dimension to remove
     * @return whether the dimension was removed
     * 
     * @see #addDimension
     */
    public boolean removeDimension(Dimension dimension) {
        if (dimensions.remove(dimension)) {
            granularity.remove(dimension);
            return true;
        }
        return false;
    }
    
    /**
     * Returns an unmodifiable set of all dimensions in this session.
     * 
     * @return all dimensions
     */
    public Set<Dimension> getDimensions() {
        return Collections.unmodifiableSet(dimensions);
    }
    
    /**
     * Returns the dimension with the given name.
     * 
     * @param name the name of the dimension to be looked up
     * @return the dimension with the given name
     * @throws NoSuchElementException   if this session does not contain a 
     *                                  dimension with the given name
     */
    public Dimension findDimension(String name) {
        for (Dimension dimension : dimensions) {
            if (dimension.getName().equals(name)) {
                return dimension;
            }
        }
        throw new NoSuchElementException(name);
    }
    
    /**
     * Adds the given measure to this session, provided that no other measure 
     * with the same name is already part of this session.
     * 
     * @param measure the measure to add
     * @return whether the measure was added
     * 
     * @see #removeMeasure
     */
    public boolean addMeasure(Measure measure) {
        return measures.add(measure);
    }
    
    /**
     * Removes the given measure from this session, if it is part of this
     * session.
     * 
     * @param measure the measure to remove
     * @return whether the measure was removed
     * 
     * @see #addMeasure
     */
    public boolean removeMeasure(Measure measure) {
        return measures.remove(measure);
    }
    
    /**
     * Returns an unmodifiable set of all measures in this session.
     * 
     * @return all measures
     */
    public Set<Measure> getMeasures() {
        return Collections.unmodifiableSet(measures);
    }
    
    /**
     * Returns the measure with the given name.
     * 
     * @param name the name of the measure to be looked up
     * @return the measure with the given name
     * @throws NoSuchElementException   if this session does not contain a 
     *                                  measure with the given name
     */
    public Measure findMeasure(String name) {
        for (Measure measure : measures) {
            if (measure.getName().equals(name)) {
                return measure;
            }
        }
        throw new NoSuchElementException(name);
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
    public void setSPARQLEndpointURL(String url) {
        this.sparqlEndpointURL = url;
    }
    
    /**
     * Sets the granularity of the given dimension to the given level.
     * 
     * @param dimension the dimension for which the aggregation level should be
     *                  changed
     * @param level     the new aggregation level of the given dimension
     * @throw IllegalArgumentException  if the given dimension is not part of
     *                                  this session or if the given level is
     *                                  less than zero
     * 
     * @see #getGranularity
     */
    public void setGranularity(Dimension dimension, int level) {
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
     *                                  this session
     * 
     * @see #setGranularity
     */
    public int getGranularity(Dimension dimension) {
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
    public void addFilter(Dimension dimension, Level level, Filter filter) {
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
    public boolean removeFilter(Dimension dimension, Level level) {
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
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public void execute(Operation operation) {
        operation.run(this);
    }
}
