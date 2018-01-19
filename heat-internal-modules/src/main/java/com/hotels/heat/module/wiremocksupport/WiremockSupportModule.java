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
import org.testng.annotations.Parameters;

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
        String action = getActionToRun(stringToProcess);

        WiremockSupportHandler wmSupportHandler = new WiremockSupportHandler(instanceName, basePath, testDetails);
        wmSupportHandler.executeAction(action);


        //processedMap.put(DEFAULT_PRELOADED_VALUE, "pippo");

        return processedMap;
    }

    private String getWmInstanceName(String stringToProcess) {
        String instanceName = tcUtils.regexpExtractor(stringToProcess, "\\$\\{" + WIREMOCK_PLACEHOLDER + "\\[(.*?)\\].*\\}", 1);
        return instanceName;
    }


    private String getWmBasePath(String envPropFilePath, String propertyName) {
        WiremockUtils utils = new WiremockUtils();
        String basePath = utils.getEnvironmentProperty(envPropFilePath, propertyName);
        return basePath;
    }


    private String getActionToRun(String stringToProcess) {
        String action = tcUtils.regexpExtractor(stringToProcess, "\\$\\{"+ WIREMOCK_PLACEHOLDER + "\\[.*?\\]\\.(.*?)\\}", 1);
        return action;
    }
}
