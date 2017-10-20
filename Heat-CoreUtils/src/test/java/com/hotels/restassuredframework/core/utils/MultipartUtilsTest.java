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

import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.specification.MultiPartSpecification;



/**
 * Created on 28/08/17.
 *
 * @author mrascioni
 */
public class MultipartUtilsTest {

    @Test
    public void testSimpleValue() throws Exception {
        final List<Map<String, String>> parts = ImmutableList.of(
            ImmutableMap.of(
                TestCaseUtils.JSON_FIELD_MULTIPART_NAME, "simpleField",
                TestCaseUtils.JSON_FIELD_MULTIPART_VALUE, "simpleValue"
            )
        );

        final List<MultiPartSpecification> spec = MultipartUtils.convertToMultipart(parts);

        assertEquals(spec.size(), 1);
        assertEquals(spec.get(0).getContent(), "simpleValue");
        assertEquals(spec.get(0).getControlName(), "simpleField");
        assertNull(spec.get(0).getCharset());
        assertNull(spec.get(0).getFileName());
        assertNull(spec.get(0).getMimeType());
    }
    @Test
    public void testMultipleValues() throws Exception {
        final List<Map<String, String>> parts = ImmutableList.of(
            ImmutableMap.of(
                TestCaseUtils.JSON_FIELD_MULTIPART_NAME, "simpleField",
                TestCaseUtils.JSON_FIELD_MULTIPART_VALUE, "simpleValue"
            ),
            ImmutableMap.of(
                TestCaseUtils.JSON_FIELD_MULTIPART_NAME, "simpleField",
                TestCaseUtils.JSON_FIELD_MULTIPART_VALUE, "simpleValue"
            )
        );

        final List<MultiPartSpecification> spec = MultipartUtils.convertToMultipart(parts);

        assertEquals(spec.size(), 2);
    }

    @Test
    public void testFile() throws Exception {
        final List<Map<String, String>> parts = ImmutableList.of(
            ImmutableMap.of(
                TestCaseUtils.JSON_FIELD_MULTIPART_NAME, "upload",
                TestCaseUtils.JSON_FIELD_MULTIPART_FILE, "/files/multipart.txt",
                TestCaseUtils.JSON_FIELD_MULTIPART_CONTENT_TYPE, "test"
            )
        );

        final List<MultiPartSpecification> spec = MultipartUtils.convertToMultipart(parts);

        assertEquals(spec.size(), 1);
        assertEquals(spec.get(0).getContent(), MultipartUtils.resolve("/files/multipart.txt"));
        assertEquals(spec.get(0).getControlName(), "upload");
        assertEquals(spec.get(0).getFileName(), "multipart.txt");
        assertEquals(spec.get(0).getMimeType(), "test");
        assertNull(spec.get(0).getCharset());
    }
    @Test
    public void testFileWithAutomaticMimetype() throws Exception {
        final List<Map<String, String>> parts = ImmutableList.of(
            ImmutableMap.of(
                TestCaseUtils.JSON_FIELD_MULTIPART_NAME, "upload",
                TestCaseUtils.JSON_FIELD_MULTIPART_FILE, "/files/multipart.txt"
            )
        );

        final List<MultiPartSpecification> spec = MultipartUtils.convertToMultipart(parts);

        assertEquals(spec.size(), 1);
        assertEquals(spec.get(0).getContent(), MultipartUtils.resolve("/files/multipart.txt"));
        assertEquals(spec.get(0).getControlName(), "upload");
        assertEquals(spec.get(0).getFileName(), "multipart.txt");
        assertEquals(spec.get(0).getMimeType(), "text/plain");
        assertNull(spec.get(0).getCharset());
    }

    @Test
    public void testFileWithNoMimetype() throws Exception {
        final List<Map<String, String>> parts = ImmutableList.of(
            ImmutableMap.of(
                TestCaseUtils.JSON_FIELD_MULTIPART_NAME, "upload",
                TestCaseUtils.JSON_FIELD_MULTIPART_FILE, "/files/multipart.something"
            )
        );
        try {
            MultipartUtils.convertToMultipart(parts);
            fail("It should fail for missing mimetype");
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "'contentType' isn't specified and can't be automatically detected. Missing for part [upload]");
        }
    }

}
