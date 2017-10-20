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
package com.hotels.restassuredframework.core.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.hotels.restassuredframework.core.specificexception.HeatException;
import com.hotels.restassuredframework.core.utils.log.LoggingUtils;


/**
 * Unit Tests for {@link DataExtractionSupport}.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(DataExtractionSupport.class)
public class DataExtractionSupportTest {

    @Mock
    private LoggingUtils logUtils;

    @InjectMocks
    private DataExtractionSupport underTest;

    @BeforeMethod
    public void setUp() {
        logUtils = new LoggingUtils();
    }

    @Test
    public void testSimpleString() {
        underTest = new DataExtractionSupport(logUtils);
        String processedStr = underTest.process(getSimpleStringObject(), null, null);
        Assert.assertEquals(processedStr, getSimpleStringObject());
    }

    @Test(expectedExceptions = { HeatException.class }, expectedExceptionsMessageRegExp = ".* actualValue/expectedValue belongs to class .* not supported")
    public void testNotSupportedObject() throws Exception {
        underTest = new DataExtractionSupport(logUtils);
        underTest.process(getNotSupportedClassObject(), null, null);
    }

    @Test
    public void testRegularExtractionMapObject() {
        underTest = new DataExtractionSupport(logUtils);
        String processedStr = underTest.process(getRegexpExtractionMapObject(), null, null);
        Assert.assertEquals(processedStr, "123");
    }

    @Test
    public void testOccurrenceExtractionMapObject() {
        underTest = new DataExtractionSupport(logUtils);
        String processedStr = underTest.process(getOccurrenceOfMapObject(), null, null);
        Assert.assertEquals(processedStr, "2");
    }

    @Test(expectedExceptions = { HeatException.class }, expectedExceptionsMessageRegExp = ".* configuration .* not supported")
    public void testNotSupportedMapObject() throws Exception {
        underTest = new DataExtractionSupport(logUtils);
        underTest.process(getNotSupportedMapObject(), null, null);
    }

    private String getSimpleStringObject() {
        return "pippo";
    }

    private List getNotSupportedClassObject() {
        List<String> list = new ArrayList<>();
        list.add("pippo");
        return list;
    }

    private Map getRegexpExtractionMapObject() {
        Map<String, Object> map = new HashMap<>();
        map.put(DataExtractionSupport.REGEXP_JSON_ELEMENT, "PIPPO_(.*?)_PLUTO");
        map.put(DataExtractionSupport.STRING_TO_PARSE_JSON_ELEMENT, "PIPPO_123_PLUTO");
        return map;
    }

    private Map getOccurrenceOfMapObject() {
        Map<String, Object> map = new HashMap<>();
        map.put(DataExtractionSupport.OCCURRENCE_JSON_ELEMENT, "PIPPO");
        map.put(DataExtractionSupport.STRING_TO_PARSE_JSON_ELEMENT, "PIPPO_PLUTO_PIPPO_MICKEY");
        return map;
    }

    private Map getNotSupportedMapObject() {
        Map<String, Object> map = new HashMap<>();
        map.put("PIPPO", "hello!");
        return map;
    }

}
