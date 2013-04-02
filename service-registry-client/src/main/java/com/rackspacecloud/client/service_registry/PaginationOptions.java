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

public class PaginationOptions {
    private Integer limit;
    private String marker;

    public PaginationOptions() {
    }

    public PaginationOptions(Integer limit, String marker) {
        this.limit = limit;
        this.marker = marker;
    }

    public PaginationOptions withLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public PaginationOptions withMarker(String marker) {
        this.marker = marker;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public String getMarker() {
        return marker;
    }

    @Override
    public String toString() {
        return String.format("[PaginagionOptions limit=%s, marker=%s", this.limit,
                             this.marker);
    }
}
