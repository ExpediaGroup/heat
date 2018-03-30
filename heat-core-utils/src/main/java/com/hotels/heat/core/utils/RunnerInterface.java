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

import java.util.Iterator;
import java.util.Map;

import org.testng.ITestContext;

import io.restassured.response.Response;


/**
 * Interface to handle all the runners present in the framework.
 */
public interface RunnerInterface {

    void beforeTestSuite(String propFilePath,
                                        String inputWebappName,
                                        ITestContext context);

    void beforeTestCase(String inputJsonParamPath,
            String enabledEnvironments,
            ITestContext context);

    Iterator<Object[]> providerJson();

    Map resolvePlaceholdersInTcParams(Map<String, Object> testCaseParams);

    void specificChecks(Map testCaseParams, Map<String, Response> rspRetrieved, String environment);

}
