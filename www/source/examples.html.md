There are a very large number of libraries and frameworks in the Java ecosystem,
 as well as many different application development patterns used to simplify
 certain types of work.

The examples below demonstrate how to integrate the Java client into a few of
 these patterns and frameworks.

## <a name="data-access-object"></a> Data Access Object (DAO) Pattern

A [Data Access Object](http://en.wikipedia.org/wiki/Data_access_object) is an
 object that provides data storage methods without exposing details of the
 database. They typically provide at least the basic persistence operations,
 possibly with additional methods to take advantage of database specific
 functionality.

```java
public class MyObjectDao {

    public MyObject findOne(final String key) { /* some logic */ }

    public void save(final MyObject myObject) { /* some logic */ }

    public void delete(final MyObject myObject) { /* some logic */ }

    // etc ..
}
```

Instead of you creating this code for every data type in your Java application's
 model, the Java client bundles a generic implementation of this pattern. It
 implements the DAO pattern for you while exposing the _asynchronous_
 operation.

```java
import io.orchestrate.client.dao.GenericAsyncDao;

public class MyObjectDao extends GenericAsyncDao<MyObject> {

    private final Client client;

    public MyObjectDao(final Client client) {
        super(client, "myCollection");
        this.client = client;
    }

}
```

Using the `MyObjectDao` object shown above, writing data to the service becomes
 even easier.

```java
Client client = new Client("your api key");
MyObjectDao dao = new MyObjectDao(client);

// save my object
Future<KvMetadata> futureSave = dao.save(new MyObject());

// wait for the save to complete
futureSave.get();
```

You can read more about the `GenericAsyncDao` in the
 [javadocs](/javadoc/latest/io/orchestrate/client/dao/GenericAsyncDao.html).

## <a name="dropwizard"></a> Dropwizard (Managed)

[Dropwizard](http://dropwizard.codahale.com/) is a Java framework for developing
 ops-friendly, high-performance, RESTful web services. If you've not come across
 it before, we're a fan of it, it's well worth checking out.

The Dropwizard framework has the concept of a
 "[Managed Object](http://dropwizard.codahale.com/manual/core/#managed-objects)"
 which are objects that control resources that need to be allocated and stopped
 as necessary. This is done by implementing the `Managed` interface.

```java
public class OrchestrateClientManager implements Managed {

    private final Client client;

    public OrchestrateClientManager(final Client client) {
        this.client = client;
    }

    @Override
    public void start() throws Exception {
        // nothing to do here
    }

    @Override
    public void stop() throws Exception {
        client.stop();
    }

}
```

Configuring the Java client for Dropwizard is easy.

## <a name="error-handling"></a> Error Handling

When you `execute` a client operation, you are returned an `OrchestrateFuture`
 for the result. This type is an extension of the `java.util.concurrent.Future`
 that allows you to register listeners that are run when the result is received
 from the asynchronous operation.

When you receive a result you can retrieve it from the `future` with the
 `Future#get(..)` method(s), they will throw an exception if one was received
 while processing the request or response. In all cases an unchecked exception
 will be thrown.

This is done primarily because usually the client can not recover from a bad
 operation, it is likely to be a programming error or a networking problem while
 making the HTTP request. As a result, forcing developers to catch a checked
 exception can result in unnecessary code in a `catch()` block.

To listen for an error in the future you can use an `OrchestrateFutureListener`.

```java
import io.orchestrate.client.OrchestrateFuture;
import io.orchestrate.client.OrchestrateFutureListener;

public class MyObjectOperationListener
        implements OrchestrateFutureListener<MyObject> {

    @Override
    public void onComplete(final OrchestrateFuture<MyObject> future) {
    }

    @Override
    public void onException(final OrchestrateFuture<MyObject> future) {
        /* do something */
    }

}
```

You can read more about the `OrchestrateFutureListener` in the
 [javadocs](/javadoc/latest/io/orchestrate/client/OrchestrateFutureListener.html).
