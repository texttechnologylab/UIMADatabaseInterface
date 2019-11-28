package org.texttechnologylab.uimadb.databases.elasticsearch;

/*
 * Copyright 2019
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
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.jcas.JCas;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.texttechnologylab.uimadb.UIMADatabaseInterface;
import org.texttechnologylab.uimadb.UIMADatabaseInterfaceService;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.CasSerializationException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.SerializerInitializationException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.UnknownFactoryException;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Implementation of the UIMADatabaseInterfaceService to use a Neo4J instance.
 */
public class Elasticsearch implements UIMADatabaseInterfaceService {

    private static final long  MEGABYTE = 1024L * 1024L;

    public static ElasticsearchConnector connector = null;


    public Elasticsearch(String sConfigFile) throws IOException {
        this(new File(sConfigFile));
    }

    public Elasticsearch(File pConfigFile) throws IOException {
        connector = new ElasticsearchConnector(pConfigFile);
        init();
    }

    /**
     *  Internal initialization to generate the indexes and to determine the primitive and complex data types based on the embedded type system descriptors.
     */
    public void init() throws IOException {

        connector.init();

    }


    private String getType(JCas jCas){

        String sType = "";

        sType = jCas.getSofa().getType().getName();

        return sType;

    }

    @Override
    public String createElement(JCas jCas) throws UIMAException, JSONException {

        createDummy(jCas);

        String sString = UIMADatabaseInterface.serializeJCas(jCas);

        String result = null;
        try {
            String sID = UIMADatabaseInterface.getID(jCas);
            System.out.println("ID: "+sID);
            long size = sString.getBytes().length / MEGABYTE;

            System.out.println("Size: "+size);

            result = connector.update(new JSONObject(sString), sID);
/*
            if(size>15 && false){
                result = connector.updateBulkSingle(sString, sID);
            }
            else{
            }
*/

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public JCas createDummy(JCas pCas) throws UIMAException, JSONException {

        try {
            if (pCas.getView(UIMADatabaseInterface.UIMADBID) != null) {
                return pCas;
            }
        }
        catch (CASRuntimeException e){
            System.out.println(e.getMessage());
        }

        JSONObject newObject = new JSONObject();

        String result = null;

        try {
            result = connector.insert(newObject);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String oID = result;
        pCas.createView(UIMADatabaseInterface.UIMADBID).setDocumentText(oID);


        return pCas;
    }

    @Override
    public String createDummy() throws UIMAException, JSONException {
        JSONObject newObject = new JSONObject();

        String result = null;

        try {
            result = connector.insert(newObject);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String oID = result;
        return oID;
    }

    @Override
    public String createElement(JCas jCas, JSONArray pAttributes) throws UIMAException, JSONException {
        return null;
    }

    @Override
    public void updateElement(JCas pJCas, JSONArray pAttributes) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException {

    }

    @Override
    public void updateElement(JCas pJCas, JSONArray pAttributes, boolean bCompressed) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException {

    }

    @Override
    public void updateElement(JCas pJCas) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException, IOException {

        String sString = null;
        try {
            sString = UIMADatabaseInterface.serializeJCas(pJCas);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String sID = UIMADatabaseInterface.getID(pJCas);

        connector.update(new JSONObject(sString), sID);
    }

    @Override
    public JCas getElement(String sID) throws IOException {
        JCas rCas = null;

        String newsID=sID;

        if(newsID.contains("/")){
            newsID = sID.substring(sID.lastIndexOf("/")+1);
        }

        String json = connector.get(newsID);

        try {
            rCas = UIMADatabaseInterface.deserializeJCas(json);
        } catch (UIMAException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return rCas;
    }

    @Override
    public long getSize(String sID) throws IOException {
        return 0;
    }

    @Override
    public Set<JCas> getElements(String sQuery) {
        return null;
    }

    @Override
    public Set<JCas> getElementsDirect(String sQuery) {
        return null;
    }

    @Override
    public Set<JCas> getElementsDirect(String sQuery, String queryValue) {
        return null;
    }

    @Override
    public Set<Object> getElementsDirectObject(String sQuery, String queryValue) {
        return null;
    }

    @Override
    public Set<JCas> getElementsWithType(String sSourceObject, String sTargetObject) {
        return null;
    }

    @Override
    public Set<JCas> getElements(KeyValue... kvs) {
        return null;
    }

    @Override
    public Set<JCas> getElementsByGeoLocation(double lat, double lon, double distance) {
        return null;
    }

    @Override
    public Set<JCas> getElementsByGeoLocation(String sType, double lat, double lon, double distance) {
        return null;
    }

    @Override
    public void deleteElements(String sID) {

    }

    @Override
    public Object getRealID(String sID) {
        return null;
    }

    @Override
    public void destroy() {
        try {
            connector.onClose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        try {
            connector.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
