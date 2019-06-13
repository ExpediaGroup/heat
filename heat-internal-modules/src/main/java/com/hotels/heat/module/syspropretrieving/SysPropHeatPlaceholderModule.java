/**
 * Copyright (C) 2015-2019 Expedia, Inc.
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
package com.hotels.heat.module.syspropretrieving;

import java.util.HashMap;
import java.util.Map;

import com.hotels.heat.core.dto.HeatTestDetails;
import com.hotels.heat.core.heatmodules.HeatPlaceholderModule;

/**
 * Heat internal module that manages the system property retrieving.
 */
public final class SysPropHeatPlaceholderModule implements HeatPlaceholderModule {

    public static final String DEFAULT_PRELOADED_VALUE = "DEFAULT";
    public static final String SYS_PROP_PLACEHOLDER_KEYWORD = "SysProp";
    public static final String SYS_PROP_PLACEHOLDER = "${" + SYS_PROP_PLACEHOLDER_KEYWORD; //${SysProp
    private static SysPropHeatPlaceholderModule sysPropInstance;

    private SysPropHeatPlaceholderModule() {

    }

    public static SysPropHeatPlaceholderModule getInstance() {
        if (sysPropInstance == null) {
            sysPropInstance = new SysPropHeatPlaceholderModule();
        }
        return sysPropInstance;
    }

    @Override
    public Map<String, String> process(String stringToProcess, HeatTestDetails testDetails) {
        Map<String, String> processedMap = new HashMap();
        processedMap.put(SysPropHeatPlaceholderModule.DEFAULT_PRELOADED_VALUE, stringToProcess);

        SysPropHandler sysPropHandler = new SysPropHandler(testDetails.getTestDescription());
        if (stringToProcess.contains(SysPropHeatPlaceholderModule.SYS_PROP_PLACEHOLDER)) {
            processedMap.put(SysPropHeatPlaceholderModule.DEFAULT_PRELOADED_VALUE, sysPropHandler.processString(stringToProcess));
        }
        return processedMap;
    }
}
