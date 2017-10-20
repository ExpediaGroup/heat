/**
 * Copyright (C) 2015-2017 Expedia Inc.
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
package com.hotels.restassuredframework.core.handlers;

import java.util.ArrayList;
import java.util.Map;

import com.hotels.restassuredframework.core.utils.log.LoggingUtils;


/**
 * This object manipulates the test cases data in order to solve all possible placeholders.
 * This class aim is not to manage placeholders, but only to delegate PlaceholderHandler if necessary
 */
public class TestCaseMapHandler {

    private final Map<String, Object> testCaseMap;
    private final PlaceholderHandler placeholderHandler;
    private final LoggingUtils logUtils;

/**
 * TestCaseMapHandler constructor.
 * @param testCaseMapInput is a map containing all data coming from json input file.
 * @param placeholderhandler is the PlaceholderHandler to use. It could contain some data coming from the parsing of
 * the first part of the json input file, so it is useful to pass it in the constructor
 */
    public TestCaseMapHandler(Map testCaseMapInput, PlaceholderHandler placeholderhandler) {
        this.placeholderHandler = placeholderhandler;
        this.logUtils = TestSuiteHandler.getInstance().getLogUtils();
        this.testCaseMap = testCaseMapInput;
    }

    /**
     * This method is called from the runner, as first thing in test running and is useful to manipulate data
     * coming from the json input file.
     * @return the same structure of the json input file, but with placeholders resolved
     */
    public Map<String, Object> retriveProcessedMap() {
        logUtils.trace("input: '{}'", testCaseMap.toString());
        Map<String, Object> output = (Map<String, Object>) process(testCaseMap);
        logUtils.trace("output: '{}'", output.toString());
        return output;
    }


    private Object processString(Object input) {
        Object output = input;
        logUtils.trace("OLD input:'{}'", input.toString());
        if (input.toString().contains(PlaceholderHandler.PLACEHOLDER_SYMBOL_BEGIN)) {
            output = placeholderHandler.placeholderProcessString((String) input);
        }
        logUtils.trace("NEW input:'{}'", output.toString());
        return output;
    }

    private Object processMap(Object input) {
        Object output = input;
        ((Map<String, Object>) input).forEach((key, valueObj) -> {
            logUtils.trace("key:'{}' / OLD value: '{}'", key, valueObj.toString());
            if (valueObj.getClass().equals(String.class)) {
                valueObj = processString(valueObj);
            } else {
                valueObj = process(valueObj);
            }
            ((Map<String, Object>) output).put(key, valueObj);
            logUtils.trace("key:'{}' / NEW value: '{}'", key, valueObj.toString());
        });

        return output;
    }

    private Object processArrayList(Object input) {
        Object output = input;
        ((ArrayList<Object>) input).forEach((valueObj) -> {
            logUtils.trace("OLD value: '{}'", valueObj.toString());
            int index = ((ArrayList<Object>) input).indexOf(valueObj);
            if (valueObj.getClass().equals(String.class)) {
                valueObj = processString(valueObj);
            } else {
                valueObj = process(valueObj);
            }
            ((ArrayList<Object>) output).set(index, valueObj);
            logUtils.trace("NEW value: '{}'", valueObj.toString());
        });


        return output;
    }

    private Object process(Object input) {
        Object outputObj = input;
        String inputObjClass = input.getClass().getSimpleName();
        logUtils.trace("Class of object to process: {}", inputObjClass);
        logUtils.trace("BEFORE '{}'", input.toString());
        switch (inputObjClass) {
        case "String":
            outputObj = processString(input.toString());
            break;
        case "HashMap":
            outputObj = processMap((Map) input);
            break;
        case "ArrayList":
            outputObj = processArrayList(input);
            break;
        default:
            logUtils.debug("the object '{}' is not yet supported", inputObjClass);
            break;
        }
        logUtils.trace("AFTER '{}'", outputObj.toString());
        return outputObj;
    }


}
