package org.texttechnologylab.uimadb.wrapper.mongo.serilization.json;

import org.apache.commons.io.IOUtils;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XCASDeserializer;
import org.apache.uima.cas.impl.XCASSerializer;
import org.json.JSONException;
import org.json.JSONML;
import org.json.JSONObject;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.ICasSerializer;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.CasSerializationException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.SerializerInitializationException;
import org.xml.sax.SAXException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CasJsonSerializer implements ICasSerializer {
	Transformer	xcasToJson	= null;
	Transformer	jsonToXCas	= null;

	private int	indent		= 0;

	public CasJsonSerializer(ITransformBuilder transformBuilder)
		throws SerializerInitializationException {
		this(transformBuilder, 0);
	}

	public CasJsonSerializer(ITransformBuilder transformBuilder,
		int indent) throws SerializerInitializationException {
		this.indent = indent;
		try {
			xcasToJson = transformBuilder.getForwardTransformer();
			jsonToXCas = transformBuilder.getReverseTransformer();
		} catch (TransformerConfigurationException e) {
			throw new SerializerInitializationException(e);
		}
	}


	public String serialize(CAS doc) throws CasSerializationException {
		ByteArrayOutputStream outTmp = null;
		ByteArrayInputStream inTmp = null;
		try {
			outTmp = new ByteArrayOutputStream();
			XCASSerializer.serialize(doc, outTmp);

			String originalXml = outTmp.toString();
			outTmp.close();

            JSONObject rJSON = JSONML.toJSONObject(originalXml);

            return rJSON.toString(indent);

//
//            try {
//
//                JSONObject p = JSONML.toJSONObject(originalXml);
//                String s = JSONML.toString(p);
//
//				JCas jcas = JCasFactory.createJCas();
//				inTmp = new ByteArrayInputStream(s.getBytes());
//
//				XCASDeserializer.deserialize(inTmp, jcas.getCas());
//
//			} catch (UIMAException e) {
//				e.printStackTrace();
//			}
//
//
//
//			System.out.println(originalXml);
//            outTmp = new ByteArrayOutputStream();
//			inTmp = new ByteArrayInputStream(originalXml.getBytes());
//			xcasToJson.transform(new StreamSource(inTmp), new StreamResult(
//				outTmp));
////            System.out.println("1: \t"+outTmp.toString());
////            JSONObject obj = JSONML.toJSONObject(outTmp.toString());
//
////            System.out.println(outTmp.toString());
//
////            JSONObject obj = XML.toJSONObject(outTmp.toString());
//            JSONObject obj = JSONML.toJSONObject(outTmp.toString());
//            System.out.println(obj.toString());
//            return obj.toString(indent);
		} catch (SAXException e) {
			throw new CasSerializationException(e);
		}  catch (IOException e) {
			throw new CasSerializationException(e);
		} catch (JSONException e) {
            e.printStackTrace();
        } finally {
			IOUtils.closeQuietly(outTmp);
			IOUtils.closeQuietly(inTmp);
		}
		return "";
	}

	public void deserialize(CAS doc, String src)
		throws CasSerializationException {
		ByteArrayInputStream inTmp = null;
		ByteArrayOutputStream outTmp = null;
		try {

            JSONObject pObject = new JSONObject(src);
            String s = JSONML.toString(pObject);

            inTmp = new ByteArrayInputStream(s.getBytes());

            XCASDeserializer.deserialize(inTmp, doc);

//            System.out.println(JSONML.toString(new JSONObject(src)));
//            System.out.println(XML.toString(new JSONObject(src)));
//
//            outTmp = new ByteArrayOutputStream();
//			inTmp = new ByteArrayInputStream(XML.toString(new JSONObject(src)).getBytes());
//
//			jsonToXCas.transform(new StreamSource(inTmp), new StreamResult(outTmp));
//
//            System.out.println(outTmp.toString());
//            inTmp.close();
//			inTmp = new ByteArrayInputStream(outTmp.toByteArray());
//
//			XCASDeserializer.deserialize(inTmp, doc);
		} catch (SAXException e) {
			throw new CasSerializationException(e);
		} catch (IOException e) {
			throw new CasSerializationException(e);
		} catch (JSONException e) {
            e.printStackTrace();
        } finally {
			IOUtils.closeQuietly(inTmp);
			IOUtils.closeQuietly(outTmp);
		}
	}
}
