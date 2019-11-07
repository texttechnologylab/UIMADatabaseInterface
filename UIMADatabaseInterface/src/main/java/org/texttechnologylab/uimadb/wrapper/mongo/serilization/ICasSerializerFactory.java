package org.texttechnologylab.uimadb.wrapper.mongo.serilization;


import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.SerializerInitializationException;

public interface ICasSerializerFactory {
	ICasSerializer createSerializer() throws SerializerInitializationException;
}
