/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.apikit.output.scopes;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Test;
import org.mule.tools.apikit.Helper;
import org.mule.tools.apikit.model.APIKitConfig;
import org.mule.tools.apikit.model.ApikitMainFlowContainer;
import org.mule.tools.apikit.model.HttpListenerConfig;

import java.io.StringReader;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.tools.apikit.TestUtils.getSaxBuilder;

public class ConsoleScopeTest {

  @Test
  public void testGenerateConsoleFlow() throws Exception {
    Document document = new Document();
    Element mule = new MuleScope(false, false).generate();
    document.setRootElement(mule);
    APIKitConfig config = new APIKitConfig();
    config.setApi("path/to/file.raml");
    config.setExtensionEnabled(true);

    mule.addContent(new APIKitConfigScope(config).generate());

    ApikitMainFlowContainer api = mock(ApikitMainFlowContainer.class);
    HttpListenerConfig listenerConfig =
        new HttpListenerConfig("HTTP_Listener_Configuration", "localhost", "7777", "HTTP", "");

    when(api.getId()).thenReturn("file");
    when(api.getPath()).thenReturn("/api/*");
    when(api.getConfig()).thenReturn(config);
    when(api.getHttpListenerConfig()).thenReturn(listenerConfig);

    mule.addContent(new HttpListenerConfigScope(listenerConfig).generate());
    mule.addContent(new FlowScope(api, true).generate());
    mule.addContent(new ConsoleFlowScope(api, true).generate());

    String muleConfigContentAsString = Helper.nonSpaceOutput(mule);
    XMLUnit.setIgnoreWhitespace(true);

    String control =
        "<mule xmlns=\"http://www.mulesoft.org/schema/mule/core\" xmlns:apikit=\"http://www.mulesoft.org/schema/mule/mule-apikit\" xmlns:doc=\"http://www.mulesoft.org/schema/mule/documentation\" xmlns:http=\"http://www.mulesoft.org/schema/mule/http\" xmlns:spring=\"http://www.springframework.org/schema/beans\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd http://www.mulesoft.org/schema/mule/mule-apikit http://www.mulesoft.org/schema/mule/mule-apikit/current/mule-apikit.xsd http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.1.xsd \"><apikit:config api=\"path/to/file.raml\" extensionEnabled=\"true\" outboundHeadersMapName=\"outboundHeaders\" httpStatusVarName=\"httpStatus\" /><http:listener-config name=\"HTTP_Listener_Configuration\"><http:listener-connection host=\"localhost\" port=\"7777\" /></http:listener-config><flow name=\"file-main\"><http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/api/*\"><http:response statusCode=\"#[vars.httpStatus default 200]\"><http:headers>#[vars.outboundHeaders default {}]</http:headers></http:response><http:error-response statusCode=\"#[vars.httpStatus default 500]\"><http:body>#[payload]</http:body><http:headers>#[vars.outboundHeaders default {}]</http:headers></http:error-response></http:listener><apikit:router /><error-handler><on-error-propagate type=\"APIKIT:BAD_REQUEST\"><ee:transform doc:name=\"Transform Message\" xmlns:ee=\"http://www.mulesoft.org/schema/mule/ee/core\" xsi:schemaLocation=\"http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd\"><ee:message><ee:set-payload><![CDATA[%dw 2.0 output application/json --- {message: \"Bad request\"}]]></ee:set-payload></ee:message><ee:variables><ee:set-variable variableName=\"httpStatus\">400</ee:set-variable></ee:variables></ee:transform></on-error-propagate><on-error-propagate type=\"APIKIT:NOT_FOUND\"><ee:transform doc:name=\"Transform Message\" xmlns:ee=\"http://www.mulesoft.org/schema/mule/ee/core\" xsi:schemaLocation=\"http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd\"><ee:message><ee:set-payload><![CDATA[%dw 2.0 output application/json --- {message: \"Resource not found\"}]]></ee:set-payload></ee:message><ee:variables><ee:set-variable variableName=\"httpStatus\">404</ee:set-variable></ee:variables></ee:transform></on-error-propagate><on-error-propagate type=\"APIKIT:METHOD_NOT_ALLOWED\"><ee:transform doc:name=\"Transform Message\" xmlns:ee=\"http://www.mulesoft.org/schema/mule/ee/core\" xsi:schemaLocation=\"http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd\"><ee:message><ee:set-payload><![CDATA[%dw 2.0 output application/json --- {message: \"Method not allowed\"}]]></ee:set-payload></ee:message><ee:variables><ee:set-variable variableName=\"httpStatus\">405</ee:set-variable></ee:variables></ee:transform></on-error-propagate><on-error-propagate type=\"APIKIT:NOT_ACCEPTABLE\"><ee:transform doc:name=\"Transform Message\" xmlns:ee=\"http://www.mulesoft.org/schema/mule/ee/core\" xsi:schemaLocation=\"http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd\"><ee:message><ee:set-payload><![CDATA[%dw 2.0 output application/json --- {message: \"Not acceptable\"}]]></ee:set-payload></ee:message><ee:variables><ee:set-variable variableName=\"httpStatus\">406</ee:set-variable></ee:variables></ee:transform></on-error-propagate><on-error-propagate type=\"APIKIT:UNSUPPORTED_MEDIA_TYPE\"><ee:transform doc:name=\"Transform Message\" xmlns:ee=\"http://www.mulesoft.org/schema/mule/ee/core\" xsi:schemaLocation=\"http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd\"><ee:message><ee:set-payload><![CDATA[%dw 2.0 output application/json --- {message: \"Unsupported media type\"}]]></ee:set-payload></ee:message><ee:variables><ee:set-variable variableName=\"httpStatus\">415</ee:set-variable></ee:variables></ee:transform></on-error-propagate><on-error-propagate type=\"APIKIT:NOT_IMPLEMENTED\"><ee:transform doc:name=\"Transform Message\" xmlns:ee=\"http://www.mulesoft.org/schema/mule/ee/core\" xsi:schemaLocation=\"http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd\"><ee:message><ee:set-payload><![CDATA[%dw 2.0 output application/json --- {message: \"Not Implemented\"}]]></ee:set-payload></ee:message><ee:variables><ee:set-variable variableName=\"httpStatus\">501</ee:set-variable></ee:variables></ee:transform></on-error-propagate></error-handler></flow><flow name=\"file-console\"><http:listener config-ref=\"HTTP_Listener_Configuration\" path=\"/console/*\"><http:response statusCode=\"#[vars.httpStatus default 200]\"><http:headers>#[vars.outboundHeaders default {}]</http:headers></http:response><http:error-response statusCode=\"#[vars.httpStatus default 500]\"><http:body>#[payload]</http:body><http:headers>#[vars.outboundHeaders default {}]</http:headers></http:error-response></http:listener><apikit:console /><error-handler><on-error-propagate type=\"APIKIT:NOT_FOUND\"><ee:transform doc:name=\"Transform Message\" xmlns:ee=\"http://www.mulesoft.org/schema/mule/ee/core\" xsi:schemaLocation=\"http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd\"><ee:message><ee:set-payload><![CDATA[%dw 2.0 output application/json --- {message: \"Resource not found\"}]]></ee:set-payload></ee:message><ee:variables><ee:set-variable variableName=\"httpStatus\">404</ee:set-variable></ee:variables></ee:transform></on-error-propagate></error-handler></flow></mule>";

    getSaxBuilder().build(new StringReader(control));
    Diff diff = XMLUnit.compareXML(control, muleConfigContentAsString);

    assertTrue(diff.toString(), diff.similar());
  }
}
