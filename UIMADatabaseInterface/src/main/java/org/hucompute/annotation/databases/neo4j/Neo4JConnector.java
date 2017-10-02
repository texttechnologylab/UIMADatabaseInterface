package org.hucompute.annotation.databases.neo4j;

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
import org.hucompute.annotation.UIMADatabaseInterface;
import org.neo4j.gis.spatial.SimplePointLayer;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.pipes.GeoPipeFlow;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.event.ErrorState;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.IndexCreator;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 *  Class to connect to a enbedded Neo4J database. The settings are made from a control file (see neo4j_example.conf)
 */
public class Neo4JConnector implements TransactionEventHandler<Object>, KernelEventHandler {

    public static GraphDatabaseService gdbs = null;
    public static SpatialDatabaseService spatial = null;

    public static final String geoLayer = "geo";

    public static File pConfFile = null;

    public Neo4JConnector(String confFile){
        this(new File(confFile));
    }

    public Neo4JConnector(File confFile){
        this.pConfFile = confFile;
        start();
    }

    /**
     *  Start the Neo4J Database
     */
    public void start(){

        if(gdbs==null){
            instance();
        }

    }

    /**
     * Get a Node by ID.
     *
     * @param pID
     * @return
     */
    public Node getNode(long pID){

        Node rNode = null;

        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {
            try {
                rNode = gdbs.getNodeById(pID);
            }
            catch (NotFoundException e){
                UIMADatabaseInterface.getErrorLogger().error(e.getMessage(), e);
            }
            tx.success();

        }
        return rNode;

    }

    /**
     * Get a Node by its URI.
     *
     * @param sNode
     * @return
     */
    public Node getNode(String sNode){

        if(sNode.contains("/")){
            sNode = sNode.substring(sNode.lastIndexOf("/")+1, sNode.length());
        }
        if(sNode.startsWith("n") || sNode.startsWith("_")){
            sNode = sNode.replace("n", "");
            sNode = sNode.replace("_", "");
        }

        return getNode(Long.valueOf(sNode));

    }

    /**
     * Return the default spatial layer.
     *
     * @return
     */
    public SimplePointLayer getSpatialLayer(){
        return getSpatialLayer(geoLayer);
    }

    /**
     * Return a specific spatial layer.
     *
     * @param sLayer
     * @return
     */
    public SimplePointLayer getSpatialLayer(String sLayer){
        return (SimplePointLayer) spatial.getLayer(sLayer);
    }

    /**
     * Add a node to specific spatial layer.
     *
     * @param sLayer
     * @param pNode
     */
    public void addNodeToSpatialLayer(String sLayer, Node pNode){

        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {
            getSpatialLayer(sLayer).add(pNode);
            tx.success();
        }
    }

    /**
     * Remove a node from a specific spatial layer.
     *
     * @param sLayer
     * @param pNode
     */
    public void removeNodeFromSpatialLayer(String sLayer, Node pNode){

        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {
            getSpatialLayer(sLayer).delete(pNode.getId());
            tx.success();
        }
    }

    /**
     * Add a node to the default spatial layer.
     *
     * @param pNode
     */
    public void addNodeToSpatialLayer(Node pNode){
        addNodeToSpatialLayer(geoLayer, pNode);
    }

    /**
     * Remove a node from the default spatial layer.
     * @param pNode
     */
    public void removeNodeFromSpatialLayer(Node pNode){
        removeNodeFromSpatialLayer(geoLayer, pNode);
    }

    /**
     * Get all nodes from the default spatial layer based on a coordinate.
     *
     * @param pCoordinate
     * @return
     */
    public Set<Node> getNodesFromSpatialLayer(Coordinate pCoordinate){
        return getNodesFromSpatialLayer(pCoordinate, -1d);
    }

    /**
     * Get all nodes from the default spatial layer based on a coordinate and a distance.
     *
     * @param pCoordinate
     * @param dDistance
     * @return
     */
    public Set<Node> getNodesFromSpatialLayer(Coordinate pCoordinate, double dDistance){
        List<GeoPipeFlow> flows = new ArrayList<>(0);
        Set<Node> nodeSet = new HashSet<>(0);

        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {

            if(dDistance>0){
                flows = getSpatialLayer().findClosestPointsTo(pCoordinate, dDistance);
            }
            else{
                flows = getSpatialLayer().findClosestPointsTo(pCoordinate);
            }

            flows.forEach(s->{
                nodeSet.add(getNode(s.getRecord().getNodeId()));
            });
            tx.success();

        }
        return nodeSet;
    }

    /**
     * Return a relationship based on a ID.
     *
     * @param pID
     * @return
     */
    public Relationship getRelationship(long pID) {

        Relationship rRelationship = null;

        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {
            try {
                rRelationship = gdbs.getRelationshipById(pID);
            }
            catch (NotFoundException e){
                e.printStackTrace();
            }
            tx.success();

        }
        return rRelationship;
    }

    /**
     * Get node matching on its label and a given key-value pair.
     *
     * @param label
     * @param sProperty
     * @param object
     * @return
     */
    public Node findNode(Label label, String sProperty, Object object) {

        Node returnNode = null;

        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {

            returnNode = gdbs.findNode(label, sProperty, object);

            tx.success();
        }

        return returnNode;

    }

    /**
     * Get nodes matching on their label and a given key-value pair.
     *
     * @param label
     * @param sProperty
     * @param object
     * @return
     */
    public Set<Node> getNodes(Label label, String sProperty, Object object) {

        Set<Node> returnSet = new HashSet<>(0);

        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {
            returnSet = gdbs.findNodes(label, sProperty, object).stream().collect(Collectors.toSet());

            tx.success();
        }

        return returnSet;

    }

    /**
     * Get nodes based on a label.
     *
     * @param label
     * @return
     */
    public Set<Node> getNodes(Label label) {
        Set<Node> returnSet = new HashSet<>(0);

        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {

            returnSet = gdbs.findNodes(label).stream().collect(Collectors.toSet());

            tx.success();
        }
        return returnSet;
    }

    /**
     * Check if a index exists.
     *
     * @param pLabel
     * @param pType
     * @return
     */
    public boolean indexExists(Label pLabel, String pType){

        boolean rBool = false;

        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {

            Schema schema = Neo4JConnector.gdbs.schema();

            Iterable<IndexDefinition> indexes = schema.getIndexes(pLabel);
            for (IndexDefinition index : indexes) {
                for (String key : index.getPropertyKeys()) {
                    if (key.equals(pType)) {
                        rBool = true; // index for label and property exists
                    }
                }
            }
            tx.success();
        }

        return rBool;

    }

    /**
     * Create a index.
     *
     * @param pLabel
     * @param ptype
     */
    public void createIndex(Label pLabel, String ptype) {

        IndexDefinition indexDefinition;
        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {

            if (!indexExists(pLabel, ptype)) {
                // create index
                UIMADatabaseInterface.getLogger().debug("Creating Index: "+ptype+" on Label: "+pLabel.name());
                System.out.println("Creating Index: "+ptype+" on Label: "+pLabel.name());
                Schema schema = Neo4JConnector.gdbs.schema();

                //schema.constraintFor(pLabel).assertPropertyIsUnique(ptype).create();

                IndexCreator creator = schema.indexFor(pLabel);

                // only one type allowed! bad!
                creator = creator.on(ptype);

                indexDefinition = creator.create();
                    UIMADatabaseInterface.getLogger().debug(String.format("Percent complete: %1.0f%%",
                            schema.getIndexPopulationProgress(indexDefinition).getCompletedPercentage()));

            }

            tx.success();
        }

    }

    /**
     * Execute query.
     *
     * @param sQuery
     * @return
     */
    public Result executeQuery(String sQuery){

        Result rResult = null;
        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {
            rResult = gdbs.execute(sQuery);
            tx.success();
        }
        return rResult;

    }

    /**
     * Deletes a node from the GraphDatabase and cleans up indices
     *
     * @param pNode
     */
    protected void delete(Node pNode) {

        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {
            for (String lIndex : gdbs.index().nodeIndexNames()) {
                gdbs.index().forNodes(lIndex).remove(pNode);
            }
            pNode.delete();
            tx.success();
        }
    }

    /**
     * Deletes a relationship from the GraphDatabase
     *
     * @param pRelationship
     */
    protected void delete(Relationship pRelationship) {
        try (Transaction tx = Neo4JConnector.gdbs.beginTx()) {
            for (String lIndex : gdbs.index().relationshipIndexNames()) {
                gdbs.index().forRelationships(lIndex).remove(pRelationship);
            }
            pRelationship.delete();
            tx.success();
        }
    }

    /**
     * Instance a Graphdatabase
     * @return
     */
    public static GraphDatabaseService instance() {
        //boolean lForceRDFReindexing = false;
        UIMADatabaseInterface.getLogger().debug("Neo4J Conf-File: "+pConfFile.getName());
        System.out.println("Neo4J Conf-File: "+pConfFile.getName());
        if (gdbs == null) {
            Properties lProperties = new Properties();
            try {
                UIMADatabaseInterface.getLogger().debug("Neo4J: Get Properties from "+pConfFile.getName());
                System.out.println("Neo4J: Get Properties from "+pConfFile.getName());
                lProperties.load(new FileInputStream(pConfFile));

            } catch (IOException e) {
                UIMADatabaseInterface.getErrorLogger().error(e.getMessage(), e);
            }
            Map<String, String> conf = new HashMap<>();
            for (String lString : lProperties.stringPropertyNames()) {
                conf.put(lString, (String) lProperties.get(lString));
            }

            UIMADatabaseInterface.getLogger().debug("Start Neo4J");
            System.out.println("Start Neo4J");
            long startTime = System.currentTimeMillis();
            File dbFile = new File(conf.get("db_dir"));
            gdbs = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(dbFile).loadPropertiesFromFile(pConfFile.getAbsolutePath()).newGraphDatabase();
            UIMADatabaseInterface.getLogger().debug("Start Neo4J finish! Time needed: "+(System.currentTimeMillis()-startTime)/1000+" seconds");
            System.out.println("Start Neo4J finitiersh! Time needed: "+(System.currentTimeMillis()-startTime)/1000+" seconds");
            spatial = new SpatialDatabaseService(gdbs);
            try {
                spatial.createSimplePointLayer("geo", "lon", "lat", "bbox_abc");
            }
            catch (Exception e){
                System.out.println(e.getMessage());
            }

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    if (gdbs != null) {
                        long startTime = System.currentTimeMillis();
                        UIMADatabaseInterface.getLogger().debug("\n=== SHUTDOWN MDB ...");
                        System.out.println("\n=== SHUTDOWN MDB ...");
                        gdbs.shutdown();
                        UIMADatabaseInterface.getLogger().debug("... SHUTDOWN finish, after: "+(System.currentTimeMillis() - startTime) / 1000 + " seconds");
                        System.out.println("... SHUTDOWN finish, after: "+(System.currentTimeMillis() - startTime) / 1000 + " seconds");
                    }
                }
            });
        }

        return gdbs;
    }


    @Override
    public void beforeShutdown() {
        UIMADatabaseInterface.getErrorLogger().error("---\tbeforeShutdown. " + new Date());
        System.out.println("---\tbeforeShutdown. " + new Date());

    }

    @Override
    public void kernelPanic(ErrorState errorState) {
        UIMADatabaseInterface.getErrorLogger().error("---\tkernelPanic.\t" + errorState.toString() + '\t' + new Date());
        UIMADatabaseInterface.getLogger().error("---\tkernelPanic.\t" + errorState.toString() + '\t' + new Date());
    }

    @Override
    public Object getResource() {
        return null;
    }

    @Override
    public ExecutionOrder orderComparedTo(KernelEventHandler kernelEventHandler) {
        return ExecutionOrder.DOESNT_MATTER;
    }

    @Override
    public Object beforeCommit(TransactionData transactionData) throws Exception {
        return null;
    }

    @Override
    public void afterCommit(TransactionData transactionData, Object o) {

    }

    @Override
    public void afterRollback(TransactionData transactionData, Object o) {
        UIMADatabaseInterface.getErrorLogger().error("---\tafterRollback.\t" + transactionData.hashCode() + "\ttime:\t" + System.currentTimeMillis());
        UIMADatabaseInterface.getLogger().error("---\tafterRollback.\t" + transactionData.hashCode() + "\ttime:\t" + System.currentTimeMillis());
    }
}
