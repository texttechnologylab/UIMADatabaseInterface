package org.texttechnologylab.uimadb.wrapper.mongo.serilization;

import org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions.UnknownFactoryException;

import java.util.HashMap;
import java.util.Map;

public final class CasSerializerMetaFactory {
	private static CasSerializerMetaFactory		instance		= null;

	private Map<String, ICasSerializerFactory>	knownFactories	= new HashMap<String, ICasSerializerFactory>();

	public static synchronized CasSerializerMetaFactory Instance() {
		if (instance == null)
			instance = new CasSerializerMetaFactory();
		return instance;
	}

	private CasSerializerMetaFactory() {
	}

	public synchronized ICasSerializerFactory getFactory(String name)
		throws UnknownFactoryException {
		if (name == null || name.isEmpty())
			throw new IllegalArgumentException(
				"Serializer factory name cannot be null or empty");
		if (!knownFactories.containsKey(name) && !tryLoadFactory(name))
			throw new UnknownFactoryException("Unknown factory", name);
		return knownFactories.get(name);
	}

	public synchronized void registerFactory(String name,
		ICasSerializerFactory factory) {
		knownFactories.put(name, factory);
	}

	private boolean tryLoadFactory(String name) {
		try {
			Class<?> aCls = Class.forName(name);
			Class<? extends ICasSerializerFactory> factCls = aCls.asSubclass(ICasSerializerFactory.class);
			ICasSerializerFactory factory = factCls.newInstance();
			knownFactories.put(name, factory);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		} catch (ReflectiveOperationException e) {
			return false;
		}
	}
}
