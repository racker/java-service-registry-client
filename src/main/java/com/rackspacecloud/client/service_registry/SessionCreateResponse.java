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

import com.rackspacecloud.client.service_registry.objects.Session;

public class SessionCreateResponse {
    private final Session session;
    private final String token;
    private final HeartBeater heartbeater;

    public SessionCreateResponse(HeartBeater heartbeater, Session session, String token) {
        this.session = session;
        this.token = token;
        this.heartbeater = heartbeater;
    }

    public Session getSession() {
        return session;
    }

    public String getToken() {
        return token;
    }

    public HeartBeater getHeartbeater() {
        return heartbeater;
    }
}
