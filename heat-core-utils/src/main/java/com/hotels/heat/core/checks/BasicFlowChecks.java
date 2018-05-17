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

import java.util.HashMap;
import java.util.Map;

import org.testng.ITestContext;

import com.hotels.heat.core.handlers.PlaceholderHandler;
import com.hotels.heat.core.handlers.TestSuiteHandler;
import com.hotels.heat.core.specificexception.HeatException;
import com.hotels.heat.core.utils.TestCaseUtils;
import com.hotels.heat.core.utils.log.LoggingUtils;
import com.jayway.restassured.response.Response;


/**
 * Basic utility class in flow tests.
 */
public class BasicFlowChecks extends BasicMultipleChecks {

    public static final String OUTPUT_PARAMS_JSON_ELEMENT = "outputParams";
    private static final String FIELD_DELAY_BEFORE = "delayBefore";
    private static final String FIELD_DELAY_AFTER = "delayAfter";

    private ITestContext context;

    private final Map<Integer, Map<String, String>> retrievedParameters = new HashMap<>();

    public BasicFlowChecks(LoggingUtils logUtils, TestCaseUtils tcUtils, ITestContext context) {
        super(context);
    }


    /**
     * This method retrieves info from the json input objects.
     * @param testCaseParamsInput the input parameters to define a test case
     * @return Map webapp name, response from the specified webapp
     */
    @Override
    public Map<String, Response> retrieveInfo(Map<String, Object> testCaseParamsInput) {
        Map<String, Response> respRetrieved = new HashMap<>();
        try {
            compactInfoToCompare(testCaseParamsInput);
            if (getIsRunnable()) {
                getLogUtils().trace("number of blocks to load: {}", getHttpMethods().size());
                Map<String, Object> singleObjects = getInputJsonObjs();
                getSteps().forEach((blockID, singleBlockName) -> {
                    getLogUtils().debug("loading the block id {}: '{}'", blockID, singleBlockName);
                    Map singleBlockObj = (Map) singleObjects.get(singleBlockName);

                    //this map has to be taken from the "beforeStep" section
                    Map<String, Object> stepPreloadedVariables = loadMapFromTestStep("beforeStep", singleBlockObj);
                    TestSuiteHandler.getInstance().getTestCaseUtils().setBeforeStepVariables(stepPreloadedVariables); //add before Step variables

                    if (!retrievedParameters.isEmpty()) {
                        // this retrieves parameters exposed with 'output' from previous steps, if present.
                        singleBlockObj = processJsonBlockWithPreviousStepsParameters(singleBlockObj);
                    }

                    addDelayOnStep(singleBlockObj, FIELD_DELAY_BEFORE);

                    Response rspStep = retrieveSingleBlockRsp(singleBlockName, singleBlockObj);
                    respRetrieved.put(singleBlockName, rspStep);
                    Map<String, Object> inputJsonBlock = (Map<String, Object>) getInputJsonObjs().get(singleBlockName);
                    getLogUtils().debug("starting common validation on the block n. {}: '{}'", blockID, singleBlockName);
                    stepValidation(rspStep, blockID, inputJsonBlock);

                    getLogUtils().debug("Retrieving the output parameters");
                    if (inputJsonBlock.containsKey(OUTPUT_PARAMS_JSON_ELEMENT)) {
                        extractOutputDataFromResponse(blockID, inputJsonBlock, rspStep);
                    }

                    addDelayOnStep(singleBlockObj, FIELD_DELAY_AFTER);
                    TestSuiteHandler.getInstance().getTestCaseUtils().getBeforeStepVariables().clear();  //reset before Step variables
                });

            }
        } catch (Exception oEx) {
            getLogUtils().error("Exception message: '{}'", oEx.getLocalizedMessage());
            throw new HeatException(getLogUtils().getExceptionDetails() + "message: " + oEx.getClass()
                    + " / cause: " + oEx.getCause() + " / message: " + oEx.getLocalizedMessage());
        }
        return respRetrieved;
    }

    private Map<String,Object> loadMapFromTestStep(String elementName, Map testStep) {
        Map<String, Object> loadedMap = new HashMap<>();
        if (testStep.containsKey(elementName) && testStep.get(elementName) != null) {
            Map<String, Object> elementMap = (Map<String, Object>) testStep.get(elementName);
            elementMap.forEach((key, value) -> {
                Object resolvedValue = TestSuiteHandler.getInstance().getEnvironmentHandler().getPlaceholderHandler().placeholderProcessString((String) value);
                loadedMap.put(key, resolvedValue);
            });
        }
        return loadedMap;
    }

    private Map processJsonBlockWithPreviousStepsParameters(Map singleBlockObj) {
        getLogUtils().debug("Processing the step before execution");
        PlaceholderHandler placeholderHandler = new PlaceholderHandler();
        placeholderHandler.setFlowVariables(retrievedParameters);
        Map processedJsonBlock = placeholderHandler.placeholderProcessMap(singleBlockObj);
        return processedJsonBlock;
    }


    private void addDelayOnStep(Map stepObject, String fieldName) {
        if (stepObject.containsKey(fieldName)) {
            int delayMs = Integer.valueOf(stepObject.get(fieldName).toString());
            try {
                if (delayMs > 0) {
                    getLogUtils().debug("Delay of {} ms", delayMs);
                    Thread.sleep(delayMs);
                }
            } catch (InterruptedException ex) {
                getLogUtils().error("Interrupted Exception during '{}' phase", fieldName);
            }
        }
    }

    private void stepValidation(Response stepResponse, Integer stepNumber, Map inputJsonBlock) {
        BasicChecks basicChecks = new BasicChecks(getContext());
        basicChecks.setResponse(stepResponse);
        getLogUtils().setFlowStep(stepNumber);
        getLogUtils().debug("response: '{}'", stepResponse.asString());
        basicChecks.setFlowOutputParameters(retrievedParameters);
        basicChecks.commonTestValidation(inputJsonBlock);
    }

        /**
     * Updates the output parameters soon after the request has made.
     *
     * @param blockID the id of the current block
     * @param paramName the name of the parameter to update
     * @param valueToStore the value updated
     */
    private void updateParameters(Integer blockID, String paramName, String valueToStore) {
        if (!retrievedParameters.containsKey(blockID)) {
            retrievedParameters.put(blockID, new HashMap<>());
        }
        Map<String, String> tmp = retrievedParameters.get(blockID);
        getLogUtils().debug("storing Step[{}].{} = '{}'", blockID, paramName, valueToStore);
        tmp.put(paramName, valueToStore);
        retrievedParameters.put(blockID, tmp);
    }

    private void extractOutputDataFromResponse(Integer blockID, Map<String, Object> inputJsonBlock, Response rsp) {
        Map<String, String> singleBlockOutputParam = (Map<String, String>) inputJsonBlock.get(OUTPUT_PARAMS_JSON_ELEMENT);
        singleBlockOutputParam.forEach((paramName, paramValue)-> {
            getLogUtils().debug("storing '{}':'{}'", paramName, paramValue);

            PlaceholderHandler placeholderHandler = new PlaceholderHandler();
            placeholderHandler.setResponse(rsp);
            updateParameters(blockID, paramName, (String) placeholderHandler.placeholderProcessString(paramValue));

        });

    }

}
