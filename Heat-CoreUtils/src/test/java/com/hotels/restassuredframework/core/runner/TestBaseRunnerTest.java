package com.hotels.restassuredframework.core.runner;

import org.junit.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.hotels.restassuredframework.core.environment.EnvironmentHandler;
import com.hotels.restassuredframework.core.handlers.TestSuiteHandler;

/**
 * Unit test for TestBaseRunner.
 * @author Luca Pelosi
 */
public class TestBaseRunnerTest {

    private TestBaseRunner underTest;

    @BeforeMethod
    public void setUp() {
        underTest = new TestBaseRunner();
        underTest.beforeTestSuite("envPropFilePath", "webappName", null);
        underTest.beforeTestCase("/testCases/listingEndpoint/ListingHcom_Compare.json", "staging1,MILAN,PROD", null);
    }

    @Test (enabled = false)
    public void isTestSkippableTest() {
        /*
         *  Correct usage of system parameters
         */

        //  No parameters
        System.clearProperty(EnvironmentHandler.SYS_PROP_HEAT_TEST);
        TestSuiteHandler.getInstance().getEnvironmentHandler().reloadSysTestIds();

        Assert.assertFalse(underTest.isTestCaseSkippable("suiteName0", "001", "SRLE", "http://searchresultlistingedge.staging.hcom/srle"));

        //  Only TestSuite
        System.setProperty(EnvironmentHandler.SYS_PROP_HEAT_TEST, "suiteName1");
        TestSuiteHandler.getInstance().getEnvironmentHandler().reloadSysTestIds();

        Assert.assertFalse(underTest.isTestCaseSkippable("suiteName1", "001", "SRLE", "http://searchresultlistingedge.staging.hcom/srle"));
        Assert.assertTrue(underTest.isTestCaseSkippable("suiteNameXXX", "001", "SRLE", "http://searchresultlistingedge.staging.hcom/srle"));


        //  TestId and Suite combinations
        System.setProperty(EnvironmentHandler.SYS_PROP_HEAT_TEST, "test_suite_name5" + TestBaseRunner.TESTCASE_ID_SEPARATOR + "001");
        TestSuiteHandler.getInstance().getEnvironmentHandler().reloadSysTestIds();

        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_nameXXX", "001", "SRLE", "http://searchresultlistingedge.staging.hcom/srle"));
        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_nameXXX", "002", "SRLE", "http://searchresultlistingedge.staging.hcom/srle"));
        Assert.assertFalse(underTest.isTestCaseSkippable("test_suite_name5", "001", "SRLE", "http://searchresultlistingedge.staging.hcom/srle"));
        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_name5", "002", "SRLE", "http://searchresultlistingedge.staging.hcom/srle"));


        /*
         *   Test with multi TestIds
         */

        //  Only TestSuite
        System.setProperty(EnvironmentHandler.SYS_PROP_HEAT_TEST, "suiteName1,suiteName2,suiteName3");
        TestSuiteHandler.getInstance().getEnvironmentHandler().reloadSysTestIds();

        Assert.assertFalse(underTest.isTestCaseSkippable("suiteName1", "001", "SRLE", "http://searchresultlistingedge.staging.hcom/srle"));
        Assert.assertFalse(underTest.isTestCaseSkippable("suiteName2", "002", "SRLE", "http://searchresultlistingedge.staging.hcom/srle"));
        Assert.assertTrue(underTest.isTestCaseSkippable("suiteNameXXX", "003", "SRLE", "http://searchresultlistingedge.staging.hcom/srle"));

        //  TestId and Suite combinations
        System.setProperty(EnvironmentHandler.SYS_PROP_HEAT_TEST,
                "test_suite_name5" + TestBaseRunner.TESTCASE_ID_SEPARATOR + "001,"
                + "test_suite_name5" + TestBaseRunner.TESTCASE_ID_SEPARATOR + "002,"
                + "test_suite_name6" + TestBaseRunner.TESTCASE_ID_SEPARATOR + "001,"
                + "test_suite_name7");
        TestSuiteHandler.getInstance().getEnvironmentHandler().reloadSysTestIds();

        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_nameXXX", "001", "SRLE", "http://searchresultlistingedge.staging.hcom/srle"));
        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_nameXXX", "002", "SRLE", "http://searchresultlistingedge.staging.hcom/srle"));
        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_name5", "003", "SRLE", "http://searchresultlistingedge.staging.hcom/srle"));
        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_name6", "002", "SRLE", "http://searchresultlistingedge.staging.hcom/srle"));
        Assert.assertFalse(underTest.isTestCaseSkippable("test_suite_name5", "001", "SRLE", "http://searchresultlistingedge.staging.hcom/srle"));
        Assert.assertFalse(underTest.isTestCaseSkippable("test_suite_name5", "002", "SRLE", "http://searchresultlistingedge.staging.hcom/srle"));
        Assert.assertFalse(underTest.isTestCaseSkippable("test_suite_name6", "001", "SRLE", "http://searchresultlistingedge.staging.hcom/srle"));
        Assert.assertFalse(underTest.isTestCaseSkippable("test_suite_name7", "003", "SRLE", "http://searchresultlistingedge.staging.hcom/srle"));


        //  TestId and Suite combinations WITH spaces and upper/lower cases
        System.setProperty(EnvironmentHandler.SYS_PROP_HEAT_TEST,
            " test_SUITE_name5" + TestBaseRunner.TESTCASE_ID_SEPARATOR + "001 ,"
                + "test_suite_NAME5" + TestBaseRunner.TESTCASE_ID_SEPARATOR + "002,   "
                + "TEST_SUITE_NAME6" + TestBaseRunner.TESTCASE_ID_SEPARATOR + "001  ,"
                + "   TEST_suite_name7 ");
        TestSuiteHandler.getInstance().getEnvironmentHandler().reloadSysTestIds();

        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_nameXXX", "001", "SRLE", "http://searchresultlistingedge.staging.hcom/srle"));
        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_nameXXX", "002", "SRLE", "http://searchresultlistingedge.staging.hcom/srle"));
        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_name5", "003", "SRLE", "http://searchresultlistingedge.staging.hcom/srle"));
        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_name6", "002", "SRLE", "http://searchresultlistingedge.staging.hcom/srle"));
        Assert.assertFalse(underTest.isTestCaseSkippable("test_suite_name5", "001", "SRLE", "http://searchresultlistingedge.staging.hcom/srle"));
        Assert.assertFalse(underTest.isTestCaseSkippable("test_suite_name5", "002", "SRLE", "http://searchresultlistingedge.staging.hcom/srle"));
        Assert.assertFalse(underTest.isTestCaseSkippable("test_suite_name6", "001", "SRLE", "http://searchresultlistingedge.staging.hcom/srle"));
        Assert.assertFalse(underTest.isTestCaseSkippable("test_suite_name7", "003", "SRLE", "http://searchresultlistingedge.staging.hcom/srle"));

        /*
         *   Missing Common params
         */
        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_nameXXX", "001", null, "http://searchresultlistingedge.staging.hcom/srle"));
        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_nameXXX", "001", "SRLE", null));
        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_nameXXX", "001", null, null));
        underTest.setInputJsonPath(null);
        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_nameXXX", "001", "SRLE", "http://searchresultlistingedge.staging.hcom/srle"));
    }


}
