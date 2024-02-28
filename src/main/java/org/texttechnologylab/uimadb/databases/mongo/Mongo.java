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

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSUploadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.lang.Nullable;
import com.mongodb.util.JSON;
import org.apache.commons.collections.KeyValue;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.CasIOUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.bson.Document;
import org.bson.conversions.Bson;
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
import org.texttechnologylab.utilities.helper.FileUtils;
import org.texttechnologylab.utilities.helper.StringUtils;
import org.texttechnologylab.utilities.helper.TempFileHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of the UIMADatabaseInterfaceService to use a MongoDB instance.
 */
public class Mongo extends MongoHelper implements UIMADatabaseInterfaceService {
    private static String gridFSName = "TextAnnotator";

    public Mongo(String sHost, String sDatabase, String sCollection, String sUsername, String sPassword) throws ResourceInitializationException {
        super(new MongoConnection(sHost, sDatabase, sCollection, sUsername, sPassword));
    }

    public Mongo(MongoConfig mongoConfig) throws ResourceInitializationException {
        super(mongoConfig);
    }

    public Mongo(String configFile) throws ResourceInitializationException, IOException {
        super(new MongoConfig(configFile));
    }

    public GridFSBucket getBucketConnection() {

        GridFSBucket gridFSBucket = GridFSBuckets.create(this.getConnection().getDB().getMongoClient().getDatabase(this.getConfigConnection().getDatabaseName()), gridFSName);
        return gridFSBucket;

    }

    public MongoCollection<Document> getGridFSCollection() {
        return this.getConnection().getDB().getMongoClient().getDatabase(this.getConfigConnection().getDatabaseName()).getCollection(gridFSName + ".files");
    }

    public GridFSUploadOptions getGridFSOptions() {
        GridFSUploadOptions options = new GridFSUploadOptions()
                .chunkSizeBytes(358400)
                .metadata(new Document("type", "uima"));

        return options;
    }

    public String createElementGridFS(JCas jCas) throws IOException {
        return createElementGridFS(jCas, null);
    }

    public String createElementGridFS(JCas jcas, @Nullable Document statistics) throws IOException {

        GridFSUploadOptions options = new GridFSUploadOptions()
                .chunkSizeBytes(358400)
                .metadata(new Document("type", "uima"));

        GridFSUploadStream uploadStream = getBucketConnection().openUploadStream(String.valueOf(System.currentTimeMillis()), options);

        File tf = TempFileHandler.getTempFile("aaa", "bbb");

        CasIOUtil.writeXmi(jcas, tf);

        byte[] data = Files.readAllBytes(tf.toPath());
        uploadStream.write(data);
        uploadStream.close();

        try {
            jcas.getView(UIMADatabaseInterface.UIMADBID).setDocumentText(uploadStream.getObjectId().toString());
        } catch (CASException e) {
            e.printStackTrace();
        }
        try {
            if (statistics != null)
                getGridFSCollection().updateOne(Filters.eq("_id", uploadStream.getObjectId()), Updates.set("metadata.statistics", statistics));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return uploadStream.getObjectId().toString();

    }

    public JCas getElementGridFS(String sID) throws IOException, UIMAException {

        File tf2 = TempFileHandler.getTempFile("aaa", "bbb");
        FileOutputStream streamToDownloadTo = new FileOutputStream(tf2);
        getBucketConnection().downloadToStream(new ObjectId(sID), streamToDownloadTo);
        streamToDownloadTo.close();

        JCas rCas = JCasFactory.createJCas();
        CasIOUtil.readXmi(rCas, tf2);

        return rCas;

    }

    public JCas getElementGridFS(String sID, String sSearch, String sReplace) throws IOException, UIMAException {

        File tf2 = TempFileHandler.getTempFile("aaa", "bbb");
        FileOutputStream streamToDownloadTo = new FileOutputStream(tf2);
        getBucketConnection().downloadToStream(new ObjectId(sID), streamToDownloadTo);
        streamToDownloadTo.close();

        String sFile = FileUtils.getContentFromFile(tf2);
        System.out.println(tf2.getName());
        sFile = sFile.replace(sSearch, sReplace);
        StringUtils.writeContent(sFile, tf2);

//        for (String s : TypeSystemDescriptionFactory.scanTypeDescriptors()) {
//            System.out.println(s);
//        }

        JCas rCas = JCasFactory.createJCas();
        CasIOUtil.readXmi(rCas, tf2);

        return rCas;

    }

    public Set<ObjectId> getGridFSObjectIds(Bson filter) {
        Set<ObjectId> rIds = new HashSet<>();
        GridFSBucket connection = getBucketConnection();
        for (GridFSFile file : connection.find(filter)) {
            rIds.add(file.getObjectId());
        }
        return rIds;
    }

    public Set<ObjectId> getGridFSObjectIds(List<? extends Bson> aggregation) {
        Set<ObjectId> rIds = new HashSet<>();
        for (Document document : getGridFSCollection().aggregate(aggregation)) {
            rIds.add((document.getObjectId("_id")));
        }
        return rIds;
    }

    public Set<JCas> getElementsGridFS(Bson filter) throws IOException, UIMAException {
        Set<JCas> rCas = new HashSet<>();
        for (ObjectId id : getGridFSObjectIds(filter)) {
            rCas.add(getElementGridFS(id.toString()));
        }
        return rCas;
    }


    public Set<JCas> getElementsGridFS(List<? extends Bson> aggregation) throws IOException, UIMAException {
        Set<JCas> rCas = new HashSet<>();
        for (ObjectId id : getGridFSObjectIds(aggregation)) {
            rCas.add(getElementGridFS(id.toString()));
        }
        return rCas;
    }

    public void deleteElementGridFS(String sMongoID) throws CASException {

        try {
            getBucketConnection().delete(new ObjectId(sMongoID));
        } catch (MongoGridFSException e) {
            e.printStackTrace();
        }

    }

    public String updateElementGridFS(String sMongoID, JCas jcas) throws IOException, CASException {
        return updateElementGridFS(sMongoID, jcas, null);
    }

    public String updateElementGridFS(String sMongoID, JCas jcas, @Nullable Document statistics) throws IOException, CASException {

        deleteElementGridFS(sMongoID);

        GridFSUploadOptions options = new GridFSUploadOptions()
                .chunkSizeBytes(358400)
                .metadata(new Document("type", "uima"));

        GridFSUploadStream uploadStream = getBucketConnection().openUploadStream(String.valueOf(System.currentTimeMillis()), options);

        File tf = TempFileHandler.getTempFile("aaa", "bbb");

        CasIOUtil.writeXmi(jcas, tf);

        byte[] data = Files.readAllBytes(tf.toPath());
        uploadStream.write(data);
        uploadStream.close();
        try {
            if (statistics != null)
                getGridFSCollection().updateOne(Filters.eq("_id", uploadStream.getObjectId()), Updates.set("metadata.statistics", statistics));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return uploadStream.getObjectId().toString();

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
        updateElement(pJCas, pAttributes, false);
    }

    @Override
    public void updateElement(JCas pJCas, JSONArray pAttributes, boolean bCompressed) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException {
        String sString = null;
        try {
            sString = UIMADatabaseInterface.serializeJCas(pJCas, pAttributes, bCompressed);
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
        updateElement(pJCas, false);
    }

    public void updateElement(JCas pJCas, boolean pCompression) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException {

        String sString = null;
        try {
            sString = UIMADatabaseInterface.serializeJCas(pJCas, pCompression);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        DBObject doc = (DBObject) JSON.parse(sString);
        String sID = UIMADatabaseInterface.getID(pJCas);

        this.updateElement(dummyObject(sID), doc);

    }

    public void updateElement(JCas pJCas, String sID) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException {
        updateElement(pJCas, sID, false);
    }


    public void updateElement(JCas pJCas, String sID, boolean pCompression) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException {

        String sString = null;
        try {
            sString = UIMADatabaseInterface.serializeJCas(pJCas, pCompression);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        DBObject doc = (DBObject) JSON.parse(sString);

        this.updateElement(dummyObject(sID), doc);

    }


    @Override
    public void updateElement(DBObject pQuery, String sObject) {
        super.updateElement(pQuery, sObject);
    }

    @Override
    public long getSize(String sID) {

        DBObject tObject = null;

        if (sID.contains("/")) {
            tObject = getDBElement(sID.substring(sID.lastIndexOf("/") + 1));
        } else {
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
        String sFinalID = "";
        if (sID.contains("/")) {
            sFinalID = sID.substring(sID.lastIndexOf("/") + 1);
            tObject = getDBElement(sFinalID);
        } else {
            sFinalID = sID;
            tObject = getDBElement(sFinalID);
        }

        String json = JSON.serialize(tObject);

        try {
            rCas = UIMADatabaseInterface.deserializeJCas(json);

            try {
                rCas.getView(UIMADatabaseInterface.UIMADBID);
            } catch (Exception e) {
                rCas.createView(UIMADatabaseInterface.UIMADBID).setDocumentText(sFinalID);
            }

        } catch (UIMAException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return rCas;
    }

    public JCas getElement(String sID, String sSearch, String sReplace) {
        JCas rCas = null;

        DBObject tObject = null;
        String sFinalID = "";
        if (sID.contains("/")) {
            sFinalID = sID.substring(sID.lastIndexOf("/") + 1);
            tObject = getDBElement(sFinalID);
        } else {
            sFinalID = sID;
            tObject = getDBElement(sFinalID);
        }

        String json = JSON.serialize(tObject);
        json = json.replaceAll(sSearch, sReplace);

        try {
            rCas = UIMADatabaseInterface.deserializeJCas(json);

            try {
                rCas.getView(UIMADatabaseInterface.UIMADBID);
            } catch (Exception e) {
                rCas.createView(UIMADatabaseInterface.UIMADBID).setDocumentText(sFinalID);
            }

        } catch (UIMAException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return rCas;
    }

//    public JCas getElementGridFS(String sID){
//
//        JCas rCas = null;
//
//        GridFSDownloadStream downloadStream = getBucketConnection().openDownloadStream(new ObjectId(sID));
//        int fileLength = (int) downloadStream.getGridFSFile().getLength();
//        byte[] bytesToWriteTo = new byte[fileLength];
//        downloadStream.read(bytesToWriteTo);
//        downloadStream.close();
//
//        String s = new String(bytesToWriteTo, StandardCharsets.UTF_8);
//        String json = JSON.serialize(s);
//        json = json.substring(1, json.length()-1);
//
//        try {
//            rCas = UIMADatabaseInterface.deserializeJCas(json);
//        } catch (UIMAException e) {
//            e.printStackTrace();
//        }
//
//
//        return rCas;
//
//    }

    private DBObject negetDBElement(String sID) {

        DBObject rElement = null;

        DBObject docSource = new BasicDBObject();
        docSource.put("_id", new ObjectId(sID));

        DBCursor dbCursor = null;

        dbCursor = this.getConnection().find(docSource);

        dbCursor.addOption(Bytes.QUERYOPTION_NOTIMEOUT).batchSize(1000);

        while (dbCursor.hasNext()) {
            rElement = dbCursor.next();
        }

        return rElement;

    }

    @Override
    public Set<JCas> getElements(String sQuery) {

        Set<JCas> rCas = new HashSet<>(0);

        DBObject qObject = (DBObject) JSON.parse(sQuery);

        DBCursor dbCursor = null;

        dbCursor = this.getConnection().find(qObject);
        dbCursor.addOption(Bytes.QUERYOPTION_NOTIMEOUT).batchSize(1000);

        while (dbCursor.hasNext()) {

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
        JSONArray pArray = new JSONArray().put(new double[]{lon, lat}).put(new BigDecimal(distance / 6371.1));

        final BasicDBObject geoWithhin = new BasicDBObject("$centerSphere", pArray);

        final BasicDBObject filter = new BasicDBObject("$geoWithin", geoWithhin);

        final BasicDBObject query = new BasicDBObject("geo", filter);

        if (sType.length() > 0) {
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
        return sID.replace("_", "");
    }

    @Override
    public void destroy() {

    }

    @Override
    public void start() {

    }

}
