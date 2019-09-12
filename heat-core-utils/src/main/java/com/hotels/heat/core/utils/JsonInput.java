package com.hotels.heat.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotels.heat.core.runner.TestBaseRunner;
import io.restassured.path.json.JsonPath;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static io.restassured.path.json.JsonPath.with;

public final class JsonInput {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static JsonInput jsonInputHandler;

    private final static String FILE_EXTENSION_YML = "yml";
    private final static String FILE_EXTENSION_JSON = "json";

    private static final String JSONPATH_GENERAL_SETTINGS = "testSuite.generalSettings";
    private static final String JSONPATH_BEFORE_SUITE_SECTION = "testSuite.beforeTestSuite";
    private static final String JSONPATH_JSONSCHEMAS = "testSuite.jsonSchemas";
    private static final String JSONPATH_TEST_CASES = "testSuite.testCases";

    public static synchronized JsonInput getInstance() {
        if (jsonInputHandler == null) {
            jsonInputHandler = new JsonInput();
        }
        return jsonInputHandler;
    }

    /**
     * It is the method that handles the reading of the json input file that is
     * driving the test suite.
     *
     * @param testSuiteFilePath the path of the json input file
     * @param context it is the context of the test. It is managed from TestNG
     * but it can be used to set and read some parameters all over the test
     * suite execution
     * @return the iterator of the test cases described in the json input file
     */
    public Iterator<Object[]> jsonReader(String testSuiteFilePath, ITestContext context) {

        if (context == null) {
            throw new HeatException(this.getClass(), "ITestContext object is null");
        }

        Iterator<Object[]> iterator;
        //check if the test suite is runnable
        // (in terms of enabled environments or test suite explicitly declared in the 'heatTest' system property)

        if (isTestSuiteRunnable(context.getName())) {
            File testSuiteJsonFile = Optional.ofNullable(getClass().getResource(testSuiteFilePath))
                    .map(url -> new File(url.getPath()))
                    .orElseThrow(() -> new HeatException(this.getClass(), String.format("the file '%s' does not exist",testSuiteFilePath)));

            try {

                JsonPath testSuiteJsonPath;
                switch (FilenameUtils.getExtension(testSuiteFilePath)) {
                    case FILE_EXTENSION_YML:
                        testSuiteJsonPath = convertToJson(testSuiteJsonFile);
                        break;
                    case FILE_EXTENSION_JSON:
                    default:
                        testSuiteJsonPath = with(testSuiteJsonFile);
                        break;
                }

                Map<String, String> generalSettings = testSuiteJsonPath.get(JSONPATH_GENERAL_SETTINGS);
                Map<String, String> beforeSuite = testSuiteJsonPath.get(JSONPATH_BEFORE_SUITE_SECTION);
                Map<String, String> jsonSchemas = testSuiteJsonPath.get(JSONPATH_JSONSCHEMAS);

                iterator = getTestCaseIterator(testSuiteJsonPath,
                        generalSettings, beforeSuite, jsonSchemas);


            } catch (Exception oEx) {
                throw new HeatException(this.getClass(), oEx);
            }

        } else {
            throw new SkipException(logUtils.getTestCaseDetails() + "Skip test: this suite is not requested");
        }
        return iterator;
    }



    private boolean isTestSuiteRunnable(String testSuiteName) {
        return true;
    }

    private JsonPath convertToJson(File yamlFile) {
        try {
            String content = FileUtils.readFileToString(yamlFile);
            Yaml yaml = new Yaml();
            Map<String, Object> map = (Map<String, Object>) yaml.load(content);
            ObjectMapper jsonWriter = new ObjectMapper();
            JsonPath testSuiteJsonPath = with(jsonWriter.writerWithDefaultPrettyPrinter().writeValueAsString(map));

            return testSuiteJsonPath;
        } catch (JsonProcessingException ex) {
            throw new HeatException(String.format("the file '%s' cannot be parsed"), ex);

        } catch (IOException ex) {
            throw new HeatException(String.format("the file '%s' cannot be opened"), ex);
        }
    }

    private Iterator<Object[]> getTestCaseIterator(JsonPath testSuiteJsonPath,
                                                   Map<String, String> generalSettings,
                                                   Map<String, String> beforeSuite,
                                                   Map<String, String> jsonSchemas) {

        Map<String, Object> testCaseData = new HashMap<>();

        Iterator<Object[]> tcArrayIterator;

        List<Object> testCases = testSuiteJsonPath.get(JSONPATH_TEST_CASES);
        List<Object[]> listOfArray = new ArrayList();
        for (Object testCase : testCases) {
            testCaseData.put("TEST_CASE", testCase);
            testCaseData.put("GENERAL_SETTINGS", generalSettings);
            testCaseData.put("BEFORE_SUITE", beforeSuite);
            testCaseData.put("JSON_SCHEMAS", jsonSchemas);
            listOfArray.add(new Object[]{testCaseData});
        }
        tcArrayIterator = listOfArray.iterator();
        return tcArrayIterator;
    }
}
