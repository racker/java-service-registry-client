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

package com.rackspacecloud.service_registry.client.service_registry;

import com.rackspacecloud.service_registry.client.service_registry.clients.*;
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
    public final SessionsClient sessions;
    public final ServicesClient services;
    public final ConfigurationClient configuration;
    public final ViewsClient views;
    public final AccountClient account;

    public Client(String username, String apiKey, String region) {
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

        this.sessions = new SessionsClient(authClient);
        this.services = new ServicesClient(authClient);
        this.configuration = new ConfigurationClient(authClient);
        this.views = new ViewsClient(authClient);
        this.account = new AccountClient(authClient);
    }
}
