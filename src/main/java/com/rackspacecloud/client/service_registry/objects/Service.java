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

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Service {
    private String id;

    @SerializedName("heartbeat_timeout")
    private Integer heartbeatTimeout;
    private Integer lastSeen = null;

    private List<String> tags = new ArrayList<String>();
    private Map<String, String> metadata = new HashMap<String, String>();

    public Service(String id, Integer heartbeatTimeout, List<String> tags,
                   Map<String, String> metadata) {
        this(id, heartbeatTimeout, null, tags, metadata);
    }

    public Service(String id, Integer heartbeatTimeout, Integer lastSeen,
                   List<String> tags, Map<String, String> metadata) {
        this.id = id;
        this.heartbeatTimeout = heartbeatTimeout;
        this.lastSeen = lastSeen;
        this.tags = tags;
        this.metadata = metadata;
    }

    public String getId() {
        return id;
    }

    public Integer getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    private Integer getLastSeen() {
        return lastSeen;
    }

    public List<String> getTags() {
        return tags;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }
}
