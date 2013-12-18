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

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.glassfish.grizzly.http.HttpHeader;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Store an event to a key in the Orchestrate.io service.
 *
 * <p>Usage:
 * <pre>
 * {@code
 * MyObject obj = new MyObject(...);
 * EventStoreOperation eventStoreOp =
 *         new EventStoreOperation("myCollection", "someKey", "eventType", obj);
 * Future<Boolean> futureResult = client.execute(deleteOp);
 * Boolean result = futureResult.get();
 * if (result)
 *     System.out.println("Successfully stored the event.");
 * }
 * </pre>
 */
@ToString(callSuper=false)
@EqualsAndHashCode(callSuper=false)
public final class EventStoreOperation extends AbstractOperation<Boolean> {

    /** The collection containing the key. */
    private final String collection;
    /** The key to store the event to. */
    private final String key;
    /** The type of event to store. */
    private final String type;
    /** The object to store as the event. */
    private final Object value;
    /** The timestamp to store the event at. */
    private final Long timestamp;

    /**
     * Create a new {@code EventStoreOperation} to store the supplied
     * {@code value} with a {@code type} to the {@code key} in the
     * {@code collection}.
     *
     * @param collection The collection containing the key.
     * @param key The key to store the event to.
     * @param type The type of event.
     * @param value The object to store as the event.
     */
    public EventStoreOperation(
            final String collection, final String key, final String type, final Object value) {
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
        if (value == null) {
            throw new IllegalArgumentException("'value' cannot be null.");
        }
        this.collection = collection;
        this.key = key;
        this.type = type;
        this.value = value;
        this.timestamp = null;
    }

    /**
     * Create a new {@code EventStoreOperation} to store the supplied
     * {@code value} with a {@code type} to the {@code key} in the
     * {@code collection} at the {@code timestamp} point in time.
     *
     * @param collection The collection containing the key.
     * @param key The key to store the event to.
     * @param type The type of event.
     * @param value The object to store as the event.
     * @param timestamp The timestamp to store the event at.
     */
    public EventStoreOperation(
            final String collection, final String key, final String type, final Object value, final long timestamp) {
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
        if (value == null) {
            throw new IllegalArgumentException("'value' cannot be null.");
        }
        if (timestamp < 0) {
            throw new IllegalArgumentException("'timestamp' cannot be negative.");
        }
        this.collection = collection;
        this.key = key;
        this.type = type;
        this.value = value;
        this.timestamp = timestamp;
    }

    /** {@inheritDoc} */
    @Override
    Boolean fromResponse(
            final int status, final HttpHeader httpHeader, final String json, final JacksonMapper mapper)
            throws IOException {
        return (status == 204);
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
     * Returns the value from this operation.
     *
     * @return The value from this operation.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns the timestamp from this operation.
     *
     * @return The timestamp from this operation, may be {@code null}.
     */
    @Nullable
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns whether a timestamp was supplied to this operation.
     *
     * @return {@code true} if a timestamp was supplied to this operation.
     */
    public boolean hasTimestamp() {
        return (timestamp != null);
    }

}
