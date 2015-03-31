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

/**
 * A roll-up operation decreases the granularity of a data cube in one
 * dimension.
 *
 * @author Michael Rudolf
 */
public class RollUp implements Operation {
    /**
     * The name of the dimension to roll up.
     */
    private final String name;
    
    /**
     * The number of steps to roll up.
     */
    private final int steps;

    /**
     * Creates a new roll-up operation for the dimension with the given name and 
     * given the number of steps to roll up.
     * 
     * @param name  the name of the dimension to roll up
     * @param steps the number of steps to roll up
     * @throws NullPointerException if the argument <code>name</code> is 
     *                              <code>null</code>
     * @throws IllegalArgumentException of the argument <code>steps</code> is 
     *                                  less than one
     */
    public RollUp(String name, int steps) {
        if (name == null) {
            throw new NullPointerException();
        } else if (steps < 1) {
            throw new IllegalArgumentException();
        }
        this.name = name;
        this.steps = steps;
    }

    @Override
    public void run(Session session) {
        Dimension dimension = session.findDimension(name);
        session.setGranularity(dimension, Math.min(dimension.getLevels().size() - 1,
                session.getGranularity(dimension) + steps));
    }
}
