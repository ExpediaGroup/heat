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
package com.hotels.heat.core.runner;

import org.junit.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.hotels.heat.core.environment.EnvironmentHandler;
import com.hotels.heat.core.handlers.TestSuiteHandler;

/**
 * Unit test for TestBaseRunner.
 */
public class TestBaseRunnerTest {

    private TestBaseRunner underTest;

    @BeforeMethod
    public void setUp() {
        underTest = new TestBaseRunner();
        underTest.beforeTestSuite("envPropFilePath", "webappName", null);
        underTest.beforeTestCase("/testCases/path/filename.json", "env1,env2,env3", null);
    }

    @Test (enabled = true)
    public void isTestSkippableTest() {
        /*
         *  Correct usage of system parameters
         */

        //  No parameters
        System.clearProperty(EnvironmentHandler.SYS_PROP_HEAT_TEST);
        System.setProperty("environment", "env1");
        TestSuiteHandler.getInstance().getEnvironmentHandler().reloadSysTestIds();
        TestSuiteHandler.getInstance().getEnvironmentHandler().reloadSysEnv();

        Assert.assertFalse(underTest.isTestCaseSkippable("suiteName0", "001", "SVC_NAME", "http://my.service.com/svc"));

        //  Only TestSuite
        System.setProperty(EnvironmentHandler.SYS_PROP_HEAT_TEST, "suiteName1");
        TestSuiteHandler.getInstance().getEnvironmentHandler().reloadSysTestIds();

        Assert.assertFalse(underTest.isTestCaseSkippable("suiteName1", "001", "SVC_NAME", "http://my.service.com/svc"));
        Assert.assertTrue(underTest.isTestCaseSkippable("suiteNameXXX", "001", "SVC_NAME", "http://my.service.com/svc"));


        //  TestId and Suite combinations
        System.setProperty(EnvironmentHandler.SYS_PROP_HEAT_TEST, "test_suite_name5" + TestBaseRunner.TESTCASE_ID_SEPARATOR + "001");
        TestSuiteHandler.getInstance().getEnvironmentHandler().reloadSysTestIds();

        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_nameXXX", "001", "SVC_NAME", "http://my.service.com/svc"));
        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_nameXXX", "002", "SVC_NAME", "http://my.service.com/svc"));
        Assert.assertFalse(underTest.isTestCaseSkippable("test_suite_name5", "001", "SVC_NAME", "http://my.service.com/svc"));
        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_name5", "002", "SVC_NAME", "http://my.service.com/svc"));


        /*
         *   Test with multi TestIds
         */

        //  Only TestSuite
        System.setProperty(EnvironmentHandler.SYS_PROP_HEAT_TEST, "suiteName1,suiteName2,suiteName3");
        TestSuiteHandler.getInstance().getEnvironmentHandler().reloadSysTestIds();

        Assert.assertFalse(underTest.isTestCaseSkippable("suiteName1", "001", "SVC_NAME", "http://my.service.com/svc"));
        Assert.assertFalse(underTest.isTestCaseSkippable("suiteName2", "002", "SVC_NAME", "http://my.service.com/svc"));
        Assert.assertTrue(underTest.isTestCaseSkippable("suiteNameXXX", "003", "SVC_NAME", "http://my.service.com/svc"));

        //  TestId and Suite combinations
        System.setProperty(EnvironmentHandler.SYS_PROP_HEAT_TEST,
                "test_suite_name5" + TestBaseRunner.TESTCASE_ID_SEPARATOR + "001,"
                + "test_suite_name5" + TestBaseRunner.TESTCASE_ID_SEPARATOR + "002,"
                + "test_suite_name6" + TestBaseRunner.TESTCASE_ID_SEPARATOR + "001,"
                + "test_suite_name7");
        TestSuiteHandler.getInstance().getEnvironmentHandler().reloadSysTestIds();

        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_nameXXX", "001", "SVC_NAME", "http://my.service.com/svc"));
        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_nameXXX", "002", "SVC_NAME", "http://my.service.com/svc"));
        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_name5", "003", "SVC_NAME", "http://my.service.com/svc"));
        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_name6", "002", "SVC_NAME", "http://my.service.com/svc"));
        Assert.assertFalse(underTest.isTestCaseSkippable("test_suite_name5", "001", "SVC_NAME", "http://my.service.com/svc"));
        Assert.assertFalse(underTest.isTestCaseSkippable("test_suite_name5", "002", "SVC_NAME", "http://my.service.com/svc"));
        Assert.assertFalse(underTest.isTestCaseSkippable("test_suite_name6", "001", "SVC_NAME", "http://my.service.com/svc"));
        Assert.assertFalse(underTest.isTestCaseSkippable("test_suite_name7", "003", "SVC_NAME", "http://my.service.com/svc"));


        //  TestId and Suite combinations WITH spaces and upper/lower cases
        System.setProperty(EnvironmentHandler.SYS_PROP_HEAT_TEST,
            " test_SUITE_name5" + TestBaseRunner.TESTCASE_ID_SEPARATOR + "001 ,"
                + "test_suite_NAME5" + TestBaseRunner.TESTCASE_ID_SEPARATOR + "002,   "
                + "TEST_SUITE_NAME6" + TestBaseRunner.TESTCASE_ID_SEPARATOR + "001  ,"
                + "   TEST_suite_name7 ");
        TestSuiteHandler.getInstance().getEnvironmentHandler().reloadSysTestIds();

        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_nameXXX", "001", "SVC_NAME", "http://my.service.com/svc"));
        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_nameXXX", "002", "SVC_NAME", "http://my.service.com/svc"));
        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_name5", "003", "SVC_NAME", "http://my.service.com/svc"));
        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_name6", "002", "SVC_NAME", "http://my.service.com/svc"));
        Assert.assertFalse(underTest.isTestCaseSkippable("test_suite_name5", "001", "SVC_NAME", "http://my.service.com/svc"));
        Assert.assertFalse(underTest.isTestCaseSkippable("test_suite_name5", "002", "SVC_NAME", "http://my.service.com/svc"));
        Assert.assertFalse(underTest.isTestCaseSkippable("test_suite_name6", "001", "SVC_NAME", "http://my.service.com/svc"));
        Assert.assertFalse(underTest.isTestCaseSkippable("test_suite_name7", "003", "SVC_NAME", "http://my.service.com/svc"));

        /*
         *   Missing Common params
         */
        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_nameXXX", "001", null, "http://my.service.com/svc"));
        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_nameXXX", "001", "SVC_NAME", null));
        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_nameXXX", "001", null, null));
        underTest.setInputJsonPath(null);
        Assert.assertTrue(underTest.isTestCaseSkippable("test_suite_nameXXX", "001", "SVC_NAME", "http://my.service.com/svc"));
    }


}
