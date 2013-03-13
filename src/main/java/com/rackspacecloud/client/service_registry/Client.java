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

import com.rackspacecloud.client.service_registry.clients.*;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

public class Client {
    private final ServicesClient services;
    private final ConfigurationClient configuration;
    private final EventsClient events;
    private final AccountClient account;

    // todo: there is no way this is going to say in sync.
    public static final String VERSION = "java-service-registry-client/v1.0.0-SNAPSHOT";

    public Client(String username, String apiKey, String region, String url) {
        AuthClient authClient = new AuthClient(new DefaultHttpClient() {
            protected HttpParams createHttpParams() {
                BasicHttpParams params = new BasicHttpParams();
                org.apache.http.params.HttpConnectionParams.setSoTimeout(params, 10000);
                params.setParameter("http.socket.timeout", 10000);
                return params;
            }

            @Override
            protected ClientConnectionManager createClientConnectionManager() {
                SchemeRegistry schemeRegistry = new SchemeRegistry();
                schemeRegistry.register(
                        new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
                schemeRegistry.register(
                        new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
                return new ThreadSafeClientConnManager(createHttpParams(), schemeRegistry);
            }
        }, username, apiKey, region);

        this.services = new ServicesClient(authClient, url);
        this.configuration = new ConfigurationClient(authClient, url);
        this.events = new EventsClient(authClient, url);
        this.account = new AccountClient(authClient, url);
    }

    public ServicesClient getServicesClient() { return this.services; }
    public ConfigurationClient getConfigurationClient() { return this.configuration; }
    public EventsClient getEventsClient () { return this.events; }
    public AccountClient getAccountClient() { return this.account; }
}
