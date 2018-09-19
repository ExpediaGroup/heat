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
package com.hotels.heat.dummypackage;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotels.heat.core.heatspecificchecks.SpecificChecks;

import io.restassured.response.Response;


/**
 * Common specific checks.
 */

public class ExampleSpecificChecks extends SpecificChecks {
    private final Logger logger = LoggerFactory.getLogger(ExampleSpecificChecks.class);

/**
 * Method in which we can specify all test suites involved in this specific check.
 * @return the set of the handled suites
 */
    @Override
    public Set<String> handledSuites() {
        Set<String> suites = new HashSet();
        suites.add("FIRST_SUITE");
        return suites;
    }

/**
 * Method of the real specific check.
 * @param testCaseParamenter parameters passed from the json input file. It contains all the single "test case" section, with request and expectations
 * @param responsesRetrieved it is the response retrieved from the service under test
 * @param testRef string with test details (test suite and test case)
 * @param environment string representing the name of the environment under test
 */
    @Override
    public void process(Map testCaseParamenter, Map<String, Response> responsesRetrieved, String testRef, String environment) {
        logger.debug("{} ExampleSpecificChecks::process", testRef);

        // here the code!
    }


}
