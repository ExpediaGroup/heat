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
public final class WiremockSupportPlaceholderModule  implements HeatPlaceholderModule {

    public static final String WIREMOCK_PLACEHOLDER = "wiremock";
    public static final String WIREMOCK_PROP_PLACEHOLDER = "${" + WIREMOCK_PLACEHOLDER; //${wiremock
    private static WiremockSupportPlaceholderModule wiremockSupportInstance;

    private WiremockSupportPlaceholderModule() {

    }

    public static WiremockSupportPlaceholderModule getInstance() {
        if (wiremockSupportInstance == null) {
            wiremockSupportInstance = new WiremockSupportPlaceholderModule();
        }
        return wiremockSupportInstance;
    }

    @Override
    public Map<String, String> process(String stringToProcess, HeatTestDetails testDetails) {
        Map<String, String> processedMap = new HashMap<>();

        return processedMap;
    }
}
