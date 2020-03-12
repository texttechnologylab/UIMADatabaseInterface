package org.texttechnologylab.uimadb;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.json.JSONException;
import org.texttechnologylab.uimadb.databases.couchbase.Couchbase;
import org.texttechnologylab.uimadb.databases.elasticsearch.Elasticsearch;
import org.texttechnologylab.uimadb.wrapper.couchbase.CouchbaseConnection;
import org.texttechnologylab.uimadb.wrapper.mongo.MongoSerialization;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.CasSerializerMetaFactory;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.ICasSerializer;

import java.io.File;
import java.io.IOException;

public class Test {
    // eigenständiges programm schreiben dass die nötigen schritte für die aufgaben ausführt
    public void testConnection() throws UIMAException, JSONException, IOException {

        Couchbase cb = new Couchbase("localhost:8091", "test-data", "admin", "shogun93");

        JCas pCas = JCasFactory.createText("Test", "de");

        JCas jpCas = cb.createDummy(pCas);

        cb.updateElement(pCas);
//
        JCas nCas = cb.getElement(UIMADatabaseInterface.getID(pCas));
        System.out.println(nCas.getDocumentText());


    }
}



