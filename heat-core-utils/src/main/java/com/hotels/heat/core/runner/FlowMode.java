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
package com.hotels.heat.core.runner;

import java.util.Arrays;
import java.util.Map;

import org.testng.annotations.Test;

import com.hotels.heat.core.checks.BasicFlowChecks;
import com.hotels.heat.core.handlers.TestCaseMapHandler;
import com.hotels.heat.core.handlers.TestSuiteHandler;
import com.hotels.heat.core.utils.RestAssuredRequestMaker;

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
            Map  testCaseParamsElaborated = super.resolvePlaceholdersInTcParams(testCaseParams, Arrays.asList("beforeStep"));
            getLogUtils().debug("test not skippable");
            RestAssuredRequestMaker restAssuredRequestMaker = new RestAssuredRequestMaker();
            getLogUtils().debug("I'm going to execute 'retrieveInfo'");
            BasicFlowChecks flowChecks = new BasicFlowChecks(getLogUtils(), testSuiteHandler.getTestCaseUtils(), getTestContext());
            flowChecks.setRestAssuredRequestMaker(restAssuredRequestMaker);

            TestCaseMapHandler tcMapHandler = new TestCaseMapHandler(testCaseParamsElaborated, getPlaceholderHandler());
            Map<String, Object> elaboratedTestCaseParams = tcMapHandler.retrieveProcessedMap();
            Map<String, Response> rspRetrieved = flowChecks.retrieveInfo(elaboratedTestCaseParams);

            super.specificChecks(testCaseParamsElaborated, rspRetrieved, testSuiteHandler.getEnvironmentHandler().getEnvironmentUnderTest());

        } else {
            getLogUtils().trace("test skippable [{}]", testCaseId);
            getTestContext().setAttribute(testSuiteName + TestBaseRunner.TESTCASE_ID_SEPARATOR + testCaseId,
                    TestBaseRunner.STATUS_SKIPPED);
        }
    }
}
