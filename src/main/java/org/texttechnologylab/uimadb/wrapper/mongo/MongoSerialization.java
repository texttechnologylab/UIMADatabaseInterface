package org.texttechnologylab.uimadb.wrapper.mongo;

import org.apache.uima.jcas.JCas;
import org.json.JSONException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.CasSerializerMetaFactory;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.ICasSerializer;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.CasSerializationException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.SerializerInitializationException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.UnknownFactoryException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.json.CasJsonSerializerFactory;

/**
 * Created by abrami on 19.06.17.
 */
public class MongoSerialization {

    public static String serializeJCas(JCas pInput, boolean noID) throws UnknownFactoryException, SerializerInitializationException, CasSerializationException, JSONException {

        ICasSerializer serializer = CasSerializerMetaFactory.Instance().getFactory(getSerializerFactory()).createSerializer();
        String s = serializer.serialize(pInput.getCas());

        return s;
//        String sID = "";

//        for(StolperwegeElement pElement : JCasUtil.select(pInput, StolperwegeElement.class)){
//            if(pElement.getId()!=null){
//                sID = pElement.getId();
//            }
//
//        }
//
//        String rString = "";
//
//        JSONObject rObject = new JSONObject(s);
//        if(sID.length()>0 && !noID) {
//            rObject.put("_id", sID);
//        }
//        rString = rObject.toString();

//        return rString;

    }

    public static String serializeJCas(JCas pInput) throws UnknownFactoryException, SerializerInitializationException, CasSerializationException, JSONException {

        return serializeJCas(pInput, true);
    }

    public static JCas deserializeJCas(JCas pTarget, String sInput) throws UnknownFactoryException, SerializerInitializationException, CasSerializationException, JSONException {

        ICasSerializer serializer = CasSerializerMetaFactory.Instance().getFactory(getSerializerFactory()).createSerializer();

        serializer.deserialize(pTarget.getCas(), sInput);

        JCas rCas = pTarget;

        return rCas;
//
//        JSONObject jObject = new JSONObject(sInput);
////        //System.out.println(jObject.getJSONObject("_id").getString("$oid"));
//        String sID = "";
//        if(jObject.has("_id")) {
//            sID = jObject.getJSONObject("_id").getString("$oid");
//            jObject.remove("_id");
//        }
//
//        serializer.deserialize(rCas.getCas(), jObject.toString());
//
//        for(StolperwegeElement pElement : JCasUtil.select(rCas, StolperwegeElement.class)){
//            if(sID.length()>0) {
//                pElement.setId(sID);
//            }
//        }
//
//        return rCas;

    }

    public static String getSerializerFactory() {
        return CasJsonSerializerFactory.class.getName();
    }



}
