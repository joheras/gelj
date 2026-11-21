<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:output method="xml" omit-xml-declaration="yes" indent="yes" encoding="ascii"/>
    <xsl:template match="study">
        <!-- First line -->
        <!-- #nexus -->
        <xsl:text>#nexus</xsl:text>
        <xsl:text>
        </xsl:text>
        <xsl:text>&#xA;</xsl:text>
        
        <!-- Second line -->
        <!-- [! name ] -->
        <xsl:text>[!</xsl:text>
        <xsl:value-of select="name"/>
        <xsl:text>]</xsl:text>
        <xsl:text>&#xA;</xsl:text>
        <xsl:text>&#xA;</xsl:text>
        
        <!-- Third line -->
        <!-- begin gdatadata; -->
        <xsl:text>begin gdadata;</xsl:text>
        <xsl:text>&#xA;</xsl:text>
        
        
        <!-- Fourth line -->
        <!-- dimensions nloci = , npops = ; -->
        <xsl:text>dimensions nloci=</xsl:text>
        <xsl:value-of select="n_loci"/>
        <xsl:text> npops=</xsl:text>
        <xsl:value-of select="n_populations"/>
        <xsl:text>;</xsl:text>
        <xsl:text>&#xA;</xsl:text>
        
        <!-- Fifth and sixth line
        format haploid tokens missing=? datapoint=standard;
        locusallelelabels-->
        <xsl:text>format haploid tokens missing=? datapoint=standard;</xsl:text>
        <xsl:text>&#xA;</xsl:text>
        <xsl:text>locusallelelabels</xsl:text>
        <xsl:text>&#xA;</xsl:text>
        
        <!-- index locus_name -->
        <xsl:for-each select="loci/locus">
            <xsl:value-of select="position()"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="."/>
            <xsl:if test="not(last()=position())">
                <xsl:text>,</xsl:text>
            </xsl:if>
            <xsl:if test="last()=position()">
                <xsl:text>;</xsl:text>
            </xsl:if>
            <xsl:text>&#xA;</xsl:text>
        </xsl:for-each>
        
        <xsl:text>matrix</xsl:text>
        <xsl:text>&#xA;</xsl:text>
        
        
        <xsl:for-each select="populations/population">
            <xsl:value-of select="name"/>
            <xsl:text>: </xsl:text>
            <xsl:for-each select="individuals/individual">
                <xsl:value-of select="name"/>
                <xsl:text> </xsl:text>
                <xsl:for-each select="locus">
                    <xsl:if test=". = 'false'">
                        <xsl:text>0</xsl:text>
                    </xsl:if>
                    <xsl:if test=". = 'true'">
                        <xsl:text>1</xsl:text>
                    </xsl:if>
                    <xsl:text> </xsl:text>
                </xsl:for-each>
                <xsl:if test="not(last()=position())">
                    <xsl:text>&#xA;</xsl:text>
                </xsl:if>
            </xsl:for-each>
            <xsl:if test="not(last()=position())">
                <xsl:text>,</xsl:text>
                <xsl:text>&#xA;</xsl:text>
            </xsl:if>
            <xsl:if test="last()=position()">
                <xsl:text>;</xsl:text>
                <xsl:text>&#xA;</xsl:text>
            </xsl:if>
            
        </xsl:for-each>
        
        
        <xsl:text>end;</xsl:text>
        
        
        
        
        
    </xsl:template>
    
    
    
    
    
</xsl:stylesheet>
