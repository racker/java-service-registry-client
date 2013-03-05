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

package com.rackspacecloud.client.service_registry.clients;

import com.google.gson.reflect.TypeToken;
import com.rackspacecloud.client.service_registry.*;
import com.rackspacecloud.client.service_registry.containers.SessionsContainer;
import com.rackspacecloud.client.service_registry.objects.HeartbeatToken;
import com.rackspacecloud.client.service_registry.objects.Session;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class SessionsClient extends BaseClient {
    private final AuthClient authClient;
    
    public SessionsClient(AuthClient authClient) {
        super(authClient);
        this.authClient = authClient;
    }

    public List<Session> list(PaginationOptions paginationOptions) throws Exception {
        Type type = new TypeToken<SessionsContainer>() {}.getType();
        ClientResponse response = this.performListRequest(paginationOptions, "/sessions", null, new HttpGet(), true, type);

        SessionsContainer container = (SessionsContainer)response.getBody();
        return container.getValues();
    }

    public Session get(String id) throws Exception {
        ClientResponse response = this.performRequest("/sessions/" + id, null, new HttpGet(), true, Session.class);
        return (Session)response.getBody();
    }

    public SessionCreateResponse create(int heartbeatTimeout, Map<String, String> metadata) throws Exception {
        Session session = new Session(null, heartbeatTimeout, null, metadata);
        ClientResponse response = this.performRequestWithPayload("/sessions", null, new HttpPost(), session, true, HeartbeatToken.class);

        String id = Utils.getIdFromLocationHeader(response.getHeader("Location")[0].getValue());
        HeartbeatToken hbt = (HeartbeatToken)response.getBody();

        HeartBeater heartBeater = new HeartBeater(this.authClient, session.getId(), hbt.getToken(), session.getHeartbeatTimeout());
        return new SessionCreateResponse(heartBeater, new Session(id, heartbeatTimeout, null, metadata), hbt.getToken());
    }

    public SessionsClient update(String id, int heartbeatTimeout, Map<String, String> metadata) throws Exception {
        Session session = new Session(null, heartbeatTimeout, null, metadata);
        ClientResponse response = this.performRequestWithPayload("/sessions/" + id, null, new HttpPut(), session, true, null);
        return this;
    }

    public String heartbeat(String id, String token) throws Exception {
        HeartbeatToken ht = new HeartbeatToken(token);
        ClientResponse response = this.performRequestWithPayload("/sessions/" + id + "/heartbeat", null, new HttpPost(), ht, true, HeartbeatToken.class);
        return ((HeartbeatToken)response.getBody()).getToken();
    }
}
