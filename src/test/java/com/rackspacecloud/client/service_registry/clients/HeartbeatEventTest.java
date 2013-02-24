package com.rackspacecloud.client.service_registry.clients;

import com.rackspacecloud.client.service_registry.ClientResponse;
import com.rackspacecloud.client.service_registry.HeartBeater;
import com.rackspacecloud.client.service_registry.events.client.ClientEvent;
import com.rackspacecloud.client.service_registry.events.client.ClientEventListener;
import com.rackspacecloud.client.service_registry.events.client.HeartbeatAckEvent;
import com.rackspacecloud.client.service_registry.events.client.HeartbeatErrorEvent;
import com.rackspacecloud.client.service_registry.events.client.HeartbeatEventListener;
import com.rackspacecloud.client.service_registry.events.client.HeartbeatStoppedEvent;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class HeartbeatEventTest {
    private BaseClient client = null;
    
    @Before
    public void setUpClient() {
        client = new BaseClient(null) {
        };
    }
    
    @Test
    public void testLatchesNeverGoNegative() {
        CountDownLatch latch = new CountDownLatch(1);
        Assert.assertEquals(1, latch.getCount());
        latch.countDown();
        Assert.assertEquals(0, latch.getCount());
        latch.countDown();
        Assert.assertEquals(0, latch.getCount());
        latch.countDown();
        Assert.assertEquals(0, latch.getCount());
    }
    
    @Test
    public void testSimpleListener() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        client.addEventListener(new ClientEventListener() {
            public void onEvent(ClientEvent ev) {
                latch.countDown();
            }
        });
        client.emit(new ClientEvent(client, 200));
        latch.await(1, TimeUnit.SECONDS);
        Assert.assertEquals(0, latch.getCount());
    }
    
    @Test
    public void testMultipleEventsFire() throws Exception {
        final CountDownLatch latch = new CountDownLatch(2);
        client.addEventListener(new ClientEventListener() {
            public void onEvent(ClientEvent ev) {
                latch.countDown();
            }
        });
        client.addEventListener(new ClientEventListener() {
            public void onEvent(ClientEvent ev) {
                latch.countDown();
            }
        });
        client.emit(new ClientEvent(client, 200));
        latch.await(1, TimeUnit.SECONDS);
    }
    
    @Test
    public void testRemoveWorks() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean errantEvent = new AtomicBoolean(false);
        ClientEventListener listener = new ClientEventListener() {
            public void onEvent(ClientEvent ev) {
                if (latch.getCount() == 0) {
                    errantEvent.set(true);
                    // in the off chance that our timeing sucks and this event bubbles up after the 1s wait, log an 
                    // error just in case.
                    throw new RuntimeException("This event should only have fired once.");
                } else {
                    latch.countDown();
                }
            }
        };
        client.addEventListener(listener);
        client.emit(new ClientEvent(client, 200));
        latch.await(1, TimeUnit.SECONDS);
        client.removeEventListener(listener);
        client.emit(new ClientEvent(client, 200));
        
        // hang around for 1sec to see if another event bubbles up to the listener. it shouldn't.
        Thread.sleep(1000);
        Assert.assertFalse(errantEvent.get());
    }
    
    @Test
    public void test() throws Exception {
        final CountDownLatch genericLatch = new CountDownLatch(5);
        final CountDownLatch heartbeatLatch = new CountDownLatch(4);
        final AtomicBoolean tooManyEvents = new AtomicBoolean(false);
        HeartbeatEventListener listener = new HeartbeatEventListener() {
            @Override
            public void onAck(HeartbeatAckEvent ack) {
                ackOrStop();
            }

            @Override
            public void onStopped(HeartbeatStoppedEvent stopped) {
                ackOrStop();
            }

            @Override
            public void onError(HeartbeatErrorEvent error) {
                ackOrStop();
            }

            @Override
            public void onEvent(ClientEvent ev) {
                genericLatch.countDown();
                super.onEvent(ev);
            }

            private void ackOrStop() {
                if (heartbeatLatch.getCount() == 0) {
                    tooManyEvents.set(true);
                } else {
                    heartbeatLatch.countDown();
                }
            }
        };
        HeartBeater hbClient = new HeartBeater(null, null, null, 1000);
        ClientResponse ok200 = new ClientResponse(new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK")));
        hbClient.addEventListener(listener);
        hbClient.emit(new ClientEvent(hbClient, 201));
        hbClient.emit(new HeartbeatAckEvent(hbClient, ok200));
        hbClient.emit(new HeartbeatAckEvent(hbClient, ok200));
        hbClient.emit(new HeartbeatStoppedEvent(hbClient, 404));
        hbClient.emit(new ClientEvent(hbClient, 202));
        hbClient.emit(new HeartbeatErrorEvent(hbClient, new Exception("something bad happened"), 503));
        
        // ok. that's 5 events total, but only 3 HB events. make sure things tally up.
        genericLatch.await(1, TimeUnit.SECONDS);
        heartbeatLatch.await(1, TimeUnit.SECONDS);
        Assert.assertFalse(tooManyEvents.get());
    }
}
