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

import org.apache.uima.UIMAException;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.AnnotationBase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for analyzing the embedded type system descriptors.
 */
public class Typesystem {

    public static JSONArray get(){

        JSONArray rArray = new JSONArray();

        AnnotationBase a = new AnnotationBase(UIMADatabaseInterface.getJCas());
        TypeSystem t = a.getCAS().getTypeSystem();
        t.getProperlySubsumedTypes(a.getType()).forEach(st->{

            JSONObject nObject = new JSONObject();

            try {
                nObject.put("id", st);
                nObject.put("definition", Typesystem.definitionToJSON(st.getName()));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (UIMAException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            rArray.put(nObject);
        });

        return rArray;
    }

    public static AnnotationBase getElement(String sClass) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor c = Class.forName(sClass).getConstructor(JCas.class);
        AnnotationBase pElement = (AnnotationBase) c.newInstance(UIMADatabaseInterface.getJCas());
        return pElement;
    }

    public static Set<Type> getSubTypes(String sClass) throws UIMAException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        Set<Type> rSet = new HashSet<>();

        JCas cas = JCasFactory.createJCas();

        AnnotationBase ab = getElement(sClass);

        cas.getTypeSystem().getProperlySubsumedTypes(ab.getType()).forEach(sub->{
            rSet.add(sub);
        });

        return rSet;

    }

    public static JSONArray definitionToJSON(String sClass) throws ClassNotFoundException, NoSuchMethodException, UIMAException, IllegalAccessException, InvocationTargetException, InstantiationException {

        JSONArray rArray = new JSONArray();

        Constructor c = Class.forName(sClass).getConstructor(JCas.class);

        AnnotationBase pElement = (AnnotationBase) c.newInstance(UIMADatabaseInterface.getJCas());

        pElement.getType().getFeatures().forEach(f->{
            JSONObject rObject = new JSONObject();

            JSONArray tArray = new JSONArray();
//
//            if(!f.getDomain().toString().contains("Annotation") || f.getShortName().equalsIgnoreCase("id")){
//
//            }
            try {
                tArray.put(new JSONObject().put("domain", f.getDomain()));
                tArray.put(new JSONObject().put("range", f.getRange()));
                rObject.put("name", f.getShortName());
                rObject.put("id", f.getName());
                rObject.put("definition", tArray);
                rArray.put(rObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        });

        return rArray;

    }

}
