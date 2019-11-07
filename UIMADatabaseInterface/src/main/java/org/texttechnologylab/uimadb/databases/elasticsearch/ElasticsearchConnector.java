package org.texttechnologylab.uimadb.databases.elasticsearch;

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

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateAction;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.seqno.RetentionLeaseActions;
import org.json.JSONObject;
import org.texttechnologylab.uimadb.UIMADatabaseInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ElasticsearchConnector {

    File pConfigFile=null;
    Properties pProperties = null;
    RestHighLevelClient client = null;


    public ElasticsearchConnector(File pConfigFile) throws IOException {
        this.pConfigFile=pConfigFile;
        init();
    }

    public ElasticsearchConnector(File pConfigFile, String sIndexName) throws IOException {
        this.pConfigFile=pConfigFile;
        init();
        pProperties.setProperty("cluster.index", sIndexName);
    }

    private JSONObject setProperties(JSONObject pObject) throws IOException {

        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        {

            pObject.keySet().forEach(k->{
                try {
                    builder.field(k, pObject.get(k));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        }
        builder.endObject();

        if(pObject.has("settings")){
            //pObject = pObject.getJSONObject("settings").put("index.mapping.ignore_malformed", true);
        }
        else{
            //pObject = pObject.put("settings", new JSONObject().put("index.mapping.ignore_malformed", true));
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

        this.client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(getProperty("cluster.host"), Integer.parseInt(getProperty("cluster.port")), "http")));

        //String sIndex = createIndex(getProperty("cluster.index"), setProperties(new JSONObject()).toString());

        //System.out.println(sIndex);
    }


    private String createIndex(String sIndex, String sDefaults) throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(sIndex);

        Settings.Builder b = Settings.builder();

            JSONObject pObject = new JSONObject(sDefaults);

            pObject.keySet().forEach(k->{
                b.put(k, pObject.getInt(k));
            });

            request.settings(b);

            try{
                CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
                return createIndexResponse.index().toString();
            }
            catch (Exception e){
                System.out.println(e.getMessage());
            }

            return "";

    }

    public String insert(JSONObject pObject) throws IOException {
        return insert(setProperties(pObject).toString());
    }


    private String insert(String sJSON) throws IOException {

        IndexRequest request = new IndexRequest(getProperty("cluster.index"));

        request.source(sJSON, XContentType.JSON);

        return request.id();
    }

    private String updateBulkSingle(String sJSON, String sID) throws IOException {
//        BulkRequestBuilder bulkRequest = client.prepareBulk();
//
//        bulkRequest.add(client.prepareUpdate(getProperty("cluster.index"), "_doc", sID)
//                .setDoc(sJSON, XContentType.JSON));
//
//        BulkResponse responses = bulkRequest.get();
//
//        return responses.getItems()[0].getId();
        return "";
    }

    public String updateBulkSingle(JSONObject pObject, String sID) throws IOException {
        return updateBulkSingle(setProperties(pObject).toString(), sID);
    }

    public String update(JSONObject pJSON, String sID) throws IOException {
        JSONObject nObject = setProperties(pJSON);
        return update(nObject.toString(), sID);
    }

    private String update(String sJSON, String sID) throws IOException {
//        UpdateResponse response = client.prepareUpdate(getProperty("cluster.index"), "_doc", sID)
//                .setDoc(sJSON, XContentType.JSON).get();
//
//        return response.getId();
        return "";
    }


    public String get(String sID) throws IOException {

        GetRequest getRequest = new GetRequest(
                "_doc",
                sID);

        return getRequest.fetchSourceContext().toString();
    }

    // on shutdown
    public void onClose() throws IOException {
        this.client.close();
    }


}
