# Java Rackspace Service Registry client

Java client for Rackspace Service Registry.

## Build and run tests

	mvn install

## Using the client

### Registration and Heartbeat

    Client client = new Client("MY_RAX_USER", "MY_RAX_API_KEY", Region.US);
    
    Map<String, String> metadata = new HashMap<String, String>();
    metadata.put("host", "127.0.0.1");
    metadata.put("testdata?", "absolutely");;
    metadata.put("version", "0u812");

    SessionCreateResponse sessionResponse = client.sessions.create(15, metadata);
    
    sessionResponse.getHeartbeater().start();