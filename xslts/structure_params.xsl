<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs" version="2.0">
    <xsl:output method="text" omit-xml-declaration="yes" indent="yes"/>
    <xsl:template match="study">

        <!-- individual name, population id, presence/absence codified by 1/0 -->

        <xsl:text>PROJNAME </xsl:text>
        <xsl:value-of select="name"/>
        <xsl:text>&#xA;</xsl:text>
        <xsl:text>NUMINDS </xsl:text>
        <xsl:value-of select="n_individuals"/>
        <xsl:text>&#xA;</xsl:text>
        <xsl:text>NUMLOCI </xsl:text>
        <xsl:value-of select="n_loci"/>
        <xsl:text>&#xA;</xsl:text>
        <xsl:text>PLOIDY 1</xsl:text>
        <xsl:text>&#xA;</xsl:text>
        <xsl:text>MISSINGVALUE 9</xsl:text>
        <xsl:text>&#xA;</xsl:text>
        <xsl:text>ONEROW 0</xsl:text>
        <xsl:text>&#xA;</xsl:text>
        <xsl:text>INDLABEL 1</xsl:text>
        <xsl:text>&#xA;</xsl:text>
        <xsl:text>POPID 1</xsl:text>
        <xsl:text>&#xA;</xsl:text>
        <xsl:text>POPFLAG 0</xsl:text>
        <xsl:text>&#xA;</xsl:text>
        <xsl:text>LOCDATA 0</xsl:text>
        <xsl:text>&#xA;</xsl:text>
        <xsl:text>PHENOTYPE 0</xsl:text>
        <xsl:text>&#xA;</xsl:text>
        <xsl:text>EXTRACOL 0</xsl:text>
        <xsl:text>&#xA;</xsl:text>
        <xsl:text>MARKERNAME 0</xsl:text>
        <xsl:text>&#xA;</xsl:text>
        <xsl:text>RECESSIVEALLELE 0</xsl:text>
        <xsl:text>&#xA;</xsl:text>
        <xsl:text>MAPDISTANCE 0</xsl:text>
        <xsl:text>&#xA;</xsl:text>
        <xsl:text>PHASED 0</xsl:text>
        <xsl:text>&#xA;</xsl:text>
        <xsl:text>PHASEINFO 0</xsl:text>
        <xsl:text>&#xA;</xsl:text>







    </xsl:template>





</xsl:stylesheet>
