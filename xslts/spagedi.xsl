<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:output method="text" omit-xml-declaration="yes" indent="yes"/>
    <xsl:template match="study">
        <!-- First line -->
        <!-- n individuals, n populations, 0, n loci, -1, 2 -->
        <xsl:value-of select="n_individuals"/>
        <xsl:text>&#x9;</xsl:text>    
        <xsl:value-of select="n_populations"/>
        <xsl:text>&#x9;</xsl:text>
        <xsl:text>0</xsl:text>
        <xsl:text>&#x9;</xsl:text>
        <xsl:value-of select="n_loci"/>
        <xsl:text>&#x9;</xsl:text>
        <xsl:text>-1</xsl:text>
        <xsl:text>&#x9;</xsl:text>
        <xsl:text>2</xsl:text>
        <xsl:text>&#xA;</xsl:text>
        
        <!-- second line -->
        <!-- 0 -->
        <xsl:text>0</xsl:text>
        <xsl:text>&#xA;</xsl:text>
        
               
        <!-- "ind”, “cat”, loci names -->
       
        <xsl:text>ind</xsl:text>
        <xsl:text>&#x9;</xsl:text>
        <xsl:text>cat</xsl:text>
        <xsl:text>&#x9;</xsl:text>
        <xsl:for-each select="loci/locus">
            <xsl:value-of select="."/>
            <xsl:if test="not(last()=position())">
                <xsl:text>&#x9;</xsl:text>    
            </xsl:if>
        </xsl:for-each>
        <xsl:text>&#xA;</xsl:text>
        
        
        <!-- Following lines -->
        <!-- individual name, population number, presence/absence codified by 1/0 -->
        
        <xsl:for-each select="populations/population">
            <xsl:variable name="pop" select="name"/>
            <xsl:for-each select="individuals/individual">
                <xsl:value-of select="name"/>
                <xsl:text>&#x9;</xsl:text>
                <xsl:value-of select="$pop"/>
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
        
        <xsl:text>END</xsl:text>
        
        
        
        
        
        
    </xsl:template>
    
    
    
    
    
</xsl:stylesheet>
