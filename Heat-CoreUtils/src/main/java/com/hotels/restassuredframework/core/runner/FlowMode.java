package com.hotels.restassuredframework.core.runner;

import java.util.Map;

import org.testng.annotations.Test;

import com.hotels.restassuredframework.core.checks.BasicFlowChecks;
import com.hotels.restassuredframework.core.handlers.TestCaseMapHandler;
import com.hotels.restassuredframework.core.handlers.TestSuiteHandler;
import com.hotels.restassuredframework.core.utils.RestAssuredRequestMaker;
import com.jayway.restassured.response.Response;


/**
 * It is the runner of the flow mode.
 */
public class FlowMode extends TestBaseRunner {

    /**
     * Method that manages the execution of a single test case.
     * @param testCaseParams Map containing test case parameters coming from the json input file
     */
    @Test(dataProvider = "provider")
    public void runningTest(Map testCaseParams) {
        TestSuiteHandler testSuiteHandler = TestSuiteHandler.getInstance();
        setContextAttributes(testCaseParams);
        String testSuiteName = getTestContext().getName();
        String testCaseId = testCaseParams.get(TestBaseRunner.ATTR_TESTCASE_ID).toString();
        getTestContext().setAttribute(TestBaseRunner.ATTR_TESTCASE_ID, testCaseId);

        if (!super.isTestCaseSkippable(testSuiteName, testCaseId, "", "")) {
            Map  testCaseParamsElaborated = super.resolvePlaceholdersInTcParams(testCaseParams);
            getLogUtils().debug("test not skippable");
            RestAssuredRequestMaker restAssuredRequestMaker = new RestAssuredRequestMaker();
            getLogUtils().debug("I'm going to execute 'retrieveInfo'");
            BasicFlowChecks flowChecks = new BasicFlowChecks(getLogUtils(), testSuiteHandler.getTestCaseUtils(), getTestContext());
            flowChecks.setRestAssuredRequestMaker(restAssuredRequestMaker);

            TestCaseMapHandler tcMapHandler = new TestCaseMapHandler(testCaseParamsElaborated, getPlaceholderHandler());
            Map<String, Object> elaboratedTestCaseParams = (Map) tcMapHandler.retriveProcessedMap();
            Map<String, Response> rspRetrieved = flowChecks.retrieveInfo(elaboratedTestCaseParams);

            super.specificChecks(testCaseParamsElaborated, rspRetrieved, testSuiteHandler.getEnvironmentHandler().getEnvironmentUnderTest());

        } else {
            getLogUtils().trace("test skippable [{}]", testCaseId);
            getTestContext().setAttribute(testSuiteName + TestBaseRunner.TESTCASE_ID_SEPARATOR + testCaseId,
                    TestBaseRunner.STATUS_SKIPPED);
        }
    }
}
