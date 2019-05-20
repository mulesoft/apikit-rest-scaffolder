/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output;

import org.mule.tools.apikit.input.APIDiff;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GenerationStrategy {

  public GenerationStrategy() {}

  public List<GenerationModel> generate(Map<ResourceActionMimeTypeTriplet, GenerationModel> ramlFilesEntries,
                                        Set<API> apisInConfigs, Set<ResourceActionMimeTypeTriplet> flowEntries) {

    Set<API> apisInMuleConfigs = apisInConfigs;

    Set<ResourceActionMimeTypeTriplet> ramlEntries = ramlFilesEntries.keySet();

    Set<ResourceActionMimeTypeTriplet> muleFlowEntries = flowEntries;

    List<GenerationModel> generationModels = new ArrayList<>();

    if (apisInMuleConfigs.isEmpty()) {
      generationModels.addAll(ramlFilesEntries.values());
    } else {
      if (ramlEntries.isEmpty()) {
        // there are implemented APIs without a RAML file. NOMB.
        String xmlFilesWithoutRaml = "";

        for (API api : apisInMuleConfigs) {
          xmlFilesWithoutRaml = xmlFilesWithoutRaml + " " + api.getXmlFile().getAbsolutePath();
        }
        generationModels.addAll(ramlFilesEntries.values());
      } else {
        Set<ResourceActionMimeTypeTriplet> diffTriplets = new APIDiff(ramlEntries, muleFlowEntries).getEntries();
        for (ResourceActionMimeTypeTriplet entry : diffTriplets) {
          if (ramlFilesEntries.containsKey(entry)) {
            generationModels.add(ramlFilesEntries.get(entry));
          }
        }
      }
    }

    Collections.sort(generationModels);
    return generationModels;
  }
}
