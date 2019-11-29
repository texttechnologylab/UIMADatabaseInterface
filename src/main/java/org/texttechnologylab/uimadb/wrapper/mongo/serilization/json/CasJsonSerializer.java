package org.texttechnologylab.uimadb.wrapper.mongo.serilization.json;

import com.siemens.ct.exi.core.CodingMode;
import com.siemens.ct.exi.core.EXIFactory;
import com.siemens.ct.exi.core.exceptions.EXIException;
import com.siemens.ct.exi.core.helpers.DefaultEXIFactory;
import com.siemens.ct.exi.json.EXI4JSONConstants;
import com.siemens.ct.exi.json.EXIforJSONGenerator;
import com.siemens.ct.exi.json.EXIforJSONParser;
import com.siemens.ct.exi.main.api.sax.EXIResult;
import com.siemens.ct.exi.main.api.sax.EXISource;
import org.apache.commons.io.IOUtils;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XCASDeserializer;
import org.apache.uima.cas.impl.XCASSerializer;
import org.codehaus.plexus.util.StringOutputStream;
import org.json.JSONException;
import org.json.JSONML;
import org.json.JSONObject;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.ICasSerializer;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.CasSerializationException;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.SerializerInitializationException;
import org.texttechnologylab.utilities.helper.TempFileHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Arrays;

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
            return serialize(doc, false);
        }

        public String serialize(CAS doc, boolean bCompress) throws CasSerializationException {
		ByteArrayOutputStream outTmp = null;
		ByteArrayInputStream inTmp = null;
		try {
			outTmp = new ByteArrayOutputStream();
			XCASSerializer.serialize(doc, outTmp);

			String originalXml = outTmp.toString();
			outTmp.close();

            JSONObject rJSON = new JSONObject();
            rJSON = JSONML.toJSONObject(originalXml);

            JSONObject oJSON = new JSONObject();
            // Compress
            if(bCompress) {

                InputStream is = new ByteArrayInputStream(rJSON.toString().getBytes());
                EXIforJSONGenerator generator = new EXIforJSONGenerator(EXI4JSONConstants.XML_SCHEMA_FOR_JSON);
                ByteArrayOutputStream baosEXI = new ByteArrayOutputStream();

                generator.generate(is, baosEXI);


                String s = Arrays.toString(baosEXI.toByteArray());

                ByteArrayOutputStream baosJSON = new ByteArrayOutputStream();

                InputStream isOut = new ByteArrayInputStream(s.getBytes());
                EXIforJSONParser e4jParser = new EXIforJSONParser(EXI4JSONConstants.XML_SCHEMA_FOR_JSON);
                e4jParser.parse(isOut, baosJSON);

                String actual = new String(baosJSON.toByteArray());

                System.out.println(actual);

            }
            else{
                return rJSON.toString(indent);
            }
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
        } catch (EXIException e) {
            e.printStackTrace();
        } finally {
			IOUtils.closeQuietly(outTmp);
			IOUtils.closeQuietly(inTmp);
		}
		return "";
	}

    @Override
    public void deserialize(CAS doc, String src)
		throws CasSerializationException {
		ByteArrayInputStream inTmp = null;
		ByteArrayOutputStream outTmp = null;


		try {

            JSONObject pObject = new JSONObject(src);

//            if(pObject.has("compress")){
//                InputSource inputSource = new InputSource(new StringReader(pObject.getString("compress")));
//
//                SAXSource exiSource = new EXISource(exiFactory);
//                exiSource.setInputSource(inputSource);
//
//                StringWriter w = new StringWriter();
//                Result result = new StreamResult(w);
//
//                TransformerFactory tf = TransformerFactory.newInstance();
//                Transformer transformer = tf.newTransformer();
//                transformer.transform(exiSource, result);
//
//                inTmp = new ByteArrayInputStream(w.toString().getBytes());
//            }
//            else{
                String s = JSONML.toString(pObject);
                inTmp = new ByteArrayInputStream(s.getBytes());
//            }




                //


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
            e.printStackTrace();
			throw new CasSerializationException(e);
		} catch (IOException e) {
            e.printStackTrace();
			throw new CasSerializationException(e);
		} catch (JSONException e) {
            e.printStackTrace();
        }
		finally {
			IOUtils.closeQuietly(inTmp);
			IOUtils.closeQuietly(outTmp);
		}
	}
}
