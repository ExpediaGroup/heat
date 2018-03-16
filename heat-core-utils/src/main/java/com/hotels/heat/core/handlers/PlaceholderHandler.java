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
package com.hotels.heat.core.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hotels.heat.core.dto.HeatTestDetails;
import com.hotels.heat.core.environment.EnvironmentHandler;
import com.hotels.heat.core.heatmodules.HeatPlaceholderModuleProvider;
import com.hotels.heat.core.specificexception.HeatException;
import com.hotels.heat.core.utils.TestCaseUtils;
import com.hotels.heat.core.utils.log.LoggingUtils;

import com.jayway.restassured.path.json.config.JsonPathConfig;
import com.jayway.restassured.response.Response;


/**
 * This class is useful to manage the placeholders used in tests.
 *
 */
public class PlaceholderHandler {

    public static final String DEFAULT_PRELOADED_VALUE = "DEFAULT";
    public static final String PLACEHOLDER_SYMBOL   = "$";
    public static final String PLACEHOLDER_SYMBOL_BEGIN = PLACEHOLDER_SYMBOL + "{";
    public static final String PLACEHOLDER_SYMBOL_END = "}";
    public static final String PLACEHOLDER_JSON_SCHEMA_NO_CHECK = PLACEHOLDER_SYMBOL_BEGIN + "NoCheck" + PLACEHOLDER_SYMBOL_END;
    public static final String PATH_PLACEHOLDER = PLACEHOLDER_SYMBOL_BEGIN + "path";
    public static final String PLACEHOLDER_NOT_PRESENT = PLACEHOLDER_SYMBOL_BEGIN + "NotPresent" + PLACEHOLDER_SYMBOL_END;
    public static final String PLACEHOLDER_PRESENT = PLACEHOLDER_SYMBOL_BEGIN + "Present" + PLACEHOLDER_SYMBOL_END;
    public static final String PATH_JSONPATH_REGEXP = ".*?\\$\\{path\\[(.*?)\\]\\}.*?";

    private static final String DEFAULT_VALUE_NOT_FOUND_MSG = "DEFAULT VALUE NOT FOUND!!!";

    private static final String JSONPATH_COMPLETE = ".";

    private static final String REGEXP_PRELOAD_PLACEHOLDER = "((?:\\$\\{preload\\[[^\\]\\}]*\\]\\})|(?:\\$\\{preload\\[[^\\]\\}]*\\]\\.get\\(.*?\\)\\}))";
    private static final String REGEXP_PATH_PLACEHOLDER = "(?:\\$\\{path\\[.*?\\]\\})"; //"(?:\\$\\{path\\[[^\\}]*\\]\\})";
    private static final String REGEXP_COOKIE_PLACEHOLDER = "(?:\\$\\{cookie\\[[^\\]\\}]*\\]\\})";
    private static final String REGEXP_COOKIE_JSONPATH = ".*?\\$\\{cookie\\[(.*?)\\]\\}.*?";
    private static final String REGEXP_HEADER_PLACEHOLDER = "(?:\\$\\{header\\[[^\\]\\}]*\\]\\})";
    private static final String REGEXP_HEADER_JSONPATH = ".*?\\$\\{header\\[(.*?)\\]\\}.*?";
    private static final String REGEXP_PRELOAD_VAR_NAME_PLACEHOLDER = "\\$\\{preload\\[(.*?)].*?\\}";
    private static final String REGEXP_GET_PRELOAD_SPECIFIC_FIELD = "\\$\\{preload\\[.*?]\\.get\\((.*?)\\)\\}";
    private static final String REGEXP_GET_STEP_PLACEHOLDER = "(?:\\$\\{getStep\\(.*?\\)\\.getOutputParam\\(.*?\\)\\})";
    private static final String REGEXP_GET_STEP_OUTPUT_PARAM_NAME = ".*?\\$\\{getStep\\(.*?\\).getOutputParam\\((.*?)\\)\\}.*?";
    private static final String REGEXP_GET_STEP_NUMBER = ".*?\\$\\{getStep\\((.*?)\\).*?\\}.*?";

    private final LoggingUtils logUtils;

    private Map<String, HeatPlaceholderModuleProvider> providerMap;

    private Map<String, Object> preloadedVariables;
    private Map<Integer, Map<String, String>> flowPreloadedVariables;
    private Response response;

    /**
     * PlaceholderHandler class is useful to placeholderProcessString json input files structure,
     * so that it is possible to manage external libraries, preloaded variables and so on
     * with specific placeholders.
     */
    public PlaceholderHandler() {
        this.logUtils = TestSuiteHandler.getInstance().getLogUtils();
        //here we are checking if there are some external libraries to load (see Java Service Provider Interface pattern)
        //providerMap contains placeholders (key) and the specific class instances that are able to manage them (value)
        providerMap = new HashMap<>();
        ServiceLoader.load(HeatPlaceholderModuleProvider.class).forEach(provider -> {
            providerMap = constructProviderMap(providerMap, provider.getHandledPlaceholders(), provider);
        });
        this.logUtils.trace("found n. {} provider(s)", providerMap.size());
        this.preloadedVariables = new HashMap<>();
        this.flowPreloadedVariables = new HashMap<>();
    }


    /**
     * Construction of provider map: it contains placeholders (key) and the specific class instances that are able to manage them (value).
     * @param providerMapInput it is the map to update
     * @param handledPlaceholders is a list of placeholders that is it possible to manage
     * @param provider HeatPlaceholderModuleProvider
     * @return an updated provider map
     */
    public Map<String, HeatPlaceholderModuleProvider> constructProviderMap(Map<String, HeatPlaceholderModuleProvider> providerMapInput,
            List<String> handledPlaceholders,
            HeatPlaceholderModuleProvider provider) {
        try {
            logUtils.trace("found provider for: {}", provider.getHandledPlaceholders());
            handledPlaceholders.forEach(placeholder -> providerMapInput.put(placeholder, provider));
        } catch (Exception oEx) {
            logUtils.error("catched exception message: '{}' \n cause: '{}'",
                        oEx.getLocalizedMessage(), oEx.getCause());
        }
        return providerMapInput;
    }


    /**
     * This method processes a string and, if necessary, substitutes placeholders.
     * It handles:
     * - getStep placeholder (used only in flow mode)
     * - preload placeholder
     * - path placeholder
     * - placeholders coming from external heat modules and present in the provider map
     * @param inputStr string to placeholderProcessString. It can contain more than one placeholders, but it does not manage innested ones.
     * @return the processed string
     */
    public Object placeholderProcessString(String inputStr) {
        logUtils.trace("inputStr = '{}'", inputStr);
        Object outputObj = inputStr;
        try {
            if (inputStr.contains(PLACEHOLDER_SYMBOL_BEGIN)) {
                outputObj = processGetStepPlaceholder(outputObj.toString());
                outputObj = processPreloadPlaceholders(outputObj.toString());
                outputObj = processPathPlaceholder(outputObj.toString());
                outputObj = processCookiePlaceholder(outputObj.toString());
                outputObj = processHeaderPlaceholder(outputObj.toString());
                outputObj = processPlaceholderFromExternalModules(outputObj.toString());
            }
        } catch (Exception oEx) {
            logUtils.error("catched exception message: '{}' \n cause: '{}'",
                        oEx.getLocalizedMessage(), oEx.getCause());
        }
        return outputObj;
    }

    /**
     * Method to placeholderProcessString a "${path[]}"-like placeholder.
     * @param input object to placeholderProcessString
     * @return processed object
     */
    private String processPathPlaceholder(String input) {
        String outputObj = input;
        if (response != null) {
            outputObj = processGenericPlaceholders(input, REGEXP_PATH_PLACEHOLDER, this::getPathVar);
        }
        return outputObj;
    }

    private String getPathVar(Object inputObj) {
        TestCaseUtils testCaseUtils = TestSuiteHandler.getInstance().getTestCaseUtils();
        String jsonPathToRetrieve = testCaseUtils.regexpExtractor(inputObj.toString(), PATH_JSONPATH_REGEXP, 1);
        return retriveStringFromPath(response, jsonPathToRetrieve);
    }

    /**
     * Method to placeholderProcessString a "${cookie[]}"-like placeholder.
     * @param input object to placeholderProcessString
     * @return processed object
     */
    private String processCookiePlaceholder(String input) {
        String outputObj = input;
        if (response != null) {
            outputObj = processGenericPlaceholders(input, REGEXP_COOKIE_PLACEHOLDER, this::getCookieVar);
        }
        return outputObj;
    }

    private String getCookieVar(Object inputObj) {
        TestCaseUtils testCaseUtils = TestSuiteHandler.getInstance().getTestCaseUtils();
        String cookieNameToRetrieve = testCaseUtils.regexpExtractor(inputObj.toString(), REGEXP_COOKIE_JSONPATH, 1);
        return response.getCookies().containsKey(cookieNameToRetrieve) ? response.getCookie(cookieNameToRetrieve) : inputObj.toString();
    }

    /**
     * Method to placeholderProcessString a "${header[]}"-like placeholder.
     * @param inputString object to placeholderProcessString
     * @return processed object
     */
    public String processHeaderPlaceholder(String inputString) {
        String outputString = inputString;
        if (response != null) {
            outputString = processGenericPlaceholders(inputString, REGEXP_HEADER_PLACEHOLDER, this::getHeaderVar);
        }
        return outputString;
    }

    private String getHeaderVar(String inputString) {
        TestCaseUtils testCaseUtils = TestSuiteHandler.getInstance().getTestCaseUtils();
        String headerNameToRetrieve = testCaseUtils.regexpExtractor(inputString, REGEXP_HEADER_JSONPATH, 1);
        return response.headers().hasHeaderWithName(headerNameToRetrieve) ? response.getHeader(headerNameToRetrieve) : inputString;
    }

    /**
     * Method to placeholderProcessString all placeholders managed in external heat modules.
     * @param input object to placeholderProcessString
     * @return processed object
     */
    private Object processPlaceholderFromExternalModules(String input) {
        EnvironmentHandler eh = TestSuiteHandler.getInstance().getEnvironmentHandler();
        Object outputObj = input;
        if (!providerMap.isEmpty()) {
            try {
                for (Map.Entry<String, HeatPlaceholderModuleProvider> entry : providerMap.entrySet()) {
                    if (outputObj.toString().contains(entry.getKey())) {
                        HeatTestDetails testDetails = new HeatTestDetails(eh.getEnvironmentUnderTest(), logUtils.getTestCaseDetails());
                        outputObj = entry.getValue().getModuleInstance().process(outputObj.toString(), testDetails);
                        break;
                    }
                }
            } catch (Exception e) {
                throw new HeatException("Error due to invoke external module '" + e.getLocalizedMessage() + "' \n cause: '" + e.getCause() + "'");
            }
        }
        return outputObj;
    }

    private static String escapeRegexCharsInRegex(String s) {
        return s.replace(".", "\\.")
                //            .replace("-", "\\-")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("*", "\\*")
                .replace("+", "\\+")
                .replace("?", "\\?")
                .replace("^", "\\*")
                .replace("$", "\\$")
                .replace("|", "\\|")
                .replace("#", "\\#");
    }

    /**
     * Method to placeholderProcessString "${preload[]}"-like placeholder.
     * @param input object to placeholderProcessString
     * @return processed object
     */
    private String processPreloadPlaceholders(String input) {
        String outputString = input;
        outputString = processGenericPlaceholders(outputString, REGEXP_PRELOAD_PLACEHOLDER, this::getPreloadedVariable);
        return outputString;
    }

    private String processGenericPlaceholders(String inputString, String regex, Function<String, String> substituctionFunct) {
        String outputStr = inputString;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(inputString);
        List<String> placeholders = new ArrayList<>();

        while (matcher.find()) {
            placeholders.add(matcher.group(0));
        }

        for (String placeholder : placeholders) {
            String modifiedString = "";
            String escapedPlaceholder = escapeRegexCharsInRegex(placeholder);
            String[] stringComponents = outputStr.split("((?<=" + escapedPlaceholder + ")|(?=" + escapedPlaceholder + "))");
            logUtils.debug("strings.length : '{}'", stringComponents.length);
            for (String stringComponent : stringComponents) {
                if (stringComponent.equals(placeholder)) {
                    logUtils.debug("substitution '{}'", placeholder);
                    //modifiedString += getPreloadedVariable(placeholder);
                    modifiedString += substituctionFunct.apply(placeholder);
                } else {
                    modifiedString += stringComponent;
                }
            }
            outputStr = modifiedString;
        }
        return outputStr;
    }

    private String getStepPlaceholderVar(String inputString) {
        String outputString = inputString;
        TestCaseUtils testCaseUtils = TestSuiteHandler.getInstance().getTestCaseUtils();
        Integer stepNumber = Integer.parseInt(testCaseUtils.regexpExtractor(outputString, REGEXP_GET_STEP_NUMBER, 1));
        if (flowPreloadedVariables.containsKey(stepNumber)) {
            String paramName = testCaseUtils.regexpExtractor(outputString, REGEXP_GET_STEP_OUTPUT_PARAM_NAME, 1);
            if (flowPreloadedVariables.get(stepNumber).containsKey(paramName)) {
                outputString = flowPreloadedVariables.get(stepNumber).get(paramName);
            } else {
                logUtils.error("The step {} has not generated any parameter whose name is {}", stepNumber, paramName);
            }
        } else {
            logUtils.error("The step {} has not generated any output parameter", stepNumber);
        }
        return outputString;
    }

    /**
     * Method to placeholderProcessString "${getStep()}"-like placeholder.
     * @param input object to placeholderProcessString
     * @return processed object
     */
    private String processGetStepPlaceholder(String input) {
        String outputObj = input;
        if (!flowPreloadedVariables.isEmpty()) {
            outputObj = processGenericPlaceholders(outputObj, REGEXP_GET_STEP_PLACEHOLDER, this::getStepPlaceholderVar);
        }
        return outputObj;
    }

    /**
     * This method is useful to retrieve a json path from a json response.
     * Example:
     * rsp:  {"status":"FAILURE","machines":[{"name":"m1","status":"OK"},{"name":"m2","status":"NOT OK"}]}
     * if path = "status" --> expected output = "FAILURE"
     * if path = "machines[0].name" --> expected output = "m1"
     * if path = "machines.name" --> expected output = "[m1,m2]"
     * if path = "." --> expected output = "{"status":"FAILURE","machines":[{"name":"m1","status":"OK"},{"name":"m2","status":"NOT OK"}]}"
     * @param rsp is the Response object coming from the request to the service under test
     * @param path is the json path to retrieve
     * @return the string retrieved
     */
    private String retriveStringFromPath(Response rsp, String path) {
        String output;
        try {
            if (JSONPATH_COMPLETE.equals(path)) {
                output = rsp.asString().trim();
            } else {
                JsonPathConfig config = new JsonPathConfig(JsonPathConfig.NumberReturnType.BIG_DECIMAL);
                output = rsp.jsonPath(config).get(path).toString();
            }
        } catch (Exception oEx) {
            logUtils.error("It is not possible to retrieve the jsonPath "
                    + "('{}') from the current response. --> response: {}", path, rsp.asString());
            throw new HeatException(logUtils.getExceptionDetails() + "It is not possible to retrieve the jsonPath (" + path
                    + ") from the current response. --> response: " + rsp.asString());
        }
        return output;
    }

    /**
     * Process all elements in request Map and return the same map with all placeholder resolved
     * This method is useful to placeholderProcessString a map: it takes all its elements and, recursively, uses string processing.
     * @param requestParamsMap is the map to placeholderProcessString
     * @return the processed map. It has the same structure as the input one, but, when needed,
     * processes the placeholders it contains
     */
    public Map<String, Object> placeholderProcessMap(Map<String, Object> requestParamsMap) {
        Map<String, Object> outputParams = requestParamsMap;
        if (requestParamsMap != null) {
            try {
                for (Map.Entry<String, Object> parameterObj : outputParams.entrySet()) {
                    Object parameterValue = parameterObj.getValue();
                    logUtils.trace("parameterObj.getValue() = '{}'", parameterValue);
                    logUtils.trace("parameterValue.getClass() = '{}'", parameterValue.getClass());
                    if (parameterValue.getClass().equals(String.class)) {
                        if (((String) parameterValue).contains(PLACEHOLDER_SYMBOL_BEGIN)) {
                            outputParams.put(parameterObj.getKey(), placeholderProcessString((String) parameterValue));
                        }
                    } else if (parameterValue.getClass().equals(Map.class) || parameterValue.getClass().equals(HashMap.class)) {
                        outputParams.put(parameterObj.getKey(), placeholderProcessMap((Map) parameterValue));
                    }
                }
            } catch (Exception oEx) {
                logUtils.error("Exception = '{}'", oEx.getLocalizedMessage());
            }
        } else {
            outputParams = requestParamsMap;
        }
        return outputParams;
    }

    /**
     * This method is used to retrieve the preloaded variables.
     * Preloaded variables are values loaded in the first part of the json input file, in test modules, loaded only
     * once for each test suite, and that can be used in each test case in that suite.
     * @param stringInput it is the input string.
     * It can be something like:
     * - ${preload[PIPPO]} and in this case we will load the variable called "PIPPO" declared in the first part of the json input file
     * - ${preload[PIPPO].get(pluto)}. Also in this case we will load the variable called "PIPPO" but we are supposing that that
     * variable is not a string but a map (it happens in case of external libraries). In that case, we will load the value corresponding
     * to the key "pluto" in "PIPPO" map.
     * @return the loaded variable.
     */
    private String getPreloadedVariable(String stringInput) {
        String outputStr = stringInput;
        preloadedVariables = getPreloadedVariables();
        if (!preloadedVariables.isEmpty()) {
            outputStr = TestSuiteHandler.getInstance().getTestCaseUtils().regexpExtractor(stringInput, REGEXP_PRELOAD_VAR_NAME_PLACEHOLDER, 1);
            if (preloadedVariables.containsKey(outputStr)) {
                // if there is not any specific variable to get
                Object objectPreloaded = preloadedVariables.get(outputStr);
                if (objectPreloaded.getClass().equals(String.class)) {
                    outputStr = objectPreloaded.toString();
                } else {
                    Map<String, String> preloadedObject = (Map<String, String>) objectPreloaded;
                    outputStr = getSpecificPreloadValue(stringInput, preloadedObject);
                }
            } else {
                throw new HeatException(logUtils.getExceptionDetails() + "variable '" + outputStr + "' not correctly preloaded");
            }
        }
        return outputStr;
    }

    /**
     * This method has to be used in case of specific variable to preload.
     * @param stringInput is the string containing the placeholder declaration ${preload[PIPPO].get(pluto)}
     * @param loadedObject is the map retrieved from 'getPreloadedVariable' method.
     * @return the value in the map, corresponding to the key specified in the placeholder (i.e. 'pluto')
     */
    private String getSpecificPreloadValue(String stringInput, Object loadedObject) {
        String outputStr = loadedObject.toString();
        try {
            String specificFieldReq = TestSuiteHandler.getInstance().getTestCaseUtils().regexpExtractor(stringInput, REGEXP_GET_PRELOAD_SPECIFIC_FIELD, 1);

            logUtils.trace("specificFieldReq {}", specificFieldReq);

            if (!loadedObject.getClass().equals(String.class)) {

                if (((Map<String, String>) loadedObject).containsKey(specificFieldReq)) {
                    outputStr = ((Map<String, String>) loadedObject).get(specificFieldReq);
                } else {
                    outputStr = ((Map<String, String>) loadedObject).getOrDefault(DEFAULT_PRELOADED_VALUE, DEFAULT_VALUE_NOT_FOUND_MSG);
                    logUtils.warning("Requested key '{}' not found: DEFAULT value will be used", specificFieldReq);
                }
            }
        } catch (Exception oEx) {
            logUtils.error("catched exception message: '{}' \n cause: '{}'",
                        oEx.getLocalizedMessage(), oEx.getCause());
        }
        return outputStr;
    }

    public void setResponse(Response rsp) {
        this.response = rsp;
    }

    public void setPreloadedVariables(Map<String, Object> preloadedVarsInput) {
        this.preloadedVariables = preloadedVarsInput;
    }

    public Map<String, Object> getPreloadedVariables() {
        return preloadedVariables;
    }

    public void setFlowVariables(Map<Integer, Map<String, String>> flowPreloadedVariables) {
        this.flowPreloadedVariables = flowPreloadedVariables;
    }

}
