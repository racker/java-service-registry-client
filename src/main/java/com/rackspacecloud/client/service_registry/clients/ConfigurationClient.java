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
import com.rackspacecloud.client.service_registry.containers.ConfigurationValuesContainer;
import com.rackspacecloud.client.service_registry.containers.ServicesContainer;
import com.rackspacecloud.client.service_registry.objects.ConfigurationValue;
import com.rackspacecloud.client.service_registry.objects.Service;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicNameValuePair;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConfigurationClient extends BaseClient {
    public ConfigurationClient(AuthClient authClient) {
        super(authClient);
    }

    public ArrayList<ConfigurationValue> list(HashMap<String, String> options) throws Exception {
        Type type = new TypeToken<ConfigurationValuesContainer>() {}.getType();
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        ClientResponse response = this.performRequest("/configuration", params, new HttpGet(), true, type);

        ConfigurationValuesContainer container = (ConfigurationValuesContainer)response.getBody();
        return (ArrayList<ConfigurationValue>)container.getValues();
    }

    public ConfigurationValue get(String id) throws Exception {
        ClientResponse response = this.performRequest("/configuration/" + id, null, new HttpGet(), true, ConfigurationValue.class);

        return (ConfigurationValue)response.getBody();
    }

    public void set(String id, String value) throws Exception {
        ConfigurationValue cv = new ConfigurationValue(null, value);
        this.performRequestWithPayload("/configuration/"+ id, null, new HttpPut(), cv, false, null);
    }
}
