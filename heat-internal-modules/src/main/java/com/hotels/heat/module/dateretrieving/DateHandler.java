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
package com.hotels.heat.module.dateretrieving;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is for generating valid dates and timestamps to use in the tests.
 */
public class DateHandler {
    private DateTimeFormatter dtfOut = DateTimeFormat.forPattern(DateHeatPlaceholderModule.TODAY_PLACEHOLDER_DEFAULT_PATTERN);
    private Logger logger = LoggerFactory.getLogger(DateHandler.class);
    private String testDetails = "";

    public DateHandler(String testDetails) {
        this.testDetails = testDetails;
    }

    /**
     * getFormattedDate takes a string input and return the formatted date.
     * @param inputDate is an input a string like ${TODAY} or ${TODAY+1}
     * @return formatted date as String
     */
    public String getFormattedDate(String inputDate) {
        String outputDate = "";
        DateTime dateToParse = getDateTime(inputDate);
        String patternForFormat = "\\$\\{" + DateHeatPlaceholderModule.TODAY_PLACEHOLDER_KEYWORD + ".*_(.*?)\\}";
        Pattern formatPattern = Pattern.compile(patternForFormat);
        Matcher formatMatcher = formatPattern.matcher(inputDate);
        String format = DateHeatPlaceholderModule.TODAY_PLACEHOLDER_DEFAULT_PATTERN;
        if (formatMatcher.find()) {
            format = formatMatcher.group(1);
        }
        dtfOut = DateTimeFormat.forPattern(format);
        outputDate = dtfOut.print(dateToParse);
        return outputDate;
    }

    /**
     * This method takes a string input and returns the formatted date.
     * @param inputDate is an input string like ${TODAY} or ${TODAY+1}
     * @return formatted date as String
     */
    public long getLongDate(String inputDate) {
        long outputDate = 0L;
        outputDate = getDateTime(inputDate).withHourOfDay(0).withMillisOfDay(0).withMinuteOfHour(0).getMillis();
        return outputDate;
    }

    private DateTime getDateTime(String inputDate) {
        DateTime todayDate = new DateTime(DateTimeZone.UTC);
        int increaseDays = 0;
        DateTime newDate;

        try {
            String increaseString = "+0";
            //String increaseString = inputDate.substring(TODAY_PLACEHOLDER.length(), inputDate.length() - 1); // // example: increaseString = "+1"

            String patternForToday = "(\\+[0-9]*|\\-[0-9]*)"; //example: increaseString = "+1"
            Pattern todayPattern = Pattern.compile(patternForToday);
            Matcher todayMatcher = todayPattern.matcher(inputDate);
            if (todayMatcher.find()) {
               // there is an increment
                increaseString = todayMatcher.group(0);
                logger.debug("{} DateHandler - getDateTime --> there is an increase date = '{}'", testDetails, increaseString);
            }
            increaseDays = Integer.parseInt(increaseString.substring(1));
            if (increaseString.startsWith("+")) {
                newDate = todayDate.plusDays(increaseDays);
            } else if (increaseString.startsWith("-")) {
                newDate = todayDate.plusDays(increaseDays * (-1));
            } else {
                newDate = todayDate;
            }
        } catch (Exception oEx) {
            logger.error("{} DateHandler - getDateTime >> exception: '{}'", testDetails, oEx.getLocalizedMessage());
            newDate = todayDate;
        }
        return newDate;
    }

    public String changeDatesPlaceholders(boolean isOutputString, String inputStr) {
        // if we have to process a String object, we can simply check if the string contains the required placeholder
        String outputStr = inputStr;
        try {
            logger.trace("{} PlaceholderHandler - changeDatesPlaceholders --> inputStr = '{}'", testDetails, inputStr);
            if (inputStr.contains(DateHeatPlaceholderModule.TODAY_PLACEHOLDER)) {
                int i = 0;
                while (outputStr.contains(DateHeatPlaceholderModule.TODAY_PLACEHOLDER)) {
                    ++i;
                    int beginninChar = outputStr.indexOf(DateHeatPlaceholderModule.TODAY_PLACEHOLDER);
                    int endChar = outputStr.substring(beginninChar).indexOf("}");
                    // 'strToReplace' is the string of the entire placeholder, for example '${TODAY+200_YYYY-MM-dd}'
                    String strToReplace = outputStr.substring(beginninChar, endChar + beginninChar + 1);
                    if (isOutputString) {
                        if (strToReplace.contains("_")) {
                            logger.trace("{} PlaceholderHandler - changeDatesPlaceholders --> required formatted date", testDetails);
                            outputStr = outputStr.replace(strToReplace, String.valueOf(getFormattedDate(strToReplace)));
                        } else {
                            logger.trace("{} PlaceholderHandler - changeDatesPlaceholders --> required Long date", testDetails);
                            outputStr = outputStr.replace(strToReplace, String.valueOf(getLongDate(strToReplace)));
                        }
                    } else {
                        outputStr = outputStr.replace("" + strToReplace + "", String.valueOf(getLongDate(strToReplace)));
                    }
                }
            }
        } catch (Exception oEx) {
            logger.error("{} PlaceholderHandler - changeDatesPlaceholders --> Exception = '{}'", testDetails, oEx.getLocalizedMessage());
            throw new InternalError(testDetails + "PlaceholderHandler - changeDatesPlaceholders --> Exception = '" + oEx.getLocalizedMessage() + "'");
        }
        return outputStr;
    }






}
