<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:output method="text" omit-xml-declaration="yes" indent="yes"/>
    <xsl:template match="study">
        <!-- First line -->
        <!-- number of populations, number of loci, locus name -->
        <xsl:value-of select="n_populations"/>
        <xsl:text>&#x9;</xsl:text>
        <xsl:value-of select="n_loci"/>
        <xsl:text>&#x9;</xsl:text>
        
        <xsl:for-each select="loci/locus">
            <xsl:value-of select="."/>
            <xsl:if test="not(last()=position())">
                <xsl:text>&#x9;</xsl:text>    
            </xsl:if>
        </xsl:for-each>
        <xsl:text>&#xA;</xsl:text>
        
        <!-- Individual lines -->
        <!-- Population name, Individual name, presence/absence of loci 
             using 1/0-->
        
        <xsl:for-each select="populations/population">
            <xsl:variable name="popname" select="name"/>
            <xsl:for-each select="individuals/individual">
                <xsl:value-of select="$popname"/>
                <xsl:text>&#x9;</xsl:text>
                <xsl:value-of select="name"/>
                <xsl:text>&#x9;</xsl:text>
                <xsl:for-each select="locus">
                    <xsl:if test=". = 'false'">
                        <xsl:text>0</xsl:text>
                    </xsl:if>
                    <xsl:if test=". = 'true'">
                        <xsl:text>1</xsl:text>
                    </xsl:if>                    
                    <xsl:if test="not(last()=position())">
                        <xsl:text>&#x9;</xsl:text>    
                    </xsl:if>
                </xsl:for-each>
                <xsl:text>&#xA;</xsl:text>
            </xsl:for-each>
        </xsl:for-each>
        
        <!-- Last line -->
        <!-- The word END -->
        <xsl:text>END</xsl:text>
        
        
    </xsl:template>
    
    
    
    
    
</xsl:stylesheet>
