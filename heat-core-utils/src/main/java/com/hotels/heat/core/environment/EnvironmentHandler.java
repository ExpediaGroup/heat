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
package com.hotels.heat.core.environment;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hotels.heat.core.handlers.PlaceholderHandler;
import com.hotels.heat.core.handlers.PropertyHandler;
import com.hotels.heat.core.handlers.TestSuiteHandler;
import com.hotels.heat.core.utils.log.LoggingUtils;


/**
 * This object is the handler of the environment.
 */
public final class EnvironmentHandler {

    public static final String SYS_PROP_HEAT_TEST = "heatTest";

    private static final String DEFAULT_ENVIRONMENT = "DEFAULT_ENVIRONMENT";
    private static final String DEFAULT_SERVICE = "DEFAULT_SERVICE";
    private static final String HTTP = "http";
    private static final String HEAT_TEST_SEPARATOR = ",";

    private PropertyHandler ph;
    private String enabledEnvironments;
    private String defaultEnvironment;
    private String environmentUnderTest;
    private String webappUnderTest;

    private LoggingUtils logUtils;
    private List<String> heatTestPropertyList;
    private PlaceholderHandler placeholderHandler;


    /**
     * Constructor with parameters.
     * @param inputPh Property Handler object
     */
    public EnvironmentHandler(PropertyHandler inputPh) {
        this.logUtils = TestSuiteHandler.getInstance().getLogUtils();
        this.ph = inputPh;
        loadSysProperties();
        this.setEnabledEnvironments(defaultEnvironment);
    }

    /**
     * Constructor of the object with the loading of the property file.
     * @param propFilePath path of the prop file
     */
    public EnvironmentHandler(String propFilePath) {
        this.logUtils = TestSuiteHandler.getInstance().getLogUtils();
        this.placeholderHandler = new PlaceholderHandler();
        this.ph = new PropertyHandler(propFilePath, placeholderHandler);
        loadSysProperties();
    }

    /**
     * This method reads the environment system properties:
     * - defaultEnvironment: the environment to load by default; defined in pom.xml
     * - environment: environment under test; if it is not defined, it will be set to "defaultEnvironment" value
     * - webappName: the name of the service under test, useful when we have to load properties from environment.properties file.
     */
    private void loadSysProperties() {
        logUtils.trace("defaultEnvironment '{}'", System.getProperty("defaultEnvironment"));
        logUtils.trace("DEFAULT_ENVIRONMENT '{}'", DEFAULT_ENVIRONMENT);
        defaultEnvironment = System.getProperty("defaultEnvironment", DEFAULT_ENVIRONMENT);
        environmentUnderTest = System.getProperty("environment", defaultEnvironment);
        logUtils.trace("Environment under test '{}'", environmentUnderTest);
        webappUnderTest = System.getProperty("webappName", DEFAULT_SERVICE);
        heatTestPropertyList = testIds2List(System.getProperty(SYS_PROP_HEAT_TEST));
    }


    /**
     * This method gets from system properties the value of the environment.
     * If the environment is different from known ones, it will be necessary to
     * set the system property to the URL on which the webapp under test is deployed (exactly
     * as it would be written in the environment.properties file):
     * example:
     * -Denvironment=http://127.0.0.1:8080/api_service
     * For APIs different from the one under test, we consider only the default environment.
     *
     * @param webApp : this is a string that identifies the webapp we want to point at.
     * @return the URL the test will point at
     */
    public String getEnvironmentUrl(String webApp) {
        String url;
        ph.loadFromPropertyFile();
        logUtils.trace("The environment I am going to test is '{}'", environmentUnderTest);
        logUtils.trace("Enabled environments are '{}'", enabledEnvironments);

        if (!environmentUnderTest.startsWith(HTTP)) { //no custom environment
            if (enabledEnvironments.contains(environmentUnderTest)) {
                logUtils.trace("The environment is among the enabled ones.");
                url = ph.getProperty(webApp + "." + environmentUnderTest + ".path");

            } else {
                url = null;
                logUtils.debug("The environment '{}' is not enabled for this test. The enabled environments are '{}'",
                        environmentUnderTest, enabledEnvironments);
            }
        } else {
            logUtils.trace("The environment is not a standard one");
            if (!webApp.equalsIgnoreCase(webappUnderTest)) {
                url = ph.getProperty(webApp + "." + defaultEnvironment + ".path");
            } else {
                url = environmentUnderTest;
            }
        }
        logUtils.trace("Url returned for '{}' is '{}'", webApp, url);
        return url;
    }

    /**
     * In case of multiple value for the "heatTest" parameter, it splits the values and runs tests one by one.
     * @param testIds string containing all the test ids to run, split with a separator
     * @return a list of test ids
     */
    private List<String> testIds2List(String testIds) {
        List<String> testIdsList = new ArrayList<>();

        if (testIds != null) {
            testIdsList.addAll(Arrays.asList(testIds.split(HEAT_TEST_SEPARATOR)));
            testIdsList.replaceAll(String::trim);
        }
        return testIdsList;
    }

    public void reloadSysTestIds() {
        this.heatTestPropertyList = testIds2List(System.getProperty(SYS_PROP_HEAT_TEST));
    }

    public void reloadSysEnv() {
        this.environmentUnderTest = System.getProperty("environment", DEFAULT_ENVIRONMENT);
    }

    public void setPh(PropertyHandler ph) {
        this.ph = ph;
    }

    public void setEnabledEnvironments(String environmentToTest) {
        this.enabledEnvironments = environmentToTest;
    }

    public String getEnabledEnvironments() {
        return this.enabledEnvironments;
    }

    public String getEnvironmentUnderTest() {
        return this.environmentUnderTest;
    }

    public List<String> getHeatTestPropertyList() {
        return heatTestPropertyList;
    }

    public String getDefaultEnvironment() {
        return defaultEnvironment;
    }

    public String getWebappUnderTest() {
        return this.webappUnderTest;
    }

}
