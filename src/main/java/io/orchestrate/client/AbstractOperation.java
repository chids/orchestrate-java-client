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
import org.glassfish.grizzly.http.HttpHeader;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A query operation to the Orchestrate.io service.
 *
 * @param <T> The type to result the result of this operation to.
 */
@EqualsAndHashCode
abstract class AbstractOperation<T> {

    /** The list of listeners for the operation's future. */
    private final List<OrchestrateFutureListener<T>> listeners;

    AbstractOperation() {
        listeners = new LinkedList<OrchestrateFutureListener<T>>();
    }

    /**
     * Constructs the result type {@code T} from the response.
     *
     * @param status The status code from the response.
     * @param httpHeader The HTTP header from the response.
     * @param json The response content.
     * @param mapper The mapper to use when marshalling objects from JSON.
     * @return The result type of this operation.
     * @throws IOException If there was a problem processing the response.
     */
    abstract T fromResponse(
            final int status, final HttpHeader httpHeader, final String json, final JacksonMapper mapper)
            throws IOException;

    /**
     * Adds the specified {@code listeners} to the future for this operation.
     *
     * @param listeners The listeners to notify when the future for this operation
     *                  completes.
     * @see io.orchestrate.client.OrchestrateFuture#addListener(OrchestrateFutureListener)
     */
    public final void addListener(final Iterable<OrchestrateFutureListener<T>> listeners) {
        if (listeners == null) {
            throw new IllegalArgumentException("'listeners' cannot be null.");
        }
        for (final OrchestrateFutureListener<T> listener : listeners) {
            this.listeners.add(listener);
        }
    }

    /**
     * Returns the list of listeners for this operation's future.
     *
     * @return The list of listeners for this operation's future.
     */
    public final List<OrchestrateFutureListener<T>> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    /**
     * Removes the specified {@code listeners} to the future for this operation.
     *
     * @param listeners The listeners to remove from the future for this operation.
     * @see io.orchestrate.client.OrchestrateFuture#removeListener(OrchestrateFutureListener)
     */
    public final void removeListener(final Iterable<OrchestrateFutureListener<T>> listeners) {
        if (listeners == null) {
            throw new IllegalArgumentException("'listeners' cannot be null.");
        }
        for (final OrchestrateFutureListener<T> listener : listeners) {
            this.listeners.remove(listener);
        }
    }

}
