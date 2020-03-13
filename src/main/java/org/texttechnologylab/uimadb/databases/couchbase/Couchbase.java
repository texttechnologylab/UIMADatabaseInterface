package org.texttechnologylab.uimadb.databases.couchbase;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQueryRow;
import org.apache.commons.collections.KeyValue;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.texttechnologylab.uimadb.UIMADatabaseInterface;
import org.texttechnologylab.uimadb.UIMADatabaseInterfaceService;
import org.texttechnologylab.uimadb.wrapper.couchbase.CouchbaseConfig;
import org.texttechnologylab.uimadb.wrapper.couchbase.CouchbaseConnection;
import org.texttechnologylab.uimadb.wrapper.couchbase.CouchbaseHelper;
import org.texttechnologylab.uimadb.wrapper.couchbase.CouchbaseStorage;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.CasSerializationException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.SerializerInitializationException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.UnknownFactoryException;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.texttechnologylab.uimadb.wrapper.couchbase.CouchbaseQueries.returnQueryResultsData;
import static org.texttechnologylab.uimadb.wrapper.couchbase.CouchbaseStorage.setBufferSize;

public class Couchbase extends CouchbaseHelper implements UIMADatabaseInterfaceService {

    public Couchbase(String sHost, String sBucket, String sUsername, String sPassword) throws ResourceInitializationException {
        super(new CouchbaseConnection(sHost, sBucket, sUsername, sPassword));
    }

    public Couchbase(CouchbaseConfig couchbaseConfig) throws ResourceInitializationException {
        super(couchbaseConfig);
    }

    public Couchbase(String configFile) throws ResourceInitializationException, IOException {
        super(new CouchbaseConfig(configFile));
    }


    @Override
    public String createDummy() throws UIMAException, JSONException {
        // Create a new empty database entry. The database ID is returned.
        String id = UUID.randomUUID().toString();
        JsonObject jsonObject = dummyObject(id);

        JsonDocument doc = JsonDocument.create(id, jsonObject);

        // upload doc
        getBucket().insert(doc);
        return id;
    }

    @Override
    public JCas createDummy(JCas pCas) throws UIMAException, JSONException {
        // Create a new database entry based on the JCas. The database ID is added to the JCas.

        //write id as sofa in UIMA doc // if else to avoid error if uimadbid already exists
        if (UIMADatabaseInterface.getID(pCas).isEmpty()) {
            String id = UUID.randomUUID().toString();
            // create empty object with only id field
            JsonObject jsonObject = dummyObject(id);
            // create json document that contains the dummy object
            JsonDocument doc = JsonDocument.create(id, jsonObject);
            // upload doc
            getBucket().insert(doc);
            // add ID to JCas
            pCas.createView(UIMADatabaseInterface.UIMADBID).setDocumentText(id);
        } else {
            // if there is already an UIMADBID use it instead of randomly generated id
            String id = UIMADatabaseInterface.getID(pCas);
            JsonObject jsonObject = dummyObject(id);
            JsonDocument doc = JsonDocument.create(id, jsonObject);
            getBucket().upsert(doc);
        }
        return pCas;
    }

    // use if data is stored as binary chunks (for data >20MB)
    public void updateElementBlob(JCas pJCas) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException, CASException {
        // replace existing document with updated version
        try {
            updateElementBlob(pJCas, false);
        } catch (CasSerializationException | CASException | UnknownFactoryException | SerializerInitializationException e) {
            e.printStackTrace();
        }
    }

    public void updateElementBlob(JCas pJCas, boolean bCompressed) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException, CASException {
        // update existing element that stored as Blob, do not use for elements that are stored as JSON-File

        String jsonString = null;
        try {
            jsonString = UIMADatabaseInterface.serializeJCas(pJCas, bCompressed);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String sID = UIMADatabaseInterface.getID(pJCas);
        // write "table of content" file to dummy and blobs to seperate new files
        CouchbaseStorage.writeContent(sID, jsonString);
    }

    public void updateElementBlob(JCas pJcas, int chunksize) throws CasSerializationException, SerializerInitializationException, CASException, UnknownFactoryException {
        setBufferSize(chunksize);
        updateElementBlob(pJcas);
    }

    public void updateElementBlob(JCas pJCas, String sID) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException {
        updateElementBlob(pJCas, sID, false);
    }

    public void updateElementBlob(JCas pJCas, JSONArray pAttributes, boolean bCompressed) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException {
        String jsonString = null;
        try {
            jsonString = UIMADatabaseInterface.serializeJCas(pJCas, pAttributes, bCompressed);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String sID = UIMADatabaseInterface.getID(pJCas);
        // write blob and jsonfile with chunk overview to content bucket
        CouchbaseStorage.writeContent(sID, jsonString);
    }

    public void updateElementBlob(JCas pJCas, String sID, boolean bCompressed) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException {
        String jsonString = null;
        try {
            jsonString = UIMADatabaseInterface.serializeJCas(pJCas, bCompressed);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CouchbaseStorage.writeContent(sID, jsonString);
    }

    public void updateMetaData(JCas pJCas) {
        JSONObject metaInformation = UIMADatabaseInterface.getMetaInformation(pJCas);
        // convert JSONObject to JsonObject which is used in couchbase
        String metaString = metaInformation.toString();
        String sID = UIMADatabaseInterface.getID(pJCas);
        CouchbaseStorage.writeMetaData(sID, metaString);
    }

    public JCas getElementBlob(String sID) {
        // get Element that is stored as a blob from CB. Deserialize it to JCas.
        JCas rCas = null;
        // get binary data associated with the id
        // convert binary data to string
        String jsonString = CouchbaseStorage.getDocumentString(sID);
        try {
            rCas = UIMADatabaseInterface.deserializeJCas(jsonString);

        } catch (UIMAException | JSONException e) {
            e.printStackTrace();
        }
        return rCas;
    }

    public void deleteElementsBlob(String sID) {
        CouchbaseStorage.deleteContent(sID);
    }

    @Override
    public String createElement(JCas jCas) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException, CASException {
        //use if data is supposed to be stored as single JSON-Doc (for data < 20MB)
        //Create a new database entry based on the JCas. The ID of the database document is returned
        //doesn't work if document is >20MB
        String jsonString = UIMADatabaseInterface.serializeJCas(jCas);
        JsonObject jsonObject = JsonObject.fromJson(jsonString);

        if (UIMADatabaseInterface.getID(jCas).isEmpty()) {
            String id = UUID.randomUUID().toString();
            // create json document that contains the dummy object
            JsonDocument doc = JsonDocument.create(id, jsonObject);
            // upload doc
            getBucket().insert(doc);
            // add ID to JCas
            jCas.createView(UIMADatabaseInterface.UIMADBID).setDocumentText(id);
            return id;
        } else {
            String id = UIMADatabaseInterface.getID(jCas);
            JsonDocument doc = JsonDocument.create(id, jsonObject);
            getBucket().upsert(doc);
            return id;
        }
    }

    public String createElement(JCas jCas, JSONArray pAttributes) throws UIMAException, JSONException {
        // Create a new database JCas with specific attributes.
        String jsonString = UIMADatabaseInterface.serializeJCas(jCas, pAttributes);
        JsonObject jsonObject = JsonObject.fromJson(jsonString);

        if (UIMADatabaseInterface.getID(jCas).isEmpty()) {
            String id = UUID.randomUUID().toString();
            // create json document that contains the dummy object
            JsonDocument doc = JsonDocument.create(id, jsonObject);
            // upload doc
            getBucket().insert(doc);
            // add ID to JCas
            jCas.createView(UIMADatabaseInterface.UIMADBID).setDocumentText(id);
            return id;
        } else {
            String id = UIMADatabaseInterface.getID(jCas);
            JsonDocument doc = JsonDocument.create(id, jsonObject);
            getBucket().upsert(doc);
            return id;
        }
    }

    @Override
    public void updateElement(JCas pJCas) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException, CASException {
        // replace existing document with updated version
        try {
            updateElement(pJCas, false);
        } catch (CasSerializationException | CASException | UnknownFactoryException | SerializerInitializationException e) {
            e.printStackTrace();
        }
    }

    public void updateElement(JCas pJCas, boolean bCompressed) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException, CASException {
        // replace existing document with updated version
        // do not use for elements that are stored as a blob, use updadeElementBlob instead.
        String jsonString = null;
        try {
            jsonString = UIMADatabaseInterface.serializeJCas(pJCas, bCompressed);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String sID = UIMADatabaseInterface.getID(pJCas);
        JsonObject jsonObject = JsonObject.fromJson(jsonString);
        JsonDocument jsonDocument = JsonDocument.create(sID, jsonObject);
        getBucket().upsert(jsonDocument);
    }

    public void updateElement(JCas pJCas, String sID) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException {
        updateElement(pJCas, sID, false);
    }

    @Override
    public void updateElement(JCas pJCas, JSONArray pAttributes) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException {
        updateElement(pJCas, pAttributes, false);
    }

    @Override
    public void updateElement(JCas pJCas, JSONArray pAttributes, boolean bCompressed) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException {
        String jsonString = null;
        try {
            jsonString = UIMADatabaseInterface.serializeJCas(pJCas, pAttributes, bCompressed);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String sID = UIMADatabaseInterface.getID(pJCas);
        JsonObject jsonObject = JsonObject.fromJson(jsonString);
        JsonDocument jsonDocument = JsonDocument.create(sID, jsonObject);
        getBucket().upsert(jsonDocument);
    }

    public void updateElement(JCas pJCas, String sID, boolean bCompressed) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException {
        String jsonString = null;
        try {
            jsonString = UIMADatabaseInterface.serializeJCas(pJCas, bCompressed);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObject jsonObject = JsonObject.fromJson(jsonString);
        JsonDocument jsonDocument = JsonDocument.create(sID, jsonObject);
        getBucket().upsert(jsonDocument);
    }

    @Override
    public JCas getElement(String sID) {
        // get JSON representation from Couchbase. Get JsonDocument and deserialize it to JCas.
        JCas rCas = null;
        JsonDocument jsonDocument = getBucket().get(sID);
        JsonObject jsonObject = jsonDocument.content();
        String jsonString = jsonObject.toString();
        try {
            rCas = UIMADatabaseInterface.deserializeJCas(jsonString);

        } catch (UIMAException | JSONException e) {
            e.printStackTrace();
        }
        return rCas;
    }

    @Override
    public void deleteElements(String sID) {
        super.deleteElementByID(sID);
    }

    @Override
    public long getSize(String sID) {
        JsonObject tObject = null;
        JsonDocument doc = getBucket().get(sID);
        tObject = doc.content();
        String jsonString = tObject.toString();
        long length = jsonString.getBytes(StandardCharsets.UTF_8).length;
        return length;
    }

    @Override
    public Set<JCas> getElements(String sQuery) throws UIMAException, IOException {
        // return results of query as Set of JCas'
        // only use if Query returns whole documents, otherwise deserializeJCas runs into problems currently

        // put all query results in list
        List<N1qlQueryRow> results = null;
        try {
            results = returnQueryResultsData(sQuery);
        } catch (IOException | ResourceInitializationException e) {
            e.printStackTrace();
        }
        Set<JCas> rCas = new HashSet<>(0);
        for (N1qlQueryRow row : results) {
            JsonObject jsonObject = row.value();
            String jsonString = jsonObject.toString();
            System.out.println(jsonString);
            JCas rCasObject = UIMADatabaseInterface.deserializeJCas(jsonString);
            rCas.add(rCasObject);
        }
        return rCas;
    }

    @Override
    public Set<JCas> getElementsDirect(String sQuery) throws UIMAException, IOException {
        return getElements(sQuery);
    }

    @Override
    public Set<JCas> getElementsDirect(String sQuery, String queryValue) throws UIMAException, IOException {
        return getElementsDirect(sQuery);
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
    public Set<JCas> getElements(KeyValue... kvs) throws UIMAException, IOException {
        StringBuilder sb = new StringBuilder();
        return getElements(sb.toString());
    }

    @Override
    public Set<JCas> getElementsByGeoLocation(double lat, double lon, double distance) throws UIMAException, IOException {
        return getElementsByGeoLocation("", lat, lon, distance);
    }

    @Override
    public Set<JCas> getElementsByGeoLocation(String sType, double lat, double lon, double distance) throws UIMAException, IOException {
        // not working

        JsonObject geoWithin = JsonObject.empty();
        JsonObject filter = JsonObject.empty();
        JsonObject query = JsonObject.empty();
        JsonArray pArray = JsonArray.create();
        pArray.add(new double[]{lon, lat}).add(new BigDecimal(distance / 6371.1));
        geoWithin.put("$centerSphere", pArray);
        filter.put("$centerSphere", geoWithin);
        query.put("geo", filter);
        if (sType.length() > 0) {
            query.put("meta.type", sType);
        }
        Set<JCas> setCas = getElements(query.toString());
        return setCas;
    }

    @Override
    public Object getRealID(String sID) {
        return sID.replace("_", "");
    }

    @Override
    public void destroy() {
        CouchbaseHelper.getBucket().close();
    }

    @Override
    public void start() {
    }
}
