<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- Edited by XMLSpy® -->
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="yes" />

	<xsl:template name="ordinaryNode">
		<xsl:variable name="node_name" select="name()" />
		<children type="{$node_name}">
			<attribs>
				<xsl:for-each select="./@*">
					<xsl:variable name="attrib_name" select="name()" />
					<xsl:element name="{$attrib_name}">
						<xsl:value-of select="." />
					</xsl:element>
				</xsl:for-each>
			</attribs>
			<xsl:for-each select="./*">
				<xsl:call-template name="ordinaryNode" />
			</xsl:for-each>
			<text><xsl:value-of select="text()" /></text>
		</children>

	</xsl:template>

	<xsl:template match="/CAS">
		<xsl:call-template name="ordinaryNode" />
	</xsl:template>
</xsl:stylesheet>

