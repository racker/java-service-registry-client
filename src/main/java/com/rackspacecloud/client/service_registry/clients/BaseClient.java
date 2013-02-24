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

import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.Override;
import java.lang.String;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rackspacecloud.client.service_registry.Client;
import com.rackspacecloud.client.service_registry.ClientResponse;
import com.rackspacecloud.client.service_registry.events.client.ClientEvent;
import com.rackspacecloud.client.service_registry.events.client.ClientEventListener;
import com.rackspacecloud.client.service_registry.events.client.ClientEventThread;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseClient {
    private static Logger logger = LoggerFactory.getLogger(BaseClient.class);
    private static final String DEFAULT_URL = "https://dfw.registry.api.rackspacecloud.com/v1.0";
    private static final int MAX_401_RETRIES = 1;

    private final String url = BaseClient.DEFAULT_URL;

    private final AuthClient authClient;
    private final HttpClient client;
    
    private final Collection<ClientEventListener> listeners;

    public BaseClient(AuthClient authClient) {
        this(new DefaultHttpClient() {
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
        }, authClient);
    }

    public BaseClient(HttpClient client, AuthClient authClient) throws IllegalArgumentException {
        this.client = client;
        this.authClient = authClient;
        listeners = new ArrayList<ClientEventListener>();
    }
    
    public void addEventListener(ClientEventListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    public void removeEventListener(ClientEventListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    public void emit(final ClientEvent event) {
        synchronized (listeners) {
            for (ClientEventListener _el : listeners) {
                final ClientEventListener el = _el;
                // todo: could generate less runnables if we first check to see if el can handle event.
                ClientEventThread.submit(new Runnable() {
                    public void run() {
                        el.onEvent(event);
                    }
                });
            }
        }
    }
    
    protected ClientResponse performRequest(String path, List<NameValuePair> params, HttpRequestBase method) throws Exception {
        return performRequest(path, params, method, false, null, false, 0);
    }

    protected ClientResponse performRequest(String path, List<NameValuePair> params, HttpRequestBase method, boolean parseAsJson, Type responseType) throws Exception {
        return performRequest(path, params, method, parseAsJson, responseType, false, 0);
    }

    protected ClientResponse performRequest(String path, List<NameValuePair> params, HttpRequestBase method, boolean parseAsJson, Type responseType, boolean reAuthenticate, int retryCount) throws Exception {
        int statusCode;

        this.authClient.refreshToken(reAuthenticate);

        String url = (this.url + "/" + this.authClient.getAuthToken().getTenant().get("id") + path);

        if (params != null) {
            url += "?" + URLEncodedUtils.format(params, "UTF-8");
        }

        method.setURI(new URI(url));
        method.setHeader("User-Agent", Client.VERSION);
        method.setHeader("X-Auth-Token", this.authClient.getAuthToken().getId());

        HttpResponse response = this.client.execute(method);
        statusCode = response.getStatusLine().getStatusCode();

        if ((statusCode == 401) && (retryCount < MAX_401_RETRIES)) {
            retryCount++;
            logger.info("API server returned 401, re-authenticating and re-trying the request");
            return this.performRequest(path, params, method, parseAsJson, responseType, true, retryCount);
        }

        return new ClientResponse(response, parseAsJson, responseType);
    }

    protected ClientResponse performRequestWithPayload(String path, List<NameValuePair> params, HttpEntityEnclosingRequestBase method, java.lang.Object payload, boolean parseAsJson, Type responseType) throws Exception {
        return this.performRequestWithPayload(path, params, method, payload, parseAsJson, responseType, false, 0);
    }

    protected ClientResponse performRequestWithPayload(String path, List<NameValuePair> params, HttpEntityEnclosingRequestBase method, java.lang.Object payload, boolean parseAsJson, Type responseType, boolean reAuthenticate, int retryCount) throws Exception {
        String body;
        int statusCode;

        this.authClient.refreshToken(reAuthenticate);

        method.setURI(new URI(this.url + "/" + this.authClient.getAuthToken().getTenant().get("id") + path));
        method.setHeader("User-Agent", Client.VERSION);
        method.setHeader("X-Auth-Token", this.authClient.getAuthToken().getId());

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .enableComplexMapKeySerialization()
                .create();

        body = gson.toJson(payload);
        StringEntity input = new StringEntity(body);
        input.setContentType("application/json");

        method.setEntity(input);

        HttpResponse response = this.client.execute(method);
        statusCode = response.getStatusLine().getStatusCode();

        if ((statusCode == 401) && (retryCount < MAX_401_RETRIES)) {
            retryCount++;
            logger.info("API server returned 401, re-authenticating and re-trying the request");
            return this.performRequestWithPayload(path, params, method, payload, parseAsJson, responseType, true, retryCount);
        }

        return new ClientResponse(response, parseAsJson, responseType);
    }
}
