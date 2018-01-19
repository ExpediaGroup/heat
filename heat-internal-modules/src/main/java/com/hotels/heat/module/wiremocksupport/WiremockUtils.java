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

import com.hotels.heat.core.handlers.TestSuiteHandler;
import com.hotels.heat.core.utils.log.LoggingUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class WiremockUtils {
    private LoggingUtils logUtils;
    private Properties properties;



    public WiremockUtils() {
        this.logUtils = TestSuiteHandler.getInstance().getLogUtils();
        this.properties = new Properties();
    }


    /**
     * Loads and cache the environment properties from file system.
     */
    private void loadEnvironmentProperties(String propFile) {

            InputStream inputStream = null;
            try {
                logUtils.trace("loading '{}' file", propFile);
                inputStream = new FileInputStream(propFile);
                properties.load(inputStream);

            } catch (Exception oEx) {
                logUtils.error("Error! '{}'", oEx.getLocalizedMessage());
            } finally {
                try {
                    inputStream.close();
                } catch (Exception oEx) {
                    logUtils.error("Error! '{}'", oEx.getLocalizedMessage());
                }
            }
    }


    public String getEnvironmentProperty(String propFile, String propertyName) {
        loadEnvironmentProperties(propFile);
        String environment = System.getProperty("environment");
        //String environment = TestSuiteHandler.getInstance().getEnvironmentHandler().getEnvironmentUnderTest();
        String basePath = this.properties.getProperty(propertyName + "." + environment + ".path");

        return basePath;
    }


}
