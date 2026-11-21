<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:output method="text" omit-xml-declaration="yes" indent="yes"/>
    <xsl:template match="study">
        <!-- First line -->
        <!-- Population,Latitude,Longitude,Loci names -->
        <xsl:text>Population</xsl:text>
        <xsl:text>,</xsl:text>
        <xsl:text>Latitude</xsl:text>
        <xsl:text>,</xsl:text>
        <xsl:text>Longitude</xsl:text>
        <xsl:text>,</xsl:text>
        <xsl:for-each select="loci/locus">
            <xsl:value-of select="."/>
            <xsl:if test="not(last()=position())">
                <xsl:text>,</xsl:text>    
            </xsl:if>
        </xsl:for-each>
        <xsl:text>&#xA;</xsl:text>

        
        <!-- Following lines -->
        <!-- Individual name, Latitude, Longitude, presence/absence -->
        
        <xsl:for-each select="populations/population">
            <xsl:for-each select="individuals/individual">
                <xsl:value-of select="name"/>
                <xsl:text>,</xsl:text>
                <xsl:value-of select="coordinates/latitude"/>
                <xsl:text>,</xsl:text>
                <xsl:value-of select="coordinates/longitude"/>
                <xsl:text>,</xsl:text>
                <xsl:for-each select="locus">
                    <xsl:if test=". = 'false'">
                        <xsl:text>0</xsl:text>
                    </xsl:if>
                    <xsl:if test=". = 'true'">
                        <xsl:text>1</xsl:text>
                    </xsl:if>
                    <xsl:if test="not(last()=position())">
                        <xsl:text>,</xsl:text>    
                    </xsl:if>
                </xsl:for-each>
                <xsl:text>&#xA;</xsl:text>
            </xsl:for-each>
        </xsl:for-each>
        
        
        
        
        
        
        
        
    </xsl:template>
    
    
    
    
    
</xsl:stylesheet>
