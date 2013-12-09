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
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpHeader;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.grizzly.memory.ByteBufferWrapper;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;

// TODO document this
public final class EventStoreOperation extends AbstractOperation<Boolean> {

    private final String collection;
    private final String key;
    private final String type;
    private final Object value;
    private final Long timestamp;

    public EventStoreOperation(
            final String collection, final String key, final String type, final Object value) {
        this(collection, key, type, value, null);
    }

    public EventStoreOperation(
            final String collection,
            final String key,
            final String type,
            final Object value,
            @Nullable final Long timestamp) {
        // TODO add input validation
        this.collection = collection;
        this.key = key;
        this.type = type;
        this.value = value;
        this.timestamp = timestamp;
    }

    public EventStoreOperation(
            final KvObject<?> kvObject, final String type, final Object value) {
        this(kvObject.getCollection(), kvObject.getKey(), type, value);
    }

    public EventStoreOperation(
            final KvObject<?> kvObject, final String type, final Object value, final long timestamp) {
        this(kvObject.getCollection(), kvObject.getKey(), type, value, timestamp);
    }

    /** {@inheritDoc} */
    @Override
    HttpContent encode() {
        final String uri = collection.concat("/").concat(key)
                .concat("/events/").concat(type);

        final HttpRequestPacket.Builder httpHeaderBuilder = HttpRequestPacket.builder()
                .method(Method.PUT)
                .contentType("application/json")
                .uri(uri);
        if (timestamp != null) {
            httpHeaderBuilder.query("timestamp=".concat(timestamp.toString()));
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
    Boolean decode(final HttpContent content, final HttpHeader header, final HttpStatus status) {
        switch (status.getStatusCode()) {
            case 204:
                return Boolean.TRUE;
            default:
                // FIXME do better with this error handling
                throw new RuntimeException();
        }
    }

}
