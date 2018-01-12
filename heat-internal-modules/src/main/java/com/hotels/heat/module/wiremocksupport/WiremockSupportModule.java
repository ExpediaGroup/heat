/**
 * Copyright (C) 2015-2018 Expedia Inc.
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
package com.hotels.heat.module.wiremocksupport;

import java.util.HashMap;
import java.util.Map;

import com.hotels.heat.core.dto.HeatTestDetails;
import com.hotels.heat.core.heatmodules.HeatPlaceholderModule;

/**
 * Wiremock Support Placeholder Module.
 */
public final class WiremockSupportModule implements HeatPlaceholderModule {

    public static final String DEFAULT_PRELOADED_VALUE = "DEFAULT";
    public static final String WIREMOCK_PLACEHOLDER = "wiremock";
    public static final String WIREMOCK_PROP_PLACEHOLDER = "${" + WIREMOCK_PLACEHOLDER; //${wiremock
    private static WiremockSupportModule wiremockSupportInstance;

    private WiremockSupportModule() {

    }

    public static WiremockSupportModule getInstance() {
        if (wiremockSupportInstance == null) {
            wiremockSupportInstance = new WiremockSupportModule();
        }
        return wiremockSupportInstance;
    }

    @Override
    public Map<String, String> process(String stringToProcess, HeatTestDetails testDetails) {
        Map<String, String> processedMap = new HashMap<>();
        processedMap.put(DEFAULT_PRELOADED_VALUE, stringToProcess);

        String instanceName = getWmInstanceName(stringToProcess);
        String action = getActionToRun(stringToProcess);

        WiremockSupportHandler wmSupportHandler = new WiremockSupportHandler(instanceName, testDetails);
        wmSupportHandler.executeAction(action);




        return processedMap;
    }

    private String getWmInstanceName(String stringToProcess) {
        return "WM_INSTANCE";
    }

    private String getActionToRun(String stringToProcess) { //TODO understand what action it is necessary to do
        return "requests";
    }
}
