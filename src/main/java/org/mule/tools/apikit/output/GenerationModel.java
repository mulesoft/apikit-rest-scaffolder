/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import org.mule.module.apikit.helpers.FlowName;
import org.mule.apikit.model.Action;
import org.mule.apikit.model.Resource;
import org.mule.apikit.model.Response;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.tools.apikit.model.ApikitMainFlowContainer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toMap;
import static org.mule.module.apikit.helpers.AttributesHelper.getMediaType;
import static org.mule.module.apikit.helpers.FlowName.FLOW_NAME_SEPARATOR;
import static org.mule.runtime.api.metadata.MediaType.parse;

public class GenerationModel implements Comparable<GenerationModel> {

  private static final String OAS_DEFAULT_STATUS_CODE = "default";

  private final String verb;
  private final Action action;
  private final Resource resource;
  private final String mimeType;
  private final String version;
  private final List<String> splitPath;
  private final ApikitMainFlowContainer api;

  public GenerationModel(ApikitMainFlowContainer api, String version, Resource resource, Action action) {
    this(api, version, resource, action, null);
  }

  public GenerationModel(ApikitMainFlowContainer api, String version, Resource resource, Action action, String mimeType) {
    this.api = api;
    Validate.notNull(api);
    Validate.notNull(action);
    Validate.notNull(action.getType());
    Validate.notNull(resource.getResolvedUri(version));

    this.resource = resource;
    this.action = action;
    this.splitPath = new ArrayList<>(Arrays.asList(this.resource.getResolvedUri(version).split("/")));
    this.verb = action.getType().toString();
    this.mimeType = mimeType;
    this.version = version;
    if (!splitPath.isEmpty()) {
      splitPath.remove(0);
      splitPath.remove(0);
    }
  }

  public String getVerb() {
    return verb;
  }

  public String getStringFromActionType() {
    switch (action.getType()) {
      case GET:
        return "retrieve";
      case POST:
        return "update";
      case PUT:
        return "create";
      case DELETE:
        return "delete";
      default:
        return action.getType().toString().toLowerCase();
    }
  }

  public String getExampleWrapper() {
    Map<String, Response> responses = action.getResponses();

    return getExampleWrapper(responses);
  }

  private String getExampleWrapper(Map<String, Response> responses) {
    // filter responses with status codes between 200 and 300 from all responses
    final LinkedHashMap<String, Response> validResponses = responses.entrySet().stream()
        .sorted(getStatusCodeComparator())
        .collect(toMap((Map.Entry<String, Response> e) -> OAS_DEFAULT_STATUS_CODE.equalsIgnoreCase(e.getKey()) ? "200"
            : e.getKey(),
                       Map.Entry::getValue, (k, v) -> v,
                       LinkedHashMap::new));

    if (validResponses.isEmpty())
      return null;

    // look for an example for status code 200
    final Response responseOk = validResponses.get("200");

    String example = null;

    if (responseOk != null)
      example = getExampleFromResponse(responseOk);

    // if there's no examples for status code 200, look for one for any status code
    if (example == null) {
      for (Response response : validResponses.values()) {
        example = getExampleFromResponse(response);
        if (example != null)
          break;
      }
    }

    return example;
  }

  private static String getExampleFromResponse(Response response) {
    final Map<String, String> examples = response.getExamples();
    if (examples.isEmpty())
      return null;
    if (examples.containsKey("application/json")) {
      return examples.get("application/json");
    } else {
      return examples.values().iterator().next();
    }
  }

  private static Comparator<Map.Entry<String, Response>> getStatusCodeComparator() {
    return (c1, c2) -> {
      final String c1Key = c1.getKey();
      final String c2Key = c2.getKey();

      if (OAS_DEFAULT_STATUS_CODE.equalsIgnoreCase(c1Key) && OAS_DEFAULT_STATUS_CODE.equalsIgnoreCase(c2Key))
        return 0;

      if (OAS_DEFAULT_STATUS_CODE.equalsIgnoreCase(c1Key))
        return -1;

      if (OAS_DEFAULT_STATUS_CODE.equalsIgnoreCase(c2Key))
        return 1;

      return c1Key.compareTo(c2Key);
    };
  }

  public String getName() {
    StringBuilder name = new StringBuilder();
    name.append(this.getStringFromActionType());
    String resourceName = this.resource.getDisplayName();

    if (resourceName == null) {
      StringBuffer buff = new StringBuffer();
      for (String i : this.splitPath) {
        buff.append(StringUtils.capitalize(i));
      }
      resourceName = buff.toString();
    }

    name.append(resourceName);

    if (this.mimeType != null) {
      MediaType mediaType = parse(mimeType);
      StringBuffer buff = new StringBuffer();
      buff.append(StringUtils.capitalize(mediaType.getPrimaryType()));
      buff.append(StringUtils.capitalize(mediaType.getSubType()));
      name.append(buff.toString());
    }

    return name.toString().replace(" ", "");
  }

  public String getRelativeURI() {
    return "/" + StringUtils.join(splitPath.toArray(), "/");
  }

  public ApikitMainFlowContainer getApi() {
    return api;
  }

  public String getFlowName() {
    StringBuilder flowName = new StringBuilder("");
    flowName.append(action.getType().toString().toLowerCase())
        .append(FLOW_NAME_SEPARATOR)
        .append(resource.getResolvedUri(version));

    if (mimeType != null) {
      flowName.append(FLOW_NAME_SEPARATOR)
          .append(getMediaType(mimeType));
    }


    if (api.getConfig() != null && !StringUtils.isEmpty(api.getConfig().getName())) {
      flowName.append(FLOW_NAME_SEPARATOR)
          .append(api.getConfig().getName());
    }
    return FlowName.encode(flowName.toString());
  }

  @Override
  public int compareTo(@Nonnull GenerationModel generationModel) {
    return this.getName().compareTo(generationModel.getName());
  }

  public List<String> getUriParameters() {
    action.getResolvedUriParameters();
    Pattern uriParamsPattern = Pattern.compile("\\{([^}]+)\\}");
    List<String> result = new ArrayList<>();
    String path = resource.getResolvedUri(version);

    Matcher matcher = uriParamsPattern.matcher(path);
    while (matcher.find()) {
      result.add(matcher.group(1));
    }

    return result;
  }
}
