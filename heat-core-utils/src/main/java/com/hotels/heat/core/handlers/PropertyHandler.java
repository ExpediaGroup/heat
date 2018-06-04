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
package com.hotels.heat.core.handlers;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import com.hotels.heat.core.utils.log.LoggingUtils;

/**
 * Manages the framework system configuration from properties file.
 */
public class PropertyHandler {
    private Properties properties;
    private final String propFile;
    private final LoggingUtils logUtils;
    private boolean isLoaded;
    private PlaceholderHandler placeholderHandler;

    /**
     * Constructor of the class PropertyHandler.
     * @param propertyFile is the path of the file to load
     * @param placeholderHandler placeholder handler
     */
    public PropertyHandler(String propertyFile, PlaceholderHandler placeholderHandler) {
        this.isLoaded = false;
        this.properties = new Properties();
        this.propFile = propertyFile;
        this.logUtils = TestSuiteHandler.getInstance().getLogUtils();
        this.placeholderHandler = placeholderHandler;
    }

    /**
     * Loads and cache the environment properties from file system.
     */
    public void loadFromPropertyFile() {

        if (!isLoaded) {
            InputStream inputStream = null;
            try {
                logUtils.trace("loading '{}' file", propFile);
                inputStream = new FileInputStream(propFile);
                properties.load(inputStream);
                isLoaded = true;

            } catch (Exception oEx) {
                logUtils.error("Error! '{}'", oEx.getLocalizedMessage());
                isLoaded = false;
            } finally {
                try {
                    inputStream.close();
                } catch (Exception oEx) {
                    logUtils.error("Error! '{}'", oEx.getLocalizedMessage());
                    isLoaded = false;
                }
            }
        }
    }

    /**
     * Retrieve the string associated to the property name.
     * @param propertyName name of the property to retrieve
     * @return the string set in environment property file.
     */
    public String getProperty(String propertyName) {
        String retrievedProp = null;
        if (!properties.isEmpty()) {
            retrievedProp = properties.getProperty(propertyName);
            if (retrievedProp.contains(PlaceholderHandler.PLACEHOLDER_SYMBOL_BEGIN)) {
                retrievedProp = ((Map<String, String>) placeholderHandler.placeholderProcessString(retrievedProp)).get("DEFAULT");
            }
        } else {
            logUtils.debug("The file containing the environment properties seems to be empty!");
        }
        return retrievedProp;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getPropFile() {
        return propFile;
    }


    public boolean isLoaded() {
        return isLoaded;
    }

}
