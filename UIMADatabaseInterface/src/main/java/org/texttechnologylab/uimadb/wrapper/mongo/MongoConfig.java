package org.texttechnologylab.uimadb.wrapper.mongo;


import java.io.*;
import java.util.Properties;

/**
 * Created by abrami on 02.06.17.
 */
public class MongoConfig extends Properties {

    public MongoConfig(String pathFile) throws IOException {
        //System.out.println(System.getProperty("user.dir"));
        String current = new File( "." ).getCanonicalPath();
        BufferedReader lReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(pathFile)), "UTF-8"));
        this.load(lReader);
        lReader.close();
    }


    public String getHost() {
        String sResult = getProperty("mongo_host", "defaulthost");
        return sResult;
    }

    public String getDatabaseName() {
        String sResult = getProperty("mongo_db", "defaultdb");
        return sResult;
    }

    public String getCollection() {
        String sResult = getProperty("mongo_collection", "defaultcollection");
        return sResult;
    }

    public String getUsername() {
        String sResult = getProperty("mongo_user", "defaultcollection");
        return sResult;
    }

    public String getPassword() {
        String sResult = getProperty("mongo_password", "defaultcollection");
        return sResult;
    }

}
