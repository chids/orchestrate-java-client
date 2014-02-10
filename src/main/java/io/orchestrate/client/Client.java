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

import java.io.IOException;

/**
 * A client used to read and write data to the Orchestrate.io service.
 */
public interface Client {

    /** Initial API; has KV, Events, Search, and early Graph support. */
    public static final API V0 = API.v0;

    /**
     * The different versions of the Orchestrate.io service.
     */
    enum API {
        v0
    }

    /**
     * Executes the specified {@code deleteOp} on the Orchestrate.io service.
     *
     * @param deleteOp The delete operation to execute.
     * @return A future for the response from this operation.
     */
    public OrchestrateFuture<Boolean> execute(final DeleteOperation deleteOp);

    /**
     * Executes the specified {@code eventFetchOp} on the Orchestrate.io service.
     *
     * @param eventFetchOp The event fetch operation to execute.
     * @param <T> The type to deserialize the results to.
     * @return A future for the response from this operation.
     */
    public <T> OrchestrateFuture<Iterable<Event<T>>> execute(final EventFetchOperation<T> eventFetchOp);

    /**
     * Executes the specified {@code eventStoreOp} on the Orchestrate.io service.
     *
     * @param eventStoreOp The event store operation to execute.
     * @return A future for the response from this operation.
     */
    public OrchestrateFuture<Boolean> execute(final EventStoreOperation eventStoreOp);

    /**
     * Executes the specified {@code kvFetchOp} on the Orchestrate.io service.
     *
     * @param kvFetchOp The KV fetch operation to execute.
     * @param <T> The type to deserialize the results to.
     * @return The future for the response from this operation.
     */
    public <T> OrchestrateFuture<KvObject<T>> execute(final KvFetchOperation<T> kvFetchOp);

    /**
     * Executes the specified {@code kvStoreOp} on the Orchestrate.io service.
     *
     * @param kvStoreOp The KV store operation to execute.
     * @return The future for the response from this operation.
     */
    public OrchestrateFuture<KvMetadata> execute(final KvStoreOperation kvStoreOp);

    /**
     * Executes the specified {@code relationFetchOp} on the Orchestrate.io
     * service.
     *
     * @param relationFetchOp The relation fetch operation to execute.
     * @return The future for the response from this operation.
     */
    public OrchestrateFuture<Iterable<KvObject<String>>> execute(final RelationFetchOperation relationFetchOp);

    /**
     * Executes the specified {@code relationStoreOp} on the Orchestrate.io
     * service.
     *
     * @param relationStoreOp The relation store operation to execute.
     * @return The future for the response from this operation.
     */
    public OrchestrateFuture<Boolean> execute(final RelationStoreOperation relationStoreOp);

    /**
     * Executes the specified {@code searchOp} on the Orchestrate.io service.
     *
     * @param searchOp The search operation to execute.
     * @param <T> The type to deserialize the results to.
     * @return The future for the response from this operation.
     */
    public <T> OrchestrateFuture<SearchResults<T>> execute(final SearchOperation<T> searchOp);

    /**
     * Stops the thread pool and closes all connections in use by all the
     * operations.
     *
     * @throws IOException If resources couldn't be stopped.
     */
    public void stop() throws IOException;

}
