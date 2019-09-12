/**
 * Copyright (C) 2015-2019 Expedia, Inc.
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
package com.hotels.heat.core.runner;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import com.hotels.heat.core.utils.HeatException;
import com.hotels.heat.core.utils.TestCaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.*;

import com.hotels.heat.core.heatspecificchecks.SpecificChecks;
import com.hotels.heat.core.utils.JsonInput;
import com.hotels.heat.core.utils.RunnerInterface;

import io.restassured.response.Response;


/**
 * Generic Test Runner class.
 */
public class TestBaseRunner implements RunnerInterface {

    public static final String SYSPROP_ENVIRONMENT = "environment";
    public static final String SYSPROP_LOG_LEVEL = "logLevel";
    public static final String SYSPROP_TESTS_TO_RUN = "heatTest";
    public static final String SYSPROP_TESTS_NOT_TO_RUN = "heatTestNotRun";

    static {
        (LoggerFactory.getLogger(TestBaseRunner.class)).info(
                "\n" +
                        "                                                   \n" +
                        "                                                   \n" +
                        "   .              __.....__                        \n" +
                        " .'|          .-''         '.                      \n" +
                        "<  |         /     .-''\"'-.  `.               .|   \n" +
                        " | |        /     /________\\   \\    __      .' |_  \n" +
                        " | | .'''-. |                  | .:--.'.  .'     | \n" +
                        " | |/.'''. \\\\    .-------------'/ |   \\ |'--.  .-' \n" +
                        " |  /    | | \\    '-.____...---.`\" __ | |   |  |   \n" +
                        " | |     | |  `.             .'  .'.''| |   |  |   \n" +
                        " | |     | |    `''-...... -'   / /   | |_  |  '.' \n" +
                        " | '.    | '.                   \\ \\._,\\ '/  |   /  \n" +
                        " '---'   '---'                   `--'  `\"   `'-'   \n"
        );

        (LoggerFactory.getLogger(TestBaseRunner.class)).info(
                "\n"
                + "+-----------------------------------------------------------------------+\n"
                + "| Environment under test : '{}'\n"
                + "+-----------------------------------------------------------------------+\n"
                + "| Requested Log Level: : '{}'\n"
                + "+-----------------------------------------------------------------------+\n"
                + "| Specific tests requested: '{}'\n"
                + "| Specific tests NOT requested: '{}'\n"
                + "+-----------------------------------------------------------------------+\n",
                System.getProperty(SYSPROP_ENVIRONMENT, System.getProperty("defaultEnvironment")),
                System.getProperty(SYSPROP_LOG_LEVEL, "INFO"),
                System.getProperty(SYSPROP_TESTS_TO_RUN, "All Tests"),
                System.getProperty(SYSPROP_TESTS_NOT_TO_RUN, "None"));

    }


    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    /*public static final String ATTR_TESTCASE_ID = "testId";
    public static final String ATTR_TESTCASE_NAME = "testName";

    public static final String STATUS_SKIPPED = "SKIPPED";

    public static final String TESTCASE_ID_SEPARATOR = ".";
    public static final String TESTCASE_ID_SEPARATOR_ESCAPED = "\\.";

    public static final String SUITE_DESCRIPTION_CTX_ATTR = "suiteDescription";
    public static final String TC_DESCRIPTION_CTX_ATTR = "tcDescription";

    public static final String NO_INPUT_WEBAPP_NAME = "noInputWebappName";

    private ITestContext testContext;

    private PlaceholderHandler placeholderHandler;
    private String inputJsonPath;*/

    public static final String CTX_ENVIRONMENT_PATHS = "CTX_ENVIRONMENT_PATHS";
    public static final String CTX_ENABLED_ENVIRONMENTS = "CTX_ENABLED_ENVIRONMENTS";

    public static final String TESTNG_ENV_PROP_FILE_PATH = "envPropFilePath";
    public static final String TESTNG_INPUT_JSON_PATH = "inputJsonPath";
    public static final String TESTNG_ENABLED_ENVIRONMENTS = "enabledEnvironments";

    public static final String ITERATOR_TEST_CASE = "TEST_CASE";
    public static final String ITERATOR_GENERAL_SETTINGS = "GENERAL_SETTINGS";
    public static final String ITERATOR_BEFORE_SUITE = "BEFORE_SUITE";
    public static final String ITERATOR_JSON_SCHEMAS = "JSON_SCHEMAS";

    public static final String ATTR_TESTCASE_ID = "testId";

    private String inputJsonPath;



    /**
     * Method that takes test suites parameters and sets some environment properties.
     * @param propFilePath path of the property file data
     * @param context testNG context
     */
    @BeforeSuite
    @Override
    @Parameters({TESTNG_ENV_PROP_FILE_PATH})
    public void beforeTestSuite(String propFilePath,
                                ITestContext context) {

        Properties environments = getEnvironmentProperties(propFilePath);
        context.setAttribute(CTX_ENVIRONMENT_PATHS, environments);

    }

    /**
     * Method that takes tests parameters and sets some environment properties.
     * @param inputJsonFilePath path of the json input file with input data for tests
     * @param enabledEnvironments environments enabled for the specific suite
     * @param context testNG context
     */
    @BeforeTest
    @Override
    @Parameters({TESTNG_INPUT_JSON_PATH, TESTNG_ENABLED_ENVIRONMENTS})
    public void beforeTestCase(String inputJsonFilePath,
        String enabledEnvironments,
        ITestContext context) {

        List<String> enabledEnvironmentList = new ArrayList<>(Arrays.asList(enabledEnvironments.split(",")));
        context.setAttribute(CTX_ENABLED_ENVIRONMENTS, enabledEnvironmentList);
        inputJsonPath = inputJsonFilePath;
    }


    @Override
    @DataProvider(name = "provider")
    public Iterator<Object[]> providerJson() {
        Iterator<Object[]> testCaseIterator = JsonInput.getInstance().jsonReader(inputJsonPath, getTestContext());
        return testCaseIterator;
    }


    /**
     * Method that manages the execution of a single test case.
     * @param testCaseParams Map containing test case parameters coming from the json inpu
     *                       t file
     */
    @Test(dataProvider = "provider")
    public void runningTest(Map<String, Object> testCaseData) {
        Map testCase = (Map) testCaseData.get(ITERATOR_TEST_CASE);

        String testSuiteName = getTestContext().getName();
        String testCaseId = testCase.get(TestBaseRunner.ATTR_TESTCASE_ID).toString();

        try {
            String completeTestCaseId = testSuiteName + "." + testCaseId;

            if (!isTheTestCaseSkippable(testSuiteName, testCaseId)) {
                logger.debug("[{}] Test Case Not Skippable", completeTestCaseId);

                // Request
                try {
                    logger.debug("[{}] >> START", completeTestCaseId);

                    TestCaseManager testCaseManager = new TestCaseManager(testCaseData, completeTestCaseId, getTestContext());
                    testCaseManager.execute();



                    logger.debug("[{}] >> END", completeTestCaseId);
                } catch (Exception oEx) {
                    throw new HeatException(String.format("[%s] Test Case failed. Unknown reason.", completeTestCaseId));
                }
                // verifications



            } else {
                logger.info("[{}] Test Case Skippable", completeTestCaseId);
                //TODO: SKIP TEST CASE
            }


        } catch (Exception oEx) {
            throw new HeatException(this.getClass(), oEx);
        }

    }

    private boolean isTheTestCaseSkippable(String testSuiteName, String testCaseId) {
        boolean isSkippable = true; //by default the test cannot be run


        // if it is among the tests NOT TO RUN / if it is NOT in the list of tests to run
        String SysPropHeatTestNotRun = System.getProperty(SYSPROP_TESTS_NOT_TO_RUN, "");
        boolean tcIsInTheBlackList = isElementContainedInTheList(testSuiteName, testCaseId, SysPropHeatTestNotRun);

        String SysPropHeatTestToRun = System.getProperty(SYSPROP_TESTS_TO_RUN, "");
        boolean tcIsInTheWhiteList = isElementContainedInTheList(testSuiteName, testCaseId, SysPropHeatTestToRun);
        if ("".equals(SysPropHeatTestToRun)) { tcIsInTheWhiteList = true; }

        if (tcIsInTheWhiteList || !tcIsInTheBlackList) {
            isSkippable = false;
        }

        return isSkippable;
    }

    private boolean isElementContainedInTheList(String testSuiteName, String testCaseId, String sysPropList) {
        boolean isContained = false;
        List<String> testsNotToRun = new ArrayList<String>(Arrays.asList(sysPropList.split(",")));
        for (String blackListElement : testsNotToRun) {
            //testSuiteName is in the format TEST.001 because it's the running test

            if (testSuiteName.equals(blackListElement.split(".")[0])) {
                if (blackListElement.contains(".")) {
                    // only a specific test case is disabled
                    String completeTestCaseId = testSuiteName + "." + testCaseId;
                    if (completeTestCaseId.equals(blackListElement)) {
                        isContained = true;
                    }
                } else {
                    // All the test suite is disabled
                    isContained = true;
                }
            }
        }
        return isContained;
    }












    /**
     * Elaboration of test case parameters before any request (method executed as first step in the runner).
     * @param testCaseParams Map containing test case parameters coming from the json input file
     * @return the same structure as the input parameters but with placeholders resolved
     */
    @Override
    public Map resolvePlaceholdersInTcParams(Map<String, Object> testCaseParams, List<String> paramsToSkip) {
        TestSuiteHandler testSuiteHandler = TestSuiteHandler.getInstance();
        testSuiteHandler.getLogUtils().setTestCaseId(testContext.getAttribute(ATTR_TESTCASE_ID).toString());

        // now we start elaborating the parameters.
        placeholderHandler = new PlaceholderHandler();
        placeholderHandler.setPreloadedVariables(testSuiteHandler.getTestCaseUtils().getBeforeSuiteVariables());

        TestCaseMapHandler tcMapHandler = new TestCaseMapHandler(testCaseParams, placeholderHandler, paramsToSkip);

        return tcMapHandler.retrieveProcessedMap();
    }

    public Map resolvePlaceholdersInTcParams(Map<String, Object> testCaseParams) {
        return resolvePlaceholdersInTcParams(testCaseParams, new ArrayList());
    }

    /**
     * Method to set useful parameters in the context managed by testNG.
     * Parameters that will be set will be: 'testId', 'suiteDescription', 'tcDescription'
     * @param testCaseParams Map containing test case parameters coming from the json input file
     */
    public void setContextAttributes(Map<String, Object> testCaseParams) {
        String testCaseID = testCaseParams.get(ATTR_TESTCASE_ID).toString();
        testContext.setAttribute(ATTR_TESTCASE_ID, testCaseID);
        String suiteDescription = TestSuiteHandler.getInstance().getTestCaseUtils().getSuiteDescription();
        testContext.setAttribute(SUITE_DESCRIPTION_CTX_ATTR, suiteDescription);
        String testCaseDesc = testCaseParams.get(ATTR_TESTCASE_NAME).toString();
        testContext.setAttribute(TC_DESCRIPTION_CTX_ATTR, testCaseDesc);
    }

    /**
     * Checks if the test case is skippable or not, basing on the name of the current test suite (if the system parameter 'heatTest' is set in the
     * test running command) and on other suppositions.
     * @param currentTestSuiteName name of the test suite currently in execution
     * @param currentTestCaseId name of the test case currently in execution
     * @param webappName name of the service under test
     * @param webappPath path of the service under test (basing on the environment)
     * @return a boolean that indicates if this test case will be skipped
     */
    public boolean isTestCaseSkippable(String currentTestSuiteName, String currentTestCaseId, String webappName, String webappPath) {
        boolean thisTestIsSkippable;
        TestSuiteHandler testSuiteHandler = TestSuiteHandler.getInstance();

        boolean isParamsValid = testSuiteHandler.getTestCaseUtils().isCommonParametersValid(webappName, webappPath, getInputJsonPath(),
                testSuiteHandler.getLogUtils(), testSuiteHandler.getEnvironmentHandler());
        if (!isParamsValid) {
            thisTestIsSkippable = true; //Skip current test if shared parameters are missing
        } else {
            boolean isCurrentInList = false;
            List<String> heatTestPropertyList = testSuiteHandler.getEnvironmentHandler().getHeatTestPropertyList();
            for (String heatTestProperty : heatTestPropertyList) {

                String[] heatTestPropertySplitted = heatTestPropertySplit(heatTestProperty);
                String suiteNameToRun = heatTestPropertySplitted[0];
                String testCaseIdToRun = heatTestPropertySplitted[1];

                if (suiteNameToRun != null && testCaseIdToRun == null && suiteNameToRun.equalsIgnoreCase(currentTestSuiteName)) {
                    isCurrentInList = true; //Only current suite is specified
                }

                if (suiteNameToRun != null && testCaseIdToRun != null
                    && suiteNameToRun.equalsIgnoreCase(currentTestSuiteName)
                    && testCaseIdToRun.equalsIgnoreCase(currentTestCaseId)) {
                    isCurrentInList = true; //Both suite and test id are specified
                }
            }
            //Skipping current test if there is a list of tests to run (sys property 'heatTest') and the current one is not in that list
            thisTestIsSkippable = !heatTestPropertyList.isEmpty() && !isCurrentInList;
        }
        return thisTestIsSkippable;
    }


    /**
     * Splits the heatTest system property.
     * @param heatTestProperty value of the 'heatTest' system property
     * @return an array of strings containing all the single properties specified in the 'heatTest' system property
     */
    public static String[] heatTestPropertySplit(String heatTestProperty) {
        String[] safeSplit;
        if (heatTestProperty != null) {
            //if an element of that list does not contain ".", it means that it is not a specific test case and the output will be a String[] of two elements
            // and the second one is null (it is a test SUITE)
            if (!heatTestProperty.contains(TESTCASE_ID_SEPARATOR)) {
                safeSplit = new String[]{heatTestProperty, null};
            } else {
                //if the element contains the separator ".", it means that it is a specific test case and the output will be a String[] of two elements not null
                safeSplit = heatTestProperty.split(TESTCASE_ID_SEPARATOR_ESCAPED);
            }
        } else {
            safeSplit = new String[]{null, null};
        }
        return safeSplit;
    }


    /**
     * Executes, if necessary, the specific checks related to the test case that is currently running.
     * @param testCaseParams Map containing test case parameters coming from the json input file
     * @param rspRetrieved Map of the responses retrieved from the just executed requests. If the current modality is 'SingleMode', this map will
     *                     contain only one element, otherwise it will contain as much elements as the single test case requires
     * @param environment environment used for this test
     */
    @Override
    public void specificChecks(Map testCaseParams, Map<String, Response> rspRetrieved, String environment) {
        ServiceLoader.load(SpecificChecks.class).forEach(checks -> checks.process(
                getTestContext().getName(), testCaseParams, rspRetrieved,
                TestSuiteHandler.getInstance().getLogUtils().getTestCaseDetails(),
                TestSuiteHandler.getInstance().getEnvironmentHandler().getEnvironmentUnderTest()
        ));
    }


    public PlaceholderHandler getPlaceholderHandler() {
        return placeholderHandler;
    }

    public String getInputJsonPath() {
        return inputJsonPath;
    }

    public void setInputJsonPath(String inputJsonPath) {
        this.inputJsonPath = inputJsonPath;
    }

    public ITestContext getTestContext() {
        return testContext;
    }

    public LoggingUtils getLogUtils() {
        return TestSuiteHandler.getInstance().getLogUtils();
    }


    private Properties getEnvironmentProperties(String propFilePath) {
        Properties environments = new Properties();
        InputStream inputStream = null;
        try {
            logger.trace("loading '{}' file", propFilePath);
            inputStream = new FileInputStream(propFilePath);
            environments.load(inputStream);

        } catch (Exception oEx) {
            logger.error("Error! '{}'", oEx.getLocalizedMessage());
        } finally {
            try {
                inputStream.close();
            } catch (Exception oEx) {
                logger.error("Error! '{}'", oEx.getLocalizedMessage());
            }
        }
        return environments;
    }

}
