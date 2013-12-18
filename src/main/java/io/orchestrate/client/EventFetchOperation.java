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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.glassfish.grizzly.http.HttpHeader;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Fetch events for a key in the Orchestrate.io service.
 *
 * <p>Usage:
 * <pre>
 * {@code
 * EventFetchOperation<MyObject> eventFetchOp =
 *         new EventFetchOperation<MyObject>("myCollection", "someKey", "eventType");
 * Future<Iterable<Event<MyObject>> futureResult = client.execute(eventFetchOp);
 * Iterable<Event<MyObject>> results = futureResult.get();
 * for (Event<MyObject> event : results)
 *     System.out.println(event.getTimestamp());
 * }
 * </pre>
 *
 * @param <T> The type to deserialize the result of this operation to.
 * @see <a href="http://orchestrate-io.github.io/orchestrate-java-client/querying/#fetch-events">http://orchestrate-io.github.io/orchestrate-java-client/querying/#fetch-events</a>
 */
@ToString(callSuper=false)
@EqualsAndHashCode(callSuper=false)
public final class EventFetchOperation<T> extends AbstractOperation<Iterable<Event<T>>> {

    /** The collection containing the key. */
    private final String collection;
    /** The key to query for events. */
    private final String key;
    /** The type of events to query. */
    private final String type;
    /** The timestamp to get events from. */
    private final Long start;
    /** The timestamp to get events up to. */
    private final Long end;
    /** Type information for marshalling objects at runtime. */
    protected final TypeReference<T> genericType;

    /**
     * Create a new {@code EventFetchOperation} to get events of the specified
     * {@code type} from the {@code key} in the {@code collection}.
     *
     * @param collection The collection containing the key.
     * @param key The key to query for events.
     * @param type The type of event.
     */
    public EventFetchOperation(final String collection, final String key, final String type) {
        if (collection == null) {
            throw new IllegalArgumentException("'collection' cannot be null.");
        }
        if (collection.length() < 1) {
            throw new IllegalArgumentException("'collection' cannot be empty.");
        }
        if (key == null) {
            throw new IllegalArgumentException("'key' cannot be null.");
        }
        if (key.length() < 1) {
            throw new IllegalArgumentException("'key' cannot be empty.");
        }
        if (type == null) {
            throw new IllegalArgumentException("'type' cannot be null.");
        }
        if (type.length() < 1) {
            throw new IllegalArgumentException("'type' cannot be empty.");
        }
        this.collection = collection;
        this.key = key;
        this.type = type;
        this.start = null;
        this.end = null;
        this.genericType = new TypeReference<T>() {};
    }

    /**
     * Create a new {@code EventFetchOperation} to get events of the specified
     * {@code type} from the {@code key} in the {@code collection} from the
     * {@code start} point in time.
     *
     * @param collection The collection containing the key.
     * @param key The key to query for events.
     * @param type The type of event.
     * @param start The start point to get events from.
     */
    public EventFetchOperation(
            final String collection, final String key, final String type, final long start) {
        if (collection == null) {
            throw new IllegalArgumentException("'collection' cannot be null.");
        }
        if (collection.length() < 1) {
            throw new IllegalArgumentException("'collection' cannot be empty.");
        }
        if (key == null) {
            throw new IllegalArgumentException("'key' cannot be null.");
        }
        if (key.length() < 1) {
            throw new IllegalArgumentException("'key' cannot be empty.");
        }
        if (type == null) {
            throw new IllegalArgumentException("'type' cannot be null.");
        }
        if (type.length() < 1) {
            throw new IllegalArgumentException("'type' cannot be empty.");
        }
        if (start < 0) {
            throw new IllegalArgumentException("'start' cannot be negative.");
        }
        this.collection = collection;
        this.key = key;
        this.type = type;
        this.start = start;
        this.end = null;
        this.genericType = new TypeReference<T>() {};
    }

    /**
     * Create a new {@code EventFetchOperation} to get events of the specified
     * {@code type} from the {@code key} in the {@code collection} between the
     * {@code start} and {@code end} range.
     *
     * @param collection The collection containing the key.
     * @param key The key to query for events.
     * @param type The type of event.
     * @param start The start point to get events from.
     * @param end The end point to get events up to.
     */
    public EventFetchOperation(
            final String collection, final String key, final String type, final long start, final long end) {
        if (collection == null) {
            throw new IllegalArgumentException("'collection' cannot be null.");
        }
        if (collection.length() < 1) {
            throw new IllegalArgumentException("'collection' cannot be empty.");
        }
        if (key == null) {
            throw new IllegalArgumentException("'key' cannot be null.");
        }
        if (key.length() < 1) {
            throw new IllegalArgumentException("'key' cannot be empty.");
        }
        if (type == null) {
            throw new IllegalArgumentException("'type' cannot be null.");
        }
        if (type.length() < 1) {
            throw new IllegalArgumentException("'type' cannot be empty.");
        }
        if (start < 0) {
            throw new IllegalArgumentException("'start' cannot be negative.");
        }
        if (end < 0) {
            throw new IllegalArgumentException("'end' cannot be negative.");
        }
        this.collection = collection;
        this.key = key;
        this.type = type;
        this.start = start;
        this.end = end;
        this.genericType = new TypeReference<T>() {};
    }

    /** {@inheritDoc} */
    @Override
    Iterable<Event<T>> fromResponse(
            final int status, final HttpHeader httpHeader, final String json, final JacksonMapper mapper)
            throws IOException {
        assert (status == 200);

        final ObjectMapper objectMapper = mapper.getMapper();
        final JsonNode jsonNode = objectMapper.readTree(json);

        final int count = jsonNode.get("count").asInt();
        final List<Event<T>> events = new ArrayList<Event<T>>(count);

        final Iterator<JsonNode> iter = jsonNode.get("results").elements();
        while (iter.hasNext()) {
            final JsonNode result = iter.next();

            final long timestamp = result.get("timestamp").asLong();

            final JsonNode valueNode = result.get("value");
            final String rawValue = valueNode.toString();
            final T value = objectMapper.readValue(rawValue, genericType);

            events.add(new Event<T>(value, rawValue, timestamp));
        }
        return events;
    }

    /**
     * Returns the collection from this operation.
     *
     * @return The collection from this operation.
     */
    public String getCollection() {
        return collection;
    }

    /**
     * Returns the key from this operation.
     *
     * @return The key from this operation.
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the type from this operation.
     *
     * @return The type from this operation.
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the start timestamp from this operation.
     *
     * @return The start timestamp from this operation, may be {@code null}.
     * @see #hasStart()
     */
    @Nullable
    public Long getStart() {
        return start;
    }

    /**
     * Returns the end timestamp from this operation.
     *
     * @return The end timestamp from this operation, may be {@code null}.
     * @see #hasEnd()
     */
    @Nullable
    public Long getEnd() {
        return end;
    }

    /**
     * Returns whether a start timestamp was supplied to this operation.
     *
     * @return {@code true} if a start timestamp was supplied to this operation.
     */
    public boolean hasStart() {
        return (start != null);
    }

    /**
     * Returns whether an end timestamp was supplied to this operation.
     *
     * @return {@code true} if an end timestamp was supplied to this operation.
     */
    public boolean hasEnd() {
        return (end != null);
    }

}
