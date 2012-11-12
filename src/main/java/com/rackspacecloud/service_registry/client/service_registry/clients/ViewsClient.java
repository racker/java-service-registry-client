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

package com.rackspacecloud.service_registry.client.service_registry.clients;

import com.google.gson.reflect.TypeToken;
import com.rackspacecloud.service_registry.client.service_registry.ClientResponse;
import com.rackspacecloud.service_registry.client.service_registry.containers.OverviewContainer;
import com.rackspacecloud.service_registry.client.service_registry.objects.Overview;
import org.apache.http.client.methods.HttpGet;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class ViewsClient extends BaseClient {
    public ViewsClient(AuthClient authClient) {
        super(authClient);
    }

    public ArrayList<Overview> getOverview(HashMap<String, String> options) throws Exception {
        Type type = new TypeToken<OverviewContainer>() {}.getType();
        ClientResponse response = this.performRequest("/views/overview", null, new HttpGet(), true, type);

        OverviewContainer container = (OverviewContainer)response.getBody();
        return (ArrayList<Overview>)container.getValues();
    }
}
