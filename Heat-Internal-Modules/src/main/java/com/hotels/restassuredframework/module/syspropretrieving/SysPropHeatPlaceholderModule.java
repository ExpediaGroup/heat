package com.hotels.restassuredframework.module.syspropretrieving;

import java.util.HashMap;
import java.util.Map;

import com.hotels.restassuredframework.core.heat.dto.HeatTestDetails;
import com.hotels.restassuredframework.core.heatmodules.HeatPlaceholderModule;

/**
 * Heat internal module that manages the system property retrieving.
 * @author adebiase
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
        Map<String, String> processedMap = new HashMap<>();
        processedMap.put(SysPropHeatPlaceholderModule.DEFAULT_PRELOADED_VALUE, stringToProcess);

        SysPropHandler sysPropHandler = new SysPropHandler(testDetails.getTestDescription());
        if (stringToProcess.contains(SysPropHeatPlaceholderModule.SYS_PROP_PLACEHOLDER)) {
            processedMap.put(SysPropHeatPlaceholderModule.DEFAULT_PRELOADED_VALUE, sysPropHandler.processString(stringToProcess));
        }
        return processedMap;
    }
}
