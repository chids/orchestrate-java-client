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

/**
 * A container for the event and its associated KV data.
 *
 * @param <T> The deserializable type for the value of the KV data belonging
 *            to this event.
 */
@ToString
@EqualsAndHashCode
public class Event<T> {

    /** The value for this event. */
    private final T value;
    /** The raw JSON value for this event. */
    private final String rawValue;
    /** The timestamp of this event. */
    private final long timestamp;

    Event(final T value, final String rawValue, final long timestamp) {
        assert (value != null);
        assert (rawValue != null);
        assert (timestamp >= 0);

        this.value = value;
        this.rawValue = rawValue;
        this.timestamp = timestamp;
    }

    /**
     * Returns the KV object for this event.
     *
     * @return The KV object for this event.
     */
    public final T getValue() {
        return value;
    }

    /**
     * Returns the raw JSON value of this event.
     *
     * @return The raw JSON value of this event.
     */
    public final String rawValue() {
        return rawValue;
    }

    /**
     * Returns the timestamp of this event.
     *
     * @return The timestamp for this event.
     */
    public final long getTimestamp() {
        return timestamp;
    }

}
