package org.texttechnologylab.uimadb.wrapper.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.texttechnologylab.uimadb.databases.couchbase.Couchbase;

import java.io.IOException;
import java.util.List;

public class CouchbaseQueries {

    public static void printQueryResultsData(String queryStatement) throws IOException, ResourceInitializationException {
        // prints query result of query passed by string
        // examples statements "SELECT * FROM `test-data` USE KEYS [\"5cb88b4ed2babe34f98aefc3\"]"
        // "SELECT META(`test-data`).id FROM `test-data` WHERE `count` = 18"
        // query id with USE KEYS ["5cb88b4ed2babe34f98aefc3"] or META(`test-data`).id

        CouchbaseConfig couchbaseConfig = new CouchbaseConfig("src/main/resources/couchbase_example.conf");
        Couchbase cb = new Couchbase(couchbaseConfig);
        Bucket bucket = CouchbaseHelper.getBucket();
        bucket.bucketManager().createN1qlPrimaryIndex(true, false);
        N1qlQuery myQuery = N1qlQuery.simple(queryStatement);
        N1qlQueryResult queryResult = bucket.query(myQuery);
        for (N1qlQueryRow row : queryResult) {
            // each Row is one result file
            System.out.println(row);
        }
        cb.destroy();
    }

    public static List<N1qlQueryRow> returnQueryResultsData(String queryStatement) throws IOException, ResourceInitializationException {
        // returns query results as a list
        CouchbaseConfig couchbaseConfig = new CouchbaseConfig("src/main/resources/couchbase_example.conf");
        Couchbase cb = new Couchbase(couchbaseConfig);
        Bucket bucket = CouchbaseHelper.getBucket();
        bucket.bucketManager().createN1qlPrimaryIndex(true, false);
        N1qlQuery myQuery = N1qlQuery.simple(queryStatement);
        N1qlQueryResult queryResult = bucket.query(myQuery);
        List<N1qlQueryRow> results = queryResult.allRows();
        //System.out.println(results);
        cb.destroy();
        return results;
        }

    public static void printQueryResultsMeta(String queryStatement) throws ResourceInitializationException, IOException {
        // prints query result of query passed by string
        // examples statements "SELECT * FROM `test-data` USE KEYS [\"5cb88b4ed2babe34f98aefc3\"]"
        // "SELECT META(`test-data`).id FROM `test-data` WHERE `count` = 18"
        // query id with USE KEYS ["5cb88b4ed2babe34f98aefc3"] or META(`test-data`).id
        CouchbaseConfig couchbaseConfig = new CouchbaseConfig("src/main/resources/couchbase_example.conf");
        Couchbase cb_meta = new Couchbase(couchbaseConfig.getHost(), couchbaseConfig.getMetaBucket(), couchbaseConfig.getUsername(), couchbaseConfig.getPassword());
        Bucket bucket = CouchbaseHelper.getBucket();
        bucket.bucketManager().createN1qlPrimaryIndex(true, false);
        N1qlQuery myQuery = N1qlQuery.simple(queryStatement);
        N1qlQueryResult queryResult = bucket.query(myQuery);
        for (N1qlQueryRow row : queryResult) {
            // each Row is one result file
            System.out.println(row);
        }
        cb_meta.destroy();
    }

    public static List<N1qlQueryRow> returnQueryResultsMeta(String queryStatement) throws ResourceInitializationException, IOException {
        // returns query results as a list
        CouchbaseConfig couchbaseConfig = new CouchbaseConfig("src/main/resources/couchbase_example.conf");
        Couchbase cb_meta = new Couchbase(couchbaseConfig.getHost(), couchbaseConfig.getMetaBucket(), couchbaseConfig.getUsername(), couchbaseConfig.getPassword());
        Bucket bucket = CouchbaseHelper.getBucket();
        bucket.bucketManager().createN1qlPrimaryIndex(true, false);
        N1qlQuery myQuery = N1qlQuery.simple(queryStatement);
        N1qlQueryResult queryResult = bucket.query(myQuery);
        List<N1qlQueryRow> results = queryResult.allRows();
        cb_meta.destroy();
        return results;
    }

    public static void queryDocByType(String type) throws ResourceInitializationException, IOException {
        // returns query results as a list
        CouchbaseConfig couchbaseConfig = new CouchbaseConfig("src/main/resources/couchbase_example.conf");
        Couchbase cb_meta = new Couchbase(couchbaseConfig.getHost(), couchbaseConfig.getMetaBucket(), couchbaseConfig.getUsername(), couchbaseConfig.getPassword());
        Bucket bucketMeta = CouchbaseHelper.getBucket();
        bucketMeta.bucketManager().createN1qlPrimaryIndex(true, false);
        String queryString = "SELECT META(`test-meta`).id FROM" + "`" + couchbaseConfig.getMetaBucket() + "`" +"WHERE `type`= " + "\"" +type+ "\"";
        List<N1qlQueryRow> dataIds = returnQueryResultsMeta(queryString);
        bucketMeta.close();
        Couchbase cb = new Couchbase(couchbaseConfig);
        Bucket bucketData = CouchbaseHelper.getBucket();
        bucketData.bucketManager().createN1qlPrimaryIndex(true, false);

        for (N1qlQueryRow row : dataIds){
            Object docID = row.value().get("id");
            // get JCas associated with ids
            JCas myJcas = cb.getElementBlob(docID.toString());
            System.out.println(myJcas.size());
        }
        bucketData.close();
    }
}

