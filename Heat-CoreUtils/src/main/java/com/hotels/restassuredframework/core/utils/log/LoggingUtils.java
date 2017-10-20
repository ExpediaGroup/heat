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
package com.hotels.restassuredframework.core.utils.log;

import java.lang.reflect.Method;

import org.slf4j.LoggerFactory;
import org.testng.ITestContext;


import com.hotels.restassuredframework.core.runner.TestBaseRunner;
import com.hotels.restassuredframework.core.specificexception.HeatException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;


/**
 * This class contains utilities for logging.
 */
public class LoggingUtils {

    public static final String LOG_LEVEL_INFO = "info";
    public static final String LOG_LEVEL_ERROR = "error";
    public static final String LOG_LEVEL_WARN = "warn";
    public static final String LOG_LEVEL_ALL = "all";
    public static final String LOG_LEVEL_TRACE_LOG = "trace";
    public static final String LOG_LEVEL_DEBUG = "debug";

    public static final int THREAD_COUNT_EXCEPTION_LEVEL = 4;
    public static final int THREAD_COUNT_LOG_LEVEL = THREAD_COUNT_EXCEPTION_LEVEL - 1;


    private ITestContext context;
    private String testID;

    private String testCaseDetails;
    private Integer flowStep;
    private String logLevel;

    public LoggingUtils() {
        this.testCaseDetails = "";
        this.setLogLevel();
        this.logLevel = LOG_LEVEL_INFO;
    }

    /**
     * This method sets the log level (logback).
     */
    public void setLogLevel() {
        logLevel = System.getProperty("logLevel", LOG_LEVEL_INFO);
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        Level logLevelSetting;
        switch (logLevel.toLowerCase()) {
        case LOG_LEVEL_ERROR:
            logLevelSetting = Level.ERROR;
            break;
        case LOG_LEVEL_WARN:
            logLevelSetting = Level.WARN;
            break;
        case LOG_LEVEL_ALL:
            logLevelSetting = Level.ALL;
            break;
        case LOG_LEVEL_TRACE_LOG:
            logLevelSetting = Level.TRACE;
            break;
        case LOG_LEVEL_INFO:
            logLevelSetting = Level.INFO;
            break;
        case LOG_LEVEL_DEBUG:
            logLevelSetting = Level.DEBUG;
            break;
        default:
            logLevelSetting = Level.INFO;
            break;
        }
        root.setLevel(logLevelSetting);
    }

    public void setTestContext(ITestContext context) {
        this.context = context;
    }

    public void setTestCaseId(String testID) {
        this.testID = testID;
    }



    public void setFlowStep(Integer flowStepInput) {
        flowStep = flowStepInput;
    }

    public String getTestCaseDetails() {
        testCaseDetails = "[" + (context != null ? context.getName() : "") + "] ";
        if (testID != null) {
            testCaseDetails = "[" + context.getName() + TestBaseRunner.TESTCASE_ID_SEPARATOR + testID + "]";
        }

        testCaseDetails += " ";
        if (flowStep != null) {
            testCaseDetails += "[FLOW STEP #" + flowStep + "] ";
        }
        return testCaseDetails;
    }

    private Class getCurrentClass(int group) {
        return Thread.currentThread().getStackTrace()[group].getClass();
    }

    private String getCurrentClassName(int group) {
        return Thread.currentThread().getStackTrace()[group].getClassName();
    }

    private String getCurrentMethodName(int group) {
        return Thread.currentThread().getStackTrace()[group].getMethodName();
    }

    public String getExceptionDetails() {
        return getExceptionDetails(THREAD_COUNT_EXCEPTION_LEVEL);
    }

    public String getExceptionDetails(int group) {
        return testCaseDetails + getCurrentClassName(group) + "::" + getCurrentMethodName(group) + " -- ";
    }

    private String getTestCaseLogDetails() {
        String details = testCaseDetails;
        if (logLevel.equals(LOG_LEVEL_TRACE_LOG)) {
            details = details + getCurrentClassName(THREAD_COUNT_LOG_LEVEL)
                    + "::" + getCurrentMethodName(THREAD_COUNT_LOG_LEVEL);
        }
        return details;
    }

    public void info(String message) {
        log(LOG_LEVEL_INFO, message);
    }

    public void debug(String message) {
        log(LOG_LEVEL_DEBUG, message);
    }

    public void trace(String message) {
        log(LOG_LEVEL_TRACE_LOG, message);
    }

    public void error(String message) {
        log(LOG_LEVEL_ERROR, message);
    }

    public void warning(String message) {
        log(LOG_LEVEL_WARN, message);
    }

    private void log(String mode, String message) {
        try {
            org.slf4j.Logger logger = LoggerFactory.getLogger(getCurrentClass(THREAD_COUNT_LOG_LEVEL));
            Class[] cArg = new Class[1];
            cArg[0] = String.class;
            Method loggerMethod = logger.getClass().getMethod(mode, cArg);
            loggerMethod.invoke(logger, getTestCaseLogDetails() + " -- " + message);
        } catch (Exception oEx) {
            throw new HeatException(getExceptionDetails() + oEx.getClass()
                    + " / cause: '" + oEx.getCause() + "' / message: '" + oEx.getLocalizedMessage() + "'");
        }
    }

    public void info(String message, Object... params) {
        log(LOG_LEVEL_INFO, message, params);
    }

    public void debug(String message, Object... params) {
        log(LOG_LEVEL_DEBUG, message, params);
    }

    public void trace(String message, Object... params) {
        log(LOG_LEVEL_TRACE_LOG, message, params);
    }

    public void error(String message, Object... params) {
        log(LOG_LEVEL_ERROR, message, params);
    }

    public void warning(String message, Object... params) {
        log(LOG_LEVEL_WARN, message, params);
    }

    private void log(String mode, String message, Object... params) {
        try {
            org.slf4j.Logger logger = LoggerFactory.getLogger(getCurrentClass(THREAD_COUNT_LOG_LEVEL));
            Class[] cArg = new Class[2];
            cArg[0] = String.class;
            cArg[1] = Object[].class;
            Method loggerMethod = logger.getClass().getMethod(mode, cArg);
            loggerMethod.invoke(logger, getTestCaseLogDetails() + " -- " + message, params);
        } catch (Exception oEx) {
            throw new HeatException(getExceptionDetails() + oEx.getClass()
                    + " / cause: '" + oEx.getCause() + "' / message: '" + oEx.getLocalizedMessage() + "'");
        }
    }



}
