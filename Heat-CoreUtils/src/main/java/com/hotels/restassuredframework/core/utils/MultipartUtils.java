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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.jayway.restassured.builder.MultiPartSpecBuilder;
import com.jayway.restassured.specification.MultiPartSpecification;



/**
 * Utils methods to work with multipart specification.
 *
 * @author mrascioni
 */
public class MultipartUtils {
    public static List<MultiPartSpecification> convertToMultipart(List<Map<String, String>> parts) {
        return parts.stream()
            .map(part -> {
                final MultiPartSpecification specification;

                final String name = part.get(TestCaseUtils.JSON_FIELD_MULTIPART_NAME);
                Preconditions.checkNotNull(name, "'%s' for a part is mandatory", TestCaseUtils.JSON_FIELD_MULTIPART_NAME);

                if (part.containsKey(TestCaseUtils.JSON_FIELD_MULTIPART_FILE)) {

                    final String filepath = part.get(TestCaseUtils.JSON_FIELD_MULTIPART_FILE);
                    Preconditions.checkState(StringUtils.isNotBlank(filepath),
                            "'%s' is mandatory. Missing for part [%s]",
                            TestCaseUtils.JSON_FIELD_MULTIPART_FILE, name);

                    final File content = resolve(filepath);
                    Preconditions.checkNotNull(content.exists(), "Can't find file: ", filepath);

                    final String contentType = Optional.ofNullable(part.get(TestCaseUtils.JSON_FIELD_MULTIPART_CONTENT_TYPE))
                            .orElse(URLConnection.guessContentTypeFromName(filepath));

                    Preconditions.checkArgument(StringUtils.isNotBlank(contentType),
                            "'%s' isn't specified and can't be automatically detected. Missing for part [%s]",
                            TestCaseUtils.JSON_FIELD_MULTIPART_CONTENT_TYPE, name);

                    specification = new MultiPartSpecBuilder(content)
                        .fileName(content.getName())
                        .mimeType(contentType)
                        .controlName(name)
                        .build();
                } else {

                    final String value = part.get(TestCaseUtils.JSON_FIELD_MULTIPART_VALUE);
                    Preconditions.checkNotNull(StringUtils.isNotBlank(value),
                            "'%s' is mandatory. Missing for part [%s]",
                            TestCaseUtils.JSON_FIELD_MULTIPART_VALUE, name);

                    specification = new MultiPartSpecBuilder(value)
                        .controlName(name)
                        .build();
                }
                return specification;
            })
            .collect(Collectors.toList());
    }


    public static File resolve(String filepath) {
        try {
            final URI path = MultipartUtils.class.getResource(filepath).toURI();
            return new File(path);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
