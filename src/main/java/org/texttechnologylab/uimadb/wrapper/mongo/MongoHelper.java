package org.texttechnologylab.uimadb.wrapper.mongo;

import com.mongodb.*;
import com.mongodb.util.JSON;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.CasSerializationException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.SerializerInitializationException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.UnknownFactoryException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by abrami on 01.06.17.
 */
public abstract class MongoHelper {

    private DBCollection coll;


    public MongoHelper(MongoConfig mongoConfig) throws ResourceInitializationException {

        try {
            MongoConnection conn = new MongoConnection(mongoConfig);
            coll = conn.coll;
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

    }
    public MongoHelper(MongoConnection mc) throws ResourceInitializationException {

       MongoConnection conn = mc;
       coll = conn.coll;

    }

    public MongoHelper(String sAdress, String mongoDatabase, String mongoCollection, String sUsername, String sPassword) throws ResourceInitializationException {
        MongoConnection conn = new MongoConnection(sAdress, mongoDatabase, mongoCollection, sUsername, sPassword);
        coll = conn.coll;
    }


    public void updateElement(DBObject pQuery, String sObject){
        updateElement(pQuery, (DBObject) JSON.parse(sObject));
    }

    public String createElement(JCas jCas) throws UIMAException, JSONException {

        String sString = MongoSerialization.serializeJCas(jCas);

        DBObject doc = (DBObject) JSON.parse(sString);

        this.coll.insert(doc);

        String oID = doc.get("_id").toString();

        return oID;

    }

    public DBObject dummyObject(String sID) {
        DBObject docSource = new BasicDBObject();
        docSource.put("_id", new ObjectId(sID));
        return docSource;
    }

    public void updateElement(DBObject pQuery, DBObject pObject){
        coll.update(pQuery, pObject);
    }

    public JCas getElement(String sID){

        Object rElement = null;

        DBObject tObject = null;

        if(sID.contains("/")){
            tObject = getDBElement(sID.substring(sID.lastIndexOf("/")+1));
        }
        else {
            tObject = getDBElement(sID);
        }

        String json = JSON.serialize(tObject);

        JCas jCas = null;
        try {
            jCas = JCasFactory.createJCas();
            MongoSerialization.deserializeJCas(jCas, json);
        } catch (UIMAException e) {
            e.printStackTrace();
        }

        return jCas;
    }

    private DBObject getDBElement(String sID){

        DBObject rElement = null;

        DBObject docSource = new BasicDBObject();
        docSource.put("_id", new ObjectId(sID));

        DBCursor dbCursor = null;

        dbCursor = this.coll.find(docSource);

        dbCursor.addOption(Bytes.QUERYOPTION_NOTIMEOUT).batchSize(1000);

        while(dbCursor.hasNext()){
            rElement = dbCursor.next();
        }

        return rElement;

    }

    public Set<JCas> getElements(String sQuery){

        Set<JCas> rCas = new HashSet<>(0);

        DBObject qObject = (DBObject) JSON.parse(sQuery);

        DBCursor dbCursor = null;

        dbCursor = this.coll.find(qObject);
        dbCursor.addOption(Bytes.QUERYOPTION_NOTIMEOUT).batchSize(1000);

        while(dbCursor.hasNext()){

            DBObject pObject = dbCursor.next();

            String json = JSON.serialize(pObject);
            try {
                JCas jCas = JCasFactory.createJCas();
                MongoSerialization.deserializeJCas(jCas, json);
                rCas.add(jCas);
            } catch (UIMAException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return rCas;

    }

    public WriteResult deleteElement(String sQuery){

        DBObject qObject = (DBObject) JSON.parse(sQuery);

        WriteResult pResult = this.coll.remove(qObject);

        return pResult;

    }

    public WriteResult deleteElementByID(String sID){

        DBObject docSource = new BasicDBObject();
        docSource.put("_id", new ObjectId(sID));

        WriteResult pResult = this.coll.remove(docSource);

        return pResult;

    }

    public DBCollection getConnection(){
        return coll;
    }

}
