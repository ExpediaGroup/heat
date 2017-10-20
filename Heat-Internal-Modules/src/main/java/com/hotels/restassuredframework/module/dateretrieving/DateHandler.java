package com.hotels.restassuredframework.module.dateretrieving;

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
 * @author adebiase
 * @author fcastaldi
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
