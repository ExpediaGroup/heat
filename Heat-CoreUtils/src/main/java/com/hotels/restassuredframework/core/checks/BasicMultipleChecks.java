package com.hotels.restassuredframework.core.checks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.testng.ITestContext;

import com.hotels.restassuredframework.core.environment.EnvironmentHandler;
import com.hotels.restassuredframework.core.handlers.TestSuiteHandler;
import com.hotels.restassuredframework.core.runner.TestBaseRunner;
import com.hotels.restassuredframework.core.specificexception.HeatException;
import com.hotels.restassuredframework.core.utils.RestAssuredRequestMaker;
import com.hotels.restassuredframework.core.utils.TestCaseUtils;
import com.hotels.restassuredframework.core.utils.TestRequest;
import com.hotels.restassuredframework.core.utils.log.LoggingUtils;
import com.jayway.restassured.internal.http.Method;
import com.jayway.restassured.response.Response;


/**
 * Basic utility class in comparing check tests (Flow and Compare mode).
 *
 */
public class BasicMultipleChecks {

    public static final String DEFAULT_FORMAT_OF_TYPE_CHECK = "string";
    private static final String E2E_FLOW_STEPS_JSON_ELEMENT = "e2eFlowSteps";
    private static final String OBJECTS_TO_COMPARE_JSON_ELEMENT = "objectsToCompare";
    private static final String OBJECT_NAME_JSON_ELEMENT = "objectName";
    private static final String WEBAPP_NAME_JSON_ELEMENT = "webappName";
    private static final String URL_JSON_ELEMENT = "url";

    private final boolean isRunnableTest = true;

    private final Map<Integer, String> steps;
    private final Map<String, String> paths;
    private final Map<String, String> httpMethods;

    private final LoggingUtils logUtils;
    private final ITestContext context;

    private Map<String, Object> inputJsonObjs = new HashMap<>();
    private RestAssuredRequestMaker restAssuredMsg;


    public BasicMultipleChecks(ITestContext context) {
        this.logUtils = TestSuiteHandler.getInstance().getLogUtils();
        this.context = context;
        this.steps = new TreeMap<>();
        this.paths = new HashMap<>();
        this.httpMethods = new HashMap<>();
    }

    public LoggingUtils getLogUtils() {
        return this.logUtils;
    }

    public ITestContext getContext() {
        return this.context;
    }


    /**
     * Flat the representation of test case's parameters in order to better analyze them.
     * @param testCaseParamsInput the input parameters to define a test case
     */
    public void compactInfoToCompare(Map<String, Object> testCaseParamsInput) {
        EnvironmentHandler eh = TestSuiteHandler.getInstance().getEnvironmentHandler();
        List<Object> objectsToCompareList;
        if (testCaseParamsInput.containsKey(OBJECTS_TO_COMPARE_JSON_ELEMENT)) {
            objectsToCompareList = (List<Object>) testCaseParamsInput.get(OBJECTS_TO_COMPARE_JSON_ELEMENT);
        } else if (testCaseParamsInput.containsKey(E2E_FLOW_STEPS_JSON_ELEMENT)) {
            objectsToCompareList = (List<Object>) testCaseParamsInput.get(E2E_FLOW_STEPS_JSON_ELEMENT);
        } else {
            throw new HeatException(this.logUtils.getExceptionDetails() + "it is not possible to retrieve a list of request objects");
        }
        this.logUtils.debug("objectsToCompareList size = {}", objectsToCompareList.size());
        Iterator<Object> multipleListIterator = objectsToCompareList.iterator();
        while (multipleListIterator.hasNext()) {
            elaborateObjects(multipleListIterator);
        }
    }

    private void elaborateObjects(Iterator<Object> multipleListIterator) {
        EnvironmentHandler eh = TestSuiteHandler.getInstance().getEnvironmentHandler();
        Map<String, Object> compareSingleObj = (Map<String, Object>) multipleListIterator.next();

        String webappName = (String) compareSingleObj.get(WEBAPP_NAME_JSON_ELEMENT);
        String singleBlockName = (String) compareSingleObj.get(OBJECT_NAME_JSON_ELEMENT);
        String url = (String) compareSingleObj.get(URL_JSON_ELEMENT);
        String webappPath = "";
        if (eh != null) {
            webappPath = eh.getEnvironmentUrl(webappName);
            if (webappPath == null || "".equals(webappPath)) {
                this.logUtils.error("test not runnable");
                throw new HeatException(this.logUtils.getExceptionDetails() + "test not runnable: webapp path not valid");
            } else if (webappPath == null) {
                this.context.setAttribute(this.context.getAttribute(TestBaseRunner.ATTR_TESTCASE_ID).toString(), TestBaseRunner.STATUS_SKIPPED);
                TestSuiteHandler.getInstance().getTestCaseUtils().setWebappPath(webappPath);
            }
        } else {
            this.logUtils.error("environment handler null");
            throw new HeatException(this.logUtils.getExceptionDetails() + "test not runnable: environment handler not valid");
        }
        String httpMethod;
        if (!compareSingleObj.containsKey(TestCaseUtils.JSON_FIELD_HTTP_METHOD) || compareSingleObj.get(TestCaseUtils.JSON_FIELD_HTTP_METHOD) == null) {
            httpMethod = TestRequest.HTTP_METHOD_DEFAULT.name();
        } else {
            httpMethod = (String) compareSingleObj.get(TestCaseUtils.JSON_FIELD_HTTP_METHOD);
        }

        String finalPath = webappPath + url;

        paths.put(singleBlockName, finalPath);
        httpMethods.put(singleBlockName, httpMethod);
        inputJsonObjs.put(singleBlockName, compareSingleObj);

        if (compareSingleObj.containsKey(TestCaseUtils.JSON_FIELD_STEP_NUMBER)) {
            try {
                Integer stepNumber = Integer.parseInt((String) compareSingleObj.get(TestCaseUtils.JSON_FIELD_STEP_NUMBER));
                steps.put(stepNumber, singleBlockName);
            } catch (NumberFormatException nfe) {
                throw new HeatException(this.logUtils.getExceptionDetails() + "test not runnable: 'stepNumber' field isn't an Integer value");
            }
        }

    }




    /**
     * This method retrieves info from the JSON input objects.
     *
     * @param testCaseParamsInput the input parameters to define a test case
     * @return Map webapp name, response from the specified webapp
     */
    public Map<String, Response> retrieveInfo(Map testCaseParamsInput) {
        Map<String, Response> respRetrieved = new HashMap<>();
        try {
            compactInfoToCompare(testCaseParamsInput);

            if (isRunnableTest) {
                this.logUtils.trace("number of blocks to load: {}", httpMethods.size());
                httpMethods.entrySet().stream().map((entry) -> entry.getKey()).forEach((serviceId) -> {
                    Response rsp = retrieveSingleBlockRsp(serviceId);

                    if (rsp == null) {
                        this.logUtils.debug("response for '{}' : null", serviceId);
                    } else {
                        this.logUtils.debug("response for '{}': '{}'", serviceId, rsp.asString());
                    }
                    respRetrieved.put(serviceId, rsp);
                });
            }
        } catch (Exception oEx) {
            this.logUtils.debug("Exception message: '{}'", oEx.getLocalizedMessage());
        }
        return respRetrieved;
    }

    private Response retrieveSingleBlockRsp(String serviceId) {
        Map singleInputJsonObj = (Map) inputJsonObjs.get(serviceId);
        return retrieveSingleBlockRsp(serviceId, singleInputJsonObj);
    }

    /**
     * Perform a test call of a single block and return the related Response object.
     *
     * @param serviceId identifier of the test case
     * @param singleInputJsonObj parameters of test case as Map
     * @return the Response object
     */
    protected Response retrieveSingleBlockRsp(String serviceId, Map singleInputJsonObj) {
        EnvironmentHandler eh = TestSuiteHandler.getInstance().getEnvironmentHandler();
        Method webappHttpMethod = Method.valueOf(httpMethods.get(serviceId));
        String webappPath = paths.get(serviceId);
        this.logUtils.trace("path of block '{}': '{}'", serviceId, webappPath);
        if (restAssuredMsg == null) {
            throw new HeatException(this.logUtils.getExceptionDetails() + "restAssuredMsg obj null");
        }
        restAssuredMsg.setBasePath(eh.getEnvironmentUrl((String) singleInputJsonObj.get(WEBAPP_NAME_JSON_ELEMENT)));
        TestRequest testRequest = restAssuredMsg.buildRequestByParams(webappHttpMethod, singleInputJsonObj);
        Response rsp = restAssuredMsg.executeTestRequest(testRequest);
        return rsp;
    }

    /**
     * expects is a method that analyses all required data.
     * @param isBlocking indicates if this kind of expectation causes the stopping of testing
     * @param testCaseParams the input parameters to define a test case
     * @param mapServiceIdResponse a map containing the responses to be compared
     */
    public void expects(boolean isBlocking, Map testCaseParams, Map<String, Response> mapServiceIdResponse) {
        if (testCaseParams.containsKey("expects")) {

            List<Object> expectedObjList = (List<Object>) testCaseParams.get("expects");
            expectedObjList.forEach(item-> {
                Map<String, Object> singleBlockCheck = (Map<String, Object>) item;
                String checkStepDescription = getCheckDescription(singleBlockCheck);
                boolean conditionOk = true;
                if (singleBlockCheck.containsKey("condition")) {
                    this.logUtils.debug("THERE IS A CONDITION");
                    conditionOk = conditionVerification((ArrayList<Object>) singleBlockCheck.get("condition"), mapServiceIdResponse, checkStepDescription + "(condition)");
                }
                if (conditionOk) {
                    singleBlockCheck(isBlocking, singleBlockCheck, mapServiceIdResponse, checkStepDescription);
                } else {
                    this.logUtils.debug("condition not verified for: '{}'", checkStepDescription);
                }
            });

        }

    }

    private String getCheckDescription(Map fieldCheck) {
        String description = "";
        try {
            description = (String) fieldCheck.get("description");
        } catch (Exception oEx) {
            this.logUtils.error("Exception: class {}, cause {}, message {}",
                    oEx.getClass(), oEx.getCause(), oEx.getLocalizedMessage());
            throw new HeatException(this.logUtils.getExceptionDetails() + "It is not possible to retrieve the description of the check");
        }
        return description;
    }


    private boolean conditionVerification(ArrayList<Object> conditionArray, Map<String, Response> mapServiceIdResponse, String checkStepDescription) {
        boolean isConditionVerified = true;
        this.logUtils.debug("There are {} conditions to verify", conditionArray.size());
        this.logUtils.debug("{}", conditionArray.toString());
        Iterator itr = conditionArray.iterator();
        while (itr.hasNext()) {
            isConditionVerified = isConditionVerified && singleBlockCheck(false, (Map<String, Object>) itr.next(), mapServiceIdResponse, checkStepDescription);
        }
        this.logUtils.debug("{} Condition verified '{}'", checkStepDescription, isConditionVerified);
        return isConditionVerified;
    }

    private boolean singleBlockCheck(boolean isBlocking, Map<String, Object> blockToCheck, Map<String, Response> mapServiceIdResponse, String checkStepDescription) {
        boolean isCheckOk = true;
        this.logUtils.trace("{} block: {}", checkStepDescription, blockToCheck.toString());
        BasicChecks basicChecks = new BasicChecks(this.context);
        isCheckOk = basicChecks.executeCheck(isBlocking, blockToCheck, mapServiceIdResponse);

        return isCheckOk;
    }

    public void setRestAssuredRequestMaker(RestAssuredRequestMaker requestMakerInput) {
        restAssuredMsg = requestMakerInput;
    }


    public boolean getIsRunnable() {
        return isRunnableTest;
    }

    public Map<Integer, String> getSteps() {
        return steps;
    }

    public Map<String, String> getHttpMethods() {
        return httpMethods;
    }

    public Map<String, Object> getInputJsonObjs() {
        return inputJsonObjs;
    }

}
