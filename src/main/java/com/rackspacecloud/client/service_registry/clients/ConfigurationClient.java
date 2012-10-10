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

import com.rackspacecloud.client.service_registry.ClientResponse;
import com.rackspacecloud.client.service_registry.objects.ConfigurationValue;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;

public class ConfigurationClient extends BaseClient {
    public ConfigurationClient(AuthClient authClient) {
        super(authClient);
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
