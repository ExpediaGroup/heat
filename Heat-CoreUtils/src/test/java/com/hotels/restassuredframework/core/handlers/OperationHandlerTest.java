package com.hotels.restassuredframework.core.handlers;

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

import com.hotels.restassuredframework.core.specificexception.HeatException;
import com.jayway.restassured.builder.ResponseBuilder;
import com.jayway.restassured.response.Cookie;
import com.jayway.restassured.response.Cookies;
import com.jayway.restassured.response.Header;
import com.jayway.restassured.response.Headers;
import com.jayway.restassured.response.Response;




/**
 * Unit Tests for {@link OperationHandler}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(OperationHandler.class)
public class OperationHandlerTest {

    @InjectMocks
    private OperationHandler underTest;

    @Test (enabled = true)
    public void testCompareEqualsFields() {
        underTest = new OperationHandler(getEqualsFieldsToCheck(), buildResponses());

        boolean execution = underTest.execute();
        Assert.assertTrue(execution);
    }


    @Test(enabled = true, expectedExceptions = HeatException.class)
    public void testCompareDifferentFields() {
        //In this case the execute method does not return a boolean with value false but trows an exception
        underTest = new OperationHandler(getDifferentFieldsToCheck(), buildResponses());

        underTest.execute();

    }

    private Map getEqualsFieldsToCheck() {
        Map fieldsToCheck = new HashMap<>();
        Map<String, String> compareActualValue = new HashMap<>();
        compareActualValue.put("referringObjectName", "rsp1");
        compareActualValue.put("actualValue", "${path[field_path1]}");
        Map<String, String> compareExpectedValue = new HashMap<>();
        compareExpectedValue.put("referringObjectName", "rsp2");
        compareExpectedValue.put("actualValue", "${path[field_path2]}");

        fieldsToCheck.put("description", "description_value");
        fieldsToCheck.put("operation", "=");
        fieldsToCheck.put("actualValue", compareActualValue);
        fieldsToCheck.put("expectedValue", compareExpectedValue);
        return fieldsToCheck;
    }

    private Map getDifferentFieldsToCheck() {
        Map fieldsToCheck = new HashMap<>();
        Map<String, String> compareActualValue = new HashMap<>();
        compareActualValue.put("referringObjectName", "rsp1");
        compareActualValue.put("actualValue", "${path[field_path1]}");
        Map<String, String> compareExpectedValue = new HashMap<>();
        compareExpectedValue.put("referringObjectName", "rsp2");
        compareExpectedValue.put("actualValue", "${path[.]}");

        fieldsToCheck.put("description", "description_value");
        fieldsToCheck.put("operation", "=");
        fieldsToCheck.put("actualValue", compareActualValue);
        fieldsToCheck.put("expectedValue", compareExpectedValue);
        return fieldsToCheck;
    }

    private Map buildResponses() {
        ResponseBuilder rspBuilder = new ResponseBuilder();
        rspBuilder.setStatusCode(200);
        rspBuilder.setBody("{\"field_path1\":\"field_value\",\"array1\":[{\"array_field1\":\"array_field_value\"}]}");

        List<Header> headerList = new ArrayList<>();
        Header header1 = new Header("test_header", "test_value");
        Header header2 = new Header("test_header2", "test_value2");
        headerList.add(header1);
        headerList.add(header2);
        Headers headers = new Headers(headerList);
        rspBuilder.setHeaders(headers);

        List<Cookie> cookieList = new ArrayList<>();
        Cookie cookie1 = new Cookie.Builder("test_cookie", "test_value").build();
        Cookie cookie2 = new Cookie.Builder("test_cookie2", "test_value2").build();
        cookieList.add(cookie1);
        cookieList.add(cookie2);
        Cookies cookies = new Cookies(cookieList);
        rspBuilder.setCookies(cookies);

        Response rsp1 = rspBuilder.build();


        rspBuilder = new ResponseBuilder();
        rspBuilder.setStatusCode(200);
        rspBuilder.setBody("{\"field_path2\":\"field_value\",\"array2\":[{\"array_field2\":\"array_field_value\"}]}");

        rspBuilder.setHeaders(headers);
        rspBuilder.setCookies(cookies);
        Response rsp2 = rspBuilder.build();

        Map<String, Response> responses = new HashMap();
        responses.put("rsp1", rsp1);
        responses.put("rsp2", rsp2);

        return responses;
    }

}
