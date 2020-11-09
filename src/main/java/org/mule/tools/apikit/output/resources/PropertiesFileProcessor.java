/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.resources;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesFileProcessor extends FileProcessor {

    public static final String HTTP_PORT_KEY = "http.port";
    public static final String HTTP_HOST_KEY = "http.host";

    @Override
    protected String processCommon(Map<String, Object> configuration, String apiAutodiscoveryId) {
        Properties properties = new Properties();
        createHttpProperties(configuration, properties);
        boolean hasApiAutodiscoveryIdForEnvironment = hasApiAutodiscoveryIdForEnvironment(configuration);
        if (apiAutodiscoveryId != null) {
            String apiAutodiscoveryIdValue =
                    getApiAutodiscoveryID(configuration, apiAutodiscoveryId, hasApiAutodiscoveryIdForEnvironment);
            properties.setProperty(API_ID_KEY, apiAutodiscoveryIdValue);
        }
        return createResult(properties);
    }

    private void createHttpProperties(Map<String, Object> configuration, Properties properties) {
        String host = HTTP_HOST_VALUE;
        String port = HTTP_PORT_VALUE;
        if (configuration != null && configuration.get("http") == null) {
            //      Object http = configuration.get("http");
            //      Map<String, String> httpMap = new ObjectMapper().convertValue(http, Map.class);
            //      host = httpMap.get("host");
            //      port = String.valueOf(httpMap.get("port"));
            properties.setProperty(HTTP_HOST_KEY, host);
            properties.setProperty(HTTP_PORT_KEY, port);
        }
    }

    private String createResult(Properties properties) {
        String result;
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        properties.list(printWriter);
        result = writer.toString().replace("-- listing properties --\n", "");
        return result;
    }

    @Override
    protected String processCustom(Map<String, Object> properties) {
        Properties propertiesFile = new Properties();
        if (properties != null) {
            for (String key : properties.keySet()) {
                Object value = properties.get(key);
                processValue(propertiesFile, key, value);
            }
        }
        return createResult(propertiesFile);
    }

    private void processValue(Properties propertiesFile, String key, Object value) {
        if (value instanceof String) {
            propertiesFile.setProperty(key, value.toString());
        }
        if (value instanceof HashMap) {
            ObjectMapper oMapper = new ObjectMapper();
            Map<String, Object> map = oMapper.convertValue(value, Map.class);
            for (String objectKey : map.keySet()) {
                propertiesFile.setProperty(key + "." + objectKey, map.get(objectKey).toString());
            }
        }
    }
}
