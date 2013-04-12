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
import com.rackspacecloud.client.service_registry.containers.ConfigurationValuesContainer;
import com.rackspacecloud.client.service_registry.objects.ConfigurationValue;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ConfigurationClient extends BaseClient {
    public ConfigurationClient(AuthClient authClient, String apiUrl) {
        super(authClient, apiUrl);
    }

    public Iterator<ConfigurationValue> list(PaginationOptions paginationOptions) throws Exception {
        return list(paginationOptions, null);
    }

    public Iterator<ConfigurationValue> list(PaginationOptions paginationOptions, String namespace) throws Exception {
        String url = "/configuration";

        Type type = new TypeToken<ConfigurationValuesContainer>() {}.getType();

        if (namespace != null) {
            // Make sure leading and trailing forward slashes are present

            if (!namespace.startsWith("/"))  {
                url += "/";
            }

            url += namespace;

            if (!namespace.endsWith("/")) {
                url += "/";
            }
        }

        return this.getListIterator(ConfigurationValue.class, url, paginationOptions, new HashMap<String, String>(), new HttpGet(), true, type);
    }

    public ConfigurationValue get(String id) throws Exception {
        ClientResponse response = this.performRequest("/configuration/" + id, null, new HttpGet(), true, ConfigurationValue.class);

        return (ConfigurationValue)response.getBody();
    }

    public ConfigurationClient set(String id, String value) throws Exception {
        ConfigurationValue cv = new ConfigurationValue(null, value);
        this.performRequestWithPayload("/configuration/"+ id, null, new HttpPut(), cv, false, null);
        return this;
    }
}
