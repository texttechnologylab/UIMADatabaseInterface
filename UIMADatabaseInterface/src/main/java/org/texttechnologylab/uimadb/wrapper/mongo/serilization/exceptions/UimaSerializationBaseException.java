package org.texttechnologylab.uimadb.wrapper.mongo.serilization.exceptions;

import org.apache.uima.UIMAException;

public class UimaSerializationBaseException extends UIMAException {
	
	private static final long	serialVersionUID	= -8015525886963479500L;

	public UimaSerializationBaseException() {
		super();
	}
	
	public UimaSerializationBaseException(Throwable e) {
		super(e);
	}
	
	public UimaSerializationBaseException(String message, Object ... args) {
		super(message, args);
	}
}
