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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.glassfish.grizzly.http.HttpHeader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Fetch objects related to a key in the Orchestrate.io service.
 *
 * <p>Usage:
 * <pre>
 * {@code
 * RelationFetchOperation relationFetchOp =
 *         new RelationFetchOperation("myCollection", "someKey", "relationName");
 * Future<Iterable<KvObject<String>>> futureResult = client.execute(relationFetchOp);
 * Iterable<KvObject<String>> results = futureResult.get();
 * for (KvObject<String> result : results)
 *     System.out.println(result.getValue());
 * }
 * </pre>
 */
@ToString(callSuper=false)
@EqualsAndHashCode(callSuper=false)
public final class RelationFetchOperation extends AbstractOperation<Iterable<KvObject<String>>> {

    /** The collection containing the key. */
    private final String collection;
    /** The key to query for kinds. */
    private final String key;
    /** The kinds to traverse in the query. */
    private final String[] kinds;

    /**
     * Create a new {@code RelationFetchOperation} to get all objects that have
     * the sequence of {@code kinds} from the {@code key} in the
     * {@code collection}.
     *
     * @param collection The collection containing the key.
     * @param key The key to fetch related objects from.
     * @param kinds The name of the relationships to traverse to the related
     *                  objects.
     */
    public RelationFetchOperation(
            final String collection, final String key, final String... kinds) {
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
        if (kinds.length < 1) {
            throw new IllegalArgumentException("'kinds' cannot be empty.");
        }
        for (final String kind : kinds) {
            if (kind.length() < 1) {
                throw new IllegalArgumentException("'kinds' cannot contain empty values.");
            }
        }
        this.collection = collection;
        this.key = key;
        this.kinds = kinds;
    }

    /** {@inheritDoc} */
    @Override
    Iterable<KvObject<String>> fromResponse(
            final int status, final HttpHeader httpHeader, final String json, final JacksonMapper mapper)
            throws IOException {
        assert (status == 200);

        final ObjectMapper objectMapper = mapper.getMapper();
        final JsonNode jsonNode = objectMapper.readTree(json);

        final int count = jsonNode.get("count").asInt();
        final List<KvObject<String>> relatedObjects = new ArrayList<KvObject<String>>(count);

        final Iterator<JsonNode> iter = jsonNode.get("results").elements();
        while (iter.hasNext()) {
            final JsonNode result = iter.next();

            // parse the PATH structure (e.g.):
            // {"collection":"coll","key":"aKey","ref":"someRef"}
            final JsonNode path = result.get("path");
            final String collection = path.get("collection").asText();
            final String key = path.get("key").asText();
            final String ref = path.get("ref").asText();

            final KvMetadata metadata = new KvMetadata(collection, key, ref);
            final String rawValue = result.get("value").toString();

            relatedObjects.add(new KvObject<String>(metadata, rawValue, rawValue));
        }

        return relatedObjects;
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
     * Returns the kinds from this operation.
     *
     * @return The kinds from this operation.
     */
    public List<String> getKinds() {
        return Arrays.asList(kinds);
    }

}
