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

/**
 * A container for metadata about a KV object.
 */
@ToString
@EqualsAndHashCode
public class KvMetadata {

    /** The collection for this KV metadata. */
    private final String collection;
    /** The key for this metadata. */
    private final String key;
    /** The version for this metadata. */
    private final String ref;

    KvMetadata(final String collection, final String key, final String ref) {
        assert (key != null);
        assert (key.length() > 0);
        assert (ref != null);
        assert (ref.length() > 0);

        this.collection = collection;
        this.key = key;
        this.ref = ref;
    }

    KvMetadata(final KvMetadata metadata) {
        this(metadata.collection, metadata.key, metadata.ref);
    }

    /**
     * Returns the collection this metadata belongs to.
     *
     * @return The collection of this metadata.
     */
    public final String getCollection() {
        return collection;
    }

    /**
     * Returns the key of this metadata.
     *
     * @return The key for this metadata.
     */
    public final String getKey() {
        return key;
    }

    /**
     * Returns the reference (i.e. "version") of this metadata.
     *
     * @return The reference for this metadata.
     */
    public final String getRef() {
        return ref;
    }

}
