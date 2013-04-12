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
import com.rackspacecloud.client.service_registry.ClientResponse;
import com.rackspacecloud.client.service_registry.HeartBeater;
import com.rackspacecloud.client.service_registry.PaginationOptions;
import com.rackspacecloud.client.service_registry.ServiceCreateResponse;
import com.rackspacecloud.client.service_registry.containers.ServicesContainer;
import com.rackspacecloud.client.service_registry.objects.HeartbeatToken;
import com.rackspacecloud.client.service_registry.objects.Service;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ServicesClient extends BaseClient {
    private final AuthClient authClient;

    public ServicesClient(AuthClient authClient, String apiUrl) {
        super(authClient, apiUrl);
        this.authClient = authClient;
    }

    public Iterator<Service> list(PaginationOptions paginationOptions) throws Exception {
        return list(paginationOptions, null);
    }

    public Iterator<Service> list(PaginationOptions paginationOptions, String tag) throws Exception {
        Type type = new TypeToken<ServicesContainer>() {}.getType();
        Map<String, String> additionalOptions = new HashMap<String, String>();

        if (tag != null) {
            additionalOptions.put("tag", tag);
        }
        
        return this.getListIterator(Service.class, "/services", paginationOptions, additionalOptions, new HttpGet(), true, type);
    }

    public Service get(String id) throws Exception {
        ClientResponse response = this.performRequest("/services/" + id, null, new HttpGet(), true, Service.class);

        return (Service)response.getBody();
    }

    public ServiceCreateResponse create(String id, int heartbeatTimeout, List<String> tags, Map<String, String> metadata) throws Exception {
        Service service = new Service(id, heartbeatTimeout, tags, metadata);
        ClientResponse response = this.performRequestWithPayload("/services", null, new HttpPost(), service, true, HeartbeatToken.class);

        HeartbeatToken hbt = (HeartbeatToken)response.getBody();
        HeartBeater heartBeater = new HeartBeater(this.authClient, id, hbt.getToken(), service.getHeartbeatTimeout(), this.getApiUrl());

        return new ServiceCreateResponse(heartBeater, service, hbt.getToken());
    }

    public ServicesClient update(String id, List<String> tags, Map<String, String> metadata) throws Exception {
        Service service = new Service(null, null, tags, metadata);

        ClientResponse response = this.performRequestWithPayload("/services/" + id, null, new HttpPut(), service, true, null);
        return this;
    }

    public ServicesClient delete(String id) throws Exception {
        ClientResponse response = this.performRequest("/services/" + id, null, new HttpDelete());
        return this;
    }
}
