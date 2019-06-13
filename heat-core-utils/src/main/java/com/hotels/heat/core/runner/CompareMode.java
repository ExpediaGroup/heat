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

import java.util.Map;

import org.testng.Reporter;
import org.testng.annotations.Test;

import com.hotels.heat.core.checks.BasicMultipleChecks;
import com.hotels.heat.core.handlers.TestSuiteHandler;
import com.hotels.heat.core.specificexception.HeatException;
import com.hotels.heat.core.utils.RestAssuredRequestMaker;

import io.restassured.response.Response;


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
