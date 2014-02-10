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
import java.nio.ByteBuffer;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A client used to read and write data to the Orchestrate.io service.
 *
 * <p>Usage:
 * <pre>
 * {@code
 * Client client = new ClientBuilder("your api key").build();
 *
 * // OR (as a shorthand with default settings):
 * Client client = new HttpClient("your api key");
 * }
 * </pre>
 */
@Slf4j
public final class HttpClient implements Client {

    /** The builder for this instance of the client. */
    private final ClientBuilder builder;
    /** The transport implementation for socket handling. */
    private final NIOTransport transport;

    /**
     * Create a new {@code client} with the specified {@code apiKey} and default
     * {@code JacksonMapper}.
     *
     * <p>Equivalent to:
     * <pre>
     * {@code
     * Client client = new ClientBuilder("your api key").build();
     * }
     * </pre>
     *
     * @param apiKey An API key for the Orchestrate.io service.
     */
    public HttpClient(final String apiKey) {
        this(new ClientBuilder(apiKey));
    }

    /**
     * Create a new {@code client} with the specified {@code apiKey} and {@code
     * objectMapper}.
     *
     * @param apiKey An API key for the Orchestrate.io service.
     * @param objectMapper The Jackson JSON mapper to marshall data with.
     */
    public HttpClient(final String apiKey, final ObjectMapper objectMapper) {
        this(new ClientBuilder(apiKey).mapper(objectMapper));
    }

    /**
     * Create a new {@code client} with the specified {@code apiKey} and {@code
     * mapper}.
     *
     * @param apiKey An API key for the Orchestrate.io service.
     * @param mapper The mapper to marshall data with.
     */
    public HttpClient(final String apiKey, final JacksonMapper mapper) {
        this(new ClientBuilder(apiKey).mapper(mapper));
    }

    /**
     * A client configured via the {@code Builder}.
     *
     * @param builder The builder used to configure the client.
     */
    HttpClient(final ClientBuilder builder) {
        assert (builder != null);

        this.builder = builder;

        // TODO allow a custom executor service to be provided?
        final ThreadPoolConfig poolConfig = ThreadPoolConfig.defaultConfig()
                .setPoolName("OrchestrateClientPool")
                .setCorePoolSize(builder.getPoolSize())
                .setMaxPoolSize(builder.getMaxPoolSize());

        // TODO add support for GZip compression
        // TODO add SSL support
        final FilterChain filterChain = FilterChainBuilder.stateless()
                .add(new TransportFilter())
//                .add(new IdleTimeoutFilter(timeoutExecutor, 10, TimeUnit.SECONDS))
                .add(new HttpClientFilter())
                .add(new ClientFilter(builder))
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

            return transport.connect(builder.getHost().getHost(), builder.getPort());
        } catch (final Exception e) {
            throw new ClientException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
    public OrchestrateFuture<Boolean> execute(final EventStoreOperation eventStoreOp) {
        final OrchestrateFutureImpl<Boolean> future =
                new OrchestrateFutureImpl<Boolean>(eventStoreOp);

        final ObjectMapper mapper = builder.getMapper().getMapper();
        final byte[] content;
        try {
            final Object value = eventStoreOp.getValue();
            if (value instanceof String) {
                content = ((String) value).getBytes();
            } else {
                content = mapper.writeValueAsBytes(value);
            }
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

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
    public OrchestrateFuture<KvMetadata> execute(final KvStoreOperation kvStoreOp) {
        final OrchestrateFutureImpl<KvMetadata> future =
                new OrchestrateFutureImpl<KvMetadata>(kvStoreOp);

        final ObjectMapper mapper = builder.getMapper().getMapper();
        final byte[] content;
        try {
            final Object value = kvStoreOp.getValue();
            if (value instanceof String) {
                content = ((String) value).getBytes();
            } else {
                content = mapper.writeValueAsBytes(value);
            }
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

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
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

    private <T> void execute(final HttpContent httpPacket, final OrchestrateFuture<T> future) {
        final Connection<?> connection;
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

    /** {@inheritDoc} */
    @Override
    public void stop() throws IOException {
        if (transport != null && !transport.isStopped()) {
            transport.shutdownNow();
        }
    }

}
