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
package io.orchestrate.client.dao;

import io.orchestrate.client.*;

import java.util.concurrent.Future;

import static java.lang.String.format;

/**
 * A generic object for CRUD data access operations.
 *
 * @param <T> The type this object will control CRUD operations for.
 */
public abstract class GenericAsyncDao<T> implements AsyncDao<T> {

    /** The client to query the Orchestrate.io service. */
    private final Client client;
    /** The name of the collection to store objects to. */
    private final String collection;

    /**
     * Create a data access object to store objects of type {@code T} to the
     * specified {@code collection} using the {@code client}.
     *
     * @param client The client used to query the Orchestrate.io service.
     * @param collection The name of the collection to store objects to.
     */
    public GenericAsyncDao(final Client client, final String collection) {
        if (client == null) {
            throw new IllegalArgumentException("'client' cannot be null.");
        }
        if (collection == null) {
            throw new IllegalArgumentException("'collection' cannot be null.");
        }
        if (collection.length() < 1) {
            throw new IllegalArgumentException("'collection' cannot be empty.");
        }
        this.client = client;
        this.collection = collection;
    }

    /** {@inheritDoc} */
    @Override
    public Future<KvObject<T>> findOne(final String key) {
        if (key == null) {
            throw new IllegalArgumentException("'key' cannot be null.");
        }
        if (key.length() < 1) {
            throw new IllegalArgumentException("'key' cannot be empty.");
        }
        final KvFetchOperation<T> kvFetchOp = new KvFetchOperation<T>(collection, key);
        return client.execute(kvFetchOp);
    }

    /** {@inheritDoc} */
    @Override
    public Future<KvObject<T>> findOne(final String key, final KvMetadata metadata) {
        if (key == null) {
            throw new IllegalArgumentException("'key' cannot be null.");
        }
        if (key.length() < 1) {
            throw new IllegalArgumentException("'key' cannot be empty.");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("'metadata' cannot be null.");
        }
        final KvFetchOperation<T> kvFetchOp =
                new KvFetchOperation<T>(collection, key, metadata);
        return client.execute(kvFetchOp);
    }

    /** {@inheritDoc} */
    @Override
    public Future<KvObject<T>> findOne(final String key, final String ref) {
        if (key == null) {
            throw new IllegalArgumentException("'key' cannot be null.");
        }
        if (key.length() < 1) {
            throw new IllegalArgumentException("'key' cannot be empty.");
        }
        if (ref == null) {
            throw new IllegalArgumentException("'ref' cannot be null.");
        }
        if (ref.length() < 1) {
            throw new IllegalArgumentException("'ref' cannot be empty.");
        }
        final KvFetchOperation<T> kvFetchOp =
                new KvFetchOperation<T>(collection, key, ref);
        return client.execute(kvFetchOp);
    }

    /** {@inheritDoc} */
    @Override
    public Future<KvMetadata> save(final String key, final T value) {
        if (key == null) {
            throw new IllegalArgumentException("'key' cannot be null.");
        }
        if (key.length() < 1) {
            throw new IllegalArgumentException("'key' cannot be empty.");
        }
        if (value == null) {
            throw new IllegalArgumentException("'value' cannot be null.");
        }
        final KvStoreOperation kvStoreOp = new KvStoreOperation(collection, key, value);
        return client.execute(kvStoreOp);
    }

    /** {@inheritDoc} */
    @Override
    public Future<KvMetadata> save(final String key, final T value, final boolean ifAbsent) {
        if (key == null) {
            throw new IllegalArgumentException("'key' cannot be null.");
        }
        if (key.length() < 1) {
            throw new IllegalArgumentException("'key' cannot be empty.");
        }
        if (value == null) {
            throw new IllegalArgumentException("'value' cannot be null.");
        }
        final KvStoreOperation kvStoreOp =
                new KvStoreOperation(collection, key, value, ifAbsent);
        return client.execute(kvStoreOp);
    }

    /** {@inheritDoc} */
    @Override
    public Future<KvMetadata> save(final String key, final T value, final KvMetadata metadata) {
        if (key == null) {
            throw new IllegalArgumentException("'key' cannot be null.");
        }
        if (key.length() < 1) {
            throw new IllegalArgumentException("'key' cannot be empty.");
        }
        if (value == null) {
            throw new IllegalArgumentException("'value' cannot be null.");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("'metadata' cannot be null.");
        }
        final KvStoreOperation kvStoreOp =
                new KvStoreOperation(collection, key, value, metadata);
        return client.execute(kvStoreOp);
    }

    /** {@inheritDoc} */
    @Override
    public Future<KvMetadata> save(final String key, final T value, final String currentRef) {
        if (key == null) {
            throw new IllegalArgumentException("'key' cannot be null.");
        }
        if (key.length() < 1) {
            throw new IllegalArgumentException("'key' cannot be empty.");
        }
        if (value == null) {
            throw new IllegalArgumentException("'value' cannot be null.");
        }
        if (currentRef == null) {
            throw new IllegalArgumentException("'currentRef' cannot be null.");
        }
        if (currentRef.length() < 1) {
            throw new IllegalArgumentException("'currentRef' cannot be empty.");
        }
        final KvStoreOperation kvStoreOp =
                new KvStoreOperation(collection, key, value, currentRef);
        return client.execute(kvStoreOp);
    }

    /** {@inheritDoc} */
    @Override
    public Future<Boolean> delete(final String key) {
        if (key == null) {
            throw new IllegalArgumentException("'key' cannot be null.");
        }
        if (key.length() < 1) {
            throw new IllegalArgumentException("'key' cannot be empty.");
        }
        final DeleteOperation deleteOp = new DeleteOperation(collection, key);
        return client.execute(deleteOp);
    }

    /** {@inheritDoc} */
    @Override
    public Future<Boolean> delete(final String key, final KvMetadata metadata) {
        if (key == null) {
            throw new IllegalArgumentException("'key' cannot be null.");
        }
        if (key.length() < 1) {
            throw new IllegalArgumentException("'key' cannot be empty.");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("'metadata' cannot be null.");
        }
        if (!metadata.getKey().equals(key)) {
            final String msg = "'metadata' belongs to a different key, " +
                    format("expected '%s' but found '%s'.", key, metadata.getKey());
            throw new IllegalArgumentException(msg);
        }
        final DeleteOperation deleteOp = new DeleteOperation(collection, key, metadata);
        return client.execute(deleteOp);
    }

    /** {@inheritDoc} */
    @Override
    public Future<Boolean> delete(final String key, final String currentRef) {
        if (key == null) {
            throw new IllegalArgumentException("'key' cannot be null.");
        }
        if (key.length() < 1) {
            throw new IllegalArgumentException("'key' cannot be empty.");
        }
        if (currentRef == null) {
            throw new IllegalArgumentException("'currentRef' cannot be null.");
        }
        if (currentRef.length() < 1) {
            throw new IllegalArgumentException("'currentRef' cannot be empty.");
        }
        final DeleteOperation deleteOp = new DeleteOperation(collection, key, currentRef);
        return client.execute(deleteOp);
    }

    /** {@inheritDoc} */
    @Override
    public Future<Boolean> deleteAll() {
        final DeleteOperation deleteOp = new DeleteOperation(collection);
        return client.execute(deleteOp);
    }

}
