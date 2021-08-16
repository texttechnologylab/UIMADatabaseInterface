package org.texttechnologylab.uimadb;

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

import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.Feature;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.testing.util.DisableLogging;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.AnnotationBase;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.TOP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.texttechnologylab.uimadb.wrapper.mongo.MongoSerialization;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.CasSerializerMetaFactory;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.ICasSerializer;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.CasSerializationException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.SerializerInitializationException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.UnknownFactoryException;

import java.util.*;

/**
 *  Class for central control of the interface for generic use of UIMA documents.
 */
public class UIMADatabaseInterface {

    private static Logger logger;
    private static Logger errorLogger;

    public static final String UIMADBID = "uimadbid";

    public static Queue<JCas> jCasQueue = new ArrayDeque<>(0);

    public static boolean run = false;

    /**
     * Test for the pre-generation of CAS elements.
     */
    public static void startJCasQueueThread(){

        Runnable threadRunnable = new Runnable() {
            @Override
            public void run() {

                DisableLogging.disableLogging();
                while (run) {

                    if(jCasQueue==null){
                        jCasQueue = new ArrayDeque<>(0);
                    }

                    if (jCasQueue.size() < 100) {
                        try {
                            jCasQueue.add(JCasFactory.createJCas());
                        } catch (Exception e) {
                            if(!(e instanceof NullPointerException)){
                                e.printStackTrace();
                            }

                        }
                    }
                    else{
                        try {
                            Thread.sleep(10);
                        } catch (Exception e) {
                            if(!(e instanceof NullPointerException)){
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        };
        if(!run) {
            Thread t = new Thread(threadRunnable);
            //t.start();
            run = true;
        }
    }

    public static Logger getLogger(){
        if(logger==null){
            logger = Logger.getLogger("Main");
        }
        return logger;
    }

    public static Logger getErrorLogger(){
        if(errorLogger==null){
            errorLogger = Logger.getLogger("Error");
        }
        return errorLogger;
    }

    /**
     * Return a new JCas
     *
     * @return
     */
    public static JCas getJCas(){

        JCas rCas = null;

        if(jCasQueue.size()>0){
            rCas = jCasQueue.poll();
            if(rCas==null){
                try {
                    rCas = JCasFactory.createJCas();

                } catch (UIMAException e) {
                    e.printStackTrace();
                }
            }
        }
        else{
            startJCasQueueThread();

            try {

                rCas = JCasFactory.createJCas();
            } catch (Exception e) {
                if(!(e instanceof NullPointerException)){
                    e.printStackTrace();
                }
            }
        }

        return rCas;

    }

    /**
     * Get the database ID of the UIMA document.
     *
     * @param pObject
     * @return
     */
    public static String getID(AnnotationBase pObject) {

        try {
            return getID(pObject.getCAS().getJCas());
        } catch (CASException e) {
            e.printStackTrace();
        }
        return "";

    }

    /**
     * Get the database ID of the UIMA document.
     *
     * @param pCAS
     * @return
     */
    public static String getID(JCas pCAS) {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(pCAS.getView(UIMADBID).getDocumentText());

        } catch (CASException e) {
            e.printStackTrace();
        }
        return sb.toString();

    }

    /**
     * Get the database ID, not the reference, of the UIMA document.
     *
     * @param pCAS
     * @return
     */
    public static String getRealID(JCas pCAS){

        try {
            return pCAS.getView(UIMADBID).getDocumentText();
        } catch (CASException e) {
            e.printStackTrace();
        }
        return "";

    }

    /**
     * Get the database ID, not the reference, of the UIMA document.
     *
     * @param pObject
     * @return
     */
    public static String getRealID(AnnotationBase pObject) {

        try {
            return getRealID(pObject.getCAS().getJCas());
        } catch (CASException e) {
            e.printStackTrace();
        }
        return "";

    }

    /**
     * Convert a set of JCas elements into a target type system.
     *
     * @param jCasSet
     * @param type
     * @param <T>
     * @return
     */
    public static <T extends TOP> Collection<T> convertList(Set<JCas> jCasSet, Class<T> type) {

        Collection<T> rSet = new HashSet<>(0);

        for(JCas j : jCasSet) {

            Iterator<JCas> cIterator = null;
            try {
                cIterator = j.getViewIterator();

            while (cIterator.hasNext()) {
                JCas view = cIterator.next();

                //JCas view = pCas.getView(UIMADBID);

                if (view != null) {

                    if(!view.getViewName().equalsIgnoreCase(UIMADBID)) {

                        try {
                            rSet.add(JCasUtil.selectSingle(view, type));
                        } catch (Exception ex) {
//                        getErrorLogger().error(ex.getMessage());
                        }
                    }
                }

            }
            } catch (CASException e) {
                e.printStackTrace();
            }


        }

//        for(JCas j : jCasSet){
//
//            j.getSofaIterator().forEachRemaining(s->{
//                if(!s.getSofaID().contains("_")){
//                    JCas pCas = JCasUtil.getView(j, s.getSofaID(), false);
//                    Collection<T> tSet = JCasUtil.select(pCas, type);
//                    if(tSet.size()>0){
//                        rSet.addAll(tSet);
//                    }
//                }
//            });
//
//        }

        return rSet;
    }

    /**
     * Convert a JCas element into a target type system.
     *
     * @param jCas
     * @param type
     * @param <T>
     * @return
     */
    public static <T extends TOP> T convert(JCas jCas, Class<T> type) throws CASException {

        T rType = null;

            Iterator<JCas> cIterator = jCas.getViewIterator();

            while(cIterator.hasNext()){
                JCas pCas = cIterator.next();

//                JCas view = pCas.getView(UIMADBID);

                if(!pCas.getViewName().equalsIgnoreCase(UIMADBID)) {

                    if (pCas != null && rType == null) {
                        try {
                            rType = JCasUtil.selectSingle(pCas, type);
                        } catch (Exception e) {

                        }
                    }
                }

            }

//
//            FSIterator<SofaFS> sofaIterator = jCas.getSofaIterator();
//
//            while(sofaIterator.hasNext()){
//                SofaFS sfs = sofaIterator.next();
//                if(!sfs.getSofaID().contains("_")){
//                    JCas pCas = JCasUtil.getView(jCas, sfs.getSofaID(), false);
//                    rType = JCasUtil.selectSingle(pCas, type);
//                }
//            }

        return rType;
    }

    /**
     * Deserialize a JCas element.
     *
     * @param sInput
     * @return
     * @throws UIMAException
     * @throws JSONException
     */
    public static JCas deserializeJCas(String sInput) throws UIMAException, JSONException {

        ICasSerializer serializer = CasSerializerMetaFactory.Instance().getFactory(MongoSerialization.getSerializerFactory()).createSerializer();

        JCas rCas = getJCas();

        JSONObject pObject = new JSONObject(sInput);

        if(pObject.has("uima")){
            serializer.deserialize(rCas.getCas(), pObject.get("uima").toString());
        }
        else{
            serializer.deserialize(rCas.getCas(), sInput);
        }

        return rCas;

    }

    /**
     * Serialize a JCas Element
     *
     * @param pInput
     * @return
     * @throws UnknownFactoryException
     * @throws SerializerInitializationException
     * @throws CasSerializationException
     * @throws JSONException
     */
    public static String serializeJCas(JCas pInput) throws UnknownFactoryException, SerializerInitializationException, CasSerializationException, JSONException {
        return serializeJCas(pInput, false);
    }

    /**
     * Serialize a JCas Element
     *
     * @param pInput
     * @return
     * @throws UnknownFactoryException
     * @throws SerializerInitializationException
     * @throws CasSerializationException
     * @throws JSONException
     */
    public static String serializeJCas(JCas pInput, boolean bCompress) throws UnknownFactoryException, SerializerInitializationException, CasSerializationException, JSONException {

        JSONArray tArray = new JSONArray();
        tArray.put(new JSONObject().put("geo", new JSONObject()));

        return serializeJCas(pInput, tArray, bCompress);
    }

    public static String serializeJCas(JCas pInput, JSONArray params) throws UnknownFactoryException, SerializerInitializationException, CasSerializationException, JSONException {
        return serializeJCas(pInput, params, false);
    }

    /**
     * Serialize a JCas Element with optional params.
     *
     * @param pInput
     * @param params
     * @return
     * @throws UnknownFactoryException
     * @throws SerializerInitializationException
     * @throws CasSerializationException
     * @throws JSONException
     */
    public static String serializeJCas(JCas pInput, JSONArray params, boolean bCompress) throws UnknownFactoryException, SerializerInitializationException, CasSerializationException, JSONException {

        ICasSerializer serializer = CasSerializerMetaFactory.Instance().getFactory(MongoSerialization.getSerializerFactory()).createSerializer();
        String s = serializer.serialize(pInput.getCas(), bCompress);

        JSONObject rObject = new JSONObject();

        // creating metadata

//        AnnotationBase pBase = JCasUtil.selectSingle(pInput, AnnotationBase.class);
//
//        pBase.getView().getAnnotationIndex().forEach(t->{
//            System.out.println(t);
//        });

        for(int a=0; a<params.length(); a++){

            JSONObject internal = params.getJSONObject(a);

            internal.keys().forEachRemaining(k->{
                JSONObject tObject = null;
                try {
                    tObject = internal.getJSONObject((String)k);
                    rObject.put((String)k, tObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });

        }

        rObject.put("meta", getMetaInformation(pInput));

        // original data
        rObject.put("uima", new JSONObject(s));
        //rObject.put("uima", new JSONObject())

        s = rObject.toString();

        return s;

    }

    /**
     * Extract meta informations of a JCas.
     *
     * @param pJCas
     * @return
     */
    public static JSONObject getMetaInformation(JCas pJCas){

        JSONObject rObject = new JSONObject();

        Collection<AnnotationBase> annotations = JCasUtil.select(pJCas, AnnotationBase.class);

        annotations.forEach(anno->{

            final List<Feature> fList = new ArrayList<>();
            fList.clear();
            if(anno.getType()!=null){
                fList.addAll(anno.getType().getFeatures());

            }
            /*
            if(anno.getCASImpl().getAnnotationIndex().size()==0){
                fList.addAll(anno.getType().getFeatures());
            }

            if(anno.getCASImpl().getAnnotationIndex().size()>0){

                anno.getCASImpl().getAnnotationIndex().forEach(ai->{

                    if(!ai.getType().equals(Sofa_Type.class)){
                       fList.addAll(ai.getType().getFeatures());
                    }

                });

            }*/

            fList.forEach(f->{

                    Object oValue = null;
                    if(f.getRange().getName().endsWith("[]")){
                        oValue = anno.getFeatureValue(f);
                        if(oValue!=null) {
                            FSArray pArray = (FSArray) oValue;
                            JSONArray tArray = new JSONArray();
                            for (int a = 0; a < pArray.size(); a++) {
                                if (pArray.get(a).getType().isPrimitive()) {
                                    tArray.put(pArray.get(a));
                                } else {
                                    String sID = "";
                                    if(pArray.get(a) instanceof AnnotationBase){
                                        sID = UIMADatabaseInterface.getID(((AnnotationBase)pArray.get(a)));
                                        if(sID.equals("_InitialView")){
                                           sID="";
                                        }
                                    }
                                    else if(pArray.get(a) instanceof TOP){
                                        try {
                                            sID = UIMADatabaseInterface.getID(((TOP)pArray.get(a)).getCAS().getJCas());
                                        } catch (CASException e) {
                                            e.printStackTrace();
                                        }
                                        if(sID.equals("_InitialView")){
                                            sID="";
                                        }
                                    }
                                    if(sID.length()>0) {
                                        tArray.put(sID);
                                    }
                                }
                            }

                            oValue = tArray;
                        }
                    }
                    else {
                        try {
                            switch (f.getRange().getName()) {
                                case "uima.cas.Integer":
                                    oValue = anno.getIntValue(f);
                                    break;

                                case "uima.cas.String":
                                    oValue = anno.getStringValue(f);
                                    break;

                                case "uima.cas.Float":
                                    oValue = anno.getFloatValue(f);
                                    break;

                                case "uima.cas.Boolean":
                                    oValue = anno.getBooleanValue(f);
                                    break;

                                case "uima.cas.Double":
                                    oValue = anno.getDoubleValue(f);
                                    break;

                                case "uima.cas.Long":
                                    oValue = anno.getLongValue(f);
                                    break;

                                default:

                                    oValue = anno.getFeatureValue(f);

                                    if (oValue != null) {
                                        if (oValue instanceof AnnotationBase) {
                                            if (!((AnnotationBase) oValue).getView().getView(UIMADBID).getDocumentText().equals(pJCas.getView(UIMADBID).getDocumentText())) {
                                                oValue = UIMADatabaseInterface.getRealID(((AnnotationBase) oValue));
                                            }
                                        } else if ((!anno.getFeatureValue(f).getCAS().getView(UIMADBID).getDocumentText().equals(pJCas.getView(UIMADBID).getDocumentText()))) {
                                            //                                        if(!ai.getFeatureValue(f).getCAS().getSofa().getSofaID().equals(pJCas.getSofa().getSofaID())) {
                                            oValue = UIMADatabaseInterface.getRealID(anno.getFeatureValue(f).getCAS().getJCas());
                                        } else {
                                            oValue = null;
                                        }


                                    }

                            }

                        }
                        catch (Exception e){

                        }
                    }


                    if(!f.getShortName().equalsIgnoreCase("sofa")) {
                        if (oValue != null) {
                            try {
                                rObject.put(f.getShortName(), oValue);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                });
            try {
                rObject.put("id", UIMADatabaseInterface.getID(pJCas));
                rObject.put("type", anno.getType());
            } catch (JSONException e) {
                e.printStackTrace();
            }


            });

        return rObject;

    }

}
