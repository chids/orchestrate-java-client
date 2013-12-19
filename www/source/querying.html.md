The client API is designed around the concept of operations you can execute on
 the Orchestrate.io service. The client library is entirely _asynchronous_ and
 conforms to the [`java.util.concurrent.Future`](http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Future.html)
 specification in the standard library.

Under the hood the client makes `HTTP` requests to the [REST API](http://docs.orchestrate.io/).
 All data is written to the platform as [`JSON`](http://json.org/) and the client
 will handle marshalling and unmarshalling the data into Java objects.

Every Key/Value object has a unique identifier that represents the current
 version of the object, this information is known as the “ref”. The ref is a
 content-based hash that identifies the specific version of a value. With it
 you can track the history of an object and retrieve old versions.

### <a name="constructing-a-client"></a> Constructing a Client

A `Client` object is the starting point for making requests to the
 Orchestrate.io service, it manages the connection pool and makes `HTTP`
 requests to the [REST API](http://docs.orchestrate.io/).

You construct a client using the `API key` for your `Application` which can be
 found in the [Dashboard](https://dashboard.orchestrate.io/) (for help, see
 [here](/#getting-started)).

```java
// An API key looks something like:
//   3854bbd7-0a31-43b0-aa94-66236847a717
Client client = new Client("your api key");
```

A `Client` can be shared across threads and you only need to construct one per
 Java application. For more advanced configuration options, check out
 [Tuning the Client](/advanced-options.html#tuning).

### Stopping a Client

Sometimes it's necessary to release the resources used by a `Client` and
 reconstruct it again at some later stage.

```java
client.stop();

// some method later, using #execute(...) will reallocate resources
client.execute(...);
```

## <a name="key-value"></a> Key-Value

Key-Value operations are the heart of the Orchestrate.io service. These are the
 most common operations you'll perform and is the primary way of storing data.

All Key-Value operations happen in the context of a `Collection`. If the
 collection does not exist it will be _implicitly_ created when data is first
 written.

As mentioned above, all client operations are _asynchronous_.

### <a name="fetch-data"></a> Fetch Data

To fetch an object from a `collection` with a given `key`.

```java
KvFetchOperation<MyObj> kvFetchOp =
    new KvFetchOperation<MyObj>("myCollection", "someKey");

// execute the operation
Future<KvObject<MyObj>> kvObjectFuture = client.execute(kvFetchOp);

// wait for the result
KvObject<MyObj> kvObject = kvObjectFuture.get(3, TimeUnit.SECONDS);

// check the data exists
if (result == null) {
    System.out.println("'someKey' does not exist.";
} else {
    MyObj data = kvObject.getValue();
    // do something with the 'data'
}
```

This example shows how to retrieve the value for a key from a collection and
 deserialize the result JSON to a [POJO](http://en.wikipedia.org/wiki/Plain_Old_Java_Object)
 called `MyObj`.

### <a name="store-data"></a> Store Data

To store an object from a `collection` to a given `key`.

```java
// create some data to store
MyObj myObj = new MyObj(...);

KvStoreOperation kvStoreOp =
    new KvStoreOperation("myCollection", "someKey", myObj);

// execute the operation
Future<KvMetadata> kvOpFuture = client.execute(kvStoreOp);

// wait for the result
KvMetadata kvMetadata = kvOpFuture.get(3, TimeUnit.SECONDS);

// print the 'ref' for the stored data
System.out.println(kvMetadata.getRef());
```

This example shows how to store a value for a key to a collection, `myObj` is
 serialized to JSON by the client before writing the data.

The `KvMetadata` returned by the store operation contains information about
 where the information has been stored and the version (`ref`) it's been written
 with.

#### <a name="conditional-store"></a> Conditional Store

The `ref` metadata returned from a store operation is important, it allows
 you to perform a "Conditional PUT".

```java
// store the 'myObj' update if the 'currentRef' matches the ref on the server
String currentRef = kvMetadata.getRef();
KvStoreOperation kvStoreOp =
    new KvStoreOperation("myCollection", "someKey", myObj, currentRef);

// store the new 'myObj' data if 'someKey' does not already exist
boolean ifAbsent = true;
KvStoreOperation kvStoreOp =
    new KvStoreOperation("myCollection", "someKey", myObj, ifAbsent);
```

This type of store operation is very useful in high write concurrency
 environments, it provides a pre-condition that must be `true` for the store
 operation to succeed.

### <a name="delete-data"></a> Delete Data

To delete an object by `key` or the `collection` of objects.

```java
DeleteOperation deleteOp = new DeleteOperation("myCollection", "someKey");

// execute the operation
Future<Boolean> deleteFuture = client.execute(deleteOp);

// wait for the result
Boolean deleted = deleteFuture.get(3, TimeUnit.SECONDS);

if (deleted) {
    System.out.println("Successfully deleted a key.");
}
```

```java
// delete an entire collection, by careful, this cannot be undone
DeleteOperation deleteOp = new DeleteOperation("myCollection");

// same as above
```

#### <a name="conditional-delete"></a> Conditional Delete

Similar to a [conditional store](#conditional-store) operation, a conditional
 delete provides a pre-condition that must be `true` for the operation to
 succeed.

```java
String currentRef = kvMetadata.getRef();
DeleteOperation deleteOp =
    new DeleteOperation("myCollection", "someKey", currentRef);

// same as above
```

The object with the key `someKey` will be deleted if and only if the
 `currentRef` matches the current ref for the object on the server.

## <a name="search"></a> Search

A powerful feature of the Orchestrate.io service is the search functionality;
 when an object is written to the platform the data will be semantically indexed
 in the background.

This allows you to perform search queries on the data without any extra
 configuration hassle and no need to run a separate search cluster of some kind
 to ask questions about the data stored.

The query language used to perform searches is the familiar
 [Lucene Syntax](http://lucene.apache.org/core/4_3_0/queryparser/org/apache/lucene/queryparser/classic/package-summary.html#Overview),
 any Lucene query is a valid Orchestrate.io search query.

The simplest search query is a `*` query.

```java
// if you don't provide a query, it defaults to '*'
SearchOperation<MyObj> searchOp = new SearchOperation("myCollection");

// execute the operation
Future<SearchResults<MyObj>> searchFuture = client.execute(searchOp);

// wait for the result
SearchResults<MyObj> searchResults = searchFuture.get(3, TimeUnit.SECONDS);

for (Result<MyObj> result : searchResults) {
    // do something with the search results
    System.out.println(result.getScore());
}
```

A more complex search query could look like this:

```java
SearchOperation<MyObj> searchOp = SearchOperation.builder("myCollection")
    .query("*")
    .limit(50)
    .offset(10)
    .build();

// same as above
```

The collection called `myCollection` will be searched with the query `*` and
 up to `50` results may be returned with a starting offset of `10` from the most
 relevant. The results will be deserialized to `MyObj`s.

### Note

Search results are currently limited to no more than __100__ results for each
 query, if this limit is not suitable for you please let us know.

By default, a search operation will only return up to __10__ results, use the
 `SearchOperation.Builder` as shown above to retrieve more results for a query.

### Some Example Queries

Here are some query examples demonstrating the Lucene query syntax.

```java
// keyword matching
SearchOperation<MyObj> searchOp = new SearchOperation<String>(
    "myCollection", "title:\"foo bar\"");

// proximity matching
SearchOperation<MyObj> searchOp = new SearchOperation<String>(
    "myCollection", "\"foo bar\"~4");

// range searches
SearchOperation<MyObj> searchOp = new SearchOperation<String>(
    "myCollection", "year_of_birth:[20020101 TO 20030101]");
```

Ignore the backslashes in the first two examples, this is necessary to escape
 the quotes in the Java string literal.

## <a name="events"></a> Events

In the Orchestrate.io service, an event is a time ordered piece of data you want
 to store in the context of a key. This specialist storage type exists because
 we believe it's pretty common in the design of most applications.

Some examples of types of objects you'd want to store as events are; comments
 that belong to a blog article, items in a user's news feed from a social
 network, or billing history from a customer.

### <a name="fetch-events"></a> Fetch Events

To fetch events belonging to a `key` in a specific `collection` of a specific
 `type`, where type could be a name like "comments" or "feed".

```java
EventFetchOperation<MyObject> eventFetchOp =
    new EventFetchOperation<MyObject>("myCollection", "someKey", "type");

// execute the operation
Future<Iterable<Event<MyObject>>> eventFuture = client.execute(eventFetchOp);

// wait for the result
Iterable<Event<MyObject>> events = eventFuture.get(3, TimeUnit.SECONDS);

// iterate on the events, they will be ordered by the most recent value
for (Event<MyObject> event : events) {
    System.out.println(event.getTimestamp());
}
```

You can also supply an optional `start` and `end` timestamp to retrieve a subset
 of the events.

```java
EventFetchOperation<MyObject> eventFetchOp =
    new EventFetchOperation<MyObject>(
    "myCollection", "someKey", "eventType", 0L, 13865200L);

// same as above
```

### <a name="store-event"></a> Store Event

You can think of storing an event like adding to the front of a time-ordered
 immutable list of objects.

To store an event to a `key` in a `collection` with a specific `type`.

```java
MyObject myObj = new MyObject(..);

EventStoreOperation eventStoreOp =
    new EventStoreOperation("myCollection", "someKey", "eventType", myObj);

// execute the operation
Future<Boolean> eventStoreFuture = client.execute(eventStoreOp);

// wait for the result
Boolean stored = eventStoreFuture.get(3, TimeUnit.SECONDS);

if (stored) {
    System.out.println("Successfully stored an event.");
}
```

You can also supply an optional `timestamp` for the event, this will be used
 instead of the timestamp of the write operation.

```java
EventStoreOperation eventStoreOp =
    new EventStoreOperation(
    "myCollection", "someKey", "eventType", myObj, 13865200L);

// same as above
```

## <a name="graph"></a> Graph

While building an application it's possible that you'll want to make associations
 between particular objects of the same type or even objects of completely
 different types that share a property of some kind.

It's this sort of data problem that makes the graph features in Orchestrate.io
 shine, if you're building a socially-aware application you might want to
 add relations between data like "friend" or "follows".

A graph query is the right choice when you have a starting object that you want
 search from to follow relevant relationships and accumulate interesting
 information.

### <a name="fetch-relations"></a> Fetch Relations

To fetch objects related to the `key` in the `collection` based on a
 relationship or number of `relation`s.

```java
RelationFetchOperation relationFetchOp =
    new RelationFetchOperation("myCollection", "someKey", "relation");

// execute the operation
Future<Iterable<KvObject<String>>> future = client.execute(relationFetchOp);

// wait for the result
Iterable<KvObject<String>> results = future.get(3, TimeUnit.SECONDS);

for (KvObject<String> result : results) {
    // the raw JSON string
    System.out.println(result.getValue());
}
```

Imagine that we'd like to know the `follow`ers of `users` that are `friend`s of
 the user `tony`. This kind of query could look like this.

```java
RelationFetchOperation relationFetchOp =
    new RelationFetchOperation("users", "tony", "friend", "follow");

// same as above
```

#### Note

At the moment, you will always get the raw JSON string back from a relation
 query because relations can span collections, it's not possible to map the
 objects back to their Java types automatically.

We may lift this restriction in a future release of the client.

### <a name="store-relation"></a> Store Relation

To store a `relation` between one `key` to another `key` within the same
 `collection` or across different `collection`s.

```java
RelationStoreOperation relationStoreOp =
    new RelationStoreOperation(
    "myCollection", "someKey", "relation", "toCollection", "toSomeKey");

// execute the operation
Future<Boolean> relationStoreFuture = client.execute(relationStoreOp);

// wait for the result
Boolean stored = relationStoreFuture.get(3, TimeUnit.SECONDS);

if (stored) {
    System.out.println("Successfully stored the relation.");
}
```
