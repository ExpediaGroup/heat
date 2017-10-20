package com.hotels.restassuredframework.core.utils;

import java.util.HashMap;
import java.util.Map;

import com.hotels.restassuredframework.core.handlers.PlaceholderHandler;
import com.hotels.restassuredframework.core.handlers.TestSuiteHandler;
import com.hotels.restassuredframework.core.specificexception.HeatException;
import com.hotels.restassuredframework.core.utils.log.LoggingUtils;
import com.jayway.restassured.response.Response;


/**
 * Class useful to extract data from all kind of modality;
 * It has to manage "actualValue" and "expectedValue", processing it and retrieving values that has to be
 * checked in the "verify" step.
 */
public class DataExtractionSupport {

    public static final String STRING_TO_PARSE_JSON_ELEMENT = "stringToParse";
    public static final String REGEXP_JSON_ELEMENT = "regexp";
    public static final String REGEXP_MATCH_JSON_ELEMENT = "regexpToMatch";
    public static final String OCCURRENCE_JSON_ELEMENT = "occurrenceOf";

    private final LoggingUtils logUtils;

    private Map<Integer, Map<String, String>> retrievedParametersFlowMode;

    public DataExtractionSupport(LoggingUtils logUtils) {
        this.logUtils = logUtils;
        this.retrievedParametersFlowMode = new HashMap<>();
    }

    /**
     * This method gets in input the structure of "actualValue" or "expectedValue" and gives in output
     * the final string we have to use for the verification step.
     * @param extractionObj the input "object" that represents "actualValue" or "expectedValue". It can be, in the
 simplest context, a string. But it can be another json object with inner parameters, for example a regexp on
 the service response, useful to retrieve a value. In case of inner json object, there is a recursive use
 of "placeholderProcessString" method.
     * @param response It is the response retrieved after the request to the service under test.
     * @param retrievedParametersFlowMode The parameters passed to the steps in flow mode
     * @return the final string we have to use for the verification step
     */
    public String process(Object extractionObj, Response response, Map retrievedParametersFlowMode) {
        String outputStr = "";
        this.retrievedParametersFlowMode = retrievedParametersFlowMode;
        logUtils.trace("extractionObj = '{}'", extractionObj.toString());
        if (extractionObj.getClass().equals(String.class)) {
            outputStr = processString((String) extractionObj, response);
        } else if (extractionObj.getClass().equals(HashMap.class)) {
            // we are using HashMap because JsonPath uses maps to manage json object
            outputStr = processMap((Map) extractionObj, response);
        } else {
            throw new HeatException(logUtils.getExceptionDetails() + "actualValue/expectedValue belongs to "
                    + extractionObj.getClass().toString() + " not supported");
        }
        logUtils.trace("outputStr = '{}' (class: {})", outputStr, extractionObj.getClass().toString());
        return outputStr;
    }


    /**
     * placeholderProcessString method simply get a string and, if necessary, processes it with PlaceholderHandler.
     * @param inputString is the string to be processed
     * @param response is the response retrieved from the service under test
     * @return the string processed. If it is a simple string (without any placeholder or
     * processing needed), the output will be the same as the input
     */
    private String processString(String inputString, Response response) {
        PlaceholderHandler placeholderHandler = new PlaceholderHandler();
        placeholderHandler.setResponse(response);
        placeholderHandler.setFlowVariables(retrievedParametersFlowMode);
        String outputStr = (String) placeholderHandler.placeholderProcessString(inputString);

        logUtils.trace("input value='{}' / outputStr='{}'", inputString, outputStr);
        return outputStr;
    }

    /**
     * This method is used when "actualValue" or "expectedValue" are json object and not simple strings.
     * In particular it can manage:
     * - regular expression extraction
     * - occurrence of a given string in another string
     * - ... TBD
     * @param map it is the map representing the json object of the "actualValue" or "expectedValue"
     * @param response is the response retrieved from the service under test.
     * @return the final string to use for the verification step
     */
    private String processMap(Map map, Response response) {
        String outputStr = "";
        if (map.containsKey(REGEXP_JSON_ELEMENT) && map.containsKey(STRING_TO_PARSE_JSON_ELEMENT)) {
            outputStr = regexpExtractorProcessing(map, response);
        } else if (map.containsKey(REGEXP_MATCH_JSON_ELEMENT) && map.containsKey(STRING_TO_PARSE_JSON_ELEMENT)) {
            outputStr = regexpMatchExtractorProcessing(map, response);
        } else if (map.containsKey(OCCURRENCE_JSON_ELEMENT) && map.containsKey(STRING_TO_PARSE_JSON_ELEMENT)) {
            outputStr = occurrenceOfProcessing(map, response);
        } else {
            throw new HeatException(logUtils.getExceptionDetails() + "configuration " + map.toString() + " not supported");
        }
        return outputStr;
    }

    /**
     * Regular expression extraction management.
     * example:
     *     "actualValue": {
     *          "regexp":"PIPPO_(.*?)_PLUTO",
     *          "stringToParse":"PIPPO_123_PLUTO"
     *     }
     * expected output = "123"
     * @param map it is the map representing the json object of the "actualValue" or "expectedValue"
     * @param response is the response retrieved from the service under test.
     * @return the final string to use for the verification step
     */
    private String regexpExtractorProcessing(Map map, Response response) {
        String stringToParse = processString((String) map.get(STRING_TO_PARSE_JSON_ELEMENT), response);
        String regularExpression = (String) map.get(REGEXP_JSON_ELEMENT);
        TestCaseUtils testCaseUtils = TestSuiteHandler.getInstance().getTestCaseUtils();
        return testCaseUtils.regexpExtractor(stringToParse, regularExpression, 1);
    }

    /**
     * Regular expression match extraction management.
     * example:
     *     "actualValue": {
     *          "regexpToMatch":"PIPPO_(.*?)_PLUTO",
     *          "stringToParse":"PIPPO_123_PLUTO"
     *     }
     * expected output = "true"
     * @param map it is the map representing the json object of the "actualValue" or "expectedValue"
     * @param response is the response retrieved from the service under test.
     * @return the final string to use for the verification step
     */
    private String regexpMatchExtractorProcessing(Map map, Response response) {
        String stringToParse = processString((String) map.get(STRING_TO_PARSE_JSON_ELEMENT), response);
        String regularExpression = (String) map.get(REGEXP_MATCH_JSON_ELEMENT);
        TestCaseUtils testCaseUtils = TestSuiteHandler.getInstance().getTestCaseUtils();
        String outputRegexp = testCaseUtils.getRegexpMatch(stringToParse, regularExpression, 1);
        return TestCaseUtils.NO_MATCH.equals(outputRegexp) ? "false" : "true";
    }

    /**
     * occurrences of a string extraction management.
     * example:
     *     "actualValue": {
     *          "occurrenceOf":"PIPPO",
     *          "stringToParse":"PIPPO_PLUTO_PIPPO_MICKEY"
     *     }
     * expected output = "2"
     * @param map it is the map representing the json object of the "actualValue" or "expectedValue"
     * @param response is the response retrieved from the service under test.
     * @return the final string to use for the verification step
     */
    private String occurrenceOfProcessing(Map map, Response response) {
        String stringToParse = processString((String) map.get(STRING_TO_PARSE_JSON_ELEMENT), response);
        String occurrenceString = (String) map.get(OCCURRENCE_JSON_ELEMENT);
        int occurrences = stringToParse.split(occurrenceString).length - 1;
        return String.valueOf(occurrences);
    }


}
