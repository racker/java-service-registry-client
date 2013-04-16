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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rackspacecloud.client.service_registry.Client;
import com.rackspacecloud.client.service_registry.ClientResponse;
import com.rackspacecloud.client.service_registry.MethodOptions;
import com.rackspacecloud.client.service_registry.containers.ContainerMeta;
import com.rackspacecloud.client.service_registry.events.client.ClientEvent;
import com.rackspacecloud.client.service_registry.events.client.ClientEventListener;
import com.rackspacecloud.client.service_registry.events.client.ClientEventThread;
import com.rackspacecloud.client.service_registry.objects.HasId;
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
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseClient {
    public static final String PRODUCTION_URL = "https://dfw.registry.api.rackspacecloud.com/v1.0";
    
    private static Logger logger = LoggerFactory.getLogger(BaseClient.class);
    private static final int MAX_401_RETRIES = 1;

    private final String apiUrl;

    private final AuthClient authClient;
    private final HttpClient client;
    
    private final Collection<ClientEventListener> listeners;

    public BaseClient(AuthClient authClient, String apiUrl) {
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
        }, authClient, apiUrl);
    }

    public BaseClient(HttpClient client, AuthClient authClient, String apiUrl) throws IllegalArgumentException {
        this.client = client;
        this.authClient = authClient;
        listeners = new ArrayList<ClientEventListener>();
        this.apiUrl = apiUrl;
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
    
    protected <T extends HasId> Iterator<T> getListIterator(final Class<T> clazz, 
                                                            final String path, 
                                                            final MethodOptions methodOptions,
                                                            final HttpRequestBase method, 
                                                            final boolean parseAsJson, 
                                                            final Type type) {
        return new Iterator<T>() {
            
            // means we are done fetching, not done iterating.
            private boolean exhausted = false;
            
            private List<T> curValues;
            private String nextMarker = methodOptions.getMarker();
            
            // NOTE: The groundwork has been laid to let methodOptions.getLimit() become a limit for the number of total
            //       results returned. We can use any value we want for the page size now.  As for now,
            //       methodOptions.getLimit() refers to the page size, which may not be intuitive.
            // these are all the keys we are interested in when paging.
            private Set<String> constantPagingParams = Collections.unmodifiableSet(new HashSet<String>() {{
                addAll(methodOptions.getNonNullKeys());
                remove(MethodOptions.PaginationOptions.Marker.toString());
            }});
            
            public boolean hasNext() {
                if (this.curValues == null && !this.exhausted) {
                    this.curValues = getNextPage();
                }
                return this.curValues != null && this.curValues.size() > 0;
            }

            public T next() {
                T item = this.curValues.remove(0);
                if (this.curValues.size() == 0 && !this.exhausted) {
                    this.curValues = this.getNextPage();
                }
                return item;
            }

            public void remove() {
                throw new RuntimeException("Not implemented");
            }
            
            private List<T> getNextPage() {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                // backfill everything from options into params except "marker". We handle that manually.
                for (String key : constantPagingParams) {
                    params.add(new BasicNameValuePair(key, methodOptions.get(key)));
                }
                
                if (this.nextMarker != null) {
                    params.add(new BasicNameValuePair("marker", this.nextMarker));
                }
                
                try {
                    ClientResponse response = performRequest(path, params, method, parseAsJson, type);
                    ContainerMeta<T> container = (ContainerMeta<T>)response.getBody();
                    List<T> values = container.getValues();
                    if (container.getNextMarker() == null) {
                        this.exhausted = true;
                    } else {
                        nextMarker = container.getNextMarker();
                    }
                    
                    return values;
                } catch (Exception ex) {
                    // be careful about throwing, you're in an iterator.
                    this.exhausted = true;
                    return new ArrayList<T>();
                }
                
            }
        };
    }

    protected ClientResponse performListRequest(MethodOptions methodOptions, String path, List<NameValuePair> params, HttpRequestBase method, boolean parseAsJson, Type responseType) throws Exception {
        if (params == null) {
            params = new ArrayList<NameValuePair>();
        }

        if (methodOptions != null) {
            if (methodOptions.getLimit() != null) {
                params.add(new BasicNameValuePair("limit", methodOptions.getLimit().toString()));
            }

            if (methodOptions.getMarker() != null) {
                params.add(new BasicNameValuePair("marker", methodOptions.getMarker()));
            }
        }

        return performRequest(path, params, method, parseAsJson, responseType, false, 0);
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

        String url = (this.apiUrl + "/" + this.authClient.getAuthToken().getTenant().get("id") + path);

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

        method.setURI(new URI(this.apiUrl + "/" + this.authClient.getAuthToken().getTenant().get("id") + path));
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
    
    protected String getApiUrl() {
        return this.apiUrl;
    }
}
