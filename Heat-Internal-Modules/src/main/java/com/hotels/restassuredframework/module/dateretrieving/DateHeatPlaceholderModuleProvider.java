package com.hotels.restassuredframework.module.dateretrieving;

import java.util.ArrayList;
import java.util.List;

import com.hotels.restassuredframework.core.heatmodules.HeatPlaceholderModule;
import com.hotels.restassuredframework.core.heatmodules.HeatPlaceholderModuleProvider;

/**
 * Provider for the module that manages dates.
 * @author adebiase
 */
public class DateHeatPlaceholderModuleProvider implements HeatPlaceholderModuleProvider {

    @Override
    public List<String> getHandledPlaceholders() {
        List<String> listPlaceholders = new ArrayList<>();
        listPlaceholders.add(DateHeatPlaceholderModule.TODAY_PLACEHOLDER);
        return listPlaceholders;
    }

    @Override
    public HeatPlaceholderModule getModuleInstance() {
        return DateHeatPlaceholderModule.getInstance();
    }

}
