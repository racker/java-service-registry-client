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

package com.rackspacecloud.client.service_registry;

import com.google.common.base.Joiner;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MethodOptions {
    public enum PaginationOptions {
        Limit("limit"),
        Marker("marker");
        
        private final String key;
        
        PaginationOptions(String key) {
            this.key = key;    
        }

        @Override
        public String toString() {
            return this.key;
        }
    }
    
    private Map<String, String> map = new HashMap<String, String>();

    public MethodOptions() {
    }

    public MethodOptions(Integer limit, String marker) {
        this.withLimit(limit).withMarker(marker);
    }
    
    public MethodOptions withOption(String key, String value) {
        // we don't allow nulls here.
        if (value == null) {
            map.remove(key);
        } else {
            map.put(key, value);
        }
        return this;
    }
    
    public MethodOptions withoutOption(String key) {
        map.remove(key);
        return this;
    }

    public MethodOptions withLimit(Integer limit) {
        return this.withOption(PaginationOptions.Limit.toString(), limit.toString());
    }

    public MethodOptions withMarker(String marker) {
        return this.withOption(PaginationOptions.Marker.toString(), marker);
    }

    public Integer getLimit() {
        return map.containsKey(PaginationOptions.Limit.toString()) ? 
                Integer.parseInt(get(PaginationOptions.Limit.toString())) : null;
    }

    public String getMarker() {
        return get(PaginationOptions.Marker.toString());
    }
    
    public Set<String> getNonNullKeys() {
        // they're all non-null. the method name just makes that obvious.
        return map.keySet();
    }
    
    public String get(String key) {
        return map.get(key);
    }

    @Override
    public String toString() {
        return String.format("[MethodOptions: %s", Joiner.on(",").join(map.entrySet()));
    }
}
