<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- Edited by XMLSpy® -->
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="yes" />

	<xsl:template name="childrenNode">
		<xsl:variable name="node_name" select="./desc.type/text()" />
		<xsl:element name="{$node_name}">
			<xsl:for-each select="./attribs/*">
				<xsl:variable name="attrib_name" select="name()" />
				<xsl:attribute name="{$attrib_name}">
					<xsl:value-of select="." />
				</xsl:attribute>
			</xsl:for-each>
			<xsl:value-of select="./text/text()" />
			<xsl:for-each select="./children">
				<xsl:call-template name="childrenNode" />
			</xsl:for-each>
		</xsl:element>
	</xsl:template>

	<xsl:template match="/children">
		<xsl:call-template name="childrenNode" />
	</xsl:template>
</xsl:stylesheet>

