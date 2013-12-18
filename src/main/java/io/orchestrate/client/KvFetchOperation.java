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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.glassfish.grizzly.http.HttpHeader;
import org.glassfish.grizzly.http.util.Header;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Fetch an object by key from the Orchestrate.io service.
 *
 * <p>Usage:
 * <pre>
 * {@code
 * KvFetchOperation<MyObject> kvFetchOp =
 *         new KvFetchOperation<MyObject>("myCollection", "someKey");
 * Future<KvObject<MyObject>> futureResult = client.execute(kvFetchOp);
 * KvObject<MyObject> result = futureResult.get();
 * System.out.println(result.getValue());
 * }
 * </pre>
 *
 * @param <T> The type to deserialize the result of this operation to.
 */
@ToString(callSuper=false)
@EqualsAndHashCode(callSuper=false)
public final class KvFetchOperation<T> extends AbstractOperation<KvObject<T>> {

    /** The collection to fetch the key from. */
    private final String collection;
    /** The key to fetch. */
    private final String key;
    /** The version of the object to fetch. */
    private final String ref;
    /** Type information for marshalling objects at runtime. */
    protected final TypeReference<T> genericType;

    /**
     * Create a new {@code KvFetchOperation} to get the object with the
     * specified {@code key} in the {@code collection}.
     *
     * @param collection The collection to fetch the key from.
     * @param key The key to fetch.
     */
    public KvFetchOperation(final String collection, final String key) {
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
        this.collection = collection;
        this.key = key;
        this.ref = null;
        this.genericType = new TypeReference<T>() {};
    }

    /**
     * Create a new {@code KvFetchOperation} to get the object with the
     * specified {@code key} in the {@code collection} with the version from
     * the {@code metadata}.
     *
     * @param collection The collection to fetch the key from.
     * @param key The key to fetch.
     * @param metadata Metadata with the version of the object to fetch.
     */
    public KvFetchOperation(
            final String collection, final String key, final KvMetadata metadata) {
        this(collection, key, metadata.getRef());
    }

    /**
     * Create a new {@code KvFetchOperation} to get the object with the
     * specified {@code key} in the {@code collection} with the {@code ref}
     * version.
     *
     * @param collection The collection to fetch the key from.
     * @param key The key to fetch.
     * @param ref The ref (version) of the object to fetch.
     */
    public KvFetchOperation(
            final String collection, final String key, final String ref) {
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
        if (ref == null) {
            throw new IllegalArgumentException("'ref' cannot be null.");
        }
        if (ref.length() < 1) {
            throw new IllegalArgumentException("'ref' cannot be empty.");
        }
        this.collection = collection;
        this.key = key;
        this.ref = ref;
        this.genericType = new TypeReference<T>() {};
    }

    /** {@inheritDoc} */
    @Override
    KvObject<T> fromResponse(
            final int status, final HttpHeader httpHeader, final String json, final JacksonMapper mapper)
            throws IOException {
        if (status == 404) {
            return null;
        }

        final ObjectMapper objectMapper = mapper.getMapper();
        final T value = objectMapper.readValue(json, genericType);
        final String ref = httpHeader.getHeader(Header.ETag)
                .replace("\"", "")
                .replace("-gzip", "");
        return new KvObject<T>(collection, key, ref, value, json);
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
     * Returns the ref (version) from this operation.
     *
     * @return The ref (version) from this operation, may be {@code null}.
     * @see #hasRef()
     */
    @Nullable
    public String getRef() {
        return ref;
    }

    /**
     * Returns whether a ref was supplied to this operation.
     *
     * @return {@code true} if a ref was supplied to this operation.
     */
    public boolean hasRef() {
        return (ref != null);
    }

}
