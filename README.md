Orchestrate Java Client
=======================

A high performance, asynchronous Java client to query the [Orchestrate.io](http://orchestrate.io/)
 service.

The client API is still in __flux__, it is not yet stable. While we'll endeavour
 to keep API-breaking changes to a minimum, you should expect that _at the moment_
 there may be some migration work necessary with new releases.

### About

The Orchestrate.io service is a platform for storing and querying data.

Using Orchestrate.io you can focus on building applications and adding new
 features while we handle safely storing the data, providing a large variety of
 ways to query the data and keeping the service highly available to support your
 applications as they grow.

You can create an account by signing up and logging in at our [Dashboard](https://dashboard.orchestrate.io).

[https://dashboard.orchestrate.io](https://dashboard.orchestrate.io)

### Getting Started

The client library is available on [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22orchestrate-java-client%22).

#### Using [Gradle](http://www.gradle.org/)

```groovy
dependencies {
    compile group: 'io.orchestrate', name: 'orchestrate-java-client', version: '0.1.0'
}
```

#### Using Maven

```xml
<dependency>
    <groupId>io.orchestrate</groupId>
    <artifactId>orchestrate-java-client</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Example

The client API is designed around the concept of `operation`s you can execute on
 the Orchestrate.io service. The client library is entirely asynchronous and
 conforms to the [java.util.concurrent.Future](http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Future.html)
 specification in the standard library.

#### Creating a Client

You construct a client using the `API key` for your `Application` which can be
 found in the [Dashboard](https://dashboard.orchestrate.io/) (for help,
 [see here](http://orchestrate-io.github.io/orchestrate-java-client/querying/)).

```java
// An API key looks something like:
//   3854bbd7-0a31-43b0-aa94-66236847a717
Client client = new Client("your api key");
```

#### Fetching Key-Value Data

For example, to fetch an object from a `collection` with a given `key`.

```java
KvFetchOperation<MyObj> kvFetchOp =
    new KvFetchOperation<MyObj>("myCollection", "someKey", MyObj.class);

// execute the operation
Future<KvObject<MyObj>> future = client.execute(kvFetchOp);

// wait for the result
KvObject<MyObj> kvObject = future.get(3, TimeUnit.SECONDS);

// check the data exists
if (result == null) {
    System.out.println("'someKey' does not exist.";
} else {
    MyObj data = kvObject.getValue();
    // do something with the 'data'
}
```

The client has operations for [Key-Value](http://orchestrate-io.github.io/orchestrate-java-client/querying/#key-value),
 [Search](http://orchestrate-io.github.io/orchestrate-java-client/querying/#search),
 [Graph](http://orchestrate-io.github.io/orchestrate-java-client/querying/#graph)
 and [Event](http://orchestrate-io.github.io/orchestrate-java-client/querying/#events)
 features from the Orchestrate.io service.

#### <a name="user-guide"></a> User Guide

There's a [User Guide](http://orchestrate-io.github.io/orchestrate-java-client/)
 for the `client` with more code examples and details on tuning the library for
 your server environment.

The Javadocs for the codebase is available
 [here](http://orchestrate-io.github.io/orchestrate-java-client/javadoc/latest).

### Developer notes

The client uses the [Grizzly framework](https://grizzly.java.net/) for
 processing HTTP requests and [Jackson JSON parser](http://wiki.fasterxml.com/JacksonHome)
 for marshalling data to and from the [Orchestrate.io](http://orchestrate.io/)
 service.

The codebase requires the [Gradle](http://gradle.org) build tool at version
 `1.6+` and the Java compiler at version `1.6.0` or greater.

#### Building the codebase

A list of all possible build targets can be displayed by Gradle with
 `gradle tasks`.

In a regular write-compile-test cycle use `gradle test`.

It is recommended to run Gradle with the
 [Build Daemon](http://www.gradle.org/docs/nightly/userguide/userguide_single.html#gradle_daemon)
 enabled to improve performance. e.g. `gradle --daemon` once the daemon is
 running it can be stopped with `gradle --stop`.

#### Building the documentation

The documentation sources for the [User Manual](#user-guide) is in the `www`
 folder.

The documentation can be built with [Middleman](http://middlemanapp.com/). To
 run the local server for viewing use `middleman server` and to build compiled
 code for deployment use `middleman build`.

### Contribute

All contributions to the documentation and the codebase are very welcome and
 feel free to open issues on the tracker wherever the documentation needs
 improving.

Also, pull requests are always welcome! `:)`
