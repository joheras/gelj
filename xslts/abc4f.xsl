<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs" version="2.0">
    <xsl:output method="text" omit-xml-declaration="yes" indent="yes"/>
    <xsl:template match="study">
        <!-- First line -->

        <xsl:text>[loci]=</xsl:text>
        <xsl:value-of select="n_loci"/>
        <xsl:variable name="nloci" select="n_loci"/>
        
        <xsl:variable name="loci" select="loci"/>
        <xsl:text>&#xA;</xsl:text>
        <xsl:text>&#xA;</xsl:text>
        <xsl:text>[populations]=</xsl:text>
        <xsl:value-of select="n_populations"/>
        <xsl:text>&#xA;</xsl:text>
        <xsl:text>&#xA;</xsl:text>

        <!-- Individual lines -->
        <!-- Individual name,  Population name, presence/absence of loci 
             using 1/0-->

        <xsl:for-each select="populations/population">
            <xsl:text>[pop]=</xsl:text>
            <xsl:value-of select="name"/>
            <xsl:text>&#xA;</xsl:text>
            <xsl:variable name="n_inds" select="n_individuals"/>
            <xsl:variable name="individuals" select="individuals/individual"/>

            <xsl:for-each select="$loci/locus">
                <xsl:value-of select="position()"/>
                <xsl:variable name="index" select="position()"/>
                <xsl:text> </xsl:text>
                <xsl:value-of select="$n_inds"/>
                <xsl:text> </xsl:text>
                 <xsl:value-of select="count($individuals[locus[position()=$index]='true'])"/>              
                <xsl:text>&#xA;</xsl:text>


            </xsl:for-each>



            <xsl:text>&#xA;</xsl:text>


        </xsl:for-each>


    </xsl:template>





</xsl:stylesheet>
