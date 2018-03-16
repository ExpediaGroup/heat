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
import com.hotels.heat.core.utils.TestCaseUtils;

/**
 * Wiremock Support Placeholder Module.
 */
public final class WiremockSupportModule implements HeatPlaceholderModule {

    public static final String DEFAULT_PRELOADED_VALUE = "DEFAULT";
    public static final String WIREMOCK_PLACEHOLDER = "wiremock";
    public static final String WIREMOCK_PROP_PLACEHOLDER = "${" + WIREMOCK_PLACEHOLDER; //${wiremock
    private static WiremockSupportModule wiremockSupportInstance;
    private TestCaseUtils tcUtils = new TestCaseUtils();
    private String propFilePath;

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
        String basePath = getWmBasePath("environment.properties", instanceName);
        WiremockAction action = getActionToRun(stringToProcess);

        WiremockSupportHandler wmSupportHandler = new WiremockSupportHandler(instanceName, basePath, testDetails);
        String actionResult = wmSupportHandler.executeAction(action);

        processedMap.put(DEFAULT_PRELOADED_VALUE, actionResult);
        processedMap.put("total", "1");

        return processedMap;
    }

    private String getWmInstanceName(String stringToProcess) {
        return tcUtils.regexpExtractor(stringToProcess, "\\$\\{" + WIREMOCK_PLACEHOLDER + "\\[(.*?)\\].*\\}", 1);
    }


    private String getWmBasePath(String envPropFilePath, String propertyName) {
        return WiremockUtils.getInstance().getEnvironmentProperty(envPropFilePath, propertyName);
    }


    private WiremockAction getActionToRun(String stringToProcess) {
        String actionName = tcUtils.regexpExtractor(stringToProcess, "\\$\\{" + WIREMOCK_PLACEHOLDER + "\\[.*?\\]\\.(.*?)\\}", 1);
        WiremockAction wiremockAction = WiremockAction.fromString(actionName);
        return wiremockAction;
    }
}
