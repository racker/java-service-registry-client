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

import java.util.Map;

public class Session  {
    private String id;
    private Integer lastSeen = null;
    @SerializedName("heartbeat_timeout")
    private Integer heartbeatTimeout;
    private Map<String, String> metadata;

    public Session(String id, Integer heartbeatTimeout, Integer lastSeen, Map<String, String> metadata) {
        this.id = id;
        this.heartbeatTimeout = heartbeatTimeout;
        this.lastSeen = lastSeen;
        this.metadata = metadata;
    }

    public String getId() {
        return id;
    }

    public Integer getLastSeen() {
        return lastSeen;
    }

    public Integer getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }
}
