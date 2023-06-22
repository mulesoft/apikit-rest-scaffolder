/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.spi;

import org.mule.apikit.model.ApiSpecification;
import org.mule.tools.apikit.model.MuleConfig;
import org.mule.tools.apikit.model.MuleDomain;
import org.mule.tools.apikit.model.ScaffolderContext;

import java.util.List;

/**
 * Extension (SPI) for the APIKit Maven Module
 */
public interface ScaffolderService {

  /**
   * Modifies or creates the Mule config files which are contained in the app directory (passed through the muleConfigs parameter)
   * by running the scaffolder on the apiSpec passed as parameter.
   *
   * @param context the context where the scaffolder will be executed. It includes, for example, the runtime version.
   * @param apiSpec the ApiSpecification that will be scaffolded
   * @param muleConfigs all the mule configs contained in the app directory
   * @param domain the mule domain of the application
   */
  void executeScaffolder(ScaffolderContext context, ApiSpecification apiSpec, List<MuleConfig> muleConfigs, MuleDomain domain);

}
