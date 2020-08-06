/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.Assert.assertEquals;
import static org.mule.tools.apikit.Helper.countOccurences;

public class XmlOccurrencesAsserterBuilder {

  private XmlOccurrencesAsserter xmlOccurrencesAsserter = new XmlOccurrencesAsserter();

  public XmlOccurrencesAsserterBuilder withHttpResponseStatusCode200Count(int count) {
    xmlOccurrencesAsserter.addAssertion(XmlOccurrencesAsserter.HTTP_RESPONSE_STATUS_CODE_200, count);
    return this;
  }

  public XmlOccurrencesAsserterBuilder withHttpResponseStatusCode500Count(int count) {
    xmlOccurrencesAsserter.addAssertion(XmlOccurrencesAsserter.HTTP_RESPONSE_STATUS_CODE_500, count);
    return this;
  }

  public XmlOccurrencesAsserterBuilder withOnErrorPropagateCount(int count) {
    xmlOccurrencesAsserter.addAssertion(XmlOccurrencesAsserter.ON_ERROR_PROPAGATE, count);
    return this;
  }

  public XmlOccurrencesAsserterBuilder withEEMessageTagCount(int count) {
    xmlOccurrencesAsserter.addAssertion(XmlOccurrencesAsserter.EE_MESSAGE, count);
    return this;
  }

  public XmlOccurrencesAsserterBuilder withEEVariablesTagCount(int count) {
    xmlOccurrencesAsserter.addAssertion(XmlOccurrencesAsserter.EE_VARIABLES, count);
    return this;
  }

  public XmlOccurrencesAsserterBuilder withEESetVariableTagCount(int count) {
    xmlOccurrencesAsserter.addAssertion(XmlOccurrencesAsserter.EE_SET_VARIABLE, count);
    return this;
  }

  public XmlOccurrencesAsserterBuilder withEESetPayloadTagCount(int count) {
    xmlOccurrencesAsserter.addAssertion(XmlOccurrencesAsserter.EE_SET_PAYLOAD, count);
    return this;
  }

  public XmlOccurrencesAsserterBuilder withHttpBodyCount(int count) {
    xmlOccurrencesAsserter.addAssertion(XmlOccurrencesAsserter.HTTP_BODY, count);
    return this;
  }

  public XmlOccurrencesAsserterBuilder withHttpHeadersCount(int count) {
    xmlOccurrencesAsserter.addAssertion(XmlOccurrencesAsserter.HTTP_HEADERS, count);
    return this;
  }

  public XmlOccurrencesAsserterBuilder withDWPayloadExpressionCount(int count) {
    xmlOccurrencesAsserter.addAssertion(XmlOccurrencesAsserter.DW_PAYLOAD, count);
    return this;
  }

  public XmlOccurrencesAsserterBuilder withHttplListenerConfigCount(int count) {
    xmlOccurrencesAsserter.addAssertion(XmlOccurrencesAsserter.HTTP_LISTENER_CONFIG, count);
    return this;
  }

  public XmlOccurrencesAsserterBuilder withHttplListenerCount(int count) {
    xmlOccurrencesAsserter.addAssertion(XmlOccurrencesAsserter.HTTP_LISTENER, count);
    return this;
  }

  public XmlOccurrencesAsserterBuilder withInterpretRequestErrorsEnabledCount(int count) {
    xmlOccurrencesAsserter.addAssertion(XmlOccurrencesAsserter.INTERPRET_REQUEST_ERRORS_ENABLED, count);
    return this;
  }

  public XmlOccurrencesAsserterBuilder withInterpretRequestErrorsDisabledCount(int count) {
    xmlOccurrencesAsserter.addAssertion(XmlOccurrencesAsserter.INTERPRET_REQUEST_ERRORS_DISABLED, count);
    return this;
  }

  public XmlOccurrencesAsserterBuilder withApikitConsoleCount(int count) {
    xmlOccurrencesAsserter.addAssertion(XmlOccurrencesAsserter.APIKIT_CONSOLE, count);
    return this;
  }

  public XmlOccurrencesAsserterBuilder withApikitConsoleEnabledCount(int count) {
    xmlOccurrencesAsserter.addAssertion(XmlOccurrencesAsserter.APIKIT_CONSOLE_ENABLED, count);
    return this;
  }

  public XmlOccurrencesAsserterBuilder withApikitConsoleDisabledCount(int count) {
    xmlOccurrencesAsserter.addAssertion(XmlOccurrencesAsserter.APIKIT_CONSOLE_DISABLED, count);
    return this;
  }

  public XmlOccurrencesAsserterBuilder withLoggerInfoCount(int count) {
    xmlOccurrencesAsserter.addAssertion(XmlOccurrencesAsserter.LOGGER_LEVEL_INFO_MESSAGE, count);
    return this;
  }

  public XmlOccurrencesAsserterBuilder withHttpHeadersOutboundHeadersDefaultCount(int count) {
    xmlOccurrencesAsserter.addAssertion(XmlOccurrencesAsserter.HTTP_HEADERS_OUTBOUND_HEADERS_DEFAULT, count);
    return this;
  }

  public XmlOccurrencesAsserterBuilder withHttpInboundCount(int count) {
    xmlOccurrencesAsserter.addAssertion(XmlOccurrencesAsserter.HTTP_INBOUND_TAG, count);
    return this;
  }

  public XmlOccurrencesAsserterBuilder withHttpInboundEndpointCount(int count) {
    xmlOccurrencesAsserter.addAssertion(XmlOccurrencesAsserter.HTTP_INBOUND_ENDPOINT, count);
    return this;
  }

  public XmlOccurrencesAsserterBuilder withErrorHandlerTagCount(int count) {
    xmlOccurrencesAsserter.addAssertion(XmlOccurrencesAsserter.ERROR_HANDLER, count);
    return this;
  }

  public XmlOccurrencesAsserterBuilder withExtensionEnabledCount(int count) {
    xmlOccurrencesAsserter.addAssertion(XmlOccurrencesAsserter.EXTENSION_ENABLED, count);
    return this;
  }

  public XmlOccurrencesAsserterBuilder withExceptionStrategyCount(int count) {
    xmlOccurrencesAsserter.addAssertion(XmlOccurrencesAsserter.EXCEPTION_STRATEGY, count);
    return this;
  }

  public XmlOccurrencesAsserterBuilder withCustomAssertionCount(String assertionString, int count) {
    xmlOccurrencesAsserter.addAssertion(assertionString, count);
    return this;
  }

  public XmlOccurrencesAsserter build() {
    return xmlOccurrencesAsserter;
  }

  public static class XmlOccurrencesAsserter {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlOccurrencesAsserter.class);

    private static final String HTTP_RESPONSE_STATUS_CODE_200 = "<http:response statusCode=\"#[vars.httpStatus default 200]\"";
    private static final String HTTP_RESPONSE_STATUS_CODE_500 =
        "<http:error-response statusCode=\"#[vars.httpStatus default 500]\"";
    private static final String ON_ERROR_PROPAGATE = "<on-error-propagate";
    private static final String EE_SET_PAYLOAD = "<ee:set-payload>";
    private static final String HTTP_BODY = "http:body";
    private static final String HTTP_HEADERS = "http:headers";
    private static final String INTERPRET_REQUEST_ERRORS_ENABLED = "interpretRequestErrors=\"true\"";
    private static final String INTERPRET_REQUEST_ERRORS_DISABLED = "interpretRequestErrors=\"false\"";
    private static final String HTTP_LISTENER_CONFIG = "<http:listener-config";
    private static final String HTTP_LISTENER = "<http:listener ";
    private static final String HTTP_HEADERS_OUTBOUND_HEADERS_DEFAULT =
        "<http:headers>#[vars.outboundHeaders default {}]</http:headers>";
    private static final String DW_PAYLOAD = "#[payload]";
    private static final String EE_MESSAGE = "<ee:message>";
    private static final String EE_VARIABLES = "<ee:variables>";
    private static final String EE_SET_VARIABLE = "<ee:set-variable";
    private static final String APIKIT_CONSOLE = "<apikit:console";
    private static final String APIKIT_CONSOLE_DISABLED = "consoleEnabled=\"false\"";
    private static final String APIKIT_CONSOLE_ENABLED = "consoleEnabled=\"true\"";
    private static final String LOGGER_LEVEL_INFO_MESSAGE = "<logger level=\"INFO\" message=";
    private static final String HTTP_INBOUND_TAG = "<http:inbound";
    private static final String HTTP_INBOUND_ENDPOINT = "http:inbound-endpoint";
    private static final String ERROR_HANDLER = "<error-handler name=";
    private static final String EXTENSION_ENABLED = "extensionEnabled";
    private static final String EXCEPTION_STRATEGY = "exception-strategy";

    private Map<String, Integer> xmlOccurrencesMap = new LinkedHashMap<>();

    protected XmlOccurrencesAsserter() {
      xmlOccurrencesMap.put(HTTP_RESPONSE_STATUS_CODE_200, 0);
      xmlOccurrencesMap.put(HTTP_RESPONSE_STATUS_CODE_500, 0);
      xmlOccurrencesMap.put(ON_ERROR_PROPAGATE, 0);
      xmlOccurrencesMap.put(EE_SET_PAYLOAD, 0);
      xmlOccurrencesMap.put(HTTP_BODY, 0);
      xmlOccurrencesMap.put(HTTP_HEADERS, 0);
      xmlOccurrencesMap.put(INTERPRET_REQUEST_ERRORS_ENABLED, 0);
      xmlOccurrencesMap.put(INTERPRET_REQUEST_ERRORS_DISABLED, 0);
      xmlOccurrencesMap.put(HTTP_LISTENER_CONFIG, 0);
      xmlOccurrencesMap.put(HTTP_LISTENER, 0);
      xmlOccurrencesMap.put(HTTP_HEADERS_OUTBOUND_HEADERS_DEFAULT, 0);
      xmlOccurrencesMap.put(DW_PAYLOAD, 0);
      xmlOccurrencesMap.put(EE_MESSAGE, 0);
      xmlOccurrencesMap.put(EE_VARIABLES, 0);
      xmlOccurrencesMap.put(EE_SET_VARIABLE, 0);
      xmlOccurrencesMap.put(APIKIT_CONSOLE, 0);
      xmlOccurrencesMap.put(APIKIT_CONSOLE_DISABLED, 0);
      xmlOccurrencesMap.put(APIKIT_CONSOLE_ENABLED, 0);
      xmlOccurrencesMap.put(LOGGER_LEVEL_INFO_MESSAGE, 0);
      xmlOccurrencesMap.put(HTTP_INBOUND_TAG, 0);
      xmlOccurrencesMap.put(HTTP_INBOUND_ENDPOINT, 0);
      xmlOccurrencesMap.put(ERROR_HANDLER, 0);
      xmlOccurrencesMap.put(EXTENSION_ENABLED, 0);
      xmlOccurrencesMap.put(EXCEPTION_STRATEGY, 0);
    }

    protected void addAssertion(String tagString, int count) {
      xmlOccurrencesMap.put(tagString, count);
    }

    public void assertOccurrences(String xml) {
      for (Entry<String, Integer> occurrencesForTagString : xmlOccurrencesMap.entrySet()) {
        Integer value = occurrencesForTagString.getValue();
        String tagString = occurrencesForTagString.getKey();
        int occurrences = countOccurences(xml, tagString);
        try {
          assertEquals(value.intValue(), occurrences);
        } catch (AssertionError e) {
          LOGGER.error("Expected count for \"{}\" is {}, but it was {}", tagString, value, occurrences);
          throw e;
        }
      }
    }

  }

}
