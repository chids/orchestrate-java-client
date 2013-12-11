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

/**
 * Listens to the result of a {@link OrchestrateFuture}.
 */
public interface OrchestrateFutureListener<T> {

    /**
     * Invoked when the operation associated with the {@link OrchestrateFuture}
     * has been completed.
     *
     * @param future The source {@link OrchestrateFuture} which called this
     *               callback.
     */
    void onComplete(final OrchestrateFuture<T> future);

    /**
     * Invoked when the operation associated with the {@link OrchestrateFuture}
     * has failed.
     *
     * @param future The source {@link OrchestrateFuture} which called this
     *               callback.
     */
    void onException(final OrchestrateFuture<T> future);

}
