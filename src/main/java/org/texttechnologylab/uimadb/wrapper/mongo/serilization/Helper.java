package org.texttechnologylab.uimadb.wrapper.mongo.serilization;

import org.apache.uima.jcas.JCas;
import org.json.JSONException;
import org.json.JSONObject;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.CasSerializationException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.SerializerInitializationException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.UnknownFactoryException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.json.CasJsonSerializerFactory;

/**
 * Created by abrami on 01.06.17.
 */
public class Helper {


    public static String serializeJCas(JCas pInput, boolean noID) throws UnknownFactoryException, SerializerInitializationException, CasSerializationException, JSONException {

        ICasSerializer serializer = CasSerializerMetaFactory.Instance().getFactory(Helper.getSerializerFactory()).createSerializer();
        String s = serializer.serialize(pInput.getCas());

        return s;

    }

    public static String serializeJCas(JCas pInput) throws UnknownFactoryException, SerializerInitializationException, CasSerializationException, JSONException {

        return serializeJCas(pInput, true);
    }

    public static JCas deserializeJCas(JCas pTarget, String sInput) throws UnknownFactoryException, SerializerInitializationException, CasSerializationException, JSONException {

        ICasSerializer serializer = CasSerializerMetaFactory.Instance().getFactory(Helper.getSerializerFactory()).createSerializer();

        JCas rCas = pTarget;

        JSONObject jObject = new JSONObject(sInput);

        String sID = "";
        if(jObject.has("_id")) {
            sID = jObject.getJSONObject("_id").getString("$oid");
            jObject.remove("_id");
        }

        serializer.deserialize(rCas.getCas(), jObject.toString());

        return rCas;


    }

    public static String getSerializerFactory() {
        return CasJsonSerializerFactory.class.getName();
    }


}
