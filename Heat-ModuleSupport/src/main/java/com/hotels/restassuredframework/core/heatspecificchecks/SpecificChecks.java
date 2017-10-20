package com.hotels.restassuredframework.core.heatspecificchecks;

import java.util.Map;
import java.util.Set;

import com.jayway.restassured.response.Response;



/**
 * Abstract class to implement to create a specific check class in test modules.
 * @author adebiase
 */
public abstract class SpecificChecks {
    
    public void process(String suiteName, Map testCaseParamenter, Map<String, Response> responsesRetrieved, String testRef, String environment) {
        if (this.handledSuites().contains(suiteName)) {
            this.process(testCaseParamenter, responsesRetrieved, testRef, environment);
        }
    }

    protected abstract void process(Map testCaseParamenter, Map<String, Response> responsesRetrieved, String testRef, String environment);

    protected abstract Set<String> handledSuites();


}
