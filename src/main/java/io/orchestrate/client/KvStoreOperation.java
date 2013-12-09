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
import org.glassfish.grizzly.http.*;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.grizzly.memory.ByteBufferWrapper;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;

// TODO document this
public final class KvStoreOperation extends AbstractOperation<KvMetadata> {

    /**  */
    private final String collection;
    /**  */
    private final String key;
    /**  */
    private final Object value;
    /**  */
    private final String currentRef;
    /**  */
    private final boolean ifAbsent;

    public KvStoreOperation(
            final String collection, final String key, final Object value) {
        this(collection, key, value, null, false);
    }

    public KvStoreOperation(
            final String collection, final String key, final Object value, final boolean ifAbsent) {
        this(collection, key, value, null, ifAbsent);
    }

    public KvStoreOperation(
            final String collection, final String key, final Object value, final KvMetadata metadata) {
        this(collection, key, value, metadata.getRef());
    }

    public KvStoreOperation(
            final String collection, final String key, final Object value, final String currentRef) {
        this(collection, key, value, currentRef, false);
    }

    private KvStoreOperation(
            final String collection,
            final String key,
            final Object value,
            @Nullable final String currentRef,
            final boolean ifAbsent) {
        // TODO add input checking
        this.collection = collection;
        this.key = key;
        this.value = value;
        this.currentRef = currentRef;
        this.ifAbsent = ifAbsent;
    }

    /** {@inheritDoc} */
    @Override
    HttpContent encode() {
        assert !(currentRef != null && ifAbsent);

        final String uri = collection.concat("/").concat(key);

        final HttpRequestPacket.Builder httpHeaderBuilder = HttpRequestPacket.builder()
                .method(Method.PUT)
                .contentType("application/json")
                .uri(uri);
        if (currentRef != null) {
            httpHeaderBuilder.header(Header.IfMatch, "\"".concat(currentRef).concat("\""));
        } else if (ifAbsent) {
            httpHeaderBuilder.header(Header.IfNoneMatch, "*");
        }

        final String json;
        try {
            json = Client.MAPPER.writeValueAsString(value);
        } catch (final JsonProcessingException e) {
            throw new ConversionException(e);
        }

        final ByteBuffer contentBuffer = ByteBuffer.wrap(json.getBytes());
        final HttpRequestPacket httpHeader = httpHeaderBuilder
                .contentLength(contentBuffer.remaining())
                .build();

        return httpHeader.httpContentBuilder()
                .httpHeader(httpHeader)
                .content(new ByteBufferWrapper(contentBuffer))
                .build();
    }

    /** {@inheritDoc} */
    @Override
    KvMetadata decode(final HttpContent content, final HttpHeader header, final HttpStatus status) {
        switch (status.getStatusCode()) {
            case 201:
                final String ref = header.getHeader(Header.ETag)
                        .replace("\"", "")
                        .replace("-gzip", "");
                return new KvMetadata(collection, key, ref);
            default:
                // FIXME do better with this error handling
                throw new RuntimeException();
        }
    }

}
