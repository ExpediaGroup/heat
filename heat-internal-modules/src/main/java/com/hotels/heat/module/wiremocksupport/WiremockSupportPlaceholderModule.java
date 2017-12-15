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
package com.hotels.heat.module.wiremocksupport;

import com.hotels.heat.core.dto.HeatTestDetails;
import com.hotels.heat.core.heatmodules.HeatPlaceholderModule;
import com.hotels.heat.module.syspropretrieving.SysPropHandler;
import com.hotels.heat.module.syspropretrieving.SysPropHeatPlaceholderModule;

public class WiremockSupportPlaceholderModule  implements HeatPlaceholderModule {

    public static final String DEFAULT_PRELOADED_VALUE = "DEFAULT";
    public static final String NEW_PLACEHOLDER = "PLACEHOLDER";
    public static final String SYS_PROP_PLACEHOLDER = "${" + NEW_PLACEHOLDER; //${SysProp
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
