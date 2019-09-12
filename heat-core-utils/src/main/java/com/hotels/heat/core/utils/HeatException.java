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
package com.hotels.heat.core.utils;

/**
 * Specific exception for heat test framework.
 */
public class HeatException extends Error {

    public HeatException(String exceptionMessage) {
        super(exceptionMessage);
    }
    public HeatException(String exceptionMessage, Throwable cause) {
        super(exceptionMessage, cause);
    }

    public HeatException(Class classObject, String exceptionMessage) {
        super(String.format( "%s :: %s >> %s",
                classObject.getCanonicalName(),
                classObject.getEnclosingMethod(),
                exceptionMessage));
    }

    public HeatException(Class classObject, Exception oEx) {
        super(String.format( "%s :: %s >> Exception raised %n Exception class: %s %n with message '%s'",
                classObject.getCanonicalName(),
                classObject.getEnclosingMethod(),
                oEx.getClass(),
                oEx.getLocalizedMessage()));
    }
}
