/**
 * Copyright (C) 2015-2017 Expedia Inc.
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
package com.hotels.heat.core.utils;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.hotels.heat.core.environment.EnvironmentHandler;
import com.hotels.heat.core.handlers.TestSuiteHandler;
import com.hotels.heat.core.runner.TestBaseRunner;


/**
 * Unit test for TestCaseUtils.
 */
public class TestCaseUtilsTest {

    private TestCaseUtils underTest;

    @BeforeMethod
    public void setUp() {
        this.underTest = new TestCaseUtils();

        System.setProperty("environment", "env1");
        EnvironmentHandler eh = new EnvironmentHandler("envPropFilePath");
        eh.setEnabledEnvironments("env1,env2,env3");

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
