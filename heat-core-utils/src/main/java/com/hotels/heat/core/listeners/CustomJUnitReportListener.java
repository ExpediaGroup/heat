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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.collections.Maps;
import org.testng.internal.Utils;
import org.testng.reporters.XMLConstants;
import org.testng.reporters.XMLStringBuffer;
import org.testng.xml.XmlSuite;

/**
 * Listener for JUNIT report.
 */
public class CustomJUnitReportListener implements IReporter {

    private static final Pattern ENTITY = Pattern.compile("&[a-zA-Z]+;.*");
    private static final Pattern LESS = Pattern.compile("<");
    private static final Pattern GREATER = Pattern.compile(">");
    private static final Pattern SINGLE_QUOTE = Pattern.compile("'");
    private static final Pattern QUOTE = Pattern.compile("\"");
    private static final Map<String, Pattern> ATTR_ESCAPES = Maps.newHashMap();

    static {
        ATTR_ESCAPES.put("&lt;", LESS);
        ATTR_ESCAPES.put("&gt;", GREATER);
        ATTR_ESCAPES.put("&apos;", SINGLE_QUOTE);
        ATTR_ESCAPES.put("&quot;", QUOTE);
    }

    private static final String TAG_HEAT_RUN = "heatRun";
    private static final String PROP_NUM_OF_SUITES = "numberOfSuites";
    private static final String PROP_STATUS = "status";
    private static final String PROP_TEST_ID = "testId";
    private static final String PASSED_TC = "passedTestCases";
    private static final String FAILED_TC = "failedTestCases";
    private static final String SKIPPED_TC = "skippedTestCases";

    private static final String REPORT_PATH = "target/junitReports";
    private static final String REPORT_NAME = "HEAT_report.xml";

    private static final String SUCCESS_STATUS = "success";
    private static final String SKIPPED_STATUS = "skipped";
    private static final String FAILED_STATUS = "failed";

    private int numberOfPassedSuites;
    private long testRunningTotalTime;

    // http://stackoverflow.com/questions/10219640/create-custom-testng-report-webdriver

    /**
     * Generates a cutom JUnit report in XML, writing it on file system as XML.
     */
    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {

        XMLStringBuffer document = new XMLStringBuffer();
        document.push(TAG_HEAT_RUN);
        suites.forEach(suite -> {
            Map<String, ISuiteResult> results = suite.getResults();
            Properties attributesTestSuites = new Properties();
            int numTestSuites = results.size();
            attributesTestSuites.setProperty(PROP_NUM_OF_SUITES, String.valueOf(numTestSuites));
            document.push(XMLConstants.TESTSUITES, attributesTestSuites);

            results.forEach((suiteName, suiteResults) -> {
                Properties attributesSingleTestSuite = new Properties();
                attributesSingleTestSuite.setProperty(XMLConstants.ATTR_NAME, suiteName);

                ITestContext suiteContext = suiteResults.getTestContext();
                attributesSingleTestSuite = setTestSuiteAttribute(attributesSingleTestSuite, suiteName, suiteContext);

                document.push(XMLConstants.TESTSUITE, attributesSingleTestSuite);
                getList(FAILED_TC, suiteContext).forEach(failedTestCase -> {
                    setFailedTcAttribute(document, failedTestCase);
                });
                getList(PASSED_TC, suiteContext).forEach(passedTestCase -> {
                    Properties attributesPassedTestSuites = new Properties();
                    String tcName = ((HashMap<String, String>) passedTestCase.getParameters()[0]).get(PROP_TEST_ID);
                    attributesPassedTestSuites.setProperty(XMLConstants.ATTR_NAME, tcName);
                    attributesPassedTestSuites.setProperty(PROP_STATUS, SUCCESS_STATUS);
                    long elapsedTimeMillis = passedTestCase.getEndMillis() - passedTestCase.getStartMillis();
                    testRunningTotalTime += elapsedTimeMillis;
                    attributesPassedTestSuites.setProperty(XMLConstants.ATTR_TIME, String.valueOf(((double) elapsedTimeMillis) / 1000));
                    document.push(XMLConstants.TESTCASE, attributesPassedTestSuites);
                    document.pop(XMLConstants.TESTCASE); // passed XMLConstants.TESTCASE
                });
                getList(SKIPPED_TC, suiteContext).forEach(skippedTestCase -> {
                    Properties attributesSkippedTestSuites = new Properties();
                    String tcName = ((HashMap<String, String>) skippedTestCase.getParameters()[0]).get(PROP_TEST_ID);
                    attributesSkippedTestSuites.setProperty(XMLConstants.ATTR_NAME, tcName);
                    long elapsedTimeMillis = skippedTestCase.getEndMillis() - skippedTestCase.getStartMillis();
                    testRunningTotalTime += elapsedTimeMillis;
                    attributesSkippedTestSuites.setProperty(PROP_STATUS, SKIPPED_STATUS);
                    document.push(XMLConstants.TESTCASE, attributesSkippedTestSuites);
                    document.pop(XMLConstants.TESTCASE); // skipped XMLConstants.TESTCASE
                });
                document.pop(XMLConstants.TESTSUITE); // XMLConstants.TESTSUITE
            });
            document.pop(); // XMLConstants.TESTSUITES
        });
        document.pop(); //heatRun
        Utils.writeUtf8File(REPORT_PATH, REPORT_NAME, document.toXML());
    }

    private List<ITestResult> getList(String listName, ITestContext context) {
        List<ITestResult> outputList = new ArrayList();
        if (context.getAttributeNames().contains(listName)) {
            outputList = (List<ITestResult>) context.getAttribute(listName);
        }
        return outputList;
    }

    private Properties setTestSuiteAttribute(Properties testSuiteAttr, String suiteName, ITestContext suiteContext) {
        Properties testSuiteAttributes = testSuiteAttr;
        int numFailures = getList(FAILED_TC, suiteContext).size();
        int numSuccess = getList(PASSED_TC, suiteContext).size();
        int numSkipped = getList(SKIPPED_TC, suiteContext).size();
        testSuiteAttributes.setProperty(XMLConstants.ATTR_NAME, suiteName);
        int tests = numFailures + numSuccess + numSkipped;
        if (tests == 0) {
            testSuiteAttributes.setProperty(PROP_STATUS, SKIPPED_STATUS);
        } else {
            testSuiteAttributes.setProperty(SUCCESS_STATUS, String.valueOf(numSuccess));
            testSuiteAttributes.setProperty(SKIPPED_STATUS, String.valueOf(numSkipped));
            int numberOfTests = numFailures + numSuccess + numSkipped;
            testSuiteAttributes.setProperty(XMLConstants.ATTR_TESTS, String.valueOf(numberOfTests));
            Date timeStamp = Calendar.getInstance().getTime();
            testSuiteAttributes.setProperty(XMLConstants.ATTR_FAILURES, String.valueOf(numFailures));
            testSuiteAttributes.setProperty(XMLConstants.ATTR_TIMESTAMP, timeStamp.toGMTString());
            if (numFailures > 0) {
                testSuiteAttributes.setProperty(PROP_STATUS, FAILED_STATUS);
            } else {
                testSuiteAttributes.setProperty(PROP_STATUS, SUCCESS_STATUS);
                numberOfPassedSuites += 1;
            }
        }

        return testSuiteAttributes;
    }

    private void setFailedTcAttribute(XMLStringBuffer doc, ITestResult failedTestCase) {
        Properties attributesFailedTestSuites = new Properties();
        String tcName = ((HashMap<String, String>) failedTestCase.getParameters()[0]).get(PROP_TEST_ID);
        attributesFailedTestSuites.setProperty(XMLConstants.ATTR_NAME, tcName);
        long elapsedTimeMillis = failedTestCase.getEndMillis() - failedTestCase.getStartMillis();
        testRunningTotalTime += elapsedTimeMillis;
        Throwable t = failedTestCase.getThrowable();
        doc.push(XMLConstants.TESTCASE, attributesFailedTestSuites);
        if (t != null) {
            attributesFailedTestSuites.setProperty(XMLConstants.ATTR_TYPE, t.getClass().getName());
            String message = t.getMessage();
            if ((message != null) && (message.length() > 0)) {
                attributesFailedTestSuites.setProperty(XMLConstants.ATTR_MESSAGE, encodeAttr(message)); // ENCODE
            }
            doc.push(XMLConstants.FAILURE, attributesFailedTestSuites);
            doc.addCDATA(Utils.stackTrace(t, false)[0]);
            doc.pop();
        } else {
            doc.addEmptyElement(XMLConstants.FAILURE); // THIS IS AN ERROR
        }
        doc.pop();
    }

    private String encodeAttr(String attr) {
        String result = replaceAmpersand(attr, ENTITY);
        for (Map.Entry<String, Pattern> e : ATTR_ESCAPES.entrySet()) {
            result = e.getValue().matcher(result).replaceAll(e.getKey());
        }

        return result;
    }

    private String replaceAmpersand(String str, Pattern pattern) {
        int start = 0;
        String outputStr = "";
        int idx = str.indexOf('&', start);
        if (idx == -1) {
            outputStr = str;
        } else {
            StringBuffer result = new StringBuffer();
            while (idx != -1) {
                result.append(str.substring(start, idx));
                if (pattern.matcher(str.substring(idx)).matches()) {
                    // do nothing it is an entity;
                    result.append("&");
                } else {
                    result.append("&amp;");
                }
                start = idx + 1;
                idx = str.indexOf('&', start);
            }
            result.append(str.substring(start));

            outputStr = result.toString();
        }
        return outputStr;
    }

}
