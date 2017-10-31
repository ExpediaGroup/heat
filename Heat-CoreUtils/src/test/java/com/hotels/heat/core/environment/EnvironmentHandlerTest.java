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
package com.hotels.heat.core.environment;

import static org.mockito.BDDMockito.given;
import static org.powermock.api.mockito.PowerMockito.mock;

import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.hotels.heat.core.handlers.PropertyHandler;


/**
 * Unit Tests for {@link EnvironmentHandler}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({EnvironmentHandler.class, PropertyHandler.class})
public class EnvironmentHandlerTest {

    @Mock
    private PropertyHandler inputPh;

    @InjectMocks
    private EnvironmentHandler underTest;

    @BeforeMethod
    public void setUp() {
        inputPh = mock(PropertyHandler.class);

    }

    private void clearSysProperties() {
        System.clearProperty("defaultEnvironment");
        System.clearProperty("environment");
        System.clearProperty("webappName");
    }

    @Test
    public void testFirstConstructorSettings() {

        //GIVEN
        System.setProperty("defaultEnvironment", "DEFAULT");
        System.setProperty("environment", "ENV_UNDER_TEST");
        System.setProperty("webappName", "WEBAPP_UNDER_TEST");


        underTest = new EnvironmentHandler(inputPh);
        underTest.setEnabledEnvironments("ENV_UNDER_TEST,ENV_NOT_UNDER_TEST");

        Assert.assertEquals(underTest.getDefaultEnvironment(), "DEFAULT");
        Assert.assertEquals(underTest.getEnvironmentUnderTest(), "ENV_UNDER_TEST");
        Assert.assertEquals(underTest.getWebappUnderTest(), "WEBAPP_UNDER_TEST");

        Assert.assertEquals(underTest.getEnabledEnvironments(), "ENV_UNDER_TEST,ENV_NOT_UNDER_TEST");

        String webapp = "WEBAPP_UNDER_TEST"; //in this case is the principal one
        // environment not starting with http
        // environment enabled

        given(inputPh.getProperty("WEBAPP_UNDER_TEST.ENV_UNDER_TEST.path")).willReturn("OUTPUT_URL");

        String url = underTest.getEnvironmentUrl(webapp);
        Assert.assertEquals(url, "OUTPUT_URL");

        //RESET SYSTEM PROPERTIES
        clearSysProperties();
    }

    @Test
    public void testFirstConstructorSettingsOne() {

        //GIVEN
        System.setProperty("defaultEnvironment", "DEFAULT");
        System.setProperty("environment", "ENV_UNDER_TEST");
        System.setProperty("webappName", "WEBAPP_UNDER_TEST");


        underTest = new EnvironmentHandler(inputPh);
        underTest.setEnabledEnvironments("ENV_UNDER_TEST,ENV_NOT_UNDER_TEST");

        Assert.assertEquals(underTest.getDefaultEnvironment(), "DEFAULT");
        Assert.assertEquals(underTest.getEnvironmentUnderTest(), "ENV_UNDER_TEST");
        Assert.assertEquals(underTest.getWebappUnderTest(), "WEBAPP_UNDER_TEST");

        Assert.assertEquals(underTest.getEnabledEnvironments(), "ENV_UNDER_TEST,ENV_NOT_UNDER_TEST");

        String webapp = "WEBAPP_NOT_UNDER_TEST"; //in this case it is NOT the principal one
        // environment not starting with http
        // environment is enabled

        given(inputPh.getProperty("WEBAPP_NOT_UNDER_TEST.DEFAULT.path")).willReturn("OUTPUT_URL");
        given(inputPh.getProperty("WEBAPP_NOT_UNDER_TEST.ENV_UNDER_TEST.path")).willReturn("OUTPUT_GOOD_URL");

        String url = underTest.getEnvironmentUrl(webapp);
        Assert.assertEquals(url, "OUTPUT_GOOD_URL");

        //RESET SYSTEM PROPERTIES
        clearSysProperties();
    }

    @Test
    public void testFirstConstructorSettingsTwo() {

        //GIVEN
        System.setProperty("defaultEnvironment", "DEFAULT");
        System.setProperty("environment", "NOT_ALLOWED_ENVIRONMENT");
        System.setProperty("webappName", "WEBAPP_UNDER_TEST");


        underTest = new EnvironmentHandler(inputPh);
        underTest.setEnabledEnvironments("ENV_UNDER_TEST,ENV_NOT_UNDER_TEST");

        Assert.assertEquals(underTest.getDefaultEnvironment(), "DEFAULT");
        Assert.assertEquals(underTest.getEnvironmentUnderTest(), "NOT_ALLOWED_ENVIRONMENT");
        Assert.assertEquals(underTest.getWebappUnderTest(), "WEBAPP_UNDER_TEST");

        Assert.assertEquals(underTest.getEnabledEnvironments(), "ENV_UNDER_TEST,ENV_NOT_UNDER_TEST");

        String webapp = "WEBAPP_UNDER_TEST";
        // environment not starting with http
        // environment is enabled

        //given(inputPh.getProperty("WEBAPP_NOT_UNDER_TEST.DEFAULT.path")).willReturn("OUTPUT_URL");

        String url = underTest.getEnvironmentUrl(webapp);
        Assert.assertNull(url);

        //RESET SYSTEM PROPERTIES
        clearSysProperties();
    }

    @Test
    public void testFirstConstructorSettingsThree() {

        //GIVEN
        System.setProperty("defaultEnvironment", "DEFAULT");
        System.setProperty("environment", "http://urlEsterna");
        System.setProperty("webappName", "WEBAPP_UNDER_TEST");


        underTest = new EnvironmentHandler(inputPh);
        underTest.setEnabledEnvironments("ENV_UNDER_TEST,ENV_NOT_UNDER_TEST");

        Assert.assertEquals(underTest.getDefaultEnvironment(), "DEFAULT");
        Assert.assertEquals(underTest.getEnvironmentUnderTest(), "http://urlEsterna");
        Assert.assertEquals(underTest.getWebappUnderTest(), "WEBAPP_UNDER_TEST");

        Assert.assertEquals(underTest.getEnabledEnvironments(), "ENV_UNDER_TEST,ENV_NOT_UNDER_TEST");

        String webapp = "WEBAPP_UNDER_TEST";
        // environment not starting with http
        // environment is enabled

        //given(inputPh.getProperty("WEBAPP_NOT_UNDER_TEST.DEFAULT.path")).willReturn("OUTPUT_URL");

        String url = underTest.getEnvironmentUrl(webapp);
        Assert.assertEquals(url, "http://urlEsterna");

        //RESET SYSTEM PROPERTIES
        clearSysProperties();
    }

    @Test
    public void testFirstConstructorSettingsFour() {

        //GIVEN
        System.setProperty("defaultEnvironment", "DEFAULT");
        System.setProperty("environment", "http://urlEsterna");
        System.setProperty("webappName", "WEBAPP_UNDER_TEST");


        underTest = new EnvironmentHandler(inputPh);
        underTest.setEnabledEnvironments("ENV_UNDER_TEST,ENV_NOT_UNDER_TEST");

        Assert.assertEquals(underTest.getDefaultEnvironment(), "DEFAULT");
        Assert.assertEquals(underTest.getEnvironmentUnderTest(), "http://urlEsterna");
        Assert.assertEquals(underTest.getWebappUnderTest(), "WEBAPP_UNDER_TEST");

        Assert.assertEquals(underTest.getEnabledEnvironments(), "ENV_UNDER_TEST,ENV_NOT_UNDER_TEST");

        String webapp = "WEBAPP_NOT_UNDER_TEST";
        // environment starting with http
        // environment is enabled

        given(inputPh.getProperty("WEBAPP_NOT_UNDER_TEST.DEFAULT.path")).willReturn("OUTPUT_URL");

        String url = underTest.getEnvironmentUrl(webapp);
        Assert.assertEquals(url, "OUTPUT_URL");

        //RESET SYSTEM PROPERTIES
        clearSysProperties();
    }

    @Test (enabled = true)
    public void testSecondConstructorSettings() throws Exception {

        //GIVEN
        System.setProperty("defaultEnvironment", "DEFAULT");
        System.setProperty("environment", "ENV_UNDER_TEST");
        System.setProperty("webappName", "WEBAPP_UNDER_TEST");

        //PowerMockito.whenNew(PropertyHandler.class).withArguments("PROPERTY_FILE_PATH", logUtils).thenReturn(inputPh);

        underTest = new EnvironmentHandler("src/test/resources/environmentSettingsForTest/environment.properties");
        underTest.setEnabledEnvironments("ENV_UNDER_TEST,ENV_NOT_UNDER_TEST");

        Assert.assertEquals(underTest.getDefaultEnvironment(), "DEFAULT");
        Assert.assertEquals(underTest.getEnvironmentUnderTest(), "ENV_UNDER_TEST");
        Assert.assertEquals(underTest.getWebappUnderTest(), "WEBAPP_UNDER_TEST");

        Assert.assertEquals(underTest.getEnabledEnvironments(), "ENV_UNDER_TEST,ENV_NOT_UNDER_TEST");

        String webapp = "WEBAPP_UNDER_TEST"; //in this case is the principal one
        // environment not starting with http
        // environment enabled

        String url = underTest.getEnvironmentUrl(webapp);
        Assert.assertEquals(url, "test_ENV_UNDER_TEST_path");

        //RESET SYSTEM PROPERTIES
        clearSysProperties();
    }

}
