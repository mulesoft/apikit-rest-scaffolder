/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.schemas;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Map;

public class JsonSchemaDataGenerator {

  private static final String OBJECT = "object";
  private static final String ARRAY = "array";
  private static final String BOOLEAN = "boolean";
  private static final String INTEGER = "integer";
  private static final String NUMBER = "number";
  private static final String NULL = "null";
  private static final String STRING = "string";


  public String buildExamplePayloadFromJsonSchema(JsonObject jsonObj) {

    StringBuilder jsonPayload = new StringBuilder();
    jsonPayload.append("{");

    if (OBJECT.equals(getJsonType(jsonObj.getAsJsonPrimitive("type")))) {

      JsonObject properties = jsonObj.getAsJsonObject("properties");
      if (properties != null) {
        for (Map.Entry<String, JsonElement> p : properties.entrySet()) {
          String id = p.getKey();
          String value = buildExamplePayloadFromJsonSchema((JsonObject) p.getValue());

          jsonPayload
              .append("\"")
              .append(id)
              .append("\"")
              .append(":")
              .append(value)
              .append(",");
        }

        // remove last comma
        jsonPayload.deleteCharAt(jsonPayload.lastIndexOf(","));
      }
    } else {
      if (ARRAY.equals(jsonObj.getAsJsonPrimitive("type").getAsString())) {
        return "[]";
      }
      if (BOOLEAN.equals(jsonObj.getAsJsonPrimitive("type").getAsString())) {
        return "false";
      }
      if (INTEGER.equals(jsonObj.getAsJsonPrimitive("type").getAsString())) {
        return "0";
      }
      if (NUMBER.equals(jsonObj.getAsJsonPrimitive("type").getAsString())) {
        return "0";
      }
      if (NULL.equals(jsonObj.getAsJsonPrimitive("type").getAsString())) {
        return "null";
      }
      if (STRING.equals(jsonObj.getAsJsonPrimitive("type").getAsString())) {
        return "\"\"";
      }
    }

    jsonPayload.append("}");
    return jsonPayload.toString();
  }

  private String getJsonType(JsonPrimitive type) {
    if (type != null) {
      return type.getAsString();
    }
    return OBJECT;
  }

}
