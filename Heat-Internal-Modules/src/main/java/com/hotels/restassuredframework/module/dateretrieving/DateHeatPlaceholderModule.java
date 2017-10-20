package com.hotels.restassuredframework.module.dateretrieving;

import java.util.HashMap;
import java.util.Map;

import com.hotels.restassuredframework.core.heat.dto.HeatTestDetails;
import com.hotels.restassuredframework.core.heatmodules.HeatPlaceholderModule;

/**
 * Module to handle dates.
 * @author adebiase
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
