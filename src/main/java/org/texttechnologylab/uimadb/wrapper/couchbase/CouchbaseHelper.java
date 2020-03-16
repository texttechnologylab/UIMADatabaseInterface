package org.texttechnologylab.uimadb.wrapper.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.texttechnologylab.uimadb.UIMADatabaseInterface;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.CasSerializationException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.SerializerInitializationException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.UnknownFactoryException;

import java.io.IOException;
import java.util.UUID;


public abstract class CouchbaseHelper {

    static Bucket bucket;
    private CouchbaseConnection connection;

    public CouchbaseHelper(CouchbaseConfig couchbaseConfig) throws ResourceInitializationException {
        try {
            connection = new CouchbaseConnection(couchbaseConfig);
            bucket = connection.bucket;
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    public CouchbaseHelper(CouchbaseConnection couchbaseConnection) throws ResourceInitializationException {
        CouchbaseConnection connection = couchbaseConnection;
        bucket = connection.bucket;
    }

    public CouchbaseHelper(String sHost, String sBucket, String sUsername, String sPassword) throws ResourceInitializationException {
        CouchbaseConnection connection = new CouchbaseConnection(sHost, sBucket, sUsername, sPassword);
        bucket = connection.bucket;
    }

    public String createElement(JCas jCas) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException, CASException {
        String jsonString = UIMADatabaseInterface.serializeJCas(jCas);
        JsonObject jsonObject = JsonObject.fromJson(jsonString);
        // create random key couchbase does not support randomly generated keys per default
        String id = UUID.randomUUID().toString();
        JsonDocument doc = JsonDocument.create(id, jsonObject);
        // fails if document already exists
        bucket.insert(doc);
        return id;
    }

    public JsonObject dummyObject(String sID) {
        // creates an empty dummy object with given id
        JsonObject docSource = JsonObject.empty().put("_id", sID);
        return docSource;
    }

    public JsonDocument deleteElementByID(String sID) {
        JsonObject docSource = JsonObject.create();
        docSource.put("_id", sID);
        JsonDocument pResult = bucket.remove(sID);
        return pResult;
    }

    public static Bucket getBucket() {
        return bucket;
    }

    public CouchbaseConnection getConnection() {
        return connection;
    }
}
