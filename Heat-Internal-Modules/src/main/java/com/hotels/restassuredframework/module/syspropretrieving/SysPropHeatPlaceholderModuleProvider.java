package com.hotels.restassuredframework.module.syspropretrieving;

import java.util.ArrayList;
import java.util.List;

import com.hotels.restassuredframework.core.heatmodules.HeatPlaceholderModule;
import com.hotels.restassuredframework.core.heatmodules.HeatPlaceholderModuleProvider;

/**
 * Provider of the internal module that manages system property retrieving.
 * @author adebiase
 */
public class SysPropHeatPlaceholderModuleProvider implements HeatPlaceholderModuleProvider {

    @Override
    public List<String> getHandledPlaceholders() {
        List<String> listPlaceholders = new ArrayList<>();
        listPlaceholders.add(SysPropHeatPlaceholderModule.SYS_PROP_PLACEHOLDER);
        return listPlaceholders;
    }

    @Override
    public HeatPlaceholderModule getModuleInstance() {
        return SysPropHeatPlaceholderModule.getInstance();
    }

}
