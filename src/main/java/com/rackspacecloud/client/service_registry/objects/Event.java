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

package com.rackspacecloud.client.service_registry.objects;

import java.util.HashMap;

public class Event {
    private String id;
    private Integer timestamp;
    private String type;
    private HashMap<String, String> payload = new HashMap<String, String>();

    public Event(String id, Integer timestamp, String type, HashMap<String, String> payload) {
        this.id = id;
        this.timestamp = timestamp;
        this.type = type;
        this.payload = payload;
    }
}