package com.hotels.heat.core.utils;

import com.hotels.heat.core.runner.TestBaseRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class TestCaseManager {

    public static final String TEST_NAME = "testName";
    public static final String TEST_STEPS = "e2eFlowSteps";
    public static final String STEP_STEPNUMBER = "stepNumber";
    public static final String STEP_SERVICENAME = "webappName";
    public static final String STEP_HTTPMETHOD = "httpMethod";

    private Map<String, Object> testCaseData;
    private String completeTestCaseId;
    private ITestContext testContext;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Map testCase;
    private Map<String, String> generalSettings;
    private Map<String, String> beforeSuite;
    //private Map<String, String> jsonSchemas;

    public TestCaseManager(Map<String, Object> testCaseData, String completeTestCaseId, ITestContext testContext) {
        this.testCaseData = testCaseData;
        this.completeTestCaseId = completeTestCaseId;
        this.testContext = testContext;
    }

    public void execute() {
        Map testCase = (Map) testCaseData.get(TestBaseRunner.ITERATOR_TEST_CASE);
        Map<String, String> generalSettings = (Map<String, String>) testCaseData.get(TestBaseRunner.ITERATOR_GENERAL_SETTINGS);
        Map<String, String> beforeSuite = (Map<String, String>) testCaseData.get(TestBaseRunner.ITERATOR_BEFORE_SUITE);
        //Map<String, String> jsonSchemas = (Map<String, String>) testCaseData.get(TestBaseRunner.ITERATOR_JSON_SCHEMAS);

        String testCaseDescription = testCase.get(TEST_NAME).toString();

        List<Map> stepList = (List<Map>) testCase.get(TEST_STEPS);

        for (Map<String, Object> step : stepList) {

            logger.debug("[{}] Description: '{}'", completeTestCaseId, testCaseDescription);
            String stepNumber = step.get(STEP_STEPNUMBER).toString();
            logger.debug("[{}][Step # {}]: START", completeTestCaseId, stepNumber);

            //TODO: HANDLING SCRIPT RUNNING

            //TODO: BEFORE STEP HANDLING

            String serviceName = step.get(STEP_SERVICENAME).toString();
            Properties envProperties = (Properties) testContext.getAttribute(TestBaseRunner.CTX_ENVIRONMENT_PATHS);
            String environmentToTest = System.getProperty(TestBaseRunner.SYSPROP_ENVIRONMENT, System.getProperty("defaultEnvironment"));
            String servicePath = envProperties.getProperty(serviceName + "." + environmentToTest + ".path");

            //TODO: PLACEHOLDER IN PATH TO HANDLE
            String httpMethod = step.get(STEP_HTTPMETHOD).toString();


        }





    }



}
