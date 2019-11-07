package org.texttechnologylab.uimadb.wrapper.mongo.serilization.json;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;

public interface ITransformBuilder {
	Transformer getForwardTransformer() throws TransformerConfigurationException;

	Transformer getReverseTransformer() throws TransformerConfigurationException;
}
