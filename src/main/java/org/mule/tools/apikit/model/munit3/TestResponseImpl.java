/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model.munit3;

import amf.client.model.domain.Payload;
import amf.client.model.domain.Response;

import javax.annotation.Nullable;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public class TestResponseImpl implements TestResponse {

  private String statusCode;
  private String contentType;
  private Object content;

  public TestResponseImpl(@Nullable Response response, @Nullable Payload payload) {
        this.statusCode = ofNullable(response).map(res -> res.statusCode().value()).orElse(null);
        this.contentType = ofNullable(payload).map(payl -> payl.mediaType().value()).orElse(null);
        ofNullable(payload).ifPresent(content -> setContent(contentType, content));
    }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<String> getStatusCode() {
    return ofNullable(statusCode);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<String> getContentType() {
    return ofNullable(contentType);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<Object> getContent() {
    return ofNullable(content);
  }

  private void setContent(String contentType, Payload payload) {
//    this.content = buildPayload(payload.schema(), contentType);;
  }
}
