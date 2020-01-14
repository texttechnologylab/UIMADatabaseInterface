package org.texttechnologylab.uimadb;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.json.JSONException;
import org.texttechnologylab.uimadb.databases.elasticsearch.Elasticsearch;
import org.texttechnologylab.uimadb.wrapper.mongo.MongoSerialization;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.CasSerializerMetaFactory;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.ICasSerializer;

import java.io.File;
import java.io.IOException;

public class Test {

    @org.junit.Test
    public void testConnection() throws UIMAException, JSONException, IOException {

        Elasticsearch es = new Elasticsearch("");

        JCas pCas = JCasFactory.createText("Test", "de");

        es.createDummy(pCas);

        es.updateElement(pCas);



        JCas nCas = es.getElement(UIMADatabaseInterface.getID(pCas));
        System.out.println(nCas.getDocumentText());

    }



    @org.junit.Test
    public void testIndex() throws IOException, UIMAException {

        File nf = new File(getClass().getClassLoader().getResource("elasticsearch_example.conf").getFile());
        Elasticsearch es = new Elasticsearch(nf);

        JCas pCas = JCasFactory.createText("Test", "de");

        es.createDummy(pCas);

        es.updateElement(pCas);



        JCas nCas = es.getElement(UIMADatabaseInterface.getID(pCas));
        System.out.println(nCas.getDocumentText());

    }

    @org.junit.Test
    public void testCompression() throws UIMAException {
        JCas pCas = JCasFactory.createText("Test", "de");

        ICasSerializer serializer = CasSerializerMetaFactory.Instance().getFactory(MongoSerialization.getSerializerFactory()).createSerializer();
        String s = serializer.serialize(pCas.getCas(), true);

        System.out.println(s);
    }

}
