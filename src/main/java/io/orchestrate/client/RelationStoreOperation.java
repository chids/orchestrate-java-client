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

import java.io.IOException;

/**
 * Store a relationship between two objects in the Orchestrate.io service.
 *
 * <p>Usage:
 * <pre>
 * {@code
 * RelationStoreOperation relationStoreOp =
 *         new RelationStoreOperation("oneCollection", "aKey", "relationName", "otherCollection", "otherKey");
 * Future<Boolean> futureResult = client.execute(relationStoreOp);
 * Boolean result = futureResult.get();
 * if (result)
 *     System.out.println("Successfully stored relationship.");
 * }
 * </pre>
 */
@ToString(callSuper=false)
@EqualsAndHashCode(callSuper=false)
public final class RelationStoreOperation extends AbstractOperation<Boolean> {

    /** The collection containing the source key. */
    private final String collection;
    /** The source key to add the relation to. */
    private final String key;
    /** The collection containing the destination key. */
    private final String toCollection;
    /** The destination key to add the relation to. */
    private final String toKey;
    /** The name of the relationship to create. */
    private final String kind;

    /**
     * Create a new {@code RelationStoreOperation} to define a relationship
     * between a {@code key} in a {@code collection} and the specified
     * {@code toKey} in the {@code toCollection}.
     *
     * @param collection The collection containing the source key.
     * @param key The source key to add the relation to.
     * @param kind The name of the relationship to create.
     * @param toCollection The collection containing the destination key.
     * @param toKey The destination key to add the relation to.
     */
    public RelationStoreOperation(
            final String collection, final String key, final String kind, final String toCollection, final String toKey) {
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
        if (kind == null) {
            throw new IllegalArgumentException("'kind' cannot be null.");
        }
        if (kind.length() < 1) {
            throw new IllegalArgumentException("'kind' cannot be empty.");
        }
        if (toCollection == null) {
            throw new IllegalArgumentException("'toCollection' cannot be null.");
        }
        if (toCollection.length() < 1) {
            throw new IllegalArgumentException("'toCollection' cannot be empty.");
        }
        if (toKey == null) {
            throw new IllegalArgumentException("'toKey' cannot be null.");
        }
        if (toKey.length() < 1) {
            throw new IllegalArgumentException("'toKey' cannot be empty.");
        }
        this.collection = collection;
        this.key = key;
        this.kind = kind;
        this.toCollection = toCollection;
        this.toKey = toKey;
    }

    /**
     * Create a new {@code RelationStoreOperation} to define a relationship
     * between an {@code obj} with a {@code kind} to the specified {@code toObj}.
     *
     * @param obj The source object to add the relation to.
     * @param kind The name of the relationship to create.
     * @param toObj The destination object to add the relation to.
     */
    public RelationStoreOperation(final KvObject<?> obj, final String kind, final KvObject<?> toObj) {
        this(obj.getCollection(), obj.getKey(), kind, toObj.getCollection(), toObj.getKey());
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
     * Returns the relationship from this operation.
     *
     * @return The relationship from this operation.
     */
    public String getKind() {
        return kind;
    }

    /**
     * Returns the destination collection from this operation.
     *
     * @return The destination collection from this operation.
     */
    public String getToCollection() {
        return toCollection;
    }

    /**
     * Returns the destination key from this operation.
     *
     * @return The destination key from this operation.
     */
    public String getToKey() {
        return toKey;
    }

}
