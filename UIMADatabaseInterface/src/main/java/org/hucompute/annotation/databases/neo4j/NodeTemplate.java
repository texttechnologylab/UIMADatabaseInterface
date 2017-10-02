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

import com.vividsolutions.jts.geom.Coordinate;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.JCas;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.Set;

/**
 *  Interface for a UIMA node in Neo4J.
 */
public interface NodeTemplate {

    void update(String sUpdate);

    String getID();

    Node getNode();

    JCas getJCas() throws UIMAException;

    void update(JCas pJCas);

    Set<Relationship> getRelationships();

    void setGeoPoints(Coordinate pCoordinate);
    void setGeoPoints(double lat, double lon);

    Set<NodeTemplate> getNodesByFeature(Feature pFeature);

    Relationship addRelationship(Feature pFeature, Node pNode);

    Relationship addRelationship(String sFeature, Node pNode);
    Relationship addRelationship(String sFeature, NodeTemplate pNode);

    Set<JCas> getJCasByFeature(Feature pFeature);

    void setProperty(String sPropertyName, Object pObject, boolean bRemove);

    void setProperty(String sPropertyName, Object pObject);

    void addProperty(String sPropertyName, Object pObject, boolean bRemove);

    void initProperty(String sPropertyName, Object pObject, boolean bForce);

    void removeProperty(String sPropertyName, Object pObject);

    void removeProperty(String sPropertyName);

    boolean hasProperty(String sPropertyName);

    Object getProperty(String sPropertyName);

    Object getProperty(String sPropertyName, Object defaultResult);
}
