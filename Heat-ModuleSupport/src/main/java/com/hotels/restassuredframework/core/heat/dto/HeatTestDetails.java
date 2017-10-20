package com.hotels.restassuredframework.core.heat.dto;

/**
 * Created by lpelosi on 03/05/17.
 */
public class HeatTestDetails {

    private String testDescription;
    private String environment;

    public HeatTestDetails(){
    }

    public HeatTestDetails(String environment, String testDescription){
        this.environment = environment;
        this.testDescription = testDescription;
    }



    public String getTestDescription() {
        return testDescription;
    }

    public void setTestDescription(String testDescription) {
        this.testDescription = testDescription;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }
}
