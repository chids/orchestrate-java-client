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
import org.glassfish.grizzly.http.util.Header;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Store an object by key to the Orchestrate.io service.
 *
 * <p>Usage:
 * <pre>
 * {@code
 * MyObject obj = new MyObject(...);
 * KvStoreOperation kvStoreOp = new KvStoreOperation("myCollection", "someKey", obj);
 * Future<KvMetadata> futureResult = client.execute(kvStoreOp);
 * KvMetadata result = futureResult.get();
 * System.out.println(metadata.getRef());
 * }
 * </pre>
 */
@ToString(callSuper=false)
@EqualsAndHashCode(callSuper=false)
public final class KvStoreOperation extends AbstractOperation<KvMetadata> {

    /** The collection to store the key to. */
    private final String collection;
    /** The key for the object to store. */
    private final String key;
    /** The object to store. */
    private final Object value;
    /** The last known version of the stored object. */
    private final String currentRef;
    /** Whether to store the object if no key already exists. */
    private final boolean ifAbsent;

    /**
     * Create a new {@code KvStoreOperation} to put the specified {@code value}
     * in the {@code collection} with the {@code key}.
     *
     * @param collection The collection to store the key to.
     * @param key The key for the object.
     * @param value The value to store.
     */
    public KvStoreOperation(
            final String collection, final String key, final Object value) {
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
        if (value == null) {
            throw new IllegalArgumentException("'value' cannot be null.");
        }
        this.collection = collection;
        this.key = key;
        this.value = value;
        this.currentRef = null;
        this.ifAbsent = false;
    }

    /**
     * Create a new {@code KvStoreOperation} to put the specified {@code value}
     * in the {@code collection} with the {@code key}.
     *
     * <p>When {@code ifAbsent} is {@code true} this operation succeeds if and
     * only if an object with this key does not already exist.
     *
     * @param collection The collection to store the key to.
     * @param key The key for the object.
     * @param value The value to store.
     * @param ifAbsent If {@code true} the operation will only succeed if the
     *                 key does not already exist.
     */
    public KvStoreOperation(
            final String collection, final String key, final Object value, final boolean ifAbsent) {
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
        if (value == null) {
            throw new IllegalArgumentException("'value' cannot be null.");
        }
        this.collection = collection;
        this.key = key;
        this.value = value;
        this.currentRef = null;
        this.ifAbsent = ifAbsent;
    }

    /**
     * Create a new {@code KvStoreOperation} to put the specified {@code value}
     * in the {@code collection} with the {@code key}.
     *
     * <p>This operation succeeds if and only if the ref from the
     * {@code metadata} matches the current ref stored.
     *
     * @param collection The collection to store the key to.
     * @param key The key for the object.
     * @param value The value to store.
     * @param metadata The metadata whose ref must match the current stored ref
     *                 for the operation to succeed.
     */
    public KvStoreOperation(
            final String collection, final String key, final Object value, final KvMetadata metadata) {
        this(collection, key, value, metadata.getRef());
    }

    /**
     * Create a new {@code KvStoreOperation} to put the specified {@code value}
     * in the {@code collection} with the {@code key}.
     *
     * <p>This store operation succeeds if and only if the {@code currentRef}
     * matches the current stored ref.
     *
     * @param collection The collection to store the key to.
     * @param key The key for the object.
     * @param value The value to store.
     * @param currentRef The ref that must match the current stored ref for the
     *                   operation to succeed.
     */
    public KvStoreOperation(
            final String collection, final String key, final Object value, final String currentRef) {
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
        if (value == null) {
            throw new IllegalArgumentException("'value' cannot be null.");
        }
        if (currentRef == null) {
            throw new IllegalArgumentException("'currentRef' cannot be null.");
        }
        if (currentRef.length() < 1) {
            throw new IllegalArgumentException("'currentRef' cannot be empty.");
        }
        this.collection = collection;
        this.key = key;
        this.value = value;
        this.currentRef = currentRef;
        this.ifAbsent = false;
    }

    /** {@inheritDoc} */
    @Override
    KvMetadata fromResponse(
            final int status, final HttpHeader httpHeader, final String json, final JacksonMapper mapper)
            throws IOException {
        if (status == 201) {
            final String ref = httpHeader.getHeader(Header.ETag)
                    .replace("\"", "")
                    .replace("-gzip", "");
            return new KvMetadata(collection, key, ref);
        }
        return null;
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
     * Returns the value from this operation.
     *
     * @return The value from this operation.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns the {@code currentRef} from this operation.
     *
     * @return The {@code currentRef} from this operation, may be {@code null}.
     * @see #hasCurrentRef()
     */
    @Nullable
    public String getCurrentRef() {
        return currentRef;
    }

    /**
     * Returns whether a {@code currentRef} was supplied to this operation.
     *
     * @return {@code true} if a {@code currentRef} was supplied to this
     *         operation.
     */
    public boolean hasCurrentRef() {
        return (currentRef != null);
    }

    /**
     * Returns whether {@code ifAbsent} is set for this operation.
     *
     * @return {@code true} when {@code ifAbsent} has been set.
     */
    public boolean hasIfAbsent() {
        return ifAbsent;
    }

}
