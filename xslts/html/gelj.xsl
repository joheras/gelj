<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:output method="html" omit-xml-declaration="yes" indent="yes"/>
    <xsl:template match="study">
        
        <!-- Following lines -->
        <!-- Population name, Sample name, loci values -->
        
        
        <h1><xsl:value-of select="name"/></h1>
        
       <h2>Number of populations: <xsl:value-of select="n_populations"/></h2>
        <h2>Number of individuals: <xsl:value-of select="n_individuals"/></h2>
        
        <xsl:variable name="loci" select="loci"/>
        
        <xsl:for-each select="populations/population">
            <hr/>
            <h3>Population <xsl:value-of select="name"/></h3>
            <p>Population size: <xsl:value-of select="n_individuals"/></p>
            
            
            <table>
                <tr>
                    <th>Name</th>
                    <xsl:for-each select="$loci/locus">
                        <th><xsl:value-of select="."/></th>
                    </xsl:for-each>
                </tr>
                <xsl:for-each select="individuals/individual">
                    <tr>
                    <td><xsl:value-of select="name"/></td>
                    <xsl:for-each select="locus">
                        <xsl:if test=". = 'false'">
                            <td>No</td>
                        </xsl:if>
                        <xsl:if test=". = 'true'">
                            <td>Yes</td>
                        </xsl:if>
                        
                    </xsl:for-each>
                    </tr>
                </xsl:for-each>
            </table> 
            
            
        </xsl:for-each>
        
        
        
        <hr/>
        
        
        
    </xsl:template>
    
    
    
    
    
</xsl:stylesheet>
