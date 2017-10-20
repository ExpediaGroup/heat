package com.hotels.restassuredframework.core.utils;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.hotels.restassuredframework.core.environment.EnvironmentHandler;
import com.hotels.restassuredframework.core.handlers.TestSuiteHandler;
import com.hotels.restassuredframework.core.runner.TestBaseRunner;


/**
 * Unit test for TestCaseUtils.
 * @author Luca Pelosi
 */
public class TestCaseUtilsTest {

    private TestCaseUtils underTest;

    @BeforeMethod
    public void setUp() {
        this.underTest = new TestCaseUtils();

        System.setProperty("environment", "staging1");
        EnvironmentHandler eh = new EnvironmentHandler("envPropFilePath");
        eh.setEnabledEnvironments("staging1,MILAN,PROD");

        TestSuiteHandler.getInstance().setEnvironmentHandler(eh);
    }

    @Test
    public void isTestSuiteRunnableTest() {

        //  No parameters
        System.clearProperty(EnvironmentHandler.SYS_PROP_HEAT_TEST);
        TestSuiteHandler.getInstance().getEnvironmentHandler().reloadSysTestIds();

        Assert.assertTrue(underTest.isTestSuiteRunnable("testSuite0"));

        //  Only TestSuite
        System.setProperty(EnvironmentHandler.SYS_PROP_HEAT_TEST, "testSuite1");
        TestSuiteHandler.getInstance().getEnvironmentHandler().reloadSysTestIds();

        Assert.assertTrue(underTest.isTestSuiteRunnable("testSuite1"));
        Assert.assertFalse(underTest.isTestSuiteRunnable("testSuiteXXX"));


        //  TestSuite.testCaseId
        System.setProperty(EnvironmentHandler.SYS_PROP_HEAT_TEST, "testSuite2" + TestBaseRunner.TESTCASE_ID_SEPARATOR + "001");
        TestSuiteHandler.getInstance().getEnvironmentHandler().reloadSysTestIds();

        Assert.assertTrue(underTest.isTestSuiteRunnable("testSuite2"));
        Assert.assertFalse(underTest.isTestSuiteRunnable("testSuiteXXX"));

        System.setProperty(EnvironmentHandler.SYS_PROP_HEAT_TEST, "testSuite4" + TestBaseRunner.TESTCASE_ID_SEPARATOR + "003");
        TestSuiteHandler.getInstance().getEnvironmentHandler().reloadSysTestIds();

        Assert.assertTrue(underTest.isTestSuiteRunnable("testSuite4"));
        Assert.assertFalse(underTest.isTestSuiteRunnable("testSuite5"));

        /*
         *   Test with multi TestIds
         */

        //  Only TestSuite
        System.setProperty(EnvironmentHandler.SYS_PROP_HEAT_TEST, "testSuite1,testSuite2");
        TestSuiteHandler.getInstance().getEnvironmentHandler().reloadSysTestIds();


        Assert.assertTrue(underTest.isTestSuiteRunnable("testSuite1"));
        Assert.assertTrue(underTest.isTestSuiteRunnable("testSuite2"));
        Assert.assertFalse(underTest.isTestSuiteRunnable("testSuiteXXX"));

        //   TestSuite and id
        System.setProperty(EnvironmentHandler.SYS_PROP_HEAT_TEST,
            "testSuite1" + TestBaseRunner.TESTCASE_ID_SEPARATOR + "001,"
            + "testSuite2" + TestBaseRunner.TESTCASE_ID_SEPARATOR + "002");
        TestSuiteHandler.getInstance().getEnvironmentHandler().reloadSysTestIds();


        Assert.assertTrue(underTest.isTestSuiteRunnable("testSuite1"));
        Assert.assertTrue(underTest.isTestSuiteRunnable("testSuite2"));
        Assert.assertFalse(underTest.isTestSuiteRunnable("testSuiteXXX"));

        //   TestSuite and id mixed with extra spaces and lowe/upper cases
        System.setProperty(EnvironmentHandler.SYS_PROP_HEAT_TEST,
            " TESTSuite1" + TestBaseRunner.TESTCASE_ID_SEPARATOR + "001 ,"
            + "   tEstSuitE2" + TestBaseRunner.TESTCASE_ID_SEPARATOR + "002 ");
        TestSuiteHandler.getInstance().getEnvironmentHandler().reloadSysTestIds();


        Assert.assertTrue(underTest.isTestSuiteRunnable("testSuite1"));
        Assert.assertTrue(underTest.isTestSuiteRunnable("testSuite2"));
        Assert.assertFalse(underTest.isTestSuiteRunnable("testSuiteXXX"));
    }
}
