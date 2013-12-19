While we try to ensure the Java client has useful default configuration settings
 it may be necessary to tweak the behaviour of the library for the requirements
 of your server environment.

The information below demonstrates how to make advanced changes to the client.

## <a name="tuning"></a> Tuning the Client

As well as the [basic constructor](querying#constructing-a-client) for the
 `Client` it's possible to create a client object using the `Builder`
 interface.

The builder allows you to configure settings like the initial size of the
 connection pool, the maximum number of connections that can be allocated, and
 other tuning parameters.

```java
Client client = Client.builder("your api key")
    .poolSize(50)
    .maxPoolSize(100)
    .build();
```

You can read more about the `Client.Builder` in the [javadocs](/javadoc/latest/io/orchestrate/client/Client.Builder.html).

## <a name="json-mapping"></a> Custom JSON Mapping

The Java client uses the excellent [Jackson JSON library](http://wiki.fasterxml.com/JacksonHome)
 to handle serializing your data to JSON before it's written to the
 Orchestrate.io service. Jackson has a very large array of configuration settings
 that allow you to tweak the way data is serialized and deserialized.

The client exposes the ability to set Jackson's `ObjectMapper` from your code.

```java
// force escaping of non-ASCII characters
// avoid exception when encountering unknown property
ObjectMapper mapper = new ObjectMapper();
mapper.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

Client client = new Client("your api key", mapper);

// OR with greater configuration control
Client client = Client.builder("your api key")
    .mapper(JacksonMapper.builder()
        .registerModule(new JodaModule())
        .enable(JsonGenerator.Feature.ESCAPE_NON_ASCII)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES))
    .build();
```

You can read more about the `JacksonMapper.Builder` in the
 [javadocs](/javadoc/latest/io/orchestrate/client/JacksonMapper.Builder.html).

### Note

At the moment there's no way to supply your own JSON mapping library to use when
 converting data to and from your application's object types. For example, you
 cannot choose to use [Google's Gson library](https://code.google.com/p/google-gson/)
 instead of Jackson.

We may lift this restriction in a future release of the client.
