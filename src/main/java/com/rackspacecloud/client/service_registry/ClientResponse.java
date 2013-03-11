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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rackspacecloud.client.service_registry.exceptions.ValidationException;
import com.rackspacecloud.client.service_registry.objects.Event;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.reflect.Type;

public class ClientResponse {
    private final HttpResponse response;
    private final boolean parseAsJson;
    private final Type responseType;
    private final Object body;

    public ClientResponse(HttpResponse response) throws IOException, ValidationException {
        this(response, false, null);
    }

    public ClientResponse(HttpResponse response, boolean parseAsJson, Type responseType) throws IOException, ValidationException {
        this.response = response;
        this.parseAsJson = parseAsJson;
        this.responseType = responseType;

        this.body = processResponse();
    }

    private Object processResponse() throws IOException, ValidationException {
        Object data = null;
        HttpEntity entity = this.response.getEntity();
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == 400) {
            data = EntityUtils.toString(entity);

            ValidationException ex = new Gson().fromJson(data.toString(), ValidationException.class);
            throw ex;
        }

        if (entity != null) {
            data = EntityUtils.toString(entity);

            if (this.parseAsJson && this.responseType != null) {
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.registerTypeAdapter(Event.class, new Event()).create();
                data = gson.fromJson(data.toString(), this.responseType);
            }
        }
        
        return data;
    }

    public Object getBody() {
        return this.body;
    }

    public Header[] getHeader(String name) {
        return this.response.getHeaders(name);
    }
    
    public int getStatusCode() {
        return this.response.getStatusLine().getStatusCode();
    }
    
    public String getStatusPhrase() {
        return this.response.getStatusLine().getReasonPhrase();
    }
}
