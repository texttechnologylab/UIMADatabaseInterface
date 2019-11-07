package org.texttechnologylab.uimadb.databases.mongo;

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

import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.apache.commons.collections.KeyValue;
import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.texttechnologylab.uimadb.UIMADatabaseInterface;
import org.texttechnologylab.uimadb.UIMADatabaseInterfaceService;
import org.texttechnologylab.uimadb.wrapper.mongo.MongoConfig;
import org.texttechnologylab.uimadb.wrapper.mongo.MongoConnection;
import org.texttechnologylab.uimadb.wrapper.mongo.MongoHelper;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.CasSerializationException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.SerializerInitializationException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.UnknownFactoryException;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 *  Implementation of the UIMADatabaseInterfaceService to use a MongoDB instance.
 */
public class Mongo extends MongoHelper implements UIMADatabaseInterfaceService {

    public Mongo(String sHost, String sDatabase, String sCollection, String sUsername, String sPassword) throws ResourceInitializationException {
        super(new MongoConnection(sHost, sDatabase, sCollection, sUsername, sPassword));
    }

    public Mongo(MongoConfig mongoConfig) throws ResourceInitializationException {
        super(mongoConfig);
    }

    public Mongo(String configFile) throws ResourceInitializationException, IOException {
        super(new MongoConfig(configFile));
    }

    @Override
    public String createElement(JCas jCas) throws UIMAException, JSONException {
        String sString = UIMADatabaseInterface.serializeJCas(jCas);

        DBObject doc = (DBObject) JSON.parse(sString);

        this.getConnection().insert(doc);

        String oID = doc.get("_id").toString();

        return oID;
    }

    @Override
    public JCas createDummy(JCas pCas) throws UIMAException, JSONException {
        DBObject doc = (DBObject) JSON.parse("{}");

        this.getConnection().insert(doc);

        String oID = doc.get("_id").toString();
        pCas.createView(UIMADatabaseInterface.UIMADBID).setDocumentText(oID);
        return pCas;
    }

    @Override
    public String createDummy() throws UIMAException, JSONException {
        DBObject doc = (DBObject) JSON.parse("{}");

        this.getConnection().insert(doc);

        String oID = doc.get("_id").toString();

        return oID;
    }


    public String createElement(JCas jCas, JSONArray pAttributes) throws UIMAException, JSONException {
        String sString = UIMADatabaseInterface.serializeJCas(jCas, pAttributes);

        DBObject doc = (DBObject) JSON.parse(sString);

        this.getConnection().insert(doc);

        String oID = doc.get("_id").toString();

        updateElement(jCas, pAttributes);

        return oID;
    }

    @Override
    public void updateElement(JCas pJCas, JSONArray pAttributes) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException {
        String sString = null;
        try {
            sString = UIMADatabaseInterface.serializeJCas(pJCas, pAttributes);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String sID = UIMADatabaseInterface.getID(pJCas);

        DBObject doc = (DBObject) JSON.parse(sString);

        this.updateElement(dummyObject(sID), doc);
    }

    @Override
    public void updateElement(DBObject pQuery, DBObject pObject) {
        super.updateElement(pQuery, pObject);
    }



    public void updateElement(JCas pJCas) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException {

        String sString = null;
        try {
            sString = UIMADatabaseInterface.serializeJCas(pJCas);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        DBObject doc = (DBObject) JSON.parse(sString);
        String sID = UIMADatabaseInterface.getID(pJCas);

        this.updateElement(dummyObject(sID), doc);

    }


    @Override
    public void updateElement(DBObject pQuery, String sObject) {
        super.updateElement(pQuery, sObject);
    }

    @Override
    public long getSize(String sID) {

            DBObject tObject = null;

            if(sID.contains("/")){
                tObject = getDBElement(sID.substring(sID.lastIndexOf("/")+1));
            }
            else {
                tObject = getDBElement(sID);
            }

            String json = JSON.serialize(tObject);

            long length = json.getBytes(StandardCharsets.UTF_8).length;

            return length;

    }

    @Override
    public JCas getElement(String sID) {
        JCas rCas = null;

            DBObject tObject = null;

            if(sID.contains("/")){
                tObject = getDBElement(sID.substring(sID.lastIndexOf("/")+1));
            }
            else {
                tObject = getDBElement(sID);
            }

            String json = JSON.serialize(tObject);

            try {
                rCas = UIMADatabaseInterface.deserializeJCas(json);
            } catch (UIMAException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


        return rCas;
    }

    private DBObject getDBElement(String sID){

        DBObject rElement = null;

        DBObject docSource = new BasicDBObject();
        docSource.put("_id", new ObjectId(sID));

        DBCursor dbCursor = null;

        dbCursor = this.getConnection().find(docSource);

        dbCursor.addOption(Bytes.QUERYOPTION_NOTIMEOUT).batchSize(1000);

        while(dbCursor.hasNext()){
            rElement = dbCursor.next();
        }

        return rElement;

    }

    @Override
    public Set<JCas> getElements(String sQuery){

        Set<JCas> rCas = new HashSet<>(0);

        DBObject qObject = (DBObject) JSON.parse(sQuery);

        DBCursor dbCursor = null;

        dbCursor = this.getConnection().find(qObject);
        dbCursor.addOption(Bytes.QUERYOPTION_NOTIMEOUT).batchSize(1000);

        while(dbCursor.hasNext()){

            DBObject pObject = dbCursor.next();

            String json = JSON.serialize(pObject);
            try {
                JCas rCasObject = UIMADatabaseInterface.deserializeJCas(json);
                rCas.add(rCasObject);
            } catch (UIMAException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return rCas;

    }

    @Override
    public Set<JCas> getElementsDirect(String sQuery) {
        return this.getElements(sQuery);
    }

    @Override
    public Set<JCas> getElementsDirect(String sQuery, String queryValue) {
        return this.getElementsDirect(sQuery);
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
        StringBuilder sb = new StringBuilder();

        return getElements(sb.toString());
    }

    @Override
    public Set<JCas> getElementsByGeoLocation(double lat, double lon, double distance) {
        return getElementsByGeoLocation("", lat, lon, distance);
    }

    @Override
    public Set<JCas> getElementsByGeoLocation(String sType, double lat, double lon, double distance) {
        JSONArray pArray = new JSONArray().put(new double[] { lon, lat }).put(new BigDecimal(distance/6371.1));

        final BasicDBObject geoWithhin = new BasicDBObject("$centerSphere", pArray);

        final BasicDBObject filter = new BasicDBObject("$geoWithin", geoWithhin);

        final BasicDBObject query = new BasicDBObject("geo", filter);

        if(sType.length()>0) {
            query.put("meta.type", sType);
        }
        Set<JCas> setCas = getElements(query.toString());

        return setCas;

    }

    @Override
    public void deleteElements(String sID) {
        super.deleteElementByID(sID);
    }

    @Override
    public Object getRealID(String sID) {
        return sID.replace("_","");
    }

    @Override
    public void destroy() {

    }

    @Override
    public void start() {

    }
}
