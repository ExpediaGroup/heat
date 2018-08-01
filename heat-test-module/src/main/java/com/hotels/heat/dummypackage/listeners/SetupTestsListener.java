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
package com.hotels.heat.dummypackage.listeners;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.Parameters;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

/**
 * Created by lpelosi on 14/05/18.
 */
public class SetupTestsListener implements ITestListener {
    private final Logger logger = LoggerFactory.getLogger(SetupTestsListener.class);
    private WireMockServer wmServer;

    @Override
    public void onTestStart(ITestResult iTestResult) {

    }

    @Override
    public void onTestSuccess(ITestResult iTestResult) {

    }

    @Override
    public void onTestFailure(ITestResult iTestResult) {

    }

    @Override
    public void onTestSkipped(ITestResult iTestResult) {

    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {

    }

    @Parameters({"wmTests", "wmBindAddress", "wmRoot", "wmPort"})
    @Override
    public void onStart(ITestContext iTestContext) {

        if (isWiremockTest(iTestContext, "wmTests")) {
            logger.info("{} is a wiremock test.\n Wiremock server is starting...", iTestContext.getCurrentXmlTest().getName());
            String wmBindAddress = iTestContext.getCurrentXmlTest().getParameter("wmBindAddress");
            String wmRoot = iTestContext.getCurrentXmlTest().getParameter("wmRoot");
            String wmPort = iTestContext.getCurrentXmlTest().getParameter("wmPort");
            this.runNewServer(wmBindAddress, wmRoot, wmPort);
            logger.info("... Wiremock server started");
        }
    }


    @Parameters("wmTests")
    @Override
    public void onFinish(ITestContext iTestContext) {
        if (isWiremockTest(iTestContext, "wmTests")) {
            logger.info("{} was a wiremock test.\n Wiremock server is stopping...", iTestContext.getCurrentXmlTest().getName());
            this.wmServer.stop();
            logger.info("... Wiremock server stopped");
        }
    }

    private boolean isWiremockTest(ITestContext iTestContext, String wmTestsParamName) {
        boolean isWiremockTest = false;
        String wiremockTests = iTestContext.getSuite().getParameter(wmTestsParamName);
        if (wiremockTests != null && !"".equals(wiremockTests)) {
            List<String> wmTests = Arrays.asList(wiremockTests.split(","));
            wmTests = wmTests.stream().map(t -> t.trim()).collect(Collectors.toList());
            String currentTest = iTestContext.getCurrentXmlTest().getName();
            isWiremockTest = wmTests.contains(currentTest);
        }
        return isWiremockTest;
    }

    /**
     * Starts a Wiremock server with specified configuration parameters.
     * @param bindAddress wiremock bind-address
     * @param wmRoot wiremock root directory that contains /mappings and /__files
     * @param wmPort wiremock exposed port
     */
    public void runNewServer(String bindAddress, String wmRoot, String wmPort) {
        WireMockConfiguration config = options()
            .bindAddress(bindAddress)
            .withRootDirectory(wmRoot);

        if (wmPort != null && !"".equals(wmPort)) {
            Integer wmPortInt = Integer.valueOf(wmPort);
            config.port(wmPortInt);
        }

        wmServer = new WireMockServer(config);
        wmServer.start();
    }
}
