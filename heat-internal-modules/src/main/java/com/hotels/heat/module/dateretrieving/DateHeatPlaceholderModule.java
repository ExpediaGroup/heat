/**
 * Copyright (C) 2015-2017 Expedia Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hotels.heat.module.dateretrieving;

import java.util.HashMap;
import java.util.Map;

import com.hotels.heat.core.dto.HeatTestDetails;
import com.hotels.heat.core.heatmodules.HeatPlaceholderModule;

/**
 * Module to handle dates.
 */
public final class DateHeatPlaceholderModule implements HeatPlaceholderModule {

    public static final String DEFAULT_PRELOADED_VALUE = "DEFAULT";
    public static final String TODAY_PLACEHOLDER_KEYWORD = "TODAY";
    public static final String TODAY_PLACEHOLDER = "${" + TODAY_PLACEHOLDER_KEYWORD; //${TODAY
    public static final String TODAY_PLACEHOLDER_DEFAULT_PATTERN = "YYYY-MM-dd";
    private static DateHeatPlaceholderModule dateHeatPlaceholderModuleInstance;

    private DateHeatPlaceholderModule() {
    }

    public static DateHeatPlaceholderModule getInstance() {
        if (dateHeatPlaceholderModuleInstance == null) {
            dateHeatPlaceholderModuleInstance = new DateHeatPlaceholderModule();
        }
        return dateHeatPlaceholderModuleInstance;
    }

    @Override
    public Map<String, String> process(String stringToProcess, HeatTestDetails testDetails) {
        Map<String, String> processedMap = new HashMap<>();
        processedMap.put(DateHeatPlaceholderModule.DEFAULT_PRELOADED_VALUE, stringToProcess);

        DateHandler dateHandler = new DateHandler(testDetails.getTestDescription());
        if (stringToProcess.contains(DateHeatPlaceholderModule.TODAY_PLACEHOLDER)) {
            processedMap.put(DateHeatPlaceholderModule.DEFAULT_PRELOADED_VALUE, dateHandler.changeDatesPlaceholders(true, stringToProcess));
        }
        return processedMap;
    }

}
