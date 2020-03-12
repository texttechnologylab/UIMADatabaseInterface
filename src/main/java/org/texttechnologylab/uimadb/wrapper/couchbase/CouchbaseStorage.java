package org.texttechnologylab.uimadb.wrapper.couchbase;

// adapted in parts from
// https://github.com/cecilelepape/cmis-couchbaseonly/tree/master/chemistry-opencmis-server-couchbaseonly/src/main/java/org/apache/chemistry/opencmis/couchbase
import com.couchbase.client.deps.io.netty.buffer.ByteBuf;
import com.couchbase.client.deps.io.netty.buffer.Unpooled;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.BinaryDocument;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CouchbaseStorage {
    // used for storage of documents > 20MB

    // size of each binary chunk
    private static int BUFFER_SIZE = 1048576;
    private static final String STORAGE_ID = "local";
    private static final String PART_SUFFIX = "::part";

    public static void writeContent(String dataId, String jsonString){
        // change jsonString to inputstream oder contentstream to allow for other data formats

        byte[] jsonStringByteArray = jsonString.getBytes();
        InputStream inputStream = new ByteArrayInputStream(jsonStringByteArray);

        // count the number of parts
        long length = jsonStringByteArray.length;
        System.out.println("length=" + length + " - buffer size=" + BUFFER_SIZE);

        long nbparts = length / BUFFER_SIZE;

        // add last part for the rest
        if (length - nbparts * BUFFER_SIZE > 0)
            nbparts++;
        System.out.println("nbparts=" + nbparts);

        // create "overview" document which is used to find all the chunks of a requested object
        JsonObject doc = JsonObject.empty();
        doc.put("count", nbparts);
        doc.put("mimetype", "JsonString"); // change if other documents than JsonStrings are stored binary
        doc.put("length", length);

        long totalLength = 0;
        int read = 0; // The number of bytes not yet read
        byte[] byteArray = new byte[BUFFER_SIZE];
        int offset = 0;
        for (int i = 0; i < nbparts; i++) {
            try {
                // read in chunks of BUFFER_SIZE from inputStream

                read = inputStream.read(byteArray, 0, BUFFER_SIZE);

                System.out.println("wrote " + read + " bytes beginning from "
                        + offset);

                totalLength += read;
                System.out.println("Number of bytes read : " + totalLength);
                offset += read;
                writeContentPart(dataId + PART_SUFFIX + i, byteArray, read);
                doc.put(dataId + PART_SUFFIX + i, read);
            } catch (IOException e) {
                e.printStackTrace();
                System.out
                        .println("Pb with reading stream : " + e.getMessage());
            }
        }

        System.out.println("Number of bytes read : " + totalLength
                + " -  length=" + length);
        if (totalLength != length)
            System.out.println("Wrong number of bytes read from stream");


        JsonDocument jsondoc = JsonDocument.create(dataId, doc);
        CouchbaseHelper.getBucket().upsert(jsondoc);

        // reset buffer size (needed if custom buffer size is used)
        //setBufferSize(1048576);
    }

    public static void writeContent(String dataId, String jsonString, Integer chunkByteSize){
        setBufferSize(chunkByteSize);
        writeContent(dataId, jsonString);
    }

    public static void setBufferSize(int buffersize){
        BUFFER_SIZE = buffersize;
        System.out.println("Buffersize changed to" + BUFFER_SIZE);
    }
    private static void writeContentPart(String partId, byte[] byteArray, int length) {
        BinaryDocument bDoc = BinaryDocument.create(partId,
                Unpooled.copiedBuffer(byteArray));
        CouchbaseHelper.getBucket().upsert(bDoc);
    }

    public static void writeMetaData(String dataId, String jsonString){
        Bucket bucket = CouchbaseHelper.getBucket();
        JsonObject jsonObject = JsonObject.fromJson(jsonString);
        JsonDocument jsonDocument = JsonDocument.create(dataId, jsonObject);

        bucket.upsert(jsonDocument);
    }

    public static String getDocumentString(String dataId){

        Bucket bucket = CouchbaseHelper.getBucket();
        JsonDocument doc = bucket.get(dataId);
        JsonObject json = doc.content();
        Integer nbparts = json.getInt("count");
        Integer length = json.getInt("length");

//        if(nbparts==null || length==null || mimeType==null){System.out.println("Document invalid");}
        if(nbparts==null || length==null){System.out.println("Document invalid");}

        // mimeType only relevant when inserting different types of documents
        //mimeType.append(json.getString("mimetype"));

        // byteArray that is filled with content of all chunks
        byte[] byteArray = new byte[length];
        // for each part, read the content into the byteArray
        int offset = 0;
        Integer partLength = null;

        for (int i = 0; i < nbparts; i++) {
            partLength = json.getInt(dataId + PART_SUFFIX + i);
            if(partLength == null){ System.out.println("length of part "+i+" is mandatory");}

            BinaryDocument bDoc =
                    bucket.get(dataId + PART_SUFFIX + i,BinaryDocument.class);
            ByteBuf part = bDoc.content();
            byte[] dst = new byte[partLength];

            // put content of current chunk in dst
            part.readBytes(dst);

            // put content of dst in list that contains whole document in byte
            for (int k = 0; k < partLength; k++) {
                byteArray[k + offset] = dst[k];
            }
            offset += partLength;

            // clear buffer for next chunk
            part.release();
        }
        //InputStream stream = new ByteArrayInputStream(byteArray);
        return new String(byteArray);
    }

    public static boolean deleteContent(String dataId) {

        System.out.println("deleteContent dataId="+dataId);
        Bucket bucket =CouchbaseHelper.getBucket();
        JsonDocument doc = bucket.get(dataId);
        JsonObject json = doc.content();

        // delete the main doc
        bucket.remove(dataId);

        // delete each part
        Integer nbparts = json.getInt("count");
        if(nbparts==null) return true;
        for(int i=0 ; i<nbparts ; i++){
            bucket.remove(dataId + PART_SUFFIX + i);
        }
        return true;
    }
}
