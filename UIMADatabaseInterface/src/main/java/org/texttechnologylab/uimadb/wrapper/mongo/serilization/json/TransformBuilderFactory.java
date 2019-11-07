package org.texttechnologylab.uimadb.wrapper.mongo.serilization.json;

public final class TransformBuilderFactory {
	private static final String	DEFAULT_FORWARD_XSLT	= "/transformations/xCasToJsonBase.xslt";
	private static final String	DEFAULT_REVERSE_XSLT	= "/transformations/jsonToXCasBase.xslt";

	public static ITransformBuilder getDefaultTransformBuilder() {
		return new SimpleFileTransformBuilder(
			DEFAULT_FORWARD_XSLT,
			DEFAULT_REVERSE_XSLT);
	}
}
