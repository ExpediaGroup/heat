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
package com.hotels.heat.core.utils;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Utils methods to work with Java {@link Map}.
 */
public class MapUtils {

    /**
     * Return a function to get a nullable element from a map, eg.
     *
     * <pre>
     *     Map<String, Object> map = ImmutableMap.of(
     *          "key", ImmutableMap.of(
     *              "nestedKey", true
     *          )
     *     );
     *
     *     MapUtils.get(map, "key")
     *        .map(Map.class::cast)
     *        .flatMap(MapUtils.get("nestedKey")
     *        .ifPresent(v -> System.out.println("the value is " + v))
     * </pre>
     * @param key
     * @return
     */
    public static Function<Map<String, ?>, Optional<?>> get(String key) {
        return m -> get(m, key);
    }

    /**
     * Retrieve a nullable key from a map as an Optional.
     * @param m
     * @param key
     * @return
     */
    public static Optional<?> get(Map<String, ?> m, String key) {
        return Optional.ofNullable(m.get(key));
    }
}
