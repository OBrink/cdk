<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:output method="xml" indent="no" omit-xml-declaration="yes"/>

  <xsl:template match="*">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="text()"/>

  <xsl:template match="/">
<para>These statistics were generated on
<xsl:value-of select="javancss/date"/> at
<xsl:value-of select="javancss/time"/>.</para>
<informaltable frame='none'>
  <tgroup cols="3">
    <thead>
        <row>
          <entry>Package</entry>
          <entry>Classes</entry>
          <entry>Functions</entry>
          <entry>NCSS</entry>
        </row>
    </thead>
    <tbody>
        <xsl:for-each select="javancss/packages/package">
          <xsl:sort select="ncss" data-type="number" order="descending"/>
          <xsl:apply-templates select="self::node()[not(contains(./name, 'test'))]"/>
        </xsl:for-each>
        <row>
          <entry>Total</entry>
          <entry><xsl:value-of select="sum(javancss/packages/package[not(contains(./name, 'test'))]/classes)"/></entry>
          <entry><xsl:value-of select="sum(javancss/packages/package[not(contains(./name, 'test'))]/functions)"/></entry>
          <entry><xsl:value-of select="sum(javancss/packages/package[not(contains(./name, 'test'))]/ncss)"/></entry>
        </row>
    </tbody>
  </tgroup>
</informaltable>
<para>And for the test classes:</para>
<informaltable frame='none'>
  <tgroup cols="3">
    <thead>
        <row>
          <entry>Package</entry>
          <entry>Classes</entry>
          <entry>Functions</entry>
          <entry>NCSS</entry>
        </row>
    </thead>
    <tbody>
        <xsl:for-each select="javancss/packages/package">
          <xsl:sort select="ncss" data-type="number" order="descending"/>
          <xsl:apply-templates select="self::node()[contains(./name, 'test')]"/>
        </xsl:for-each>
        <row>
          <entry>Total</entry>
          <entry><xsl:value-of select="sum(javancss/packages/package[contains(./name, 'test')]/classes)"/></entry>
          <entry><xsl:value-of select="sum(javancss/packages/package[contains(./name, 'test')]/functions)"/></entry>
          <entry><xsl:value-of select="sum(javancss/packages/package[contains(./name, 'test')]/ncss)"/></entry>
        </row>
    </tbody>
  </tgroup>
</informaltable>
  </xsl:template>

  <xsl:template match="package">
      <row>
        <entry><xsl:value-of select="name"/></entry>
        <entry><xsl:value-of select="classes"/></entry>
        <entry><xsl:value-of select="functions"/></entry>
        <entry><xsl:value-of select="ncss"/></entry>
      </row>
  </xsl:template>

</xsl:stylesheet>
