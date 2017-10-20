package com.hotels.restassuredframework.core.handlers;


import com.hotels.restassuredframework.core.environment.EnvironmentHandler;
import com.hotels.restassuredframework.core.utils.TestCaseUtils;
import com.hotels.restassuredframework.core.utils.log.LoggingUtils;

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
