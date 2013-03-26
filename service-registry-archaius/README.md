# Service Registry Provider for Archaius

This library provides bindings for the [Archaius Library](https://github.com/Netflix/archaius).  That library provides a set of configuration management API's for 
managing values.

This particular extension provides a new `source` called the `ServiceRegistryConfigurationProvider`.  It 
provides a configuration source backed by [Rackspace Service Registry](http://www.rackspace.com/blog/keep-track-of-your-services-and-applications-with-the-new-rackspace-service-registry/).
It works in an idiomatic archaius way which plugs in with the rest of the system.

Here is a snippet of example code

```java
final Client client = new Client(System.getProperty("rackspace.serviceregistry.username"),
        System.getProperty("rackspace.serviceregistry.apikey"),
        Region.US);
PolledConfigurationSource configSource = new ServiceRegistryConfigurationProvider(client);
AbstractPollingScheduler scheduler = new FixedDelayPollingScheduler();
DynamicConfiguration config = new DynamicConfiguration(configSource, scheduler);
```

# TODO

* Upload a maven artifact
* Add more tests
* Add docs around the service bindings
