package org.texttechnologylab.uimadb;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.util.CasCopier;
import org.texttechnologylab.uimadb.databases.elasticsearch.Elasticsearch;
import org.json.JSONException;
import org.texttechnologylab.uimadb.databases.mongo.Mongo;
import org.texttechnologylab.uimadb.wrapper.mongo.MongoConnection;
import org.texttechnologylab.uimadb.wrapper.mongo.MongoHelper;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.ICasSerializer;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.json.CasJsonSerializer;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.json.CasJsonSerializerFactory;
import org.texttechnologylab.utilities.helper.StringUtils;

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
    public void testIndex() throws IOException, UIMAException {

        File nf = new File(getClass().getClassLoader().getResource("elasticsearch_example.conf").getFile());
        Elasticsearch es = new Elasticsearch(nf);

        JCas pCas = JCasFactory.createText("Dies ist ein Text und ich weiß sonst auch keinen besseren", "de");

        es.createDummy(pCas);

        es.updateElement(pCas);



        JCas nCas = es.getElement(UIMADatabaseInterface.getID(pCas));
        System.out.println(nCas.getDocumentText());

    }

}
