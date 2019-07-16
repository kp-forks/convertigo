<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output encoding="ISO-8859-1" media-type="text/plain" indent="yes"/>
	<xsl:include href="./EJBUtils.xsl"/>
	<xsl:template match="/child::*[local-name()='definitions']">	
package com.twinsoft.convertigo.ejb;

/**
 * Home interface for <xsl:call-template name="getClassNameOfCurrent"/>EJB.
 * Automatically generated by twinsoft Convertigo.
 */
public interface <xsl:call-template name="getClassNameOfCurrent"/>EJBHome extends javax.ejb.EJBHome{
	public static final String COMP_NAME = "java:comp/env/ejb/<xsl:call-template name="getClassNameOfCurrent"/>EJB";
   	public static final String JNDI_NAME = "<xsl:call-template name="getClassNameOfCurrent"/>Bean";

	public com.twinsoft.convertigo.ejb.<xsl:call-template name="getClassNameOfCurrent"/>EJB create() throws javax.ejb.CreateException,java.rmi.RemoteException;
	
	public com.twinsoft.convertigo.ejb.<xsl:call-template name="getClassNameOfCurrent"/>EJB create(java.net.URL portAddress) throws javax.ejb.CreateException,java.rmi.RemoteException;
}
	</xsl:template>
</xsl:stylesheet>