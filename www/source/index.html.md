A high performance, asynchronous Java client to query the Orchestrate.io service.

The client uses the [Grizzly framework](https://grizzly.java.net/) for
 processing HTTP requests and [Jackson JSON parser](http://wiki.fasterxml.com/JacksonHome)
 for marshalling data to and from the [Orchestrate.io](http://orchestrate.io/)
 service.

## <a name="about"></a> About Orchestrate

[Orchestrate.io](http://orchestrate.io) eliminates the need for
  developers to manage multiple databases themselves when building
  applications, or adding features to existing applications. It unifies, stores
  and queries data including key/value, search, graph and events through a
  single, ambient layer between the application and the backend, which is
  accessed easily through a single, simple API.

Orchestrate.io gives you full control and ownership of your data while
  safely storing it for you, provides a large variety of ways to query that
  data, and keeps the service highly available to support your application as
  it grows. It handles uptime across all of these systems, and ensures that
  your applications are still able to read and write data in the event of a
  “flash crowd” (a dramatically increase in traffic that requires the data
  system to scale rapidly to cope with the increase demand on resources). It
  improves fault tolerance, reduces complexity, improves security and puts data
  closer to global end users. 

### Creating an account

You can create an account by signing up at our
 [Dashboard](https://dashboard.orchestrate.io/).

[https://dashboard.orchestrate.io](https://dashboard.orchestrate.io)

## <a name="getting-started"></a> Getting Started

Once you've created an account with the Orchestrate.io Dashboard you're ready to
 build an application with the Java client.

All operations to the Orchestrate.io service happen in the context of an
 `Application` and you can create one from the Dashboard. An `Application` can
 contain any number of `Collection`s. A `Collection` is a namespace for data of
 the same type, in exactly the same way as a table in a `SQL` database.

For example, if you were building a social network, you might have a single
 application named “MyPlace”. The application would then have multiple collections
 within it, based on the different types/groups of information you are storing.
 There might be collections named "users", "pages" and "games".

When first setting up your application, you will need to create it via the
 [Dashboard](https://dashboard.orchestrate.io/). This creation process will
 generate an API key which the Java client will use to access the data in the
 application. Note that while the `Application` must be created manually, it's
 not necessary to create Collections up front — collections are created
 automatically on the first write.

To start reading and writing data to your application, have a look at
 [Querying](/querying.html)

For more information check out the [Getting Started](https://dashboard.orchestrate.io/getting_started)
 section in the Dashboard.

## <a name="managed-dependency"></a> Managed Dependency

The Orchestrate Java Client is available on
 [Maven Central](http://search.maven.org/#artifactdetails%7Cio.orchestrate%7Corchestrate-java-client%7C0.1.0%7Cbundle).

### Gradle

```groovy
dependencies {
compile group: 'io.orchestrate', name: 'orchestrate-client', version: '0.1.0'
}
```

### Maven

```xml
<dependency>
    <groupId>io.orchestrate</groupId>
    <artifactId>orchestrate-client</artifactId>
    <version>0.1.0</version>
</dependency>
```

## <a name="download"></a> Download

If you're not using Maven (or a dependency resolver that's compatible with Maven
 repositories), you can download the JARs you need for your project from Maven
 Central.

### Source Code

The codebase for this library is open source on
 [GitHub](https://github.com/orchestrate-io/orchestrate-java-client):

[https://github.com/orchestrate-io/orchestrate-java-client](https://github.com/orchestrate-io/orchestrate-java-client)

Code licensed under the [Apache License v2.0](http://www.apache.org/licenses/LICENSE-2.0).
 Documentation licensed under [CC BY 3.0](http://creativecommons.org/licenses/by/3.0/).

### Contribute

All contributions to the documentation and the codebase are very welcome. Feel
 free to open issues on the tracker wherever the documentation or code needs
 improving.

Also, pull requests are always welcome\! `:)`

### Note

The client API is still in _flux_, we're looking for [feedback](/feedback.html)
 from developers and learning what you need to build incredible applications.

## <a name="javadoc"></a> Javadoc

The javadoc for the latest version of the client is available at:

[http://orchestrate-io.github.io/orchestrate-java-client/javadoc/latest](http://orchestrate-io.github.io/orchestrate-java-client/javadoc/latest)

For older versions of the documentation:

* [0.1.0](orchestrate-java-client/javadoc/0.1.0/)

