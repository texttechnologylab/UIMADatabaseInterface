package org.texttechnologylab.uimadb.databases.neo4j;

/*
 * Copyright 2017
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

import com.vividsolutions.jts.geom.Coordinate;
import org.apache.commons.collections.KeyValue;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.graphdb.*;
import org.texttechnologylab.uimadb.UIMADatabaseInterface;
import org.texttechnologylab.uimadb.UIMADatabaseInterfaceService;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.CasSerializationException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.SerializerInitializationException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.UnknownFactoryException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the UIMADatabaseInterfaceService to use a Neo4J instance.
 */
public class Neo4J implements UIMADatabaseInterfaceService {

    public static Neo4JConnector connector = null;

    public static Map<String, String> properties = new HashMap<>(0);
    public static Map<String, String> relations = new HashMap<>(0);

    public Neo4J(String sNeo4JConfigFile){
        this(new File(sNeo4JConfigFile));
    }

    public Neo4J(File pNeo4JConfigFile){
        connector = new Neo4JConnector(pNeo4JConfigFile);
        init();
    }

    public static Label getLabel(Type pType){
        return getLabel(pType.getName());
    }

    public static Label getLabel(String pString){
        Label pLabel = Label.label(pString);
        return pLabel;
    }

    /**
     *  Internal initialization to generate the indexes and to determine the primitive and complex data types based on the embedded type system descriptors.
     */
    public void init(){

        try {
            JCas pJCas = JCasFactory.createJCas();

            pJCas.getTypeSystem().getTypeIterator().forEachRemaining(t->{

                Label pLabel = getLabel(t);

                t.getFeatures().forEach(f->{

                    if(f.getRange().isPrimitive()){
                        this.connector.createIndex(pLabel, f.getShortName());
                        properties.put(f.getShortName(), f.getName());
                    }
                    if(f.getRange().isArray() && !f.getRange().isPrimitive()){
                        relations.put(f.getShortName(), f.getName());

                    }

                });

                if(!t.toString().endsWith("[]") && !t.toString().contains("uima.cas.")) {
                    this.connector.createIndex(pLabel, "type");
                }

            });

        } catch (UIMAException e) {
            e.printStackTrace();
        }


    }


    private String getType(JCas jCas){

        String sType = "";

            sType = jCas.getSofa().getType().getName();

        return sType;

    }

    @Override
    public String createElement(JCas jCas) throws UIMAException, JSONException {
        NodeTemplate nt = NodeTemplate_Impl.create(getType(jCas), connector);
        return String.valueOf(nt.getID());

    }

    @Override
    public JCas createDummy(JCas pCas) throws UIMAException, JSONException {
        NodeTemplate nt = NodeTemplate_Impl.create(this.connector);
            pCas.createView(UIMADatabaseInterface.UIMADBID).setDocumentText(nt.getID());
        return pCas;
    }

    @Override
    public String createDummy() throws UIMAException, JSONException {
        NodeTemplate nt = NodeTemplate_Impl.create(this.connector);
        return String.valueOf(nt.getID());
    }

    @Override
    public String createElement(JCas jCas, JSONArray pAttributes) throws UIMAException, JSONException {
        String sString = UIMADatabaseInterface.serializeJCas(jCas, pAttributes);
        NodeTemplate t = NodeTemplate_Impl.create(getType(jCas), this.connector);
        t.update(sString);

        return String.valueOf(t.getID());

    }

    @Override
    public void updateElement(JCas pJCas, JSONArray pAttributes) throws SerializerInitializationException, CasSerializationException, UnknownFactoryException {

        updateElement(pJCas, pAttributes, false);

    }

    @Override
    public void updateElement(JCas pJCas, JSONArray pAttributes, boolean bCompressed) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException {
        String id = UIMADatabaseInterface.getID(pJCas);

        try {
            String sString = UIMADatabaseInterface.serializeJCas(pJCas, pAttributes);
            NodeTemplate t = NodeTemplate_Impl.getNode(id, this.connector);
            t.update(sString);

        } catch (UnknownFactoryException e) {
            e.printStackTrace();
        } catch (SerializerInitializationException e) {
            e.printStackTrace();
        } catch (CasSerializationException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateElement(JCas pJCas) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException {
        String id = UIMADatabaseInterface.getID(pJCas);

        NodeTemplate t = NodeTemplate_Impl.getNode(id, this.connector);
        t.update(pJCas);

    }

    @Override
    public JCas getElement(String sID) {

        NodeTemplate t = NodeTemplate_Impl.getNode(sID, this.connector);
        try {
            return t.getJCas();
        } catch (UIMAException e) {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    public long getSize(String sID) throws IOException {
        return -1l;
    }

    @Override
    public Set<JCas> getElements(String sQuery) {

        Set<JCas> rCasSet = new HashSet<>(0);
        try {
        JSONObject queryObject = new JSONObject(sQuery);;


        Map<String, Object> queryMap = new HashMap<String, Object>();

        queryObject.keys().forEachRemaining(key->{

            String[] sString = ((String)key).split("\\.");

            if(sString.length==2){
                if(sString[0].equalsIgnoreCase("meta")){
                    try {
                        queryMap.put(sString[1], queryObject.get((String)key));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        });


        Set<NodeTemplate> nSet = new HashSet<>();
        Set<Node> pNodeSet = null;


        Map<String, Object> filterMap = new HashMap<>(0);

        if(queryMap.containsKey("type")) {

            String type = "";
            for (String key : queryMap.keySet()) {
                if (key.equalsIgnoreCase("type")) {
                    type = (String) queryMap.get("type");
                } else {
                    filterMap.put(key, queryMap.get(key));
                }
            }

            if (sQuery.contains("$regex")) {

                String cypherQuery = "MATCH (n) WHERE n.type = '" + type + "' AND ";


                for (String s : filterMap.keySet()) {

                    Object tObject = filterMap.get(s);

                    if (tObject instanceof JSONObject) {

                        JSONObject sObject = (JSONObject) tObject;

                        if (sObject.toString().contains("$regex")) {

                            JSONObject regexObject = sObject;
                            Iterator<String> regIt = regexObject.keys();

                            while(regIt.hasNext()){
                                String s1 = regIt.next();
                                cypherQuery += "( n." + s + " =~ '(?i).*" + regexObject.getString(s1) + "' OR ";
                                cypherQuery += " n." + s + " =~ '(?i)" + regexObject.getString(s1) + ".*' OR ";
                                cypherQuery += " n." + s + " =~ '(?i).*" + regexObject.getString(s1) + ".*' ) AND ";
                            }

                        }

                    } else {
                        cypherQuery += " n." + s + " = " + filterMap.get(s) + " AND ";

                    }

                }
                cypherQuery = cypherQuery.substring(0, cypherQuery.lastIndexOf("AND"));
                cypherQuery += " RETURN n;";

//                System.out.println(cypherQuery);

                try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {
                    Result r = this.connector.executeQuery(cypherQuery);

                    pNodeSet = new HashSet<>(0);
                    while(r.hasNext()){
                        Map<String, Object> tMap = r.next();
                        Node n = (Node)tMap.get("n");
                        if(n!=null) {
                            pNodeSet.add(n);
                        }
                    }
                    tx.success();
                }


            } else {


                if (filterMap.size() == 1) {
                    for (String s : filterMap.keySet()) {
                        if (pNodeSet == null && properties.containsKey(s)) {
                            pNodeSet = this.connector.getNodes(Neo4J.getLabel(type), s, filterMap.get(s));
                            filterMap.remove(s);
                        } else {
                            pNodeSet = this.connector.getNodes(Neo4J.getLabel(type));
                        }
                    }

                } else if (filterMap.size() == 0 && type.length() > 0) {
                    pNodeSet = this.connector.getNodes(Neo4J.getLabel(type));

                }

                for (String s : filterMap.keySet()) {
                    if (pNodeSet == null) {
                        if (!s.equals("type")) {
                            if (properties.containsKey(s)) {
                                pNodeSet = this.connector.getNodes(Neo4J.getLabel(type), s, filterMap.get(s));
                                filterMap.remove(s);
                            } else {
                                pNodeSet = this.connector.getNodes(Neo4J.getLabel(type));
                            }
                        }
                    }

                }
                if (pNodeSet == null) {
                    pNodeSet = new HashSet<>(0);
                }
                if (filterMap.size() > 0) {
                    pNodeSet = pNodeSet.stream().filter(n -> {
                        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {

                            for (String s : filterMap.keySet()) {

                                if (properties.containsKey(s)) {
                                    Object tObject = properties.get(s);
                                    if (n.hasProperty(s)) {
                                        if (n.getProperty(s).equals(filterMap.get(s))) {
                                            return true;
                                        }
                                    }
                                } else if (relations.containsKey(s)) {

                                    if (n.hasRelationship(Direction.OUTGOING, new TTRelationshipType(relations.get(s)))) {

                                        Iterator<Relationship> relIt = n.getRelationships(Direction.BOTH, new TTRelationshipType(relations.get(s))).iterator();
                                        boolean found = false;

                                        while (relIt.hasNext() && !found) {

                                            Relationship r = relIt.next();

                                            if (r.getEndNode().getId() == (long) filterMap.get(s)) {
                                                found = true;
                                            }

                                        }

                                        return found;

                                    }

                                }

                            }
                            tx.success();
                        }
                        return false;

                    }).collect(Collectors.toSet());


                }


            }
        }

        if(pNodeSet!=null){
            pNodeSet.stream().forEach(n -> {

                NodeTemplate_Impl nt = new NodeTemplate_Impl(n, this.connector);

                try {
                    rCasSet.add(nt.getJCas());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rCasSet;

    }

    @Override
    public Set<JCas> getElementsDirect(String sQuery) {
        return getElementsDirect(sQuery, "n");
    }

    @Override
    public Set<JCas> getElementsDirect(String sQuery, String queryValue) {

        Set<JCas> rCasSet = new HashSet<>(0);

        Set<Node> pNodeSet = new HashSet<>(0);

        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {
            Result r = this.connector.executeQuery(sQuery);

            while(r.hasNext()){
                try {
                    Map<String, Object> tMap = r.next();
                    Node n = (Node) tMap.get(queryValue);
                    if (n != null) {
                        if (n.hasProperty("uima")) {
                            pNodeSet.add(n);
                        }
                    }
                }
                catch (NoSuchElementException e){
                    e.printStackTrace();
                }
            }

            pNodeSet.stream().forEach(n -> {

                NodeTemplate_Impl nt = new NodeTemplate_Impl(n, this.connector);

                try {
                    rCasSet.add(nt.getJCas());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            tx.success();
        }

        return rCasSet;

    }

    @Override
    public Set<Object> getElementsDirectObject(String sQuery, String queryValue) {

        Set<Object> rSet = new HashSet<>(0);

        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {
            Result r = this.connector.executeQuery(sQuery);

            while(r.hasNext()){
                try {
                    Map<String, Object> tMap = r.next();
                    Node n = (Node) tMap.get(queryValue);
                    rSet.add(n);
                }
                catch (NoSuchElementException e){
                    e.printStackTrace();
                }
            }
            tx.success();
        }

        return rSet;

    }

    @Override
    public Set<JCas> getElementsWithType(String sSourceObject, String sTargetObject) {
        return null;
    }

    @Override
    public Set<JCas> getElements(KeyValue... kvs) {

        Set<JCas> rCasSet = new HashSet<>(0);

        for (KeyValue kv : kvs) {

        }

        return rCasSet;
    }

    @Override
    public Set<JCas> getElementsByGeoLocation(double lat, double lon, double distance) {

        Set<Node> nSet = this.connector.getNodesFromSpatialLayer(new Coordinate(lon, lat), distance);

        Set<JCas> jSet = new HashSet<>(0);

        nSet.forEach(n->{
            try {
                jSet.add(new NodeTemplate_Impl(n, this.connector).getJCas());
            } catch (UIMAException e) {
                e.printStackTrace();
            }
        });

        return jSet;

    }

    @Override
    public Set<JCas> getElementsByGeoLocation(String sType, double lat, double lon, double distance) {
        Set<Node> nSet = this.connector.getNodesFromSpatialLayer(new Coordinate(lon, lat), distance);

        Set<JCas> jSet = new HashSet<>(0);
        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {

            nSet.stream().filter(f -> {
                if (sType.length() > 0) {
                    if (((String) f.getProperty("type", "")).equalsIgnoreCase(sType)) {
                        return true;
                    } else {
                        return false;
                    }
                }
                return true;
            }).forEach(n -> {
                try {
                    jSet.add(new NodeTemplate_Impl(n, this.connector).getJCas());
                } catch (UIMAException e) {
                    e.printStackTrace();
                }
            });
            tx.success();
        }

        return jSet;
    }


    @Override
    public void deleteElements(String sID) {

        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {

            Node n = Neo4JConnector.gdbs.getNodeById((long)getRealID(sID));

            n.getRelationships(Direction.BOTH).forEach(rel->{
                rel.delete();
            });

            n.delete();

            tx.success();
        }

    }

    @Override
    public Object getRealID(String sID) {
        return Long.valueOf(sID.replace("n", ""));
    }

    @Override
    public void destroy() {
        ((Neo4JConnector)connector).gdbs.shutdown();
    }

    @Override
    public void start() {

    }
}
