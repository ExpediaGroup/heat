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
package com.hotels.heat.module.wiremocksupport;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.hotels.heat.core.handlers.PlaceholderHandler;
import com.hotels.heat.core.handlers.PropertyHandler;
import com.hotels.heat.core.handlers.TestSuiteHandler;
import com.hotels.heat.core.utils.log.LoggingUtils;
import com.jayway.restassured.path.json.config.JsonPathConfig;
import com.jayway.restassured.response.Response;

public class WiremockUtils {
    public static final JsonPathConfig JSON_CONFIG = new JsonPathConfig(JsonPathConfig.NumberReturnType.BIG_DECIMAL);

    private static WiremockUtils instance;
    private LoggingUtils logUtils;
    private PropertyHandler ph;


    private WiremockUtils() {
        this.logUtils = TestSuiteHandler.getInstance().getLogUtils();
    }

    public static WiremockUtils getInstance() {
        if (instance == null) {
            instance = new WiremockUtils();
        }
        return instance;
    }


    /**
     * Loads and cache the environment properties from file system.
     */
    private void loadEnvironmentProperties(String propFile) {
        ph = new PropertyHandler(propFile, new PlaceholderHandler());
        ph.loadFromPropertyFile();
    }


    public String getEnvironmentProperty(String propFile, String propertyName) {
        String basePath = null;
        loadEnvironmentProperties(propFile);

        String environment = TestSuiteHandler.getInstance().getEnvironmentHandler().getEnvironmentUnderTest();
        if (environment == null) {
            environment = System.getProperty("environment");
        }
        basePath = ph.getProperty(propertyName + "." + environment + ".path");
        return basePath;
    }


    /**
     * Extract all wiremock requests by REST subpath.
     * @param wiremockAdminRequests JSON Response of the service, represents the Wiremock admin requests
     * @param urlSubPath Subpath to use in order to individuate the needed requests
     * @return list of the wiremock requests
     */
    public static List<Map<String, Object>> getWiremockRequestsByRequestSubpath(Response wiremockAdminRequests, String urlSubPath) {
        String jsonPath = "requests.request.findAll{request -> request.url == '" + urlSubPath + "'}";
        List<Map<String, Object>> wmRequests = wiremockAdminRequests.jsonPath(JSON_CONFIG).get(jsonPath);
        return wmRequests;
    }

    /**
     * Extract the first wiremock request by REST subpath.
     * @param wiremockAdminRequests JSON Response of the service, represents the Wiremock admin requests
     * @param urlSubPath Subpath to use in order to individuate the needed request
     * @return first of the found wiremock requests
     */
    public static Map<String, Object> getWiremockFirstRequestBySubpath(Response wiremockAdminRequests, String urlSubPath) {
        List<Map<String, Object>> wmRequests = getWiremockRequestsByRequestSubpath(wiremockAdminRequests, urlSubPath);
        Map<String, Object> wmRequest = wmRequests.get(0);
        return wmRequest;
    }

    /**
     * Extract all wiremock  request-response blocks by REST request subpath.
     * @param wiremockAdminRequests JSON Response of the service, represents the Wiremock admin requests
     * @param urlSubPath Subpath to use in order to individuate the needed block request-response
     * @return list of the wiremock resppnses
     */
    public static List<Map<String, Object>> getWiremockReqRespByRequestSubpath(Response wiremockAdminRequests, String urlSubPath) {
        String jsonPath = "requests.findAll{requests -> requests.request.url == '" + urlSubPath + "'}";
        List<Map<String, Object>> wmResponses = wiremockAdminRequests.jsonPath(JSON_CONFIG).get(jsonPath);
        return wmResponses;
    }

    /**
     * Extract the first wiremock request-response block by REST request subpath.
     * @param wiremockAdminRequests JSON Response of the service, represents the Wiremock admin requests
     * @param urlSubPath Subpath to use in order to individuate the needed request
     * @return first of the found wiremock responses
     */
    public static Map<String, Object> getWiremockFirstReqRespByRequestSubpath(Response wiremockAdminRequests, String urlSubPath) {
        List<Map<String, Object>> wmResponses = getWiremockReqRespByRequestSubpath(wiremockAdminRequests, urlSubPath);
        Map<String, Object> wmResponse = wmResponses.get(0);
        return wmResponse;
    }



    /**
     * Extract the body of wiremock response.
     * @param wiremockAdminRequests JSON Response of the service, represents the Wiremock admin requests
     * @param urlSubPath Subpath to use in order to individuate the needed request-response
     * @return response body as String
     */
    public static String getWiremockBodyResponseByRequestSubpath(Response wiremockAdminRequests, String urlSubPath) {
        Map<String, Object> wmResponse = getWiremockFirstReqRespByRequestSubpath(wiremockAdminRequests, urlSubPath);
        String wmBodyResponse = ((Map<String, String>) wmResponse.get("response")).get("body");
        return wmBodyResponse;
    }

    /**
     * Extract the body of wiremock request.
     * @param wiremockAdminRequests JSON Response of the service, represents the Wiremock admin requests
     * @param urlSubPath Subpath to use in order to individuate the needed request-response
     * @return request body as String
     */
    public static String getWiremockBodyRequestBySubpath(Response wiremockAdminRequests, String urlSubPath) {
        Map<String, Object> wmRequest = getWiremockFirstRequestBySubpath(wiremockAdminRequests, urlSubPath);
        String wmBodyResponse = (String) wmRequest.get("body");
        return wmBodyResponse;
    }

}
