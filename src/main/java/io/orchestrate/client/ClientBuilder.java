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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;

import java.net.URI;

/**
 * Builder used to create {@code Client} instances.
 *
 * <p>Usage:
 * <pre>
 * {@code
 * Client client = new ClientBuilder("your api key")
 *         .host("https://api.orchestrate.io")  // optional
 *         .port(80)           // optional
 *         .version(Client.V0) // optional
 *         .poolSize(0)        // optional
 *         .maxPoolSize(15)    // optional
 *         .build();
 * }
 * </pre>
 */
@Getter(AccessLevel.PACKAGE)
public final class ClientBuilder {

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
    private Client.API version;
    /** The number of threads to use with the client. */
    private int poolSize;
    /** The maximum size of the thread pool to use with the client. */
    private int maxPoolSize;
    /** The configured JSON mapper. */
    private JacksonMapper mapper;

    public ClientBuilder(final String apiKey) {
        if (apiKey == null) {
            throw new IllegalArgumentException("'apiKey' cannot be null.");
        }
        if (apiKey.length() < 1) {
            throw new IllegalArgumentException("'apiKey' cannot be empty.");
        }

        this.apiKey = apiKey;
        host(DEFAULT_HOST);
        port(DEFAULT_PORT);
        version(HttpClient.V0);
        poolSize(Runtime.getRuntime().availableProcessors());
        maxPoolSize(Integer.MAX_VALUE);
        mapper(JacksonMapper.builder());
    }

    /**
     * Set the hostname for the Orchestrate.io service, defaults to {@code
     * Builder.DEFAULT_HOST}.
     *
     * @param host The hostname for the Orchestrate.io service.
     * @return This builder.
     * @see ClientBuilder#DEFAULT_HOST
     */
    public ClientBuilder host(final String host) {
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
     * @see ClientBuilder#DEFAULT_PORT
     */
    public ClientBuilder port(final int port) {
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
    public ClientBuilder version(final Client.API version) {
        if (version == null) {
            throw new IllegalArgumentException("'version' cannot be null.");
        }
        this.version = version;
        return this;
    }

    /**
     * The initial number of threads to use with the client, defaults to
     * {@link Runtime#availableProcessors()}.
     *
     * @param poolSize The size of the thread pool to start with.
     * @return This builder.
     */
    public ClientBuilder poolSize(final int poolSize) {
        if (poolSize < 0) {
            throw new IllegalArgumentException("'poolSize' cannot be negative.");
        }
        this.poolSize = poolSize;
        return this;
    }

    /**
     * The maximum number of threads to use with the client, defaults to
     * {@link Integer#MAX_VALUE}.
     *
     * @param maxPoolSize The maximum size to grow the thread pool to.
     * @return This builder.
     */
    public ClientBuilder maxPoolSize(final int maxPoolSize) {
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
    public ClientBuilder mapper(final ObjectMapper objectMapper) {
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
    public ClientBuilder mapper(final JacksonMapper.Builder mapperBuilder) {
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
    public ClientBuilder mapper(final JacksonMapper mapper) {
        if (mapper == null) {
            throw new IllegalArgumentException("'mapper' cannot be null.");
        }
        this.mapper = mapper;
        return this;
    }

    public Client build() {
        return new HttpClient(this);
    }

}
