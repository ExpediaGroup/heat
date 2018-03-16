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
package com.hotels.heat.module.wiremocksupport;

import com.jayway.restassured.internal.http.Method;

public enum WiremockAction {
    UNKNOWN,
    REQUESTS("requests", "/__admin/requests", Method.GET),
    RESET("reset", "/__admin/reset", Method.POST);

    private String actionName;
    private String actionSubpath;
    private Method actionHttpMethod;

    private WiremockAction(String actionName, String actionSubpath, Method actionHttpMethod) {
        this.actionName = actionName;
        this.actionSubpath = actionSubpath;
        this.actionHttpMethod = actionHttpMethod;
    }

    private WiremockAction(){

    }

    public String getActionName() {
        return actionName;
    }

    public String getActionSubpath() {
        return actionSubpath;
    }

    public Method getActionHttpMethod() {
        return actionHttpMethod;
    }

    public static WiremockAction fromString(String text) {
        for (WiremockAction a : WiremockAction.values()) {
            if (a.getActionName().equalsIgnoreCase(text)) {
                return a;
            }
        }
        return null;
    }
}
