/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.helpers;

import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.util.MultiMap;

import java.util.LinkedList;
import java.util.Map;

import static org.mule.runtime.api.metadata.MediaType.parse;

public class AttributesHelper {

  private AttributesHelper() {
    // Prevents instantiation :)
  }

  public static MultiMap<String, String> addParam(MultiMap<String, String> oldParams, String key, String value) {
    MultiMap<String, String> mapParam = new MultiMap<>();
    LinkedList<String> valueList = new LinkedList<>();
    valueList.add(value);
    mapParam.put(key, valueList);
    for (Map.Entry<String, String> entry : oldParams.entrySet()) {
      LinkedList<String> list = new LinkedList<>();
      list.add(entry.getValue());
      mapParam.put(entry.getKey(), list);
    }
    return mapParam;
  }

  public static String getMediaType(String mediaType) {
    MediaType mType = parse(mediaType);
    return String.format("%s/%s", mType.getPrimaryType(), mType.getSubType());
  }

}
