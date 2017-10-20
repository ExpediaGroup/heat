package com.hotels.restassuredframework.core.heatmodules;

import java.util.Map;

import com.hotels.restassuredframework.core.heat.dto.HeatTestDetails;

/**
 *
 * @author adebiase
 */
public interface HeatPlaceholderModule extends HeatModule {
    
    public Map<String, String> process(String stringToProcess, HeatTestDetails testDetails);
    
}