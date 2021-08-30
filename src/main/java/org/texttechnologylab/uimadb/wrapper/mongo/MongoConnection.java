package org.texttechnologylab.uimadb.wrapper.mongo;


import com.mongodb.*;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper for a connection to Mongo.
 *
 * @author renaud.richardet@epfl.ch
 */
public class MongoConnection {

    public final DB db;
    public final DBCollection coll;
    private MongoConfig mongoConfig;

    private String sDatabase = "";

    public MongoConnection(String sHost, String sDatabase, String sCollection, String sUsername, String sPassword) {
        this.mongoConfig = null;
        MongoCredential credential = MongoCredential.createScramSha1Credential(sUsername, sDatabase, sPassword.toCharArray());
        List<MongoCredential> creds = new ArrayList(0);
        creds.add(credential);
        ServerAddress seed = new ServerAddress(sHost);
        List<ServerAddress> seeds = new ArrayList(0);
        seeds.add(seed);
        this.sDatabase = sDatabase;
        MongoClient mongoClient = new MongoClient(seeds, creds);
        this.db = mongoClient.getDB(sDatabase);
        this.coll = this.db.getCollection(sCollection);
    }

    public MongoConnection(String sHost, String sDatabase, String sCollection) {
        this.mongoConfig = null;
        this.sDatabase = sDatabase;
        MongoClient mongoClient = new MongoClient(sHost);
        this.db = mongoClient.getDB(sDatabase);
        this.coll = this.db.getCollection(sCollection);
    }

    public MongoConnection(MongoConfig mongoConfig) throws UnknownHostException, MongoException {
        this(mongoConfig.getHost(), mongoConfig.getDatabaseName(), mongoConfig.getCollection(), mongoConfig.getUsername(), mongoConfig.getPassword());
        this.mongoConfig = mongoConfig;
        this.sDatabase = this.mongoConfig.getDatabaseName();
    }

    public String toString() {
        return "MongoConnection: " + this.mongoConfig.getHost() + ":" + this.mongoConfig.getDatabaseName() + "::" + this.mongoConfig.getCollection();
    }

    public MongoConfig getMongoConfig(){
        return mongoConfig;
    }

    public String getDatabaseName(){
        return sDatabase;
    }
}
