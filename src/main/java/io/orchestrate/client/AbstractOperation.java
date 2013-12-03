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

import lombok.AccessLevel;
import lombok.Getter;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpHeader;
import org.glassfish.grizzly.http.util.HttpStatus;

/**
 * A query operation to the Orchestrate.io service.
 *
 * @param <T> The type to deserialize the result of this operation to.
 */
abstract class AbstractOperation<T> {

    /** A future for the result of this operation. */
    @Getter(AccessLevel.PACKAGE)
    private final OrchestrateFutureImpl<T> future;

    AbstractOperation() {
        future = new OrchestrateFutureImpl<T>();
    }

    /**
     * Encodes this message as a HTTP request.
     *
     * @return The HTTP request object for this message.
     */
    abstract HttpContent encode();

    /**
     * Decodes the HTTP response to the type {@code T} of this message.
     *
     * @return The result type of this message.
     */
    abstract T decode(final HttpContent content, final HttpHeader header, final HttpStatus status);

    /**
     * Adds the specified {@code listener} to the future for this operation.
     *
     * @param listener The listener to notify when the future for this operation
     *                 completes.
     * @see OrchestrateFuture#addListener(OrchestrateFutureListener)
     */
    public final void addListener(final OrchestrateFutureListener<T> listener) {
        future.addListener(listener);
    }

    /**
     * Removes the specified {@code listener} to the future for this operation.
     *
     * @param listener The listener to remove from the future for this operation.
     * @see OrchestrateFuture#removeListener(OrchestrateFutureListener)
     */
    public final void removeListener(final OrchestrateFutureListener<T> listener) {
        future.removeListener(listener);
    }

}
