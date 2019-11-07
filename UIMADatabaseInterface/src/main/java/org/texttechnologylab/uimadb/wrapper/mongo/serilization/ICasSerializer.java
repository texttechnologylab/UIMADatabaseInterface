package org.texttechnologylab.uimadb.wrapper.mongo.serilization;

import org.apache.uima.cas.CAS;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.CasSerializationException;

public interface ICasSerializer {
	String serialize(CAS doc) throws CasSerializationException;

	void deserialize(CAS doc, String src) throws CasSerializationException;
}
