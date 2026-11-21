<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:output method="text" omit-xml-declaration="yes" indent="yes"/>
    <xsl:template match="study">
        <!-- First line -->
        <!-- Study name -->
        <xsl:value-of select="name"/>
        <xsl:text>&#xA;</xsl:text>
        
        <!-- second line -->
        <!-- n population, n loci, 1, 1 -->
        <xsl:value-of select="n_populations"/>
        <xsl:text>&#x9;</xsl:text>    
        <xsl:value-of select="n_loci"/>
        <xsl:text>&#x9;</xsl:text> 
        <xsl:text>1&#x9;1</xsl:text>
        <xsl:text>&#xA;</xsl:text>
        
        <!-- population names -->
        <xsl:for-each select="populations/population">
            <xsl:value-of select="name"/>
            <xsl:text>&#xA;</xsl:text>
        </xsl:for-each>
        
        <!-- "pop”, “ind”, loci names -->
       
        <xsl:text>pop</xsl:text>
        <xsl:text>&#x9;</xsl:text>
        <xsl:text>ind</xsl:text>
        <xsl:text>&#x9;</xsl:text>
        <xsl:for-each select="loci/locus">
            <xsl:value-of select="."/>
            <xsl:if test="not(last()=position())">
                <xsl:text>&#x9;</xsl:text>    
            </xsl:if>
        </xsl:for-each>
        <xsl:text>&#xA;</xsl:text>
        
        
        <!-- Following lines -->
        <!-- population number, individual name, presence/absence codified by 1/2 -->
        
        <xsl:for-each select="populations/population">
            <xsl:variable name="pos" select="position()"/>
            <xsl:for-each select="individuals/individual">
                <xsl:value-of select="$pos"/>
                <xsl:text>&#x9;</xsl:text>
                <xsl:value-of select="name"/>
                <xsl:text>&#x9;</xsl:text>
                <xsl:for-each select="locus">
                    <xsl:if test=". = 'false'">
                        <xsl:text>2</xsl:text>
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
        
        
        
        
        
        
        
        
    </xsl:template>
    
    
    
    
    
</xsl:stylesheet>
