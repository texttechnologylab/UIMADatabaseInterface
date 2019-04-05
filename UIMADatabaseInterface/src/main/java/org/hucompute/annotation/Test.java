package org.hucompute.annotation;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.hucompute.annotation.databases.elasticsearch.Elasticsearch;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;

public class Test {

    @org.junit.Test
    public void testConnection() throws UIMAException, JSONException, IOException {

        Elasticsearch es = new Elasticsearch("");

        JCas pCas = JCasFactory.createText("Dies ist ein Text und ich weiß sonst auch keinen besseren", "de");

        es.createDummy(pCas);

        es.updateElement(pCas);



        JCas nCas = es.getElement(UIMADatabaseInterface.getID(pCas));
        System.out.println(nCas.getDocumentText());

    }


    @org.junit.Test
    public void testIndex() throws IOException {

        File nf = new File(getClass().getClassLoader().getResource("elasticsearch_example.conf").getFile());
        Elasticsearch es = new Elasticsearch(nf);

    }

}
