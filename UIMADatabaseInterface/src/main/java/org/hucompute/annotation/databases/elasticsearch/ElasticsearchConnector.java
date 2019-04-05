package org.hucompute.annotation.databases.elasticsearch;

/*
 * Copyright 2019
 * Texttechnology Lab
 * Goethe-Universität Frankfurt am Main
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.hucompute.annotation.UIMADatabaseInterface;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

public class ElasticsearchConnector {

    File pConfigFile=null;
    Properties pProperties = null;
    TransportClient client = null;


    public ElasticsearchConnector(File pConfigFile) throws IOException {
        this.pConfigFile=pConfigFile;
        init();
    }

    public ElasticsearchConnector(File pConfigFile, String sIndexName) throws IOException {
        this.pConfigFile=pConfigFile;
        init();
        pProperties.setProperty("cluster.index", sIndexName);
    }

    private JSONObject setProperties(JSONObject pObject){

        if(pObject.has("settings")){
            pObject = pObject.getJSONObject("settings").put("index.mapping.ignore_malformed", true);
        }
        else{
            pObject = pObject.put("settings", new JSONObject().put("index.mapping.ignore_malformed", true));
        }
        return pObject;

    }

    public String getProperty(String sProperty){
        return pProperties.getProperty(sProperty);
    }

    public void init() throws IOException {

        pProperties = new Properties();
        try {
            UIMADatabaseInterface.getLogger().debug("Elasticsearch: Get Properties from "+pConfigFile.getName());
            System.out.println("Elasticsearch: Get Properties from "+pConfigFile.getName());
            pProperties.load(new FileInputStream(pConfigFile));

        } catch (IOException e) {
            UIMADatabaseInterface.getErrorLogger().error(e.getMessage(), e);
        }


        Settings settings = Settings.builder()
                .put("cluster.name", getProperty("cluster.name")).build();

        this.client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new TransportAddress(InetAddress.getByName(getProperty("cluster.host")), Integer.valueOf(getProperty("cluster.port"))));

        //createIndex("textannotator", setProperties(new JSONObject()).toString());
    }


    private String createIndex(String sIndex, String sDefaults) throws IOException {
        IndexResponse response = client.prepareIndex(sIndex, "_doc")
                .setSource(sDefaults, XContentType.JSON)
                .get();
        return response.getId();
    }

    public String insert(JSONObject pObject) throws IOException {
        return insert(setProperties(pObject).toString());
    }


    private String insert(String sJSON) throws IOException {
        IndexResponse response = client.prepareIndex(getProperty("cluster.index"), "_doc")
                .setSource(sJSON, XContentType.JSON)
                .get();
        return response.getId();

    }

    private String updateBulkSingle(String sJSON, String sID) throws IOException {
        BulkRequestBuilder bulkRequest = client.prepareBulk();

        bulkRequest.add(client.prepareUpdate(getProperty("cluster.index"), "_doc", sID)
                .setDoc(sJSON, XContentType.JSON));

        BulkResponse responses = bulkRequest.get();

        return responses.getItems()[0].getId();
    }

    public String updateBulkSingle(JSONObject pObject, String sID) throws IOException {
        return updateBulkSingle(setProperties(pObject).toString(), sID);
    }

    public String update(JSONObject pJSON, String sID) throws IOException {
        JSONObject nObject = setProperties(pJSON);
        return update(nObject.toString(), sID);
    }

    private String update(String sJSON, String sID) throws IOException {
        UpdateResponse response = client.prepareUpdate(getProperty("cluster.index"), "_doc", sID)
                .setDoc(sJSON, XContentType.JSON).get();

        return response.getId();
    }


    public String get(String sID) throws IOException {
        GetResponse response = client.prepareGet(getProperty("cluster.index"), "_doc", sID).get();
        return new JSONObject(response.toString()).getJSONObject("_source").toString();
    }

    // on shutdown
    public void onClose(){
        this.client.close();
    }


}
