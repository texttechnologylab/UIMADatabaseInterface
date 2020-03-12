package org.texttechnologylab.uimadb.wrapper.couchbase;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class CouchbaseConfig extends Properties {

    public CouchbaseConfig(String pathFile) throws IOException {
        String current = new File( "." ).getCanonicalPath();
        BufferedReader lReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(pathFile)), StandardCharsets.UTF_8));
        this.load(lReader);
        lReader.close();
    }


    public String getHost() {
            String sResult = getProperty("couchbase_host", "defaulthost");
            return sResult;
    }

    public String getDataBucket() {
        String sResult = getProperty("couchbase_databucket", "defaultdbucket");
        return sResult;
    }

    public String getMetaBucket() {
        String sResult = getProperty("couchbase_metabucket", "defaultdbucket");
        return sResult;
    }
    public String getUsername() {
        String sResult = getProperty("couchbase_user", "defaultuser");
        return sResult;
    }

    public String getPassword() {
        String sResult = getProperty("couchbase_password", "defaultpassword");
        return sResult;
    }

    public String getChunkSize(){
        String chunksize = getProperty("chunksize", "1048576");
        return chunksize;
    }
}
