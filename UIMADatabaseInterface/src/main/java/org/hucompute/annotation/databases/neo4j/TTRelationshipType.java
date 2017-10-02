package org.hucompute.annotation.databases.neo4j;

/*
 * Copyright 2017
 * Texttechnology Lab
 * Goethe-Universität Frankfurt am Main
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */

import org.neo4j.graphdb.RelationshipType;

/**
 * Implementation and representation of any relation based on the complex data types defined in the Type System Descriptor.
 */
public class TTRelationshipType implements RelationshipType {

    String sName = "";

    public TTRelationshipType(String sName){
        this.sName = sName;
    }

    @Override
    public String name() {
        return this.sName;
    }
}
