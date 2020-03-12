package org.texttechnologylab.uimadb.wrapper.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;

import java.net.UnknownHostException;

public class CouchbaseConnection {
    public  Bucket bucket;
    public  Cluster cluster;
    private CouchbaseConfig couchbaseConfig;

    public CouchbaseConnection(String sHost, String sBucket, String sUsername, String sPassword) {
        this.couchbaseConfig = null;

        // cluster is object for the connection
        this.cluster = CouchbaseCluster.create(sHost);
        this.cluster.authenticate(sUsername, sPassword);
        this.bucket = cluster.openBucket(sBucket);
    }

    public CouchbaseConnection(CouchbaseConfig couchbaseConfig) throws UnknownHostException {
        this(couchbaseConfig.getHost(), couchbaseConfig.getDataBucket(), couchbaseConfig.getUsername(), couchbaseConfig.getPassword());
        this.couchbaseConfig = couchbaseConfig;
    }


    public Cluster getCluster() {
        return this.cluster;
    }

    public String toString() {
        return "CouchbaseConnection: " + this.couchbaseConfig.getHost() + ":" + this.couchbaseConfig.getDataBucket();
    }
}
