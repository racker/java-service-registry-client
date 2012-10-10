package com.rackspacecloud.client.service_registry;

import java.lang.Exception;
import java.lang.String;
import java.lang.System;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) throws Exception {
       Client client = new Client("kamislo", "9a3c35bf4fd89992471ae4698630567e", "us");

        //client.create(30, null);

        HashMap<String, String> metadata = new HashMap<String, String>() {{
            put("region", "dfw");
            put("something", "test");
        }};

        SessionCreateResponse result = client.sessions.create(5, metadata);
        System.out.println(result);
        result.heartbeater.start();

        /*Session s1= client.sessions.get(result.getSession().getId());
        System.out.println("========");
        System.out.println(s1.getMetadata());

        String sessionId = result.getSession().getId();
        client.sessions.heartbeat(sessionId, result.getToken());

        Service srv = client.services.create("messenger1", sessionId, new ArrayList<String>(Arrays.asList(new String [] {"www", "api"})), null);
        System.out.println(srv);

        ArrayList<Service> srvs = client.services.list(null, "www");

        client.configuration.set("key1", "value 1 2 3");
        System.out.println(client.configuration.get("key1").getId());
        System.out.println(client.configuration.get("key1").getValue());

        ArrayList<Overview> ov = client.views.getOverview(null);
        System.out.println(new Gson().toJson(ov));

        System.out.println("========");
        System.out.println(client.account.getLimits().getRate());
        System.out.println(client.account.getLimits().getResource());

        //System.out.println(new Gson().toJson(client.list(null)));
        */
    }
}
