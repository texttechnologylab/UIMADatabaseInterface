package org.texttechnologylab.uimadb.wrapper.couchbase;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.texttechnologylab.uimadb.UIMADatabaseInterface;
import org.xml.sax.SAXException;

import java.io.*;

public class XmiHandler {

    public static JCas XmiFileToJCas(String xmiFilePath) throws IOException, InvalidXMLException, ResourceInitializationException, CollectionException, CASException {
        JCas myJCas = UIMADatabaseInterface.getJCas();
        CAS myCas = myJCas.getCas();
        File currentFile = new File(xmiFilePath);
        try (InputStream inputStream = new FileInputStream(currentFile)) {
            // Deserialize Xmi to CAS via inputstream
            XmiCasDeserializer.deserialize(inputStream, myCas);
        } catch (SAXException e) {
            e.printStackTrace();
        }
        myJCas = myCas.getJCas();
        return myJCas;
    }

    public static void JCasToXmiFile(JCas jCas, String FileName) {
        // create XMI file from CAS in directroy xmi_output
        String file = "xmi_output/" + FileName;
        File currentFile = new File(file);
        CAS myCas = jCas.getCas();
        try (OutputStream outputStream = new FileOutputStream(currentFile)) {
            // serialize CAS to XMI via Outputstream
            XmiCasSerializer.serialize(myCas, outputStream);
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
    }
}
