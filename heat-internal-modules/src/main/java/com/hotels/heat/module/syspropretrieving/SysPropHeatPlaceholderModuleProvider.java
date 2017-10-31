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
package com.hotels.heat.module.syspropretrieving;

import java.util.ArrayList;
import java.util.List;

import com.hotels.heat.core.heatmodules.HeatPlaceholderModule;
import com.hotels.heat.core.heatmodules.HeatPlaceholderModuleProvider;

/**
 * Provider of the internal module that manages system property retrieving.
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
