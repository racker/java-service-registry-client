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
import com.rackspacecloud.client.service_registry.PaginationOptions;
import com.rackspacecloud.client.service_registry.containers.ServicesContainer;
import com.rackspacecloud.client.service_registry.objects.Service;
import com.rackspacecloud.client.service_registry.objects.Session;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicNameValuePair;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServicesClient extends BaseClient {
    public ServicesClient(AuthClient authClient) {
        super(authClient);
    }

    public List<Service> list(PaginationOptions paginationOptions) throws Exception {
        return list(paginationOptions, null);
    }

    public List<Service> list(PaginationOptions paginationOptions, String tag) throws Exception {
        Type type = new TypeToken<ServicesContainer>() {}.getType();
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        if (tag != null) {
            params.add(new BasicNameValuePair("tag", tag));
        }

        ClientResponse response = this.performListRequest(paginationOptions, "/services", params, new HttpGet(), true, type);

        ServicesContainer container = (ServicesContainer)response.getBody();
        return container.getValues();
    }

    public Service get(String id) throws Exception {
        ClientResponse response = this.performRequest("/services/" + id, null, new HttpGet(), true, Service.class);

        return (Service)response.getBody();
    }

    public Service create(String id, String sessionId, List<String> tags, Map<String, String> metadata) throws Exception {
        Service service = new Service(id, sessionId, tags, metadata);
        ClientResponse response = this.performRequestWithPayload("/services", null, new HttpPost(), service, false, null);

        return new Service(id, sessionId, tags, metadata);
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
