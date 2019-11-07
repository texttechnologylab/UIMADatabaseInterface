package org.texttechnologylab.uimadb;

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

import org.apache.commons.collections.KeyValue;
import org.apache.uima.UIMAException;
import org.apache.uima.jcas.JCas;
import org.json.JSONArray;
import org.json.JSONException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.CasSerializationException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.SerializerInitializationException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.UnknownFactoryException;

import java.io.IOException;
import java.util.Set;

/**
 *  Interface for integrating databases.
 */
public interface UIMADatabaseInterfaceService {

    /**
     * Create a new database entry based on the JCas. The database ID is added to the JCas.
     *
     * @param jCas
     * @return
     * @throws UIMAException
     * @throws JSONException
     */
    String createElement(JCas jCas) throws UIMAException, JSONException;

    /**
     * Create a new database entry based on the JCas. The database ID is added to the JCas.
     *
     * @param pCas
     * @return
     * @throws UIMAException
     * @throws JSONException
     */
    JCas createDummy(JCas pCas) throws UIMAException, JSONException;

    /**
     * Create a new empty database entry. The database ID is returned.
     *
     * @return
     * @throws UIMAException
     * @throws JSONException
     */
    String createDummy() throws UIMAException, JSONException;

    /**
     * Create a new database JCas with specific attributes.
     *
     * @param jCas
     * @param pAttributes
     * @return
     * @throws UIMAException
     * @throws JSONException
     */
    String createElement(JCas jCas, JSONArray pAttributes) throws UIMAException, JSONException;

    /**
     * Update a existing database JCas with specific attributes.
     *
     * @param pJCas
     * @param pAttributes
     * @throws CasSerializationException
     * @throws SerializerInitializationException
     * @throws UnknownFactoryException
     */
    void updateElement(JCas pJCas, JSONArray pAttributes) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException;

    /**
     * Update a existing database JCas.
     *
     * @param pJCas
     * @throws CasSerializationException
     * @throws SerializerInitializationException
     * @throws UnknownFactoryException
     */
    void updateElement(JCas pJCas) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException, IOException;

    /**
     * Return a JCas based on its database ID.
     *
     * @param sID
     * @return
     */
    JCas getElement(String sID) throws IOException;

    /**
     * Return this Size of the Document
     *
     * @param sID
     * @return
     */
    long getSize(String sID) throws IOException;

    /**
     * Select JCas elements based on a generic query.
     *
     * @param sQuery
     * @return
     */
    Set<JCas> getElements(String sQuery);

    /**
     * Select JCas elements based on a special query.
     *
     * @param sQuery
     * @return
     */
    Set<JCas> getElementsDirect(String sQuery);

    /**
     * Select JCas elements based on a special query.
     *
     * @param sQuery
     * @return
     */
    Set<JCas> getElementsDirect(String sQuery, String queryValue);

    /**
     * Select elements based on a special query.
     *
     * @param sQuery
     * @return
     */
    Set<Object> getElementsDirectObject(String sQuery, String queryValue);

    /**
     * Select elements by type (type system descriptor).
     *
     * @param sSourceObject
     * @param sTargetObject
     * @return
     */
    Set<JCas> getElementsWithType(String sSourceObject, String sTargetObject);

    /**
     * Return elements by key-value pairs.
     * @param kvs
     * @return
     */
    Set<JCas> getElements(KeyValue... kvs);

    /**
     * Select JCas elements by geo-location.
     *
     * @param lat
     * @param lon
     * @param distance
     * @return
     */
    Set<JCas> getElementsByGeoLocation(double lat, double lon, double distance);

    /**
     * Select JCas elements by geo-location.
     * @param sType
     * @param lat
     * @param lon
     * @param distance
     * @return
     */
    Set<JCas> getElementsByGeoLocation(String sType, double lat, double lon, double distance);

    /**
     * Delete Element
     * @param sID
     */
    void deleteElements(String sID);

    /**
     * Return the ID without reference präfix.
     * @param sID
     * @return
     */
    Object getRealID(String sID);

    /**
     * Destroy the database conection
     */
    void destroy();

    /**
     * Start the database connection
     */
    void start();

}
