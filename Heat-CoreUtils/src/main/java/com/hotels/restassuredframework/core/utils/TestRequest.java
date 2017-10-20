package com.hotels.restassuredframework.core.utils;

import java.util.Map;

import com.jayway.restassured.internal.http.Method;


/**
 * Object describing the request to send.
 * @author adebiase
 */
public class TestRequest {

    public static final Method HTTP_METHOD_DEFAULT = Method.GET;
    private String url;
    private Method httpMethod;
    private Map<String, Object> queryParams;
    private Map<String, String> headersParams;
    private Map<String, String> cookieParams;

    /**
     * Constructor of the TestRequest object.
     * It is used to set all useful data for a specific request
     * @param url is the URL related to the specific request against a service
     */
    public TestRequest(String url) {
        this.url = url;
    }

    public void setHttpMethod(Method httpMethod) {
        this.httpMethod = httpMethod;
    }

    public void setQueryParams(Map<String, Object> queryParams) {
        this.queryParams = queryParams;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public Method getHttpMethod() {
        if (httpMethod == null) {
            httpMethod = HTTP_METHOD_DEFAULT;
        }
        return httpMethod;
    }

    public Map<String, Object> getQueryParams() {
        return queryParams;
    }

    public Map<String, String> getHeadersParams() {
        return headersParams;
    }

    public Map<String, String> getCookieParams() {
        return cookieParams;
    }

    public void setHeadersParams(Map<String, String> headersParams) {
        this.headersParams = headersParams;
    }

    public void setCookieParams(Map<String, String> cookieParams) {
        this.cookieParams = cookieParams;
    }


}
