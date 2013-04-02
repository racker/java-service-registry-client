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

import com.google.common.collect.AbstractIterator;
import com.google.gson.reflect.TypeToken;
import com.rackspacecloud.client.service_registry.ClientResponse;
import com.rackspacecloud.client.service_registry.HeartBeater;
import com.rackspacecloud.client.service_registry.PaginationOptions;
import com.rackspacecloud.client.service_registry.ServiceCreateResponse;
import com.rackspacecloud.client.service_registry.containers.Container;
import com.rackspacecloud.client.service_registry.containers.ServicesContainer;
import com.rackspacecloud.client.service_registry.objects.HeartbeatToken;
import com.rackspacecloud.client.service_registry.objects.Service;
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
    private final AuthClient authClient;

    public ServicesClient(AuthClient authClient, String apiUrl) {
        super(authClient, apiUrl);
        this.authClient = authClient;
    }

    public AbstractIterator<Service> list(PaginationOptions paginationOptions) throws Exception {
        return list(paginationOptions, null);
    }

    public AbstractIterator<Service> list(PaginationOptions paginationOptions, String tag) throws Exception {
        Type type = new TypeToken<ServicesContainer>() {}.getType();

        List<NameValuePair> params = new ArrayList<NameValuePair>();

        if (tag != null) {
            params.add(new BasicNameValuePair("tag", tag));
        }

        if (paginationOptions == null) {
            paginationOptions = new PaginationOptions();
        }

        //ClientResponse response = this.performListRequest(paginationOptions, "/services", params, new HttpGet(), true, type);
        //
        //ServicesContainer container = (ServicesContainer)response.getBody();
        //return container.getValues();

        AbstractIterator<Service> iterator = this.getCollectionIterator(this, paginationOptions.getMarker(),
                                                                        paginationOptions.getLimit(), "/services", params);
        return iterator;
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

    // TODO: Move into BaseClient and make it generic
    protected AbstractIterator<Service> getCollectionIterator(final BaseClient client,
                                                              final String startMarker,
                                                              final Integer pageSize,
                                                              final String uriPath,
                                                              final List<NameValuePair> baseParams) {


        return new AbstractIterator<Service>() {
            private List<Service> results = new ArrayList<Service>();
            private Integer resultsOffset = 0;
            private Integer page = 0;
            private String nextMarker = null;
            private boolean exhausted = false;

            @Override
            protected Service computeNext() {
                Type type = new TypeToken<ServicesContainer>() {}.getType();
                ClientResponse response;

                PaginationOptions paginationOptions = new PaginationOptions(pageSize, startMarker);
                List<NameValuePair> params = new ArrayList<NameValuePair>(baseParams);

                // Emit values we have already retrieved
                if ((this.results.size() > 0) && (this.resultsOffset < this.results.size())) {
                    return this.results.get(this.resultsOffset++);
                }

                if (exhausted) {
                    // No more data, iterator has been exhausted
                    return endOfData();
                }

                if (nextMarker != null) {
                    paginationOptions.withMarker(nextMarker);
                }

                try {
                    response = client.performListRequest(paginationOptions, uriPath, params, new HttpGet(), true, type);
                }
                catch (Exception ex) {
                    return endOfData();
                }

                ServicesContainer container = (ServicesContainer)response.getBody();
                List<Service> values = container.getValues();

                if (values.size() == 0) {
                    // No results
                    exhausted = true;
                    return endOfData();
                }

                this.results.addAll(values);

                if (container.getNextMarker() != null) {
                    // There is more data
                    nextMarker = container.getNextMarker();
                    page++;
                }
                else {
                    exhausted = true;
                }

                return this.results.get(this.resultsOffset++);
            }
        };
    }
}