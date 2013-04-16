/**
 *  Copyright 2012 Rackspace
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.rackspacecloud.client.service_registry.containers;

import java.util.Map;

public abstract class Container {
    private Map<String, Object> metadata;

    public Integer getCount() {
        return Integer.parseInt(this.metadata.get("count").toString());
    }

    public Integer getLimit() {
        return Integer.parseInt(this.metadata.get("limit").toString());
    }

    public String getMarker() {
        return stringOrNull(this.metadata.get("marker"));
    }

    public String getNextMarker() {
        return stringOrNull(this.metadata.get("next_marker"));
    }

    private Map<String, Object> getMetadata() {
        return this.metadata;
    }
    
    private static String stringOrNull(Object o) {
        return o == null ? null : o.toString();
    }
}
