package com.hotels.restassuredframework.core.heatmodules;


import java.util.List;

/**
 * Generic Handling of external utility modules.
 *
 * @author adebiase
 */
public interface HeatPlaceholderModuleProvider {

    List<String> getHandledPlaceholders();
    
    HeatPlaceholderModule getModuleInstance();
    

}
