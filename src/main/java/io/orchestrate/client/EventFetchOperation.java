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
import com.fasterxml.jackson.databind.JsonNode;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpHeader;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.util.HttpStatus;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// TODO document this
public final class EventFetchOperation<T> extends AbstractOperation<Iterable<Event<T>>> {

    private final String collection;
    private final String key;
    private final String type;
    private final Class<T> clazz;
    private final Long start;
    private final Long end;

    public EventFetchOperation(
            final String collection, final String key, final String type, final Class<T> clazz) {
        this(collection, key, type, clazz, null, null);
    }

    public EventFetchOperation(
            final String collection, final String key, final String type, final Class<T> clazz, final long start) {
        this(collection, key, type, clazz, start, null);
    }

    public EventFetchOperation(
            final String collection,
            final String key,
            final String type,
            final Class<T> clazz,
            @Nullable final Long start,
            @Nullable final Long end) {
        // TODO add input validation
        this.collection = collection;
        this.key = key;
        this.type = type;
        this.clazz = clazz;
        this.start = start;
        this.end = end;
    }

    /** {@inheritDoc} */
    @Override
    HttpContent encode() {
        final String uri = collection.concat("/").concat(key)
                .concat("/events/").concat(type);
        final HttpRequestPacket.Builder httpHeaderBuilder = HttpRequestPacket.builder()
                .method(Method.GET)
                .uri(uri);
        String query = null;
        if (start != null) {
            query = "start=".concat(start.toString());
        }
        if (end != null) {
            if (start != null) {
                query = query.concat("&");
            }
            query = query.concat("end=").concat(end.toString());
        }
        final HttpRequestPacket httpHeader = httpHeaderBuilder.query(query).build();

        return httpHeader.httpContentBuilder()
                .httpHeader(httpHeader)
                .build();
    }

    /** {@inheritDoc} */
    @Override
    Iterable<Event<T>> decode(
            final HttpContent content, final HttpHeader header, final HttpStatus status) {
        switch (status.getStatusCode()) {
            case 200:
                final JsonNode jsonNode;
                try {
                    jsonNode = Client.MAPPER.readTree(content.getContent().toStringContent());
                } catch (final IOException e) {
                    throw new ConversionException(e);
                }

                final int count = jsonNode.get("count").asInt();
                final List<Event<T>> events = new ArrayList<Event<T>>(count);
                final Iterator<JsonNode> iter = jsonNode.get("results").elements();
                while (iter.hasNext()) {
                    final JsonNode result = iter.next();

                    final long timestamp = result.get("timestamp").asLong();

                    final JsonNode valueNode = result.get("value");
                    final String rawValue = valueNode.toString();

                    final T value;
                    try {
                        value = Client.MAPPER.treeToValue(valueNode, clazz);
                    } catch (final JsonProcessingException e) {
                        throw new ConversionException(e);
                    }

                    events.add(new Event<T>(value, rawValue, timestamp));
                }

                return events;
            default:
                // FIXME do better with this error handling
                throw new RuntimeException();
        }
    }

}
