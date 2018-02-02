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
package com.hotels.heat.core.checks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.hotels.heat.core.specificexception.HeatException;

import com.jayway.restassured.builder.ResponseBuilder;
import com.jayway.restassured.response.Response;


/**
 * Unit Tests for {@link BasicChecks}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BasicChecksTest.class)
public class BasicChecksTest {

    private Map<String, Object> tcParamsWithRspCode;
    private Map<String, Object> tcParamsWithoutRspCode;
    private Response mockedResponse403;
    private Response mockedResponse200;

    @InjectMocks
    private BasicChecks underTest;

    @Mock
    private ITestContext inputTestContext;

    @BeforeMethod
    public void setUp() {

        //inputTestContext = mock(ITestContext.class);
        tcParamsWithRspCode = buildTcParamsWithRspCode();
        tcParamsWithoutRspCode = buildTcParamsWithoutRspCode();

        ResponseBuilder rspBuilder = new ResponseBuilder();
        rspBuilder.setBody("{\"mocked response\" : \"yes\"}");
        rspBuilder.setStatusCode(403);
        mockedResponse403 = rspBuilder.build();

        rspBuilder = new ResponseBuilder();
        rspBuilder.setBody("{\"mocked response\" : \"yes\"}");
        rspBuilder.setStatusCode(200);
        mockedResponse200 = rspBuilder.build();

    }

    private Map buildTcParamsWithRspCode() {

        Map<String, Object> step1 = new HashMap<>();
        step1.put("objectName", "Find_Distance");
        step1.put("stepNumber", "1");
        step1.put("testName", "flow mode test for new heat #1");

        Map<String, String> beforeStep = new HashMap<>();
        beforeStep.put("WM_REQUESTS", "${wiremock[WM_INSTANCE].requests}");
        step1.put("beforeStep", beforeStep);

        Map<String, String> headers = new HashMap<>();
        headers.put("Cache-Control", "no-cache");
        step1.put("headers", headers);

        Map<String, Object> expects = new HashMap<>();
        expects.put("responseCode", "200");

        List<Map<String, Object>> fieldCheck = new ArrayList<>();
        Map<String, Object> check1 = new HashMap<>();
        check1.put("description", "result has to be OK");
        check1.put("actualValue", "${WM_REQUESTS.requestNumber}");
        check1.put("expectedValue", "OK");
        fieldCheck.add(check1);

        expects.put("fieldCheck", fieldCheck);
        step1.put("expects", expects);
        return step1;
    }

    private Map buildTcParamsWithoutRspCode() {

        Map<String, Object> step1 = new HashMap<>();
        step1.put("objectName", "Find_Distance");
        step1.put("stepNumber", "1");
        step1.put("testName", "flow mode test for new heat #1");

        Map<String, String> beforeStep = new HashMap<>();
        beforeStep.put("WM_REQUESTS", "${wiremock[WM_INSTANCE].requests}");
        step1.put("beforeStep", beforeStep);

        Map<String, String> headers = new HashMap<>();
        headers.put("Cache-Control", "no-cache");
        step1.put("headers", headers);

        Map<String, Object> expects = new HashMap<>();

        List<Map<String, Object>> fieldCheck = new ArrayList<>();
        Map<String, Object> check1 = new HashMap<>();
        check1.put("description", "result has to be OK");
        check1.put("actualValue", "${WM_REQUESTS.requestNumber}");
        check1.put("expectedValue", "OK");
        fieldCheck.add(check1);

        expects.put("fieldCheck", fieldCheck);
        step1.put("expects", expects);
        return step1;
    }

    @Test(enabled = true, expectedExceptions = { HeatException.class })
    public void testResponseCodePresentAndWrong() {

        underTest = new BasicChecks(inputTestContext);
        underTest.setResponse(mockedResponse403);
        underTest.checkResponseCode(tcParamsWithRspCode);

    }

    @Test(enabled = true)
    public void testResponseCodePresentAndCorrect() {

        underTest = new BasicChecks(inputTestContext);
        underTest.setResponse(mockedResponse200);
        Assert.assertTrue(underTest.checkResponseCode(tcParamsWithRspCode));

    }

    @Test(enabled = true)
    public void testResponseCodeNotPresentAndWrong() {

        underTest = new BasicChecks(inputTestContext);
        underTest.setResponse(mockedResponse403);
        Assert.assertTrue(underTest.checkResponseCode(tcParamsWithoutRspCode));

    }

    @Test(enabled = true)
    public void testResponseCodeNotPresentAndCorrect() {

        underTest = new BasicChecks(inputTestContext);
        underTest.setResponse(mockedResponse200);
        Assert.assertTrue(underTest.checkResponseCode(tcParamsWithoutRspCode));

    }


}
