package com.hotels.restassuredframework.core.runner;

import java.util.Map;

import org.testng.Reporter;
import org.testng.annotations.Test;

import com.hotels.restassuredframework.core.checks.BasicMultipleChecks;
import com.hotels.restassuredframework.core.handlers.TestSuiteHandler;
import com.hotels.restassuredframework.core.specificexception.HeatException;
import com.hotels.restassuredframework.core.utils.RestAssuredRequestMaker;
import com.jayway.restassured.response.Response;


/**
 * It is the runner of the compare mode.
 */
public class CompareMode extends TestBaseRunner {

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
            try {
                RestAssuredRequestMaker restAssuredRequestMaker = new RestAssuredRequestMaker();
                BasicMultipleChecks compareChecks = new BasicMultipleChecks(getTestContext());
                compareChecks.setRestAssuredRequestMaker(restAssuredRequestMaker);
                Map<String, Response> rspRetrieved = compareChecks.retrieveInfo(testCaseParamsElaborated);
                if (rspRetrieved.isEmpty()) {
                    getLogUtils().debug("not any retrieved response");
                } else {
                    rspRetrieved.entrySet().stream().forEach((entry) -> {
                        if (entry.getValue() == null) {
                            getLogUtils().debug("RSP retrieved by {} --> null", entry.getKey());
                        } else {
                            getLogUtils().debug("RSP retrieved by {} --> {}",
                                    entry.getKey(), entry.getValue().asString());
                            Reporter.log(entry.getValue().asString());
                        }
                    });
                    compareChecks.expects(testSuiteHandler.getTestCaseUtils().getSystemParamOnBlocking(), testCaseParamsElaborated, rspRetrieved);

                    super.specificChecks(testCaseParamsElaborated, rspRetrieved, testSuiteHandler.getEnvironmentHandler().getEnvironmentUnderTest());


                }
            } catch (Exception oEx) {
                getLogUtils().error("Exception ({}) occourred: '{}'", oEx.getClass(), oEx.getLocalizedMessage());
                throw new HeatException(testSuiteHandler.getLogUtils().getExceptionDetails() + "Exception (" + oEx.getClass() + ") occourred: '"
                        + oEx.getLocalizedMessage() + "'");
            }
        } else {
            getLogUtils().trace("test skippable [{}]", testCaseId);
            getTestContext().setAttribute(testSuiteName + TestBaseRunner.TESTCASE_ID_SEPARATOR + testCaseId,
                    TestBaseRunner.STATUS_SKIPPED);
        }
    }

}
