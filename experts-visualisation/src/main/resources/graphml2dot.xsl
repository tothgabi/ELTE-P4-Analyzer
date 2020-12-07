<!--

    Copyright 2020, Eötvös Loránd University.
    All rights reserved.

-->
<!-- Author: Dániel, Lukács (2020, Hungary) -->
<xsl:stylesheet  version="2.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:g="http://graphml.graphdrawing.org/xmlns"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
  xmlns:my="http://whatever">

  <xsl:output method="text" indent="no"/>
  <xsl:strip-space elements="*"/>

  <xsl:template match="/">
    <xsl:apply-templates select="/g:graphml/g:graph" />
  </xsl:template>

  <xsl:template match="g:graph">
    <xsl:call-template name="my:graphtype" /> 
    <xsl:text> </xsl:text> 
    <xsl:value-of select="@id" /> {
    graph [margin=0];
    node  [ style=filled
          , fillcolor=cornsilk
          , color=dimgrey
          , fontcolor=gray30
          , fontname=helvetica
          , shape=Mrecord
          ];
    edge  [ color=dimgrey 
          , fontcolor=gray30 
          , fontname=helvetica
          ];
    <xsl:apply-templates select="node()" />
} 
  </xsl:template> 

  <xsl:template match="g:node">
    <xsl:value-of select="@id" />
    <xsl:text> [</xsl:text> 
    <xsl:call-template name="my:attributes" > 
        <xsl:with-param name="elemType" select="'node'"/>
        <xsl:with-param name="prefix" select="'label=&quot;{'"/>
        <xsl:with-param name="separator" select="' | '"/>
        <xsl:with-param name="postfix" select="'}&quot;'"/>
    </xsl:call-template>
    <xsl:text>];
    </xsl:text> 
  </xsl:template>

  <xsl:template match="g:edge">
    <xsl:value-of select="@source" /> 
    <xsl:call-template name="my:arrow" /> 
    <xsl:value-of select="@target" /> 
    <xsl:text> [</xsl:text> 
    <xsl:call-template name="my:attributes" > 
        <xsl:with-param name="elemType" select="'edge'"/>
        <xsl:with-param name="prefix" select="'label=&quot;'"/>
        <xsl:with-param name="separator" select="'\l'"/>
        <xsl:with-param name="postfix" select="'&quot;'"/>
    </xsl:call-template>
    <xsl:text>];
    </xsl:text> 
  </xsl:template>


  <xsl:template name="my:attributes">
    <xsl:param name="elemType" />
    <xsl:param name="prefix" />
    <xsl:param name="separator" />
    <xsl:param name="postfix" />

    <xsl:value-of select="$prefix"/> 

    <xsl:call-template name="my:defined-attributes" > 
        <xsl:with-param name="elemType" select="$elemType"/>
        <xsl:with-param name="separator" select="$separator"/>
    </xsl:call-template>

    <xsl:call-template name="my:undefined-attributes" > 
        <xsl:with-param name="elemType" select="$elemType"/>
        <xsl:with-param name="separator" select="$separator"/>
    </xsl:call-template>

    <xsl:value-of select="$postfix"/> 
  </xsl:template>

  <xsl:template name="my:defined-attributes">
    <xsl:param name="elemType" />
    <xsl:param name="separator" />

<!--    <xsl:for-each select="g:data[@key != 'labelV' and @key != 'labelE']"> -->
    <xsl:for-each select="g:data">
        <xsl:variable name="currKey" select="./@key"/>
        <xsl:if test="position() > 1"> 
          <xsl:value-of select="$separator"/> 
        </xsl:if>

        <xsl:value-of select="//g:key[@id=$currKey and @for=$elemType]/@attr.name"/> 
        <xsl:text> : </xsl:text>
        <xsl:value-of select="."/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="my:undefined-attributes">
    <xsl:param name="elemType" />
    <xsl:param name="separator" />

    <xsl:variable name="definedData" select="g:data"/>

    <xsl:for-each select="//g:key[@for=$elemType and not($definedData/@key=./@id)]">

      <xsl:if test="exists(g:default)">
        <xsl:if test="exists($definedData) or position() > 1"> 
            <xsl:value-of select="$separator"/> 
        </xsl:if>

        <xsl:variable name="currKey" select="./@id"/>
        <xsl:value-of select="//g:key[@id=$currKey]/@attr.name"/> 
        <xsl:text> : </xsl:text>
        <xsl:value-of select="."/> 
      </xsl:if>
    </xsl:for-each>

  </xsl:template>

  <xsl:template name="my:graphtype">
    <xsl:choose>
      <xsl:when test="//g:graph/@edgedefault = 'undirected'">graph</xsl:when>
      <xsl:when test="//g:graph/@edgedefault = 'directed'">digraph</xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">
          ERROR: unknown value of //g:graph/@edgedefault:
          <xsl:value-of select="//g:graph/@edgedefault"/>
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template> 

  <xsl:template name="my:arrow">
    <xsl:choose>
      <xsl:when test="//g:graph/@edgedefault = 'undirected'">--</xsl:when>
      <xsl:when test="//g:graph/@edgedefault = 'directed'">-></xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">
          ERROR: unknown value of //g:graph/@edgedefault:
          <xsl:value-of select="//g:graph/@edgedefault"/>
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template> 



  <xsl:template match="*">
        <xsl:message terminate="yes">
ERROR: unknown element:
          <xsl:value-of select="."/>
        </xsl:message>
  </xsl:template>
</xsl:stylesheet>

