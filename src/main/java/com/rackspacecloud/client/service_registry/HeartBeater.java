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

import com.rackspacecloud.client.service_registry.clients.AuthClient;
import com.rackspacecloud.client.service_registry.clients.BaseClient;
import com.rackspacecloud.client.service_registry.events.client.HeartbeatAckEvent;
import com.rackspacecloud.client.service_registry.events.client.HeartbeatErrorEvent;
import com.rackspacecloud.client.service_registry.events.client.HeartbeatStoppedEvent;
import com.rackspacecloud.client.service_registry.objects.HeartbeatToken;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Exception;
import java.lang.Integer;
import java.lang.InterruptedException;
import java.lang.String;

public class HeartBeater extends BaseClient {
    private static final long INTERVAL_PROACTIVE_MILLIS = 4000L;
    
    private final String serviceId;
    private final Integer heartbeatTimeoutSecs;
    private final long heartbeatIntervalMillis;
    private final String initialToken;
    
    private volatile boolean stopped = false;
    private volatile Thread hbThread = null;
    
    private static final Logger logger = LoggerFactory.getLogger(HeartBeater.class);

    public HeartBeater(AuthClient authClient, String serviceId, String initialToken, int timeout, String url) {
        super(authClient, url);
        this.serviceId = serviceId;
        this.initialToken = initialToken;
        this.heartbeatTimeoutSecs = timeout;
        this.heartbeatIntervalMillis = (long)(timeout * 1000L * (timeout < 15 ? 0.6d : 0.9d));
    }

    private void runInThread() {
        ClientResponse response;
        String path = String.format("/services/%s/heartbeat", this.serviceId);
        int lastHttpStatus = 0;
        boolean isError = false;
        String nextToken = initialToken;
        
        while (!this.stopped && (nextToken != null)) {
            long start = System.currentTimeMillis();
            logger.debug(String.format("Sending hearbeat (timeout=%d secs)...", this.heartbeatTimeoutSecs));

            HeartbeatToken payload = new HeartbeatToken(nextToken);

            try {
                response = this.performRequestWithPayload(path, null, new HttpPost(), payload, true, HeartbeatToken.class);
                lastHttpStatus = response.getStatusCode();
                nextToken = ((HeartbeatToken)response.getBody()).getToken();
                if (lastHttpStatus != 200) {
                    Header[] headers = response.getHeader("x-response-id");
                    String responseId = (headers == null || headers.length == 0) ? "unknown" : headers[0].getValue();
                    logger.debug(String.format("Heartbeat response was %d for service %s with txn %s", lastHttpStatus, this.serviceId, responseId));
                    // heartbeat again instantly or exit out of the loop because a 404 will yield a null token.
                    continue;
                }
            }
            catch (Exception ex) {
                logger.error(String.format("Got exception while sending heartbeat, stopping heartbeating..."), ex);
                this.stopped = true;
                isError = true;
                this.emit(new HeartbeatErrorEvent(this, ex, lastHttpStatus));
                break;
            }

            // actual sleep interval is interval - time wasted - INTERVAL_PROACTIVE_MILLIS
            long actualInterval = heartbeatIntervalMillis - System.currentTimeMillis() + start - INTERVAL_PROACTIVE_MILLIS;

            this.emit(new HeartbeatAckEvent(this, response));
            try {
                logger.debug(String.format("Sleeping before sending next heartbeat (delay=%sms, nextToken=%s)", actualInterval, nextToken));
                hbThread.sleep(actualInterval);
            }
            catch (InterruptedException ex) {
                logger.debug("Heartbeater woken up. Stopping?");
                continue;
            }
        }
        hbThread = null; // reset sentinel for start().
        
        if (!isError) {
            this.emit(new HeartbeatStoppedEvent(this, lastHttpStatus));
        }
    }
    
    public synchronized void start() {
        if (hbThread == null) {
            stopped = false;
            hbThread = new Thread(this.toString()) {
                public void run() {
                    runInThread();
                }
            };
            hbThread.start();
        }
    }

    public synchronized void stop() {
        this.stopped = true;
        hbThread.interrupt();
    }
}
