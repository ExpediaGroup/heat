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

import com.hotels.heat.core.dto.HeatTestDetails;
import com.hotels.heat.core.utils.RestAssuredRequestMaker;
import com.jayway.restassured.internal.http.Method;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

import java.util.HashMap;

/**
 * Wiremock Support Handler.
 */
public class WiremockSupportHandler {

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

    public void executeAction(String action) {
        switch (action) {
        case "requests":
            String urlOperation = wmPath + "/__admin/requests";
            Method httpMethod = Method.GET;
            requestMaker.setBasePath(urlOperation);
            requestMaker.setRequestSpecification(new HashMap<>(), new HashMap<>(), urlOperation);
            Response wmRsp = requestMaker.executeHttpRequest(httpMethod, urlOperation, new HashMap<>());
            String rspAsString = wmRsp.asString();



            break;
        case "reset":
            //TODO do something
            break;

        default:
            //TODO action not supported yet
            break;
        }
    }
}
