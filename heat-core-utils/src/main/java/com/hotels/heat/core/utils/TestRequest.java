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
package com.hotels.heat.core.utils;

import java.util.Map;

import io.restassured.http.Method;


/**
 * Object describing the request to send.
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
