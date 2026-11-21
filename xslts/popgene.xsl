<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:output method="text" omit-xml-declaration="yes" indent="yes"/>
    <xsl:template match="study">
        
        <!-- First line -->
        <!-- Name of the study between /* and */ -->
        <xsl:text>/* </xsl:text> 
        <xsl:value-of select="name"/>
        <xsl:text> */ </xsl:text>
        <xsl:text>&#xA;</xsl:text>
        
        <!-- Second line -->
        <!-- "Number of populations = " n_populations-->
        <xsl:text>Number of populations = </xsl:text>
        <xsl:value-of select="n_populations"/>
        <xsl:text>&#xA;</xsl:text>
        
        <!-- Third line -->
        <!-- "Number of loci = " n_loci -->
        <xsl:text>Number of loci = </xsl:text>
        <xsl:value-of select="n_loci"/>
        <xsl:text>&#xA;</xsl:text>
        
        <!-- Fourth line -->
        <!-- "Locus name : " -->
        <xsl:text>Locus name : </xsl:text>
        <xsl:text>&#xA;</xsl:text>
        
        <!-- Names of loci separated by line breaks -->
        <xsl:for-each select="loci/locus">
            <xsl:value-of select="."/>
            <xsl:text> </xsl:text>
        </xsl:for-each>
        <xsl:text>&#xA;</xsl:text>
        <xsl:text>&#xA;</xsl:text>
        
        <!-- For each individual, it is shown its ID, its name, and 
             the presence/absence of loci using 1/0 respectively. The 
             ID, name, and presence/absence are separated by line breaks 
        -->
        <xsl:for-each select="populations/population">
            <xsl:text>ID = </xsl:text>
            <xsl:value-of select="ID"/>
            <xsl:text>&#xA;</xsl:text>
            
            <xsl:text>name = </xsl:text>
            <xsl:value-of select="name"/>
            <xsl:text>&#xA;</xsl:text>
            
            <xsl:for-each select="individuals/individual">
                <xsl:for-each select="locus">
                    <xsl:if test=". = 'false'">
                        <xsl:text>0</xsl:text>
                    </xsl:if>
                    <xsl:if test=". = 'true'">
                        <xsl:text>1</xsl:text>
                    </xsl:if>
                </xsl:for-each>
                <xsl:text>&#xA;</xsl:text>
            </xsl:for-each>
            <xsl:text>&#xA;</xsl:text>
        </xsl:for-each>
        
        
    </xsl:template>
    
    
    
    
    
</xsl:stylesheet>
