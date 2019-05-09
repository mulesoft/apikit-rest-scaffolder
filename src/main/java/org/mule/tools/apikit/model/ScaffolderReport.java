/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import org.mule.parser.service.result.ParsingIssue;

import java.util.ArrayList;
import java.util.List;

/**
 *  Collects information while scaffolding an API
 *
 *  The purpose is to notify scaffolding result
 *
 */
public class ScaffolderReport {

  private final String vendorId;
  private final String version;
  private final Status status;
  private final List<ParsingIssue> scaffoldingErrors;

  private ScaffolderReport(String vendorId, String version, Status status, List<ParsingIssue> errors) {
    this.vendorId = vendorId;
    this.version = version;
    this.status = status;
    this.scaffoldingErrors = errors;
  }

  public String getVersion() {
    return version;
  }

  public String getStatus() {
    return status.toString();
  }

  public String getVendorId() {
    return vendorId;
  }

  public List<ParsingIssue> getScaffoldingErrors() {
    return scaffoldingErrors;
  }

  public static class Builder {

    private String vendorId;

    private String version;

    private Status status;

    private final List<ParsingIssue> parsingErrors;

    public Builder() {
      this.parsingErrors = new ArrayList<>();
    }

    public Builder withVendorId(String vendorId) {
      this.vendorId = vendorId;
      return this;
    }

    public Builder withVersion(String version) {
      this.version = version;
      return this;
    }

    public Builder withStatus(Status status) {
      this.status = status;
      return this;
    }

    public Builder withScaffoldingErrors(List<ParsingIssue> errors) {
      this.parsingErrors.addAll(errors);
      return this;
    }

    public ScaffolderReport build() {
      return new ScaffolderReport(vendorId, version, status, parsingErrors);
    }
  }
}
