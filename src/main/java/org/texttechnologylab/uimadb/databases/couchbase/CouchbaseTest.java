package org.texttechnologylab.uimadb.databases.couchbase;

import org.apache.uima.UIMAException;
import org.apache.uima.cas.SofaFS;
import org.apache.uima.jcas.JCas;
import org.json.JSONException;
import org.texttechnologylab.uimadb.databases.couchbase.Couchbase;
import org.texttechnologylab.uimadb.wrapper.couchbase.CouchbaseConfig;
import org.texttechnologylab.uimadb.wrapper.couchbase.CouchbaseQueries;
import org.texttechnologylab.uimadb.wrapper.couchbase.CouchbaseStorage;
import org.texttechnologylab.uimadb.wrapper.couchbase.XmiHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.texttechnologylab.uimadb.wrapper.couchbase.CouchbaseStorage.setBufferSize;

public class CouchbaseTest {

    @org.junit.Test
    public void testConnection() throws UIMAException, JSONException, IOException {
        uploadXmiData(); // upload XmiData to Couchbase. Uses chunking.
        downloadXmiData(); // download Data from Couchbase and write to xmi_output


        CouchbaseQueries.printQueryResultsMeta("SELECT META(`test-meta`).id FROM `test-meta` WHERE `type` = \"org.texttechnologylab.annotation.ocr.OCRParagraph\"");
        //CouchbaseQueries.queryDocByType("org.texttechnologylab.annotation.ocr.OCRParagraph");
    }

    public void uploadXmiData() throws UIMAException, IOException {
        CouchbaseConfig couchbaseConfig = new CouchbaseConfig("src/main/resources/couchbase_example.conf");
        Couchbase cb = new Couchbase(couchbaseConfig);

        // all xmi input files
        File[] files = new File("xmi_input").listFiles();

        // used to get meta data from every jcas
        List<JCas> jCasList = new ArrayList<JCas>();
        // upload every file from directory
        for (File file : files) {

            // set buffer size (= chunk size in byte) for file

            CouchbaseStorage.setBufferSize(Integer.parseInt(couchbaseConfig.getChunkSize())); // get chunksize from config
            JCas myJCas = XmiHandler.XmiFileToJCas(file.getPath());

            System.out.println(myJCas.size());
            JCas myJpCas = cb.createDummy(myJCas);
            cb.updateElementBlob(myJCas);
            jCasList.add(myJCas);
            System.out.println("file " + file.getPath() + " done");
        }

        // close connection to data bucket
        cb.destroy();

        // open connection to meta bucket // not possible to just insert couchbaseConfig since only one bucket can be specified this way
        Couchbase cb_meta = new Couchbase(couchbaseConfig.getHost(), couchbaseConfig.getMetaBucket(), couchbaseConfig.getUsername(), couchbaseConfig.getPassword());
        // upload meta data to meta bucket
        for (JCas jCas : jCasList) {
            cb.updateMetaData(jCas);
        }
        // close connection to meta bucket
        cb_meta.destroy();
    }

    public void downloadXmiData() throws IOException, UIMAException {
        CouchbaseConfig couchbaseConfig = new CouchbaseConfig("src/main/resources/couchbase_example.conf");
        Couchbase cb = new Couchbase(couchbaseConfig);
        // restoring Xmi Files
        String[] idArray = new String[5];
        idArray[0] = "5cb88bbcd2babe34f98aeffd"; // 3718079.xmi as reference to compare with input
        idArray[1] = "5cb88b4ed2babe34f98aefc3"; // 3621555.xmi
        idArray[2] = "5cb88b0fd2babe34f98aefa3"; // 3752953.xmi
        idArray[3] = "5cb88ba3d2babe34f98aeff1"; // 3924211.xmi
        idArray[4] = "5cb88b2ad2babe34f98aefb1"; // 4523122.xmi

        for (String id : idArray) {
            // get UIMA-Doc from database as JCas
            JCas rCas = cb.getElementBlob(id);
            // Convert Jcas to Xmi and write to file
            XmiHandler.JCasToXmiFile(rCas, id);
        }

        cb.destroy();

    }
}
