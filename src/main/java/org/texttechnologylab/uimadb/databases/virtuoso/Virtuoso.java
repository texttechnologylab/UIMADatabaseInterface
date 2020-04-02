package org.texttechnologylab.uimadb.databases.virtuoso;

import org.apache.commons.collections.KeyValue;
import org.apache.uima.UIMAException;
import org.apache.uima.jcas.JCas;
import org.json.JSONArray;
import org.json.JSONException;
import org.texttechnologylab.uimadb.UIMADatabaseInterfaceService;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.CasSerializationException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.SerializerInitializationException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.UnknownFactoryException;

import java.io.IOException;
import java.util.Set;

public class Virtuoso implements UIMADatabaseInterfaceService {

    public Virtuoso(String sServer, String sUser, String sPass){



    }

    @Override
    public String createElement(JCas jCas) throws UIMAException, JSONException {
        return null;
    }

    @Override
    public JCas createDummy(JCas pCas) throws UIMAException, JSONException {
        return null;
    }

    @Override
    public String createDummy() throws UIMAException, JSONException {
        return null;
    }

    @Override
    public String createElement(JCas jCas, JSONArray pAttributes) throws UIMAException, JSONException {
        return null;
    }

    @Override
    public void updateElement(JCas pJCas, JSONArray pAttributes) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException {

    }

    @Override
    public void updateElement(JCas pJCas, JSONArray pAttributes, boolean bCompressed) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException {

    }

    @Override
    public void updateElement(JCas pJCas) throws CasSerializationException, SerializerInitializationException, UnknownFactoryException, IOException {

    }

    @Override
    public JCas getElement(String sID) throws IOException {
        return null;
    }

    @Override
    public long getSize(String sID) throws IOException {
        return 0;
    }

    @Override
    public Set<JCas> getElements(String sQuery) {
        return null;
    }

    @Override
    public Set<JCas> getElementsDirect(String sQuery) {
        return null;
    }

    @Override
    public Set<JCas> getElementsDirect(String sQuery, String queryValue) {
        return null;
    }

    @Override
    public Set<Object> getElementsDirectObject(String sQuery, String queryValue) {
        return null;
    }

    @Override
    public Set<JCas> getElementsWithType(String sSourceObject, String sTargetObject) {
        return null;
    }

    @Override
    public Set<JCas> getElements(KeyValue... kvs) {
        return null;
    }

    @Override
    public Set<JCas> getElementsByGeoLocation(double lat, double lon, double distance) {
        return null;
    }

    @Override
    public Set<JCas> getElementsByGeoLocation(String sType, double lat, double lon, double distance) {
        return null;
    }

    @Override
    public void deleteElements(String sID) {

    }

    @Override
    public Object getRealID(String sID) {
        return null;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void start() {

    }
}
