/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.orchestrate.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.FilterChain;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.http.*;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.memory.ByteBufferWrapper;
import org.glassfish.grizzly.nio.NIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.strategies.WorkerThreadIOStrategy;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A client used to read and write data to the Orchestrate.io service.
 *
 * <p>Usage:
 * <pre>
 * {@code
 * Client client = Client.builder("your api key").build();
 *
 * // OR (as a shorthand with default settings):
 * Client client = new Client("your api key");
 * }
 * </pre>
 */
@Slf4j
public final class Client {

    /** Initial API; has KV, Events, Search, and early Graph support. */
    public static final API V0 = API.v0;

    /**
     * The different versions of the Orchestrate.io service.
     */
    private enum API {
        v0
    }

    /** The builder for this instance of the client. */
    private final Builder builder;
    /** The transport implementation for socket handling. */
    private final NIOTransport transport;

    /**
     * Create a new {@code client} with the specified {@code apiKey} and default
     * {@code JacksonMapper}.
     *
     * <p>Equivalent to:
     * <pre>
     * {@code
     * Client client = Client.builder("your api key").build();
     * }
     * </pre>
     *
     * @param apiKey An API key for the Orchestrate.io service.
     */
    public Client(final String apiKey) {
        this(builder(apiKey));
    }

    /**
     * Create a new {@code client} with the specified {@code apiKey} and {@code
     * objectMapper}.
     *
     * @param apiKey An API key for the Orchestrate.io service.
     * @param objectMapper The Jackson JSON mapper to marshall data with.
     */
    public Client(final String apiKey, final ObjectMapper objectMapper) {
        this(builder(apiKey).mapper(objectMapper));
    }

    /**
     * Create a new {@code client} with the specified {@code apiKey} and {@code
     * mapper}.
     *
     * @param apiKey An API key for the Orchestrate.io service.
     * @param mapper The mapper to marshall data with.
     */
    public Client(final String apiKey, final JacksonMapper mapper) {
        this(builder(apiKey).mapper(mapper));
    }

    /**
     * A client configured via the {@code Builder}.
     *
     * @param builder The builder used to configure the client.
     */
    private Client(final Builder builder) {
        assert (builder != null);

        this.builder = builder;

        // TODO allow a custom executor service to be provided?
        final ThreadPoolConfig poolConfig = ThreadPoolConfig.defaultConfig()
                .setPoolName("OrchestrateClientPool")
                .setCorePoolSize(builder.poolSize)
                .setMaxPoolSize(builder.maxPoolSize);

        // TODO add support for GZip compression
        // TODO add SSL support
        final FilterChain filterChain = FilterChainBuilder.stateless()
                .add(new TransportFilter())
//                .add(new IdleTimeoutFilter(timeoutExecutor, 10, TimeUnit.SECONDS))
                .add(new HttpClientFilter())
                .add(new ClientFilter(builder.host.toString(), builder.apiKey, builder.version.name(), builder.mapper))
                .build();
        // TODO experiment with the Leader-Follower IOStrategy
        this.transport = TCPNIOTransportBuilder.newInstance()
                .setTcpNoDelay(true)
                .setKeepAlive(true)
                .setWorkerThreadPoolConfig(poolConfig)
                .setIOStrategy(WorkerThreadIOStrategy.getInstance())
                .setProcessor(filterChain)
                .build();
    }

    private Future<Connection> newConnection() {
        try {
            if (transport.isStopped()) {
                transport.start();
            }

            return transport.connect(builder.host.getHost(), builder.port);
        } catch (final Exception e) {
            throw new ClientException(e);
        }
    }

    /**
     * Executes the specified {@code deleteOp} on the Orchestrate.io service.
     *
     * @param deleteOp The delete operation to execute.
     * @return A future for the response from this operation.
     */
    public OrchestrateFuture<Boolean> execute(final DeleteOperation deleteOp) {
        final OrchestrateFuture<Boolean> future =
                new OrchestrateFutureImpl<Boolean>(deleteOp);

        String uri = deleteOp.getCollection();
        if (deleteOp.hasKey()) {
            uri = uri.concat("/").concat(deleteOp.getKey());
        }

        final HttpRequestPacket.Builder httpHeaderBuilder = HttpRequestPacket
                .builder()
                .method(Method.DELETE)
                .uri(uri);
        if (!deleteOp.hasKey()) {
            httpHeaderBuilder.query("force=true");
        }
        if (deleteOp.hasKey() && deleteOp.hasCurrentRef()) {
            final String value = "\"".concat(deleteOp.getCurrentRef()).concat("\"");
            httpHeaderBuilder.header(Header.IfMatch, value);
        }

        execute(httpHeaderBuilder.build().httpContentBuilder().build(), future);
        return future;
    }

    /**
     * Executes the specified {@code eventFetchOp} on the Orchestrate.io service.
     *
     * @param eventFetchOp The event fetch operation to execute.
     * @param <T> The type to deserialize the results to.
     * @return A future for the response from this operation.
     */
    public <T> OrchestrateFuture<Iterable<Event<T>>> execute(final EventFetchOperation<T> eventFetchOp) {
        final OrchestrateFuture<Iterable<Event<T>>> future =
                new OrchestrateFutureImpl<Iterable<Event<T>>>(eventFetchOp);

        final String uri = eventFetchOp.getCollection()
                .concat("/")
                .concat(eventFetchOp.getKey())
                .concat("/events/")
                .concat(eventFetchOp.getType());

        final HttpRequestPacket.Builder httpHeaderBuilder = HttpRequestPacket
                .builder()
                .method(Method.GET)
                .uri(uri);
        String query = null;
        if (eventFetchOp.hasStart()) {
            query += "start=" + eventFetchOp.getStart();
        }
        if (eventFetchOp.hasEnd()) {
            query += "&end=" + eventFetchOp.getEnd();
        }
        httpHeaderBuilder.query(query);

        execute(httpHeaderBuilder.build().httpContentBuilder().build(), future);
        return future;
    }

    /**
     * Executes the specified {@code eventStoreOp} on the Orchestrate.io service.
     *
     * @param eventStoreOp The event store operation to execute.
     * @return A future for the response from this operation.
     */
    public OrchestrateFuture<Boolean> execute(final EventStoreOperation eventStoreOp) {
        final OrchestrateFutureImpl<Boolean> future =
                new OrchestrateFutureImpl<Boolean>(eventStoreOp);

        final ObjectMapper mapper = builder.mapper.getMapper();
        final byte[] content;
        try {
            content = mapper.writeValueAsBytes(eventStoreOp.getValue());
        } catch (final JsonProcessingException e) {
            future.setException(e);
            return future;
        }

        final String uri = eventStoreOp.getCollection()
                .concat("/")
                .concat(eventStoreOp.getKey())
                .concat("/events/")
                .concat(eventStoreOp.getType());

        final HttpRequestPacket.Builder httpHeaderBuilder = HttpRequestPacket
                .builder()
                .method(Method.PUT)
                .contentType("application/json")
                .uri(uri);
        if (eventStoreOp.hasTimestamp()) {
            httpHeaderBuilder.query("timestamp=" + eventStoreOp.getTimestamp());
        }
        httpHeaderBuilder.contentLength(content.length);

        final HttpContent httpContent = httpHeaderBuilder.build()
                .httpContentBuilder()
                .content(new ByteBufferWrapper(ByteBuffer.wrap(content)))
                .build();

        execute(httpContent, future);
        return future;
    }

    /**
     * Executes the specified {@code kvFetchOp} on the Orchestrate.io service.
     *
     * @param kvFetchOp The KV fetch operation to execute.
     * @param <T> The type to deserialize the results to.
     * @return The future for the response from this operation.
     */
    public <T> OrchestrateFuture<KvObject<T>> execute(final KvFetchOperation<T> kvFetchOp) {
        final OrchestrateFuture<KvObject<T>> future =
                new OrchestrateFutureImpl<KvObject<T>>(kvFetchOp);

        String uri = kvFetchOp.getCollection()
                .concat("/")
                .concat(kvFetchOp.getKey());
        if (kvFetchOp.hasRef()) {
            uri = uri.concat("/refs/").concat(kvFetchOp.getRef());
        }

        final HttpRequestPacket httpPacket = HttpRequestPacket
                .builder()
                .method(Method.GET)
                .uri(uri)
                .build();

        execute(httpPacket.httpContentBuilder().build(), future);
        return future;
    }

    /**
     * Executes the specified {@code kvStoreOp} on the Orchestrate.io service.
     *
     * @param kvStoreOp The KV store operation to execute.
     * @return The future for the response from this operation.
     */
    public OrchestrateFuture<KvMetadata> execute(final KvStoreOperation kvStoreOp) {
        final OrchestrateFutureImpl<KvMetadata> future =
                new OrchestrateFutureImpl<KvMetadata>(kvStoreOp);

        final ObjectMapper mapper = builder.mapper.getMapper();
        final byte[] content;
        try {
            content = mapper.writeValueAsBytes(kvStoreOp.getValue());
        } catch (final JsonProcessingException e) {
            future.setException(e);
            return future;
        }

        final String uri = kvStoreOp.getCollection()
                .concat("/")
                .concat(kvStoreOp.getKey());

        final HttpRequestPacket.Builder httpHeaderBuilder = HttpRequestPacket
                .builder()
                .method(Method.PUT)
                .contentType("application/json")
                .uri(uri);
        if (kvStoreOp.hasCurrentRef()) {
            final String ref = "\"".concat(kvStoreOp.getCurrentRef()).concat("\"");
            httpHeaderBuilder.header(Header.IfMatch, ref);
        } else if (kvStoreOp.hasIfAbsent()) {
            httpHeaderBuilder.header(Header.IfNoneMatch, "\"*\"");
        }
        httpHeaderBuilder.contentLength(content.length);

        final HttpContent httpContent = httpHeaderBuilder.build()
                .httpContentBuilder()
                .content(new ByteBufferWrapper(ByteBuffer.wrap(content)))
                .build();

        execute(httpContent, future);
        return future;
    }

    /**
     * Executes the specified {@code relationFetchOp} on the Orchestrate.io
     * service.
     *
     * @param relationFetchOp The relation fetch operation to execute.
     * @return The future for the response from this operation.
     */
    public OrchestrateFuture<Iterable<KvObject<String>>> execute(
            final RelationFetchOperation relationFetchOp) {
        final OrchestrateFuture<Iterable<KvObject<String>>> future =
                new OrchestrateFutureImpl<Iterable<KvObject<String>>>(relationFetchOp);

        String uri = relationFetchOp.getCollection()
                .concat("/")
                .concat(relationFetchOp.getKey())
                .concat("/relations");
        for (final String kind : relationFetchOp.getKinds()) {
            uri = uri.concat("/").concat(kind);
        }

        final HttpRequestPacket httpPacket = HttpRequestPacket
                .builder()
                .method(Method.GET)
                .uri(uri)
                .build();

        execute(httpPacket.httpContentBuilder().build(), future);
        return future;
    }

    /**
     * Executes the specified {@code relationStoreOp} on the Orchestrate.io
     * service.
     *
     * @param relationStoreOp The relation store operation to execute.
     * @return The future for the response from this operation.
     */
    public OrchestrateFuture<Boolean> execute(final RelationStoreOperation relationStoreOp) {
        final OrchestrateFuture<Boolean> future =
                new OrchestrateFutureImpl<Boolean>(relationStoreOp);

        final String uri = relationStoreOp.getCollection()
                .concat("/")
                .concat(relationStoreOp.getKey())
                .concat("/relation/")
                .concat(relationStoreOp.getKind())
                .concat("/")
                .concat(relationStoreOp.getToCollection())
                .concat("/")
                .concat(relationStoreOp.getToKey());

        final HttpRequestPacket httpPacket = HttpRequestPacket
                .builder()
                .method(Method.PUT)
                .uri(uri)
                .build();

        execute(httpPacket.httpContentBuilder().build(), future);
        return future;
    }

    /**
     * Executes the specified {@code searchOp} on the Orchestrate.io service.
     *
     * @param searchOp The search operation to execute.
     * @param <T> The type to deserialize the results to.
     * @return The future for the response from this operation.
     */
    public <T> OrchestrateFuture<SearchResults<T>> execute(final SearchOperation<T> searchOp) {
        final OrchestrateFuture<SearchResults<T>> future =
                new OrchestrateFutureImpl<SearchResults<T>>(searchOp);

        final String query = "query=".concat(searchOp.getQuery())
                .concat("&limit=").concat(searchOp.getLimit() + "")
                .concat("&offset=").concat(searchOp.getOffset() + "");

        final HttpRequestPacket httpPacket = HttpRequestPacket
                .builder()
                .method(Method.GET)
                .uri(searchOp.getCollection())
                .query(query)
                .build();

        execute(httpPacket.httpContentBuilder().build(), future);
        return future;
    }

    @SuppressWarnings("unchecked")
    private <T> void execute(final HttpContent httpPacket, final OrchestrateFuture<T> future) {
        final Connection connection;
        try {
            final Future<Connection> connectionFuture = newConnection();
            connection = connectionFuture.get(5, TimeUnit.SECONDS);
            log.info("{}", connection);
        } catch (final Exception e) {
            throw new ClientException(e);
        }

        // TODO abort the future early if the write fails
        connection.getAttributes().setAttribute(ClientFilter.HTTP_RESPONSE_ATTR, future);
        connection.write(httpPacket);
    }

    /**
     * Stops the thread pool and closes all connections in use by all the
     * operations.
     *
     * @throws IOException If resources couldn't be stopped.
     */
    public void stop() throws IOException {
        if (transport != null && !transport.isStopped()) {
            transport.shutdownNow();
        }
    }

    /**
     * A new builder to create a {@code Client} with default settings.
     *
     * @param apiKey An API key for the Orchestrate.io service.
     * @return A new {@code Builder} with default settings.
     */
    public static Builder builder(final String apiKey) {
        if (apiKey == null) {
            throw new IllegalArgumentException("'apiKey' cannot be null.");
        }
        if (apiKey.length() < 1) {
            throw new IllegalArgumentException("'apiKey' cannot be empty.");
        }
        if (apiKey.length() != 36) {
            final String message =
                    "'apiKey' is invalid. " +
                            "Currently the Orchestrate.io service uses 36 character keys.";
            throw new IllegalArgumentException(message);
        }
        return new Builder(apiKey);
    }

    /**
     * Builder used to create {@code Client} instances.
     *
     * <p>Usage:
     * <pre>
     * {@code
     * Client client = Client.builder("your api key")
     *         .host("https://api.orchestrate.io")  // optional
     *         .port(80)            // optional
     *         .version(Client.V0)  // optional
     *         .poolSize(0)         // optional
     *         .maxPoolSize(15)     // optional
     *         .build();
     * }
     * </pre>
     */
    public static final class Builder {

        /** The default host for the Orchestrate.io service. */
        public static final String DEFAULT_HOST = "https://api.orchestrate.io";
        /** The default port for the Orchestrate.io service. */
        public static final int DEFAULT_PORT = 80;

        /** An API key for the Orchestrate.io service. */
        private final String apiKey;
        /** The host for the Orchestrate.io service. */
        private URI host;
        /** The port for the Orchestrate.io service. */
        private int port;
        /** The version of the Orchestrate API to use. */
        private API version;
        /** The number of threads to use with the client. */
        private int poolSize;
        /** The maximum size of the thread pool to use with the client. */
        private int maxPoolSize;
        /** The configured JSON mapper. */
        private JacksonMapper mapper;

        private Builder(final String apiKey) {
            assert (apiKey != null);
            assert (apiKey.length() == 36);

            this.apiKey = apiKey;
            host(DEFAULT_HOST);
            port(DEFAULT_PORT);
            version(Client.V0);
            poolSize(0);
            // TODO benchmark this number
            maxPoolSize(Runtime.getRuntime().availableProcessors() * 15);
            mapper(JacksonMapper.builder());
        }

        /**
         * Set the hostname for the Orchestrate.io service, defaults to {@code
         * Builder.DEFAULT_HOST}.
         *
         * @param host The hostname for the Orchestrate.io service.
         * @return This builder.
         * @see Builder#DEFAULT_HOST
         */
        public Builder host(final String host) {
            if (host == null) {
                throw new IllegalArgumentException("'host' cannot be null.");
            }
            if (host.length() < 1) {
                throw new IllegalArgumentException("'host' cannot be empty.");
            }
            this.host = URI.create(host);
            return this;
        }

        /**
         * Set the port for the Orchestrate.io service, defaults to {@code
         * Builder.DEFAULT_PORT}.
         *
         * @param port The port for the Orchestrate.io service.
         * @return This builder.
         * @see Builder#DEFAULT_PORT
         */
        public Builder port(final int port) {
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("'port' must be between 1 and 65535.");
            }
            this.port = port;
            return this;
        }

        /**
         * The version of the API to use with the Orchestrate.io service,
         * defaults to the latest and greatest version of the API.
         *
         * @param version The version of the Orchestrate.io service to use, e.g.
         *                {@code Client.V0}.
         * @return This builder.
         */
        public Builder version(final API version) {
            if (version == null) {
                throw new IllegalArgumentException("'version' cannot be null.");
            }
            this.version = version;
            return this;
        }

        /**
         * The initial number of threads to use with the client, defaults to
         * zero.
         *
         * @param poolSize The size of the thread pool to start with.
         * @return This builder.
         */
        public Builder poolSize(final int poolSize) {
            if (poolSize < 0) {
                throw new IllegalArgumentException("'poolSize' cannot be negative.");
            }
            this.poolSize = poolSize;
            return this;
        }

        /**
         * The maximum number of threads to use with the client, defaults to a
         * factor of {@link Runtime#availableProcessors()}.
         *
         * @param maxPoolSize The maximum size to grow the thread pool to.
         * @return This builder.
         */
        public Builder maxPoolSize(final int maxPoolSize) {
            if (maxPoolSize < 1) {
                throw new IllegalArgumentException("'maxPoolSize' cannot be smaller than one.");
            }
            this.maxPoolSize = maxPoolSize;
            return this;
        }

        /**
         * The Jackson JSON {@code ObjectMapper} to use when marshalling data to
         * and from the service, defaults to {@link io.orchestrate.client.JacksonMapper#builder()}.
         *
         * @param objectMapper A Jackson JSON {@code ObjectMapper}.
         * @return This builder.
         */
        public Builder mapper(final ObjectMapper objectMapper) {
            if (objectMapper == null) {
                throw new IllegalArgumentException("'objectMapper' cannot be null.");
            }
            return mapper(JacksonMapper.builder(objectMapper));
        }

        /**
         * A {@code Builder} used to build the {@code JacksonMapper} to use when
         * marshalling data to and from the service.
         *
         * @param mapperBuilder A {@code JacksonMapper.Builder}.
         * @return This builder.
         */
        public Builder mapper(final JacksonMapper.Builder mapperBuilder) {
            if (mapperBuilder == null) {
                throw new IllegalArgumentException("'mapperBuilder' cannot be null.");
            }
            return mapper(mapperBuilder.build());
        }

        /**
         * A {@code JacksonMapper} to use when marshalling data to and from the
         * service.
         *
         * @param mapper A {@code JacksonMapper}.
         * @return This builder.
         */
        public Builder mapper(final JacksonMapper mapper) {
            if (mapper == null) {
                throw new IllegalArgumentException("'mapper' cannot be null.");
            }
            this.mapper = mapper;
            return this;
        }

        /**
         * Creates a new {@code Client}.
         *
         * @return A new {@link Client}.
         */
        public Client build() {
            return new Client(this);
        }

    }

}
