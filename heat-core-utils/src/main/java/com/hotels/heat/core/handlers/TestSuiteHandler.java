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
package com.hotels.heat.core.handlers;

import com.hotels.heat.core.environment.EnvironmentHandler;
import com.hotels.heat.core.utils.TestCaseUtils;
import com.hotels.heat.core.utils.log.LoggingUtils;


/**
 * This object stores and mantains all needed data for the entire Test suite.
 * All these information are mantained here and reused in the project.
 * NOTE: this version is designed for a single thread use of the framework.
 */
public final class TestSuiteHandler {

    private static final String NO_INPUT_WEBAPP_NAME = "noInputWebappName";
    private static final String WEBAPP_NAME = "webappName";
    private static final String NOT_DEFINED_SERVICE = "Not Defined Service";

    private static TestSuiteHandler testSuiteHandler;
    private EnvironmentHandler environmentHandler;
    private TestCaseUtils tcUtils;
    private LoggingUtils logUtils;
    private String webappName;
    private String propertyFilePath;

    private TestSuiteHandler() {
        logUtils = new LoggingUtils();
        tcUtils = new TestCaseUtils();
    }

    /**
     * Singleton implementation for the object.
     * @return the singleton instance of the object
     */
    public static synchronized TestSuiteHandler getInstance() {
        if (testSuiteHandler == null) {
            testSuiteHandler = new TestSuiteHandler();
        }
        return testSuiteHandler;

    }

    public String getWebappName() {
        return webappName;
    }

    public void setWebappName(String webappName) {
        if (NO_INPUT_WEBAPP_NAME.equals(webappName)) {
            this.webappName = System.getProperty(WEBAPP_NAME, NOT_DEFINED_SERVICE);
        } else {
            this.webappName = webappName;
        }
    }

    public void setPropertyFilePath(String propertyFilePath) {
        this.propertyFilePath = propertyFilePath;
    }

    public EnvironmentHandler getEnvironmentHandler() {
        return environmentHandler;
    }

    public void setEnvironmentHandler(EnvironmentHandler eh) {
        environmentHandler = eh;
    }

    public void populateEnvironmentHandler() {
        this.environmentHandler = new EnvironmentHandler(propertyFilePath);
    }

    public LoggingUtils getLogUtils() {
        return logUtils;
    }

    public void populateTestCaseUtils() {
        tcUtils.setLogUtils(logUtils);
    }

    public TestCaseUtils getTestCaseUtils() {
        return tcUtils;
    }

}
