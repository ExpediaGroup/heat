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
package com.hotels.heat.core.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import com.hotels.heat.core.runner.TestBaseRunner;



/**
 * CustomTestNgListener is a listener that makes easier to log errors.
 */
public class CustomTestNgListener extends TestListenerAdapter {

    public static final String FAILED_TEST_CASES = "failedTestCases";
    public static final String SKIPPED_TEST_CASES = "skippedTestCases";
    public static final String PASSED_TEST_CASES = "passedTestCases";
    private final Logger logger = LoggerFactory.getLogger(CustomTestNgListener.class);

    private List<ITestResult> skippedTc;
    private List<ITestResult> passedTc;
    private List<ITestResult> failedTc;

    /**
     * This method is useful to print the output console log in case of test failed.
     * We are assuming that we put in the context an attribute whose name is the complete test case ID (example: TEST_SUITE.001) and whose value is
     * 'PASSED' or 'SKIPPED' or 'FAILED'.
     * @param tr test case result - testNG handling
     */
    @Override
    public void onTestFailure(ITestResult tr) {
        if (tr.getParameters().length > 0) {
            Map<String, String> paramMap = (HashMap<String, String>) tr.getParameters()[0];
            ITestContext testContext = tr.getTestContext();
            //testCaseCompleteID - Example: TEST_SUITE.001
            String testCaseCompleteID = testContext.getName() + TestBaseRunner.TESTCASE_ID_SEPARATOR + testContext.getAttribute(TestBaseRunner.ATTR_TESTCASE_ID);
            logger.error("[{}][{}][{}] -- FAILED", testCaseCompleteID,
                        testContext.getAttribute(TestBaseRunner.SUITE_DESCRIPTION_CTX_ATTR).toString(),
                        testContext.getAttribute(TestBaseRunner.TC_DESCRIPTION_CTX_ATTR).toString());

            if (testContext.getAttribute(FAILED_TEST_CASES) == null) {
                failedTc = new ArrayList();
            } else {
                failedTc = (List<ITestResult>) testContext.getAttribute(FAILED_TEST_CASES);
            }
            failedTc.add(tr);
            testContext.setAttribute(FAILED_TEST_CASES, failedTc);

        } else {
            super.onTestFailure(tr);
        }
    }

    /**
     * This method is useful to print the output console log in case of test success or test skipped.
     * We are assuming that we put in the context an attribute whose name is the complete test case ID (example: TEST_SUITE.001) and whose value is
     * 'PASSED' or 'SKIPPED' or 'FAILED'.
     * @param tr test case result - testNG handling
     */
    @Override
    public void onTestSuccess(ITestResult tr) {
        if (tr.getParameters().length > 0) {
            Map<String, String> paramMap = (HashMap<String, String>) tr.getParameters()[0];
            ITestContext testContext = tr.getTestContext();
            //testCaseCompleteID - Example: TEST_SUITE.001
            String testCaseCompleteID = testContext.getName() + TestBaseRunner.TESTCASE_ID_SEPARATOR + testContext.getAttribute(TestBaseRunner.ATTR_TESTCASE_ID);
            if (testContext.getAttributeNames().contains(testCaseCompleteID)
                    && TestBaseRunner.STATUS_SKIPPED.equals(testContext.getAttribute(testCaseCompleteID))) {
                logger.info("[{}][{}][{}] -- SKIPPED", testCaseCompleteID,
                        testContext.getAttribute(TestBaseRunner.SUITE_DESCRIPTION_CTX_ATTR).toString(),
                        testContext.getAttribute(TestBaseRunner.TC_DESCRIPTION_CTX_ATTR).toString());

                if (testContext.getAttribute(SKIPPED_TEST_CASES) == null) {
                    skippedTc = new ArrayList();
                } else {
                    skippedTc = (List<ITestResult>) testContext.getAttribute(SKIPPED_TEST_CASES);
                }
                skippedTc.add(tr);
                testContext.setAttribute(SKIPPED_TEST_CASES, skippedTc);

            } else {
                logger.info("[{}][{}][{}] -- PASSED", testCaseCompleteID,
                        testContext.getAttribute(TestBaseRunner.SUITE_DESCRIPTION_CTX_ATTR).toString(),
                        testContext.getAttribute(TestBaseRunner.TC_DESCRIPTION_CTX_ATTR).toString());

                if (testContext.getAttribute(PASSED_TEST_CASES) == null) {
                    passedTc = new ArrayList();
                } else {
                    passedTc = (List<ITestResult>) testContext.getAttribute(PASSED_TEST_CASES);
                }
                passedTc.add(tr);
                testContext.setAttribute(PASSED_TEST_CASES, passedTc);

            }
        } else {
            super.onTestSuccess(tr);
        }
    }

    /**
     * This method is invoked at the end of each test suite. Is useful to create console output logs, in terms of summary of the output
     * of the test suite just executed. In particular it prints the lists of success test cases, failing ones and skipped ones.
     * Data about the test case outputs are collected in 'onTestSuccess' and 'onTestFailure' methods.
     * @param testContext test context - testNG handling
     */
    @Override
    public void onFinish(ITestContext testContext) {
        String testName = testContext.getCurrentXmlTest().getName(); //test suite name
        int numTestSuccess = testContext.getPassedTests().size(); //number of successful test cases in the current test suite
        int numTestFailed = testContext.getFailedTests().size(); //number of failed test cases in the current test suite
        List<ITestResult> localSkippedTests = (List<ITestResult>) testContext.getAttribute(SKIPPED_TEST_CASES);
        int numTestSkipped = localSkippedTests.size() + testContext.getSkippedTests().size(); //number of skipped test cases in the current test suite
        if (!localSkippedTests.isEmpty()) {
            numTestSuccess = numTestSuccess - numTestSkipped;
        }
        if (numTestSkipped > 0 && numTestSuccess == 0 && numTestFailed == 0) {
            // test suite totally skipped
            logger.info("*************[{}] SKIPPED Test Suite", testName, testContext.getAttribute(TestBaseRunner.SUITE_DESCRIPTION_CTX_ATTR));
            logger.debug("*************[{}][{}] END Test Suite: Success: {} / Failed: {} / Skipped: {}",
                testName, testContext.getAttribute(TestBaseRunner.SUITE_DESCRIPTION_CTX_ATTR), numTestSuccess, numTestFailed, numTestSkipped);
        } else {
            logger.debug("*************[{}][{}] END Test Suite: Success: {} / Failed: {} / Skipped: {}",
                testName, testContext.getAttribute(TestBaseRunner.SUITE_DESCRIPTION_CTX_ATTR), numTestSuccess, numTestFailed, numTestSkipped);
        }

        if (numTestFailed > 0) {
            Iterator testFailedIterator = testContext.getFailedTests().getAllResults().iterator();
            while (testFailedIterator.hasNext()) {
                ITestResult singleResult = (ITestResult) testFailedIterator.next();
                logger.error("******* TC FAILED >> {}",
                        singleResult.getThrowable().getMessage());
            }
        }
    }

    @Override
    public void onStart(ITestContext testContext) {
        String testName = testContext.getCurrentXmlTest().getName();
        MDC.put("testName", testName);
        List<ITestResult> localSkippedTests = new ArrayList();
        testContext.setAttribute(SKIPPED_TEST_CASES, localSkippedTests);
    }

}
