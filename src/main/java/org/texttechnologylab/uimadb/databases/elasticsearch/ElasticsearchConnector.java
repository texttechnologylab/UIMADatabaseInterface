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
import org.apache.uima.UIMAException;
import org.apache.uima.cas.CAS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.CasIOUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateAction;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.seqno.RetentionLeaseActions;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.texttechnologylab.uimadb.UIMADatabaseInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

public class ElasticsearchConnector {

    File pConfigFile=null;
    Properties pProperties = null;
    RestHighLevelClient client = null;


    public ElasticsearchConnector(File pConfigFile) throws IOException {
        this.pConfigFile=pConfigFile;
        init();
    }

//    public ElasticsearchConnector(File pConfigFile, String sIndexName) throws IOException {
//        this.pConfigFile=pConfigFile;
//        pProperties.setProperty("cluster.index", sIndexName);
//    }

    private JSONObject setProperties(JSONObject pObject) throws IOException {

        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        {
            pObject.keySet().forEach(k->{
                try {
                    builder.field(k, pObject.get(k).toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        }
        builder.endObject();

        return pObject;

    }

    public String getProperty(String sProperty){
        return pProperties.getProperty(sProperty);
    }

    public void init() throws IOException {

        pProperties = new Properties();
        try {
            UIMADatabaseInterface.getLogger().debug("Elasticsearch: Get Properties from "+pConfigFile.getName());
            System.out.println("init():  Elasticsearch: Get Propertoties from "+pConfigFile.getName());
            pProperties.load(new FileInputStream(pConfigFile));

        } catch (IOException e) {
            UIMADatabaseInterface.getErrorLogger().error(e.getMessage(), e);
        }


        Settings settings = Settings.builder()
                .put("cluster.name", getProperty("cluster.name")).build();

        this.client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(getProperty("cluster.host"), Integer.parseInt(getProperty("cluster.port")), "http")));

    }


    private String createIndex(String sIndex, String sDefaults) throws IOException {

        CreateIndexRequest request = new CreateIndexRequest(sIndex);

        request.source(
                "{\n" +
                        "  \"mappings\": {\n" +
                        "    \"date_detection\": false,\n" +
                        "    \"numeric_detection\": false,\n" +
                        "   \"dynamic_templates\" : \n[" +
                        "{" +
                        "   \"integers\" : {\n" +
                        "   \"match_mapping_type\": \"double\",\n" +
                        "	    \"mapping\": {\n" +
                        "       \"type\": \"text\" }}\n" +
                        "  }, \n" +
                        "{" +
                        "   \"floats\" : {\n" +
                        "   \"match_mapping_type\": \"long\",\n" +
                        "	    \"mapping\": {\n" +
                        "       \"type\": \"text\" }}}]\n" +
                        "  }, \n" +
                        "  \"settings\": {\n" +
                        "    \"index.mapping.total_fields.limit\": 100000\n" +
                        "  }\n" +
                        "}"
                , XContentType.JSON);

        Settings.Builder b = Settings.builder();

            JSONObject pObject = new JSONObject(sDefaults);

            pObject.keySet().forEach(k->{
                b.put(k, pObject.getInt(k));
            });

            request.settings(b);

            try{
                CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
                return createIndexResponse.index();
            }
            catch (Exception e){
                System.out.println("Index already exists");
            }

            return "";
    }

    public String insert(JSONObject pObject) throws IOException {
        return insert(setProperties(pObject).toString());
    }


    private String insert(String sJSON) throws IOException {

        try {
            JCas json = UIMADatabaseInterface.deserializeJCas(sJSON);
            String indexID = UIMADatabaseInterface.getID(json);

            createIndex(indexID, setProperties(new JSONObject()).toString());

            IndexRequest request = new IndexRequest();
            request.index(indexID);
            request.id(indexID);
            request.source(sJSON, XContentType.JSON);

            return indexID;
        } catch (UIMAException e) {
            e.printStackTrace();
        }

        return null;
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

        try {
            DeleteIndexRequest deleteRequest = new DeleteIndexRequest(sID);
            client.indices().delete(deleteRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            System.out.println("New document");
        }

        createIndex(sID, setProperties(new JSONObject()).toString());

        IndexRequest request = new IndexRequest();
        request.index(sID);
        request.id(sID);
        request.source(sJSON, XContentType.JSON);

        IndexResponse response = client.index(request, RequestOptions.DEFAULT);

        return response.getIndex();
    }


    public String get(String sID) throws IOException {

        GetRequest getRequest = new GetRequest(sID, sID);

        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);

        return getResponse.getSourceAsString();
    }

    public String query(String query) throws IOException {

        SearchRequest searchRequest = new SearchRequest();
        QueryBuilder qb = queryStringQuery(query);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(qb);

        searchRequest.source(searchSourceBuilder);

        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        return query;
    }

    public void delete(String sID) throws IOException {

        DeleteIndexRequest deleteRequest = new DeleteIndexRequest(sID);
        client.indices().delete(deleteRequest, RequestOptions.DEFAULT);

    }
    // on shutdown
    public void onClose() throws IOException {
        this.client.close();
    }


}
