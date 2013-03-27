# Java Rackspace Service Registry Client

Java client for Rackspace Service Registry.

## Build and run tests

    mvn install

## Using the client

### Installing the library using Maven

#### Using Snapshot Builds

Snapshots are hosted on the Sonatype repository.

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

#### Using Released Builds

Released versions of the library are integrated into Maven Central.  There is no need to specify complicated resolvers.

```xml
       <dependency>
           <groupId>com.rackspacecloud</groupId>
           <artifactId>service-registry-client</artifactId>
           <version>1.0.0</version>
       </dependency>
```

### Other Modules

This repository also includes other modules:

`service-registry-curator` - Service Registry bindings for Netflix Curator
library
`service-registry-archaius` - Service Registry bindings for Netflix Archaius
library
`service-registry-examples` - Usage examples

### Examples

Please see the [examples](tree/master/service-registry-examples/src/main/java/com/rackspacecloud/client/service_registry/examples) module.

