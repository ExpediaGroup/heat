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
package com.hotels.heat.core.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.restassured.builder.ResponseBuilder;
import io.restassured.http.Cookie;
import io.restassured.http.Cookies;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;



/**
 * Unit Tests for {@link PlaceholderHandler}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(PlaceholderHandler.class)
public class PlaceholderHandlerTest {


    @InjectMocks
    private PlaceholderHandler underTest;

    @Test (enabled = true)
    public void testNoPlaceholderString() {
        underTest = new PlaceholderHandler();
        String processedStr = (String) underTest.placeholderProcessString("pippo");
        Assert.assertEquals(processedStr, "pippo");
    }

    @Test (enabled = true)
    public void testGetStepString() {
        underTest = new PlaceholderHandler();
        Map<Integer, Map<String, String>> flowVariables = new HashMap();
        Map<String, String> step1 = new HashMap();
        step1.put("outputParamName", "outputParamValue");
        flowVariables.put(1, step1);
        underTest.setFlowVariables(flowVariables);
        String stringToProcess = "${getStep(1).getOutputParam(outputParamName)}";
        String processedStr = (String) underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(processedStr, "outputParamValue");

        stringToProcess = "PIPPO_${getStep(1).getOutputParam(outputParamName)}";
        processedStr = (String) underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(processedStr, "PIPPO_outputParamValue");

        stringToProcess = "PIPPO_${getStep(1).getOutputParam(outputParamName)}_PLUTO";
        processedStr = (String) underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(processedStr, "PIPPO_outputParamValue_PLUTO");

        stringToProcess = "PIPPO_${getStep(1).getOutputParam(outputParamName)}_PLUTO_${getStep(1).getOutputParam(outputParamName)}";
        processedStr = (String) underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(processedStr, "PIPPO_outputParamValue_PLUTO_outputParamValue");

        stringToProcess = "PIPPO_${getStep(1).getOutputParam(outputParamName)}_PLUTO_${getStep(1).getOutputParam(outputParamName)}_PAPERINO";
        processedStr = (String) underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(processedStr, "PIPPO_outputParamValue_PLUTO_outputParamValue_PAPERINO");

    }

    @Test (enabled = true)
    public void testGetPreloadSimpleString() {
        underTest = new PlaceholderHandler();
        Map<String, Object> beforeSuiteVariables = new HashMap();
        beforeSuiteVariables.put("preloadedVarName", "preloadedVarValue");
        TestSuiteHandler.getInstance().getTestCaseUtils().setBeforeSuiteVariables(beforeSuiteVariables);
        String stringToProcess = "${preload[preloadedVarName]}";
        String processedStr = (String) underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(processedStr, "preloadedVarValue");
    }

    @Test (enabled = true)
    public void testGetPreloadComplexDefaultString() {
        underTest = new PlaceholderHandler();
        Map<String, Object> beforeSuiteVariables = new HashMap();
        Map<String, String> complexStructure = new HashMap();
        complexStructure.put("DEFAULT", "default_value");
        complexStructure.put("param1", "param1_value");
        complexStructure.put("param2", "param2_value");
        beforeSuiteVariables.put("preloadedVarName", complexStructure);
        TestSuiteHandler.getInstance().getTestCaseUtils().setBeforeSuiteVariables(beforeSuiteVariables);


        String stringToProcess = "${preload[preloadedVarName]}";
        String processedStr = (String) underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(processedStr, "default_value");

        stringToProcess = "${preload[preloadedVarName].get(param2)}";
        processedStr = (String) underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(processedStr, "param2_value");

        stringToProcess = "PIPPO_${preload[preloadedVarName].get(param2)}";
        processedStr = (String) underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(processedStr, "PIPPO_param2_value");

        stringToProcess = "PIPPO_${preload[preloadedVarName].get(param2)}_PLUTO";
        processedStr = (String) underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(processedStr, "PIPPO_param2_value_PLUTO");

        stringToProcess = "PIPPO_${preload[preloadedVarName].get(param2)}_PLUTO_${preload[preloadedVarName].get(param1)}_CICCIO";
        processedStr = (String) underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(processedStr, "PIPPO_param2_value_PLUTO_param1_value_CICCIO");

        stringToProcess = "PIPPO_${preload[preloadedVarName].get(param2)}_PLUTO_${preload[preloadedVarName].get(param1)}_CICCIO_${preload[preloadedVarName]}";
        processedStr = (String) underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(processedStr, "PIPPO_param2_value_PLUTO_param1_value_CICCIO_default_value");
    }


    @Test(enabled = true)
    public void testGetPreloadNotExistentString() {
        underTest = new PlaceholderHandler();
        Map<String, Object> beforeSuiteVariables = new HashMap();
        beforeSuiteVariables.put("preloadedVarName", "preloadedVarValue");
        TestSuiteHandler.getInstance().getTestCaseUtils().setBeforeSuiteVariables(beforeSuiteVariables);
        String stringToProcess = "${preload[NotpreloadedVarName]}";
        Object o = underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(stringToProcess, o, "When a preloaded variable is not found, the string should be unmodified");
    }

    @Test (enabled = true)
    public void testGetNotExistentStepString() {
        underTest = new PlaceholderHandler();
        Map<Integer, Map<String, String>> flowVariables = new HashMap();
        Map<String, String> step1 = new HashMap();
        step1.put("outputParamName", "outputParamValue");
        flowVariables.put(1, step1);
        underTest.setFlowVariables(flowVariables);
        String stringToProcess = "${getStep(2).getOutputParam(outputParamName)}";
        String processedStr = (String) underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(processedStr, "${getStep(2).getOutputParam(outputParamName)}");
    }

    @Test (enabled = true)
    public void testGetNotExistentOutputString() {
        underTest = new PlaceholderHandler();
        Map<Integer, Map<String, String>> flowVariables = new HashMap();
        Map<String, String> step1 = new HashMap();
        step1.put("outputParamName", "outputParamValue");
        flowVariables.put(1, step1);
        underTest.setFlowVariables(flowVariables);
        String stringToProcess = "${getStep(1).getOutputParam(pippo)}";
        String processedStr = (String) underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(processedStr, "${getStep(1).getOutputParam(pippo)}");
    }

    @Test (enabled = true)
    public void testPathPlaceholderString() {
        underTest = new PlaceholderHandler();
        underTest.setResponse(buildResponse());
        String stringToProcess = "${path[field_path]}";
        String processedStr = (String) underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(processedStr, "field_value");

        stringToProcess = "${path[array1.size()]}";
        processedStr = (String) underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(processedStr, "1");

        stringToProcess = "${path[array1[0].array_field1]}";
        processedStr = (String) underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(processedStr, "array_field_value1");
    }

    @Test (enabled = true)
    public void testPathPlaceholderTotalRspString() {
        underTest = new PlaceholderHandler();
        underTest.setResponse(buildResponse());
        String stringToProcess = "${path[.]}";
        String processedStr = (String) underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(processedStr, "{\"field_path\":\"field_value\",\"array1\":[{\"array_field1\":\"array_field_value1\"}]}");
    }

//    @Test(enabled = true, expectedExceptions = { HeatException.class },
//            expectedExceptionsMessageRegExp = ".* It is not possible to retrieve the jsonPath (.*) from the current response. --> response: .*")
    @Test
    public void testPathPlaceholderNotExistentString() throws Exception {
        underTest = new PlaceholderHandler();
        underTest.setResponse(buildResponse());
        String stringToProcess = "${path[pippo]}";
        underTest.placeholderProcessString(stringToProcess);
    }

    @Test (enabled = true)
    public void testProcessHeaderPlaceholder() {
        underTest = new PlaceholderHandler();
        underTest.setResponse(buildResponse());

        String stringToProcess = "PIPPO_${header[test_header]}";
        String output = (String) underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(output, "PIPPO_test_value");

        stringToProcess = "PIPPO_${header[test_header]}_PLUTO";
        output = (String) underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(output, "PIPPO_test_value_PLUTO");

        stringToProcess = "PIPPO_${header[test_header]}_PLUTO_${header[test_header]}";
        output = (String) underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(output, "PIPPO_test_value_PLUTO_test_value");

        stringToProcess = "PIPPO_${header[test_header]}_PLUTO_${header[test_header2]}";
        output = (String) underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(output, "PIPPO_test_value_PLUTO_test_value2");

        stringToProcess = "PIPPO_${header[test_header]}_PLUTO_${header[test_header2]}_PAPERINO";
        output = (String) underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(output, "PIPPO_test_value_PLUTO_test_value2_PAPERINO");

    }

    @Test (enabled = true)
    public void testProcessCookiePlaceholder() {
        underTest = new PlaceholderHandler();
        underTest.setResponse(buildResponse());

        String stringToProcess = "PIPPO_${cookie[test_cookie]}";
        String output = (String) underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(output, "PIPPO_test_value");

        stringToProcess = "PIPPO_${cookie[test_cookie]}_PLUTO";
        output = (String) underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(output, "PIPPO_test_value_PLUTO");

        stringToProcess = "PIPPO_${cookie[test_cookie]}_PLUTO_${cookie[test_cookie]}";
        output = (String) underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(output, "PIPPO_test_value_PLUTO_test_value");

        stringToProcess = "PIPPO_${cookie[test_cookie]}_PLUTO_${cookie[test_cookie2]}";
        output = (String) underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(output, "PIPPO_test_value_PLUTO_test_value2");

        stringToProcess = "PIPPO_${cookie[test_cookie]}_PLUTO_${cookie[test_cookie2]}_PAPERINO";
        output = (String) underTest.placeholderProcessString(stringToProcess);
        Assert.assertEquals(output, "PIPPO_test_value_PLUTO_test_value2_PAPERINO");

    }

    private Response buildResponse() {
        ResponseBuilder rspBuilder = new ResponseBuilder();
        rspBuilder.setStatusCode(200);
        rspBuilder.setBody("{\"field_path\":\"field_value\",\"array1\":[{\"array_field1\":\"array_field_value1\"}]}");

        List<Header> headerList = new ArrayList();
        Header header1 = new Header("test_header", "test_value");
        Header header2 = new Header("test_header2", "test_value2");
        headerList.add(header1);
        headerList.add(header2);
        Headers headers = new Headers(headerList);
        rspBuilder.setHeaders(headers);

        List<Cookie> cookieList = new ArrayList();
        Cookie cookie1 = new Cookie.Builder("test_cookie", "test_value").build();
        Cookie cookie2 = new Cookie.Builder("test_cookie2", "test_value2").build();
        cookieList.add(cookie1);
        cookieList.add(cookie2);
        Cookies cookies = new Cookies(cookieList);
        rspBuilder.setCookies(cookies);

        return rspBuilder.build();
    }

    @Test (enabled = true)
    public void testGeneralMapAsInput() {
        underTest = new PlaceholderHandler();

        Map<String, Object> beforeSuiteVariables = new HashMap();
        beforeSuiteVariables.put("preloadedVarName", "preloadedVarValue");
        TestSuiteHandler.getInstance().getTestCaseUtils().setBeforeSuiteVariables(beforeSuiteVariables);

        Map<String, Object> mapToProcess = new HashMap();
        mapToProcess.put("field1", "field1_value");
        Map<String, String> secondLevelMap = new HashMap();
        secondLevelMap.put("field2_1", "${preload[preloadedVarName]}");
        mapToProcess.put("field2", secondLevelMap);

        Map<String, Object> expectedMap = new HashMap();
        expectedMap.put("field1", "field1_value");
        Map<String, String> secondLevelExpectedMap = new HashMap();
        secondLevelExpectedMap.put("field2_1", "preloadedVarValue");
        expectedMap.put("field2", secondLevelExpectedMap);

        Map processedMap = underTest.placeholderProcessMap(mapToProcess);
        Assert.assertEquals(processedMap, expectedMap);
    }

    @Test (enabled = true)
    public void testProcessPlaceholders() {
        underTest = new PlaceholderHandler();
        Map<String, Object> beforeSuiteVariables = new HashMap();
        beforeSuiteVariables.put("preloadedVarName1", "preloadedVarValue1");
        beforeSuiteVariables.put("preloadedVarName2", "preloadedVarValue2");
        beforeSuiteVariables.put("preloadedVarName3", "preloadedVarValue3");
        TestSuiteHandler.getInstance().getTestCaseUtils().setBeforeSuiteVariables(beforeSuiteVariables);

        Map<String, Object> mapToProcess = new HashMap();
        mapToProcess.put("field1", "{\"custname\":\"pippo\",\"custemail\":\"pippo@test.test\",\"delivery\":\"${preload[preloadedVarName1]}\""
                + ",\"size\":\"large\",\"topping\": [\"bacon\",\"cheese\"],\"comment\":\"${preload[preloadedVarName2]}\"}");
        Map processedMap = underTest.placeholderProcessMap(mapToProcess);

        Map<String, Object> expectedMap = new HashMap();
        expectedMap.put("field1", "{\"custname\":\"pippo\",\"custemail\":\"pippo@test.test\",\"delivery\":\"preloadedVarValue1\","
                + "\"size\":\"large\",\"topping\": [\"bacon\",\"cheese\"],\"comment\":\"preloadedVarValue2\"}");

        Assert.assertEquals(processedMap, expectedMap);

        mapToProcess = new HashMap();
        mapToProcess.put("field1", "${preload[preloadedVarName3]}:\"pippo\",\"custemail\":\"pippo@test.test\","
                + "\"delivery\":\"${preload[preloadedVarName1]}\",\"size\":\"large\",\"topping\": [\"bacon\",\"cheese\"],\"comment\":\"${preload[preloadedVarName2]}\"}");
        processedMap = underTest.placeholderProcessMap(mapToProcess);

        expectedMap = new HashMap();
        expectedMap.put("field1", "preloadedVarValue3:\"pippo\",\"custemail\":\"pippo@test.test\","
                + "\"delivery\":\"preloadedVarValue1\",\"size\":\"large\",\"topping\": [\"bacon\",\"cheese\"],\"comment\":\"preloadedVarValue2\"}");

        Assert.assertEquals(processedMap, expectedMap);

        mapToProcess = new HashMap();
        mapToProcess.put("field1", "${preload[preloadedVarName1]}:\"pippo\",\"custemail\":\"pippo@test.test\","
                + "\"delivery\":\"${preload[preloadedVarName1]}\",\"size\":\"large\",\"topping\": [\"bacon\",\"cheese\"],\"comment\":\"${preload[preloadedVarName1]}\"}");
        processedMap = underTest.placeholderProcessMap(mapToProcess);

        expectedMap = new HashMap();
        expectedMap.put("field1", "preloadedVarValue1:\"pippo\",\"custemail\":\"pippo@test.test\","
                + "\"delivery\":\"preloadedVarValue1\",\"size\":\"large\",\"topping\": [\"bacon\",\"cheese\"],\"comment\":\"preloadedVarValue1\"}");

        Assert.assertEquals(processedMap, expectedMap);

        mapToProcess = new HashMap();
        mapToProcess.put("field1", "${preload[preloadedVarName1]}:\"pippo\",\"custemail\":\"pippo@test.test\","
                + "\"delivery\":\"${preload[preloadedVarName1]}\",\"size\":\"large\",\"topping\": [\"bacon\",\"cheese\"],"
                + "\"comment\":\"${preload[preloadedVarName1]}\"}${preload[preloadedVarName1]}");
        processedMap = underTest.placeholderProcessMap(mapToProcess);

        expectedMap = new HashMap();
        expectedMap.put("field1", "preloadedVarValue1:\"pippo\",\"custemail\":\"pippo@test.test\",\"delivery\":\"preloadedVarValue1\","
                + "\"size\":\"large\",\"topping\": [\"bacon\",\"cheese\"],\"comment\":\"preloadedVarValue1\"}preloadedVarValue1");

        Assert.assertEquals(processedMap, expectedMap);

        mapToProcess = new HashMap();
        mapToProcess.put("field1", "preloadedVarValue1:\"pippo\",\"custemail\":\"pippo@test.test\",\"delivery\":\"${preload[preloadedVarName1]}\","
                + "\"size\":\"large\",\"topping\": [\"bacon\",\"cheese\"],\"comment\":\"preloadedVarValue1\"}preloadedVarValue1");
        processedMap = underTest.placeholderProcessMap(mapToProcess);

        expectedMap = new HashMap();
        expectedMap.put("field1", "preloadedVarValue1:\"pippo\",\"custemail\":\"pippo@test.test\",\"delivery\":\"preloadedVarValue1\",\"size\":\"large\","
                + "\"topping\": [\"bacon\",\"cheese\"],\"comment\":\"preloadedVarValue1\"}preloadedVarValue1");

        Assert.assertEquals(processedMap, expectedMap);

        mapToProcess = new HashMap();
        mapToProcess.put("field1", "preloadedVarValue1:\"pippo\",\"custemail\":\"pippo@test.test\",\"delivery\":\"preloadedVarValue1\","
                + "\"size\":\"large\",\"topping\": [\"bacon\",\"cheese\"],\"comment\":\"preloadedVarValue1\"}preloadedVarValue1");
        processedMap = underTest.placeholderProcessMap(mapToProcess);

        expectedMap = new HashMap();
        expectedMap.put("field1", "preloadedVarValue1:\"pippo\",\"custemail\":\"pippo@test.test\",\"delivery\":\"preloadedVarValue1\","
                + "\"size\":\"large\",\"topping\": [\"bacon\",\"cheese\"],\"comment\":\"preloadedVarValue1\"}preloadedVarValue1");

        Assert.assertEquals(processedMap, expectedMap);

        mapToProcess = new HashMap();
        mapToProcess.put("field1", "preloadedVarValue1:\"pippo\",\"custemail\":\"pippo@test.test\",\"delivery\":\"{preload[preloadedVarName1]}\","
                + "\"size\":\"large\",\"topping\": [\"bacon\",\"cheese\"],\"comment\":\"preloadedVarValue1\"}preloadedVarValue1");
        processedMap = underTest.placeholderProcessMap(mapToProcess);

        expectedMap = new HashMap();
        expectedMap.put("field1", "preloadedVarValue1:\"pippo\",\"custemail\":\"pippo@test.test\",\"delivery\":\"{preload[preloadedVarName1]}\","
                + "\"size\":\"large\",\"topping\": [\"bacon\",\"cheese\"],\"comment\":\"preloadedVarValue1\"}preloadedVarValue1");

        Assert.assertEquals(processedMap, expectedMap);


    }


}
