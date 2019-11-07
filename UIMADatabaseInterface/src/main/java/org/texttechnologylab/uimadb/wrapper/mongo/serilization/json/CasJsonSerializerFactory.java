package org.texttechnologylab.uimadb.wrapper.mongo.serilization.json;


import org.texttechnologylab.uimadb.wrapper.mongo.serilization.ICasSerializer;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.ICasSerializerFactory;
import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.SerializerInitializationException;

public class CasJsonSerializerFactory implements ICasSerializerFactory {

	private ITransformBuilder	transformBuilder	= null;

	public CasJsonSerializerFactory() {
		this(TransformBuilderFactory.getDefaultTransformBuilder());
	}

	public CasJsonSerializerFactory(ITransformBuilder builder) {
		this.transformBuilder = builder;
	}

	public synchronized ICasSerializer createSerializer()
		throws SerializerInitializationException {
		return new CasJsonSerializer(transformBuilder);
	}
}
