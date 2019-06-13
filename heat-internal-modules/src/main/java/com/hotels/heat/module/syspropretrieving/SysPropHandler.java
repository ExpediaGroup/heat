/**
 * Copyright (C) 2015-2019 Expedia, Inc.
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
package com.hotels.heat.module.syspropretrieving;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processing of system property retrieving placeholder.
 */
public class SysPropHandler {
    private static final String NO_PARAMS_FOUND = "NO_PARAMS_FOUND";
    private static final String NO_DEFAULT_FOUND = "NO_DEFAULT_FOUND";
    private static final String PROPERTIES_SEPARATOR = ",";
    private Logger logger = LoggerFactory.getLogger(SysPropHandler.class);
    private String testDetails = "";

    public SysPropHandler(String testDetails) {
        this.testDetails = testDetails;
    }

    /**
     * Method to process only the system property placeholder.
     * @param inputString is the complete string, not only the placeholder
     * @return the processed placeholder
     */
    private String getSysProperty(String inputString) {
        String processedPlaceholder = inputString;
        String patternForFormat = ".*?\\$\\{" + SysPropHeatPlaceholderModule.SYS_PROP_PLACEHOLDER_KEYWORD + "\\[(.*?)\\]\\}.*?";
        Pattern formatPattern = Pattern.compile(patternForFormat);
        Matcher formatMatcher = formatPattern.matcher(inputString);
        String placeholderParams = NO_PARAMS_FOUND;
        if (formatMatcher.find()) {
            placeholderParams = formatMatcher.group(1);
        }
        if (!NO_PARAMS_FOUND.equals(placeholderParams)) {
            String[] params = placeholderParams.split(PROPERTIES_SEPARATOR);
            String sysPropName = params[0];
            if (!"".equals(sysPropName)) {
                String defaultSysPropValue = NO_DEFAULT_FOUND;
                if (params.length > 1) {
                    defaultSysPropValue = params[1];
                }
                if (System.getProperty(sysPropName) != null) {
                    // the system property has been set
                    processedPlaceholder = System.getProperty(sysPropName);
                } else {
                    if (!NO_DEFAULT_FOUND.equals(defaultSysPropValue)) {
                        processedPlaceholder = defaultSysPropValue;
                    }
                }
            }

        }
        return processedPlaceholder;
    }

    /**
     * Method to process the string containing the system property placeholder.
     * @param inputString is the complete string, not only the placeholder
     * @return the processed string
     */
    public String processString(String inputString) {
        String outputString = inputString;
        try {
            if (inputString.contains(SysPropHeatPlaceholderModule.SYS_PROP_PLACEHOLDER)) {
                String patternForFormat = ".*?(\\$\\{" + SysPropHeatPlaceholderModule.SYS_PROP_PLACEHOLDER_KEYWORD + ".*?\\}).*?";
                String placeholder = processRegex(patternForFormat, inputString);

                // I am going to process only the placeholder
                String placeholderProcessed = getSysProperty(placeholder);

                // I am going to process the entire string
                String patternForFormatBefore = "(.*?)\\$\\{" + SysPropHeatPlaceholderModule.SYS_PROP_PLACEHOLDER_KEYWORD + ".*?\\}.*?";
                String outputStrBefore = processRegex(patternForFormatBefore, inputString);

                String patternForFormatAfter = ".*?\\$\\{" + SysPropHeatPlaceholderModule.SYS_PROP_PLACEHOLDER_KEYWORD + ".*?\\}(.*)";
                String outputStrAfter = processRegex(patternForFormatAfter, inputString);

                outputString = outputStrBefore + placeholderProcessed + outputStrAfter;

            }
        } catch (Exception oEx) {
            logger.error("{} SysPropHandler - processString --> Exception occurred '{}' / description '{}'", testDetails, oEx.getClass(), oEx.getLocalizedMessage());
        }
        return outputString;
    }

    private String processRegex(String regex, String stringToProcess) {
        String outputStr = stringToProcess;
        Pattern formatPattern = Pattern.compile(regex);
        Matcher formatMatcher = formatPattern.matcher(stringToProcess);
        if (formatMatcher.find()) {
            outputStr = formatMatcher.group(1);
        }
        return outputStr;
    }




}
