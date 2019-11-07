package org.texttechnologylab.uimadb.wrapper.mongo.serilization.json;

import org.apache.commons.io.IOUtils;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

public class SimpleFileTransformBuilder implements ITransformBuilder {
	private String	forward	= null;
	private String	reverse	= null;

	public SimpleFileTransformBuilder(String forwardXsltFile, String reverseXsltFile) {
		this.forward = forwardXsltFile;
		this.reverse = reverseXsltFile;
	}

	public Transformer getForwardTransformer()
		throws TransformerConfigurationException {
		return loadTransformer(forward);
	}

	public Transformer getReverseTransformer()
		throws TransformerConfigurationException {
		return loadTransformer(reverse);
	}

	protected Transformer loadTransformer(String resource)
		throws TransformerConfigurationException {
		InputStream xsltStr = null;
		try {
			xsltStr = getClass().getResourceAsStream(resource);
			return TransformerFactory.newInstance().newTransformer(
				new StreamSource(xsltStr));
		} finally {
			IOUtils.closeQuietly(xsltStr);
		}
	}
}
