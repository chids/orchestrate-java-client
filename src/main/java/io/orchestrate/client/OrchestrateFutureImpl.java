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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A listenable future based on {@link java.util.concurrent.Future} for an
 * operation.
 *
 * @param <T> The type of the future result.
 */
final class OrchestrateFutureImpl<T> implements OrchestrateFuture<T> {

    private enum State {
        CREATED, COMPLETE, CANCELLED;

        static void check(final State current, final State... valid) {
            assert (valid != null);

            if (Arrays.binarySearch(valid, current) < 0) {
                final String message =
                        "Future in illegal state, expected '" + Arrays.toString(valid) +
                        "', current '" + current + "'";
                throw new IllegalStateException(message);
            }
        }

    }

    /** The operation for this future. */
    private final AbstractOperation<T> operation;
    /** The synchronization util to wait for the result of this future. */
    private final CountDownLatch latch;
    /** The exception thrown by this future. */
    private volatile Throwable exception;
    /** The result of this future. */
    private volatile T result;
    /** The current state of the future operation. */
    private volatile State state;
    /** The mutex for synchronising listener set operations. */
    private final ReentrantLock listenersLock;
    /** The listeners for this future. */
    private final Set<OrchestrateFutureListener<T>> listeners;
    /** Tracks whether these listeners have already fired. */
    private volatile boolean listenersFired;

    OrchestrateFutureImpl(final AbstractOperation<T> operation) {
        this.operation = operation;
        latch = new CountDownLatch(1);
        exception = null;
        result = null;
        state = State.CREATED;
        listenersLock = new ReentrantLock();
        listeners = new HashSet<OrchestrateFutureListener<T>>(operation.getListeners());
        listenersFired = false;
    }

    synchronized void setResult(final T result) {
        State.check(state, State.CREATED);
        exception = null;
        this.result = result;
        state = State.COMPLETE;
        latch.countDown();
        fireListeners();
    }

    synchronized void setException(final Throwable exception) {
        State.check(state, State.CREATED);
        this.exception = exception;
        state = State.COMPLETE;
        latch.countDown();
        fireListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void addListener(final OrchestrateFutureListener<T> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("'listener' cannot be null.");
        }

        boolean fireNow = false;
        listenersLock.lock();
        try {
            if (listenersFired) {
                fireNow = true;
            } else {
                listeners.add(listener);
            }
        } finally {
            listenersLock.unlock();
        }

        if (fireNow) {
            if (exception != null) {
                listener.onException(this);
            } else {
                listener.onComplete(this);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeListener(final OrchestrateFutureListener<T> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("'listener' cannot be null.");
        }

        listenersLock.lock();
        try {
            if (!listenersFired) {
                listeners.remove(listener);
            }
            // else it's too late, we've fired listeners for this future
        } finally {
            listenersLock.unlock();
        }
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        return Boolean.FALSE;
    }

    @Override
    public boolean isCancelled() {
        return state == State.CANCELLED;
    }

    @Override
    public boolean isDone() {
        return state == State.COMPLETE;
    }

    @Nullable
    @Override
    public T get() throws InterruptedException, ExecutionException {
        latch.await();
        if (exception != null) {
            throw new ExecutionException(exception);
        }
        return result;
    }

    @Nullable
    @Override
    public T get(final long timeout, @Nonnull final TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        final boolean succeed = latch.await(timeout, unit);
        if (!succeed) {
            throw new TimeoutException();
        }
        if (exception != null) {
            throw new ExecutionException(exception);
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public AbstractOperation<T> getOperation() {
        return operation;
    }

    private void fireListeners() {
        boolean fireNow = false;
        listenersLock.lock();
        try {
            if (!listenersFired) {
                fireNow = true;
                listenersFired = true;
            }
        } finally {
            listenersLock.unlock();
        }

        if (fireNow) {
            for (final OrchestrateFutureListener<T> listener : listeners) {
                if (exception != null) {
                    listener.onException(this);
                } else {
                    listener.onComplete(this);
                }
            }
        }
    }

}
