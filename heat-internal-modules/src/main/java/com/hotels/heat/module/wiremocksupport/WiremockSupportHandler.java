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

import java.util.HashMap;
import java.util.Map;

import com.hotels.heat.core.dto.HeatTestDetails;
import com.hotels.heat.core.utils.RestAssuredRequestMaker;
import com.jayway.restassured.internal.http.Method;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

/**
 * Wiremock Support Handler.
 */
public class WiremockSupportHandler {

    public static final String DEFAULT_VALUE = "";
    private String testDescription;
    private String environment;
    private String wmPath;
    private RequestSpecification reqSpec;
    private RestAssuredRequestMaker requestMaker;


    public WiremockSupportHandler(String wmInstanceName, String wmBasePath, HeatTestDetails heatTestDetails) {
        this.testDescription = heatTestDetails.getTestDescription();
        this.environment = heatTestDetails.getEnvironment();
        requestMaker = new RestAssuredRequestMaker();
        wmPath = wmBasePath;


    }

    public Map<String, String> executeAction(WiremockAction action) {
        String urlOperation;
        Method httpMethod;
        Map<String, String> rsp = new HashMap<>();
        Response httpResp = null;

        switch (action) {
        case REQUESTS:
            urlOperation = wmPath + WiremockAction.REQUESTS.getActionSubpath();
            httpMethod = WiremockAction.REQUESTS.getActionHttpMethod();
            httpResp = this.makeHttpCall(urlOperation, httpMethod);
            int total = applyJsonPath(httpResp.asString(), "meta.total");

            rsp.put("response", httpResp.asString());
            rsp.put("status", String.valueOf(httpResp.statusCode()));
            rsp.put("total", String.valueOf(total));
            break;
        case RESET:
            urlOperation = wmPath + WiremockAction.RESET.getActionSubpath();
            httpMethod = WiremockAction.RESET.getActionHttpMethod();
            httpResp = this.makeHttpCall(urlOperation, httpMethod);

            rsp.put("response", httpResp.asString());
            rsp.put("status", String.valueOf(httpResp.statusCode()));
            break;
        case RESETSCENARIOS:
            urlOperation = wmPath + WiremockAction.RESETSCENARIOS.getActionSubpath();
            httpMethod = WiremockAction.RESETSCENARIOS.getActionHttpMethod();
            httpResp = this.makeHttpCall(urlOperation, httpMethod);

            rsp.put("response", httpResp.asString());
            rsp.put("status", String.valueOf(httpResp.statusCode()));
            break;
        case UNKNOWN:
        default:
            rsp.put("response" , DEFAULT_VALUE);
            break;
        }

        rsp.put(WiremockSupportModule.DEFAULT_PRELOADED_VALUE, rsp.get("response"));
        return rsp;
    }

    private  <T> T applyJsonPath(String httpResp, String jsonPath) {
        JsonPath jsonPathResp = new JsonPath(httpResp);
        T resp = jsonPathResp.get(jsonPath);
        return resp;
    }

    private Response makeHttpCall(String urlOperation, Method httpMethod) {
        requestMaker.setBasePath(urlOperation);
        requestMaker.setRequestSpecification(new HashMap<>(), new HashMap<>(), urlOperation);
        Response wmRsp = requestMaker.executeHttpRequest(httpMethod, urlOperation, new HashMap<>());

        return wmRsp;
    }
}
