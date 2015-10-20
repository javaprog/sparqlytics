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

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.PatternVars;
import de.tud.inf.db.sparqlytics.repository.FixedRepository;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Represents a cube consisting of facts, dimensions and measures.
 *
 * @author Michael Rudolf
 */
public class Cube extends NamedObject {
    /**
     * The pattern to use for selecting facts.
     */
    private final Element factPattern;

    /**
     * The measures in this cube.
     */
    private final FixedRepository impl;

    /**
     * Creates a new cube with the given name, fact selection pattern,
     * dimensions and measures.
     *
     * @param name          the name of the cube
     * @param factPattern   the pattern used to select facts
     * @param dimensions    the dimensions to apply to the facts
     * @param measures      the measures to compute for the facts
     * @throws NullPointerException     if any argument is {@code null}
     * @throws IllegalArgumentException if no dimensions or no measures were
     *                                  supplied or if a dimension or a measure
     *                                  does not reference any fact pattern
     *                                  variable
     */
    public Cube(final String name, final Element factPattern,
            final Set<Dimension> dimensions, final Set<Measure> measures) {
        super(name);
        if (factPattern == null) {
            throw new NullPointerException();
        }
        this.factPattern = factPattern;
        if (dimensions.isEmpty() || measures.isEmpty()) {
            throw new IllegalArgumentException();
        }
        final Collection<Var> factPatternVars = PatternVars.vars(factPattern);
        for (Dimension dimension : dimensions) {
            List<Var> vars = new LinkedList<>();
            PatternVars.vars(vars, dimension.getSeedPattern());
            vars.retainAll(factPatternVars);
            if (vars.isEmpty()) {
                throw new IllegalArgumentException("Dimension \"" +
                        dimension.getName() +
                        "\" does not reference any fact pattern variables");
            }
        }
        for (Measure measure : measures) {
            List<Var> vars = new LinkedList<>();
            PatternVars.vars(vars, measure.getSeedPattern());
            vars.retainAll(factPatternVars);
            if (vars.isEmpty()) {
                throw new IllegalArgumentException("Dimension \"" +
                        measure.getName() +
                        "\" does not reference any fact pattern variables");
            }
        }
        this.impl = new FixedRepository(Collections.<Cube>emptySet(),
                dimensions, measures);
    }

    /**
     * Returns the pattern used to select facts.
     *
     * @return the pattern for selecting facts
     */
    public Element getFactPattern() {
        return factPattern;
    }

    /**
     * Returns an unmodifiable set of all dimensions in this cube.
     *
     * @return all dimensions
     */
    public Set<Dimension> getDimensions() {
        return impl.getDimensions();
    }

    /**
     * Returns the dimension with the given name.
     *
     * @param name the name of the dimension to be looked up
     * @return the dimension with the given name
     * @throws NoSuchElementException   if this cube does not contain a
     *                                  dimension with the given name
     */
    public Dimension findDimension(final String name) {
        return impl.findDimension(name);
    }

    /**
     * Returns an unmodifiable set of all measures in this cube.
     *
     * @return all measures
     */
    public Set<Measure> getMeasures() {
        return impl.getMeasures();
    }

    /**
     * Returns the measure with the given name.
     *
     * @param name the name of the measure to be looked up
     * @return the measure with the given name
     * @throws NoSuchElementException   if this cube does not contain a
     *                                  measure with the given name
     */
    public Measure findMeasure(final String name) {
        return impl.findMeasure(name);
    }
}
