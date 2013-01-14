# Java Rackspace Service Registry client

Java client for Rackspace Service Registry.

## Build and run tests

	mvn install

## Using the client

### Installing the library using Maven

Add the following lines in the `repositories` and `dependencies` section in your `pom.xml`.

```xml
        <repository>
            <id>sonatype-nexus-snapshots</id>
            <name>Nexus Snapshots Repository</name>
            <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
        </repository>
```

If you're using ivy, this needs to be in your ivysettings.xml:

```xml
    <ibiblio name="sonatype" 
             m2compatible="true" 
             usepoms="true"
             root="https://oss.sonatype.org/content/repositories/snapshots/"
             pattern="[organisation]/[module]/[revision]/[artifact]-[revision](-[classifier]).[ext]"
             changingPattern="SNAPSHOT*"/> 
```

This lines are necessary because artifacts are currently only stored on Sonatype staging Maven servers.

```xml
        <dependency>
            <groupId>com.rackspacecloud</groupId>
            <artifactId>service-registry-client</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
```

Replace `1.0.0-SNAPSHOT` with the desired version.

### Registration and Heartbeat

```java
Client client = new Client("MY_RAX_USER", "MY_RAX_API_KEY", Region.US);
    
Map<String, String> metadata = new HashMap<String, String>();
metadata.put("host", "127.0.0.1");
metadata.put("version", "0u812");

SessionCreateResponse sessionResponse = client.sessions.create(15, metadata);
    
sessionResponse.getHeartbeater().start();
```
