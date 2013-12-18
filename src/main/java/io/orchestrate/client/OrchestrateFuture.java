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

import java.util.concurrent.Future;

/**
 * A listenable future, based on {@link Future}.
 *
 * @param <T> The type of the future result.
 */
public interface OrchestrateFuture<T> extends Future<T> {

    /**
     * Return the operation for this future.
     *
     * @return The operation for this future.
     */
    AbstractOperation<T> getOperation();

    /**
     * Add the specified {@code listener} to this future.
     *
     * <p>The listener is notified when this future {@link #isDone()}. If the
     * future is already completed, the listener is notified immediately.
     *
     * @param listener The listener to notify when this future completes.
     */
    void addListener(final OrchestrateFutureListener<T> listener);

    /**
     * Remove the specified {@code listener} from this future.
     *
     * <p>The listener is no longer notified when this future {@link #isDone()}.
     * If the listener is not associated with this future, this method does
     * nothing and returns silently.
     *
     * @param listener The listener to remove from this future.
     */
    void removeListener(final OrchestrateFutureListener<T> listener);

}
