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
import org.apache.uima.UIMAException;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.JCas;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.graphdb.*;
import org.texttechnologylab.uimadb.UIMADatabaseInterface;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.CasSerializationException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.SerializerInitializationException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.UnknownFactoryException;
import org.texttechnologylab.utilities.helper.ArrayUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *  Implementation of a UIMA node in Neo4J
 */
public class NodeTemplate_Impl implements NodeTemplate {

    /**
     * Defining Variables
     */
    private Node pNode = null;
    private Neo4JConnector pConnector = null;

    /**
     *  Constants for the Node
     */
    public static final String UIMA = "uima";
    public static final String GEO = "geo";

    public NodeTemplate_Impl(Node pNode, Neo4JConnector pConnector){
        this.pNode = pNode;
        this.pConnector = pConnector;
    }

    public static NodeTemplate getNode(String sNode, Neo4JConnector pConnector){

        NodeTemplate rNode = null;
        if(sNode.contains("/")){
            sNode = sNode.substring(sNode.lastIndexOf("/")+1, sNode.length());
        }
        if(sNode.startsWith("n") || sNode.startsWith("_")){
            sNode = sNode.replace("n", "");
            sNode = sNode.replace("_", "");
        }

        Node pNode = pConnector.getNode(Long.valueOf(sNode));

        rNode = new NodeTemplate_Impl(pNode, pConnector);

        return rNode;

    }

    @Override
    public void update(JCas pJCas){

        try {
            update(UIMADatabaseInterface.serializeJCas(pJCas));
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
    public void update(String sUpdate){

        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {

//            this.getNode().getPropertyKeys().forEach(k->{
//                this.getNode().removeProperty(k);
//            });

            this.getNode().getRelationships(Direction.OUTGOING).forEach(r->{
                r.delete();
            });

//            this.pConnector.removeNodeFromSpatialLayer(this.getNode());



            String sDeserialized = sUpdate;
            JSONObject deCas = new JSONObject(sDeserialized);

            if (deCas.has("geo")) {

                JSONObject geo = deCas.getJSONObject("geo");

                if(geo.has("coordinates")){
                    Double dLon = deCas.getJSONObject("geo").getJSONArray("coordinates").getDouble(0);
                    Double dLat = deCas.getJSONObject("geo").getJSONArray("coordinates").getDouble(1);

                    Coordinate pCoordinate = new Coordinate(Double.valueOf(dLon), Double.valueOf(dLat));
                    this.setGeoPoints(pCoordinate);
                }

            }

            if (deCas.has("meta")) {

                JSONObject internalObject = deCas.getJSONObject("meta");

                internalObject.keys().forEachRemaining(key -> {

                    Object jObject = null;
                    try {
                        jObject = internalObject.get((String)key);


                    if (jObject instanceof JSONArray) {

                        JSONArray idArray = internalObject.getJSONArray((String)key);

                        for(int a=0; a<idArray.length(); a++){
                            String lid = idArray.getString(a);

                            NodeTemplate tNode = new NodeTemplate_Impl(pConnector.getNode(lid), pConnector);
                            if(tNode!=null) {
                                addRelationship((String) key, tNode);
                            }
                        }

                    } else {
                        if(key.equals("type")){
                            setProperty((String)key, internalObject.get((String)key));
                            this.pNode.addLabel(Neo4J.getLabel(internalObject.getString((String)key)));
                        }

                        if(Neo4J.properties.containsKey(key)){
                            setProperty((String)key, internalObject.get((String)key));
                        }
                        else if(Neo4J.relations.containsKey(key)){
                            String lid = internalObject.getString((String)key);
                            NodeTemplate tNode = new NodeTemplate_Impl(pConnector.getNode(lid), pConnector);
                            addRelationship((String)key, tNode);
                        }



                    }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }

            if (deCas.has("uima")) {
                String sInput = deCas.getJSONObject("uima").toString();
                setProperty(UIMA, sInput);
            }


            tx.success();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static NodeTemplate create(String sLabel, Neo4JConnector pConnector){

        NodeTemplate tTemplate = null;

            Label pLabel = Neo4J.getLabel(sLabel);

            if(pLabel!=null){
                try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {
                    tTemplate = new NodeTemplate_Impl(Neo4JConnector.gdbs.createNode(pLabel), pConnector);
                    tx.success();
                }
            }

        return tTemplate;

    }

    public static NodeTemplate create(Neo4JConnector pConnector){

        NodeTemplate tTemplate = null;

        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {
            tTemplate = new NodeTemplate_Impl(Neo4JConnector.gdbs.createNode(), pConnector);
            tx.success();
        }

        return tTemplate;

    }

    @Override
    public String getID() {
        long rLong = -1;

        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {
            rLong = this.pNode.getId();
            tx.success();
        }

        return "n"+rLong;
    }

    @Override
    public Node getNode() {
        return this.pNode;
    }

    @Override
    public JCas getJCas() throws UIMAException {

        String uima = (String)getProperty(UIMA);

        JCas rCas = null;
        try {
            rCas = UIMADatabaseInterface.deserializeJCas(uima);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return rCas;

    }

    @Override
    public Set<Relationship> getRelationships() {

        Set<Relationship> relationshipSet = new HashSet<>(0);

        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {

            this.pNode.getRelationships(Direction.OUTGOING).forEach(r->{
                relationshipSet.add(r);
            });

            tx.success();
        }

        return relationshipSet;

    }

    @Override
    public void setGeoPoints(Coordinate pCoordinate) {

        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {
            pNode.setProperty("lat", pCoordinate.getOrdinate(Coordinate.Y));
            pNode.setProperty("lon", pCoordinate.getOrdinate(Coordinate.X));
            tx.success();
        }

        pConnector.addNodeToSpatialLayer(this.pNode);


    }

    @Override
    public void setGeoPoints(double lat, double lon) {
        this.setGeoPoints(new Coordinate(lon, lat));
    }

    @Override
    public Set<NodeTemplate> getNodesByFeature(Feature pFeature) {

        Set<NodeTemplate> templateNodesSet = new HashSet<>(0);

        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {

            this.pNode.getRelationships(Direction.OUTGOING, getRelationshipType(pFeature)).forEach(r->{
                templateNodesSet.add(new NodeTemplate_Impl(r.getEndNode(), this.pConnector));
            });

            tx.success();
        }

        return templateNodesSet;

    }

    @Override
    public Relationship addRelationship(Feature pFeature, Node pNode) {
        return addRelationship(pFeature.getName(), pNode);
    }

    @Override
    public Relationship addRelationship(String sFeature, Node pNode) {

        Relationship rRelationship = null;

        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {

            Iterable<Relationship> rShips = this.pNode.getRelationships(Direction.BOTH, new TTRelationshipType(sFeature));

            Iterator<Relationship> rIt = rShips.iterator();

            if(!pNode.equals(this.pNode)){

                boolean found = false;

                while(rIt.hasNext()){
                    Relationship tRel = rIt.next();

                    if(tRel.getEndNode().equals(pNode)){
                        found = true;
                    }
                }

                if(!found){
                    this.pNode.createRelationshipTo(pNode, new TTRelationshipType(sFeature));
                }

            }

            tx.success();
        }

        return rRelationship;

    }

    @Override
    public Relationship addRelationship(String sFeature, NodeTemplate pNode) {
        return this.addRelationship(sFeature, pNode.getNode());
    }

    private RelationshipType getRelationshipType(Feature pFeature){
        RelationshipType rType = new TTRelationshipType(pFeature.getName());
        return rType;
    }

    @Override
    public Set<JCas> getJCasByFeature(Feature pFeature) {
        return null;
    }

    @Override
    public void setProperty(String sPropertyName, Object pObject, boolean bRemove) {
        if(bRemove){
            removeProperty(sPropertyName, pObject);
        }
        else{

            try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {
                this.pNode.setProperty(sPropertyName, pObject);
                tx.success();
            }

        }
    }

    @Override
    public void setProperty(String sPropertyName, Object pObject) {
        setProperty(sPropertyName, pObject, false);
    }

    @Override
    public void addProperty(String sPropertyName, Object pObject, boolean bRemove) {

    }

    @Override
    public void initProperty(String sPropertyName, Object pObject, boolean bForce) {
        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {

            if(bForce){
                pNode.setProperty(sPropertyName, pObject);
            }
            else{
                if(!pNode.hasProperty(sPropertyName)){
                    pNode.setProperty(sPropertyName, pObject);
                }
            }
            tx.success();
        }
    }

    @Override
    public void removeProperty(String sPropertyName, Object pObject) {
        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {

            Object oldObject = null;

            if(pNode.hasProperty(sPropertyName)){
                oldObject = pNode.getProperty(sPropertyName);

                if(oldObject instanceof Object[]){

                    Set<Object> oSet = ArrayUtils.objectArrayToSet((Object[])oldObject);

                    oSet = oSet.parallelStream().filter(o->{
                        return !pObject.equals(o);
                    }).collect(Collectors.toSet());

                    Object[] nSet = ArrayUtils.objectSetToArray(oSet);

                    pNode.setProperty(sPropertyName, nSet);

                }
                else{
                    pNode.removeProperty(sPropertyName);
                }

            }

            tx.success();
        }
    }


    @Override
    public void removeProperty(String sPropertyName) {
        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {
            pNode.removeProperty(sPropertyName);
            tx.success();
        }
    }

    @Override
    public boolean hasProperty(String sPropertyName) {
        boolean rBool = false;

        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {
            rBool = pNode.hasProperty(sPropertyName);
            tx.success();
        }

        return rBool;
    }

    @Override
    public Object getProperty(String sPropertyName) {
        return getProperty(sPropertyName, "");
    }

    @Override
    public Object getProperty(String sPropertyName, Object defaultResult) {
        Object rObject = null;

        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {
            rObject = pNode.getProperty(sPropertyName, defaultResult);
            tx.success();
        }

        return rObject;
    }


}
