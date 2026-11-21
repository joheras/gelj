<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:output method="text" omit-xml-declaration="yes" indent="yes"/>
    <xsl:template match="study">
        
        
        <!-- Individual lines -->
        <!-- presence/absence of loci using 1/0, id of the individual-->
        
        <xsl:for-each select="populations/population">
            <xsl:for-each select="individuals/individual">
                <xsl:value-of select="coordinates/longitude"/>
                <xsl:text> </xsl:text>
                <xsl:value-of select="coordinates/latitude"/>
                <xsl:text>&#xA;</xsl:text>
            </xsl:for-each>
        </xsl:for-each>
        
        
        
    </xsl:template>
    
    
    
    
    
</xsl:stylesheet>