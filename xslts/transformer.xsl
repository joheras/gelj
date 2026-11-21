<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs" version="2.0">
    <xsl:output method="xml" omit-xml-declaration="yes" indent="yes"/>
    <xsl:template match="study">


        <tfm4 version="2.0" xmlns="http://dis.itccanarias.org/schema/demiurgo">
            <matrix>
                <dominant>
                    <species>
                        <xsl:attribute name="name">
                            <xsl:value-of select="name"/>
                        </xsl:attribute>

                        <xsl:variable name="locusnames" select="loci/locus"/>

                        <xsl:for-each select="populations/population">
                            <population>
                                <xsl:attribute name="code">
                                    <xsl:value-of select="name"/>
                                </xsl:attribute>
                                <xsl:for-each select="individuals/individual">
                                    <individual>
                                        <xsl:attribute name="id">
                                            <xsl:value-of select="ID"/>
                                        </xsl:attribute>
                                        <location>
                                            <x>
                                                <xsl:value-of select="coordinates/latitude"/>
                                            </x>
                                            <y>
                                                <xsl:value-of select="coordinates/longitude"/>
                                            </y>
                                        </location>
                                        <xsl:for-each select="locus">
                                            <xsl:variable name="pos" select="position()"/>
                                            <locus>
                                                <xsl:attribute name="name">
                                                    <xsl:value-of select="$locusnames[$pos]"/>
                                                </xsl:attribute>
                                                <xsl:if test=". = 'false'">
                                                  <genotype>0</genotype>
                                                </xsl:if>
                                                <xsl:if test=". = 'true'">
                                                  <genotype>1</genotype>
                                                </xsl:if>
                                            </locus>
                                        </xsl:for-each>

                                    </individual>
                                </xsl:for-each>

                            </population>
                        </xsl:for-each>
                    </species>


                </dominant>
            </matrix>
        </tfm4>










    </xsl:template>





</xsl:stylesheet>
