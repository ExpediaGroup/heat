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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.hotels.restassuredframework.core.utils.log.LoggingUtils;
import com.jayway.restassured.internal.http.Method;

/**
 * Format the HTTP request.
 */
public class HttpRequestFormatter {

    private TestRequest tcRequest;
    private LoggingUtils logUtils;

    /**
     * Constructor of HTTPRequestFormatter object.
     * This class is useful to print (output console) the curl of the request just made. It prints the output only if in DEBUG log level mode.
     * @param tcRequest objects that represent the test case that contains the HTTP request data
     * @param logUtils objects that contains test case information useful for logging
     */
    public HttpRequestFormatter(TestRequest tcRequest, LoggingUtils logUtils) {
        this.tcRequest = tcRequest;
        this.logUtils = logUtils;
    }

    /**
     * Generate the cURL command.
     * @return the cURL command as String
     */
    public String toCURL() {
        StringBuilder sb = new StringBuilder();
        sb.append("curl -kv");

        tcRequest.getHeadersParams()
            .forEach((k, v)
                -> sb.append(" -H '").append(k).append(": ").append(v).append("'"));


        Map<String, String> cookieParams = tcRequest.getCookieParams();
        if (cookieParams.size() > 0) {
            int cookieId = 0;
            sb.append(" --cookie '");
            for (Map.Entry<String, String> entry : cookieParams.entrySet()) {
                if (cookieId > 0) {
                    sb.append(";");
                }
                sb.append(entry.getKey()).append("=").append(entry.getValue());
                cookieId++;
            }
        }


        if (tcRequest.getHttpMethod() != null) {
            if (Method.GET.equals(tcRequest.getHttpMethod())) {
                sb.append(" -G");
            } else {
                sb.append(" -X ");
                sb.append(tcRequest.getHttpMethod());
            }
        }

        Map queryParams = tcRequest.getQueryParams();
        if (queryParams != null) {
            queryParams.forEach((key, value) -> {
                if (value instanceof String) {
                    String valueString = (String) value;
                    if (TestCaseUtils.JSON_FIELD_POST_BODY.equals(key)) {
                        sb.append(" -H \"Content-Type: application/json\""); // This is an assumption
                        sb.append(" -d '");
                        try {
                            sb.append(readPostBodyFromFile(valueString));
                        } catch (IOException e) {
                            logUtils.error("It's not possible to load body post data from: {}", valueString);
                            sb.append("");
                        }
                        sb.append("'");
                    } else {
                        sb.append(" -d '");
                        sb.append(key);
                        sb.append("=");
                        sb.append(encodeParamInCURL(valueString));
                        sb.append("'");
                    }
                } else {
                    if (TestCaseUtils.JSON_FIELD_MULTIPART_BODY.equals(key)) {
                        ((List<Map<String, String>>) value).forEach(part -> {
                            if (part.containsKey(TestCaseUtils.JSON_FIELD_MULTIPART_FILE)) {
                                final URL file = this.getClass().getResource(part.get(TestCaseUtils.JSON_FIELD_MULTIPART_FILE));
                                sb.append(" -F ")
                                    .append(part.get(TestCaseUtils.JSON_FIELD_MULTIPART_NAME))
                                    .append("=@")
                                    .append(file.getPath());

                            } else {
                                sb.append(" -F ")
                                    .append(part.get(TestCaseUtils.JSON_FIELD_MULTIPART_NAME))
                                    .append('=')
                                    .append(part.get(TestCaseUtils.JSON_FIELD_MULTIPART_VALUE));
                            }
                        });
                    } else {
                        ((List<?>) value).forEach(singleValue -> {
                            sb.append(" -d ");
                            sb.append(key);
                            sb.append("=");
                            sb.append(singleValue);
                        });
                    }
                }
            });
        }
        sb.append(" '");
        sb.append(tcRequest.getUrl());
        sb.append("'");
        return sb.toString();
    }

    /**
     * Method that returns tru if the path in input represent a file path (JSON, XML or TXT file).
     * @param key the string that could contain a file path
     * @return true if the input is a path
     */
    public static boolean isFilePath(String key) {
        boolean isFilePath = true;
        isFilePath &= key != null;
        isFilePath &= !key.contains(" ");
        isFilePath &= key.endsWith(".json") || key.endsWith(".xml") || key.endsWith(".txt");

        return isFilePath;
    }

    private String encodeParamInCURL(String value) {
        String encodedValue = value;
        try {
            encodedValue = URLEncoder.encode(encodedValue, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logUtils.error("Error during encoding of request value: {}", value);
        }
        return encodedValue;
    }

    /**
     * Method that read from file system (project resuources) the requested file that represent the post data.
     * @param postBodyFilePath the string that contain the file path
     * @return The post data read from file as string
     */
    public static String readPostBodyFromFile(String postBodyFilePath) throws IOException {
        InputStream is;
        String postBodyStr = "";
        if (isFilePath(postBodyFilePath)) {
            is = HttpRequestFormatter.class.getClassLoader().getResourceAsStream(postBodyFilePath);
            postBodyStr = IOUtils.toString(is);
        } else {
            postBodyStr = postBodyFilePath;
        }
        postBodyStr = postBodyStr.replaceAll("\n", "");
        postBodyStr = postBodyStr.replaceAll("\t", "");
        return postBodyStr;
    }
}
