package com.hotels.restassuredframework.core.checks;

import java.util.HashMap;
import java.util.Map;

import org.testng.ITestContext;

import com.hotels.restassuredframework.core.handlers.PlaceholderHandler;
import com.hotels.restassuredframework.core.specificexception.HeatException;
import com.hotels.restassuredframework.core.utils.TestCaseUtils;
import com.hotels.restassuredframework.core.utils.log.LoggingUtils;
import com.jayway.restassured.response.Response;


/**
 * Basic utility class in flow tests.
 */
public class BasicFlowChecks extends BasicMultipleChecks {

    private static final String FIELD_DELAY_BEFORE = "delayBefore";
    private static final String FIELD_DELAY_AFTER = "delayAfter";
    private static final String OUTPUT_PARAMS_JSON_ELEMENT = "outputParams";

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
    public Map<String, Response> retrieveInfo(Map testCaseParamsInput) {
        Map<String, Response> respRetrieved = new HashMap<>();
        try {
            compactInfoToCompare(testCaseParamsInput);
            if (getIsRunnable()) {
                int numberOfBlocks = getHttpMethods().size();
                getLogUtils().trace("number of blocks to load: {}", numberOfBlocks);
                Map<String, Object> singleObjecs = getInputJsonObjs();
                getSteps().forEach((blockID, singleBlockName) -> {
                    getLogUtils().debug("loading the block id {}: '{}'", blockID, singleBlockName);
                    Map singleBlockObj = (Map) singleObjecs.get(singleBlockName);

                    if (!retrievedParameters.isEmpty()) {
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

                });

            }
        } catch (Exception oEx) {
            getLogUtils().error("Exception message: '{}'", oEx.getLocalizedMessage());
            throw new HeatException(getLogUtils().getExceptionDetails() + "message: " + oEx.getClass()
                    + " / cause: " + oEx.getCause() + " / message: " + oEx.getLocalizedMessage());
        }
        return respRetrieved;
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
                getLogUtils().info("Delay of {} ms", delayMs);
                Thread.sleep(delayMs);
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
