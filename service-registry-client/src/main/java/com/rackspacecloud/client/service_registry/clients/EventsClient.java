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
import com.rackspacecloud.client.service_registry.PaginationOptions;
import com.rackspacecloud.client.service_registry.objects.*;
import org.apache.http.client.methods.HttpGet;
import com.rackspacecloud.client.service_registry.containers.EventsContainer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;


public class EventsClient extends BaseClient {

    public EventsClient(AuthClient authClient, String apiUrl) {
        super(authClient, apiUrl);
    }

    public Iterator<Event> list(PaginationOptions paginationOptions) throws Exception {
        Type type = new TypeToken<EventsContainer>() {}.getType();
        
        return this.getListIterator(Event.class,
                                    "/events",
                                    paginationOptions,
                                    new HashMap<String, String>(),
                                    new HttpGet(),
                                    true,
                                    type);
    }
}