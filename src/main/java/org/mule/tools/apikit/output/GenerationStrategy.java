/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output;

import static java.util.Collections.sort;

import org.mule.tools.apikit.input.APIDiff;
import org.mule.tools.apikit.model.ApikitMainFlowContainer;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GenerationStrategy {

  public GenerationStrategy() {}

  public List<GenerationModel> generate(Map<ResourceActionMimeTypeTriplet, GenerationModel> ramlFilesEntries,
                                        Set<ApikitMainFlowContainer> apisInConfigs,
                                        Set<ResourceActionMimeTypeTriplet> flowEntries) {

    Set<ResourceActionMimeTypeTriplet> ramlEntries = ramlFilesEntries.keySet();
    List<GenerationModel> generationModels = new ArrayList<>();

    if (apisInConfigs.isEmpty()) {
      generationModels.addAll(ramlFilesEntries.values());
    } else {
      if (ramlEntries.isEmpty()) {
        generationModels.addAll(ramlFilesEntries.values());
      } else {
        Set<ResourceActionMimeTypeTriplet> diffTriplets = new APIDiff(ramlEntries, flowEntries).getEntries();
        for (ResourceActionMimeTypeTriplet entry : diffTriplets) {
          if (ramlFilesEntries.containsKey(entry)) {
            generationModels.add(ramlFilesEntries.get(entry));
          }
        }
      }
    }

    sort(generationModels);
    return generationModels;
  }
}
