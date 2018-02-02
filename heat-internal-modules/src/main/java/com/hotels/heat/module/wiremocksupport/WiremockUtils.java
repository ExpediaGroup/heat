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

import java.util.Properties;

import com.hotels.heat.core.handlers.PlaceholderHandler;
import com.hotels.heat.core.handlers.PropertyHandler;
import com.hotels.heat.core.handlers.TestSuiteHandler;
import com.hotels.heat.core.utils.log.LoggingUtils;

public class WiremockUtils {
    private static WiremockUtils instance;
    private LoggingUtils logUtils;
    private Properties properties;


    private WiremockUtils() {
        this.logUtils = TestSuiteHandler.getInstance().getLogUtils();
        this.properties = new Properties();
    }

    public static WiremockUtils getInstance() {
        if (instance == null) {
            instance = new WiremockUtils();
        }
        return instance;
    }


    /**
     * Loads and cache the environment properties from file system.
     */
    private void loadEnvironmentProperties(String propFile) {
        PropertyHandler ph = new PropertyHandler(propFile, new PlaceholderHandler());
        ph.loadFromPropertyFile();
        properties = ph.getProperties();
    }


    public String getEnvironmentProperty(String propFile, String propertyName) {
        loadEnvironmentProperties(propFile);
        String environment = TestSuiteHandler.getInstance().getEnvironmentHandler().getEnvironmentUnderTest();
        if (environment == null) {
            environment = System.getProperty("environment");
        }
        String basePath = this.properties.getProperty(propertyName + "." + environment + ".path");

        return basePath;
    }


}
