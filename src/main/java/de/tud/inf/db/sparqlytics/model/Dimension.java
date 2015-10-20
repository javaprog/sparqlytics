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
import com.hp.hpl.jena.sparql.expr.ExprVars;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.PatternVars;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents a dimension.
 *
 * @author Michael Rudolf
 */
public class Dimension extends NamedObject {
    /**
     * The seed pattern connecting the level expressions with the facts.
     */
    private final Element seedPattern;

    /**
     * The levels in this dimension.
     */
    private final List<Level> levels;

    /**
     * Creates a new dimension with the given name, seed pattern and levels.
     * The special level {@link Level#ALL} is automatically appended to the
     * provided levels.
     *
     * @param name          the name of the dimension
     * @param seedPattern   the seed pattern connecting the level expression
     *                      with the facts
     * @param levels        the levels
     *
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if a level expressions references a
     *                                  variable not present in the seed pattern
     *                                  or if no levels were provided
     */
    public Dimension(final String name, final Element seedPattern,
            final List<Level> levels) {
        super(name);
        if (levels.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Collection<Var> seedPatternVars = PatternVars.vars(seedPattern);
        for (Level level : levels) {
            List<Var> vars = new LinkedList<>();
            ExprVars.varsMentioned(vars, level.getExpression());
            vars.removeAll(seedPatternVars);
            if (!vars.isEmpty()) {
                throw new IllegalArgumentException("Level \"" + level.getName() +
                        "\" references unknown variable(s): " +
                        StringUtils.join(vars, ", "));
            }
        }
        this.seedPattern = seedPattern;
        this.levels = new ArrayList<>(levels.size() + 1);
        this.levels.addAll(levels);
        this.levels.add(Level.ALL);
    }

    /**
     * Returns the seed pattern that connects the level expressions with the
     * facts.
     *
     * @return the seed pattern
     */
    public Element getSeedPattern() {
        return seedPattern;
    }

    /**
     * Returns the levels in this dimension.
     *
     * @return the levels in this dimension
     */
    public List<Level> getLevels() {
        return Collections.unmodifiableList(levels);
    }

    /**
     * Returns the level with the given name.
     *
     * @param name the name of the level to be looked up
     * @return the level with the given name
     * @throws NoSuchElementException   if this dimension does not contain a
     *                                  level with the given name
     */
    public Level findLevel(final String name) {
        for (Level level : levels) {
            if (level.getName().equals(name)) {
                return level;
            }
        }
        throw new NoSuchElementException(name);
    }
}
