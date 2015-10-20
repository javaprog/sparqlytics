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

/**
 * Super class for named objects.
 *
 * @author Michael Rudolf
 */
public abstract class NamedObject {
    /**
     * The object's name.
     */
    private String name;

    /**
     * Creates a new named object.
     *
     * @param name the object's name, must never be {@code null}
     *
     * @throws NullPointerException if the argument is {@code null}
     */
    protected NamedObject(final String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
    }

    /**
     * Returns the object's name.
     *
     * @return the name of the object
     */
    public final String getName() {
        return name;
    }

    @Override
    public final int hashCode() {
        return name.hashCode();
    }

    @Override
    public final boolean equals(final Object o) {
        return o instanceof NamedObject && ((NamedObject) o).name.equals(name);
    }
}
