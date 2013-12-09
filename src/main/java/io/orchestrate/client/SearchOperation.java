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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.glassfish.grizzly.http.*;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Search for data from the Orchestrate.io service.
 *
 * <p>Usage:
 * <pre>
 * {@code
 * SearchOperation searchOp = SearchOperation.builder("myCollection")
 *         .query("*")  // a lucene-style query
 *         .limit(10)   // default
 *         .offset(0)   // default
 *         .build();
 * }
 * </pre>
 */
@ToString(callSuper=false)
@EqualsAndHashCode(callSuper=false)
public final class SearchOperation<T> extends AbstractOperation<SearchResults<T>> {

    /** The builder for this search operation. */
    private final Builder builder;
    /** The converter to deserialize the JSON results with. */
    private final Class<T> clazz;

    /**
     * Create a search operation on the specified {@code collection}.
     *
     * <p>Equivalent to:
     * <pre>
     * {@code
     * SearchOperation searchOp = SearchOperation.builder("myCollection", MyObject.class).build();
     * }
     * </pre>
     *
     * @param collection The collection to search.
     * @param
     * @see Builder#LUCENE_STAR_QUERY
     */
    public SearchOperation(final String collection, final Class<T> clazz) {
        this(builder(collection), clazz);
    }

    // TODO document this
    public SearchOperation(
            final String collection, final Class<T> clazz, final String query) {
        this(builder(collection).query(query), clazz);
    }

    /**
     * A SearchOperation configured via the {@code Builder}.
     *
     * @param builder The builder used to configure this search operation.
     */
    private SearchOperation(final Builder builder, final Class<T> clazz) {
        assert (builder != null);

        this.builder = builder;
        this.clazz = clazz;
    }

    /** {@inheritDoc} */
    @Override
    HttpContent encode() {
        final String query = "query=".concat(builder.query)
                .concat("&limit=").concat(Integer.toString(builder.limit))
                .concat("&offset=").concat(Integer.toString(builder.offset));

        final HttpRequestPacket httpHeader = HttpRequestPacket.builder()
                .method(Method.GET)
                .uri(builder.collection)
                .query(query)
                .build();
        return httpHeader.httpContentBuilder()
                .httpHeader(httpHeader)
                .build();
    }

    /** {@inheritDoc} */
    @Override
    SearchResults<T> decode(final HttpContent content, final HttpHeader header, final HttpStatus status) {
        switch (status.getStatusCode()) {
            case 200:
                final JsonNode jsonNode;
                try {
                    jsonNode = Client.MAPPER.readTree(content.getContent().toStringContent());
                } catch (final IOException e) {
                    throw new ConversionException(e);
                }

                final int totalCount = jsonNode.get("total_count").asInt();
                final int count = jsonNode.get("count").asInt();

                final List<Result<T>> results = new ArrayList<Result<T>>(count);
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

                    // parse result structure (e.g.):
                    // {"path":{...},"value":{},"score":1.0}
                    final double score = result.get("score").asDouble();
                    final JsonNode valueNode = result.get("value");
                    final String rawValue = valueNode.toString();

                    final T value;
                    try {
                        value = Client.MAPPER.treeToValue(valueNode, clazz);
                    } catch (final JsonProcessingException e) {
                        throw new ConversionException(e);
                    }

                    final KvObject<T> kvObject = new KvObject<T>(metadata, value, rawValue);

                    results.add(new Result<T>(kvObject, score));
                }

                return new SearchResults<T>(results, totalCount);
            default:
                // FIXME do better with this error handling
                throw new RuntimeException();
        }
    }

    /**
     * A new builder to create a {@code SearchOperation} with default settings.
     *
     * @param collection The name of the collection to search.
     * @return A new {@code Builder} with default settings.
     */
    public static Builder builder(final String collection) {
        if (collection == null) {
            throw new IllegalArgumentException("'collection' cannot be null.");
        }
        if (collection.length() < 1) {
            throw new IllegalArgumentException("'collection' cannot be empty.");
        }
        return new Builder(collection);
    }

    /**
     * A new builder to create a {@code SearchOperation} based on the specified
     * {@code searchOp}.
     *
     * @param searchOp The search operation to copy settings from.
     * @return A new {@code Builder} from the specified operation.
     */
    public static Builder builder(final SearchOperation searchOp) {
        if (searchOp == null) {
            throw new IllegalArgumentException("'searchOp' cannot be null.");
        }
        return new Builder(searchOp);
    }

    /**
     * Builder used to create {@code SearchOperation}s.
     */
    @ToString(callSuper=false)
    @EqualsAndHashCode(callSuper=false)
    public static final class Builder {

        /** A blank lucene query. */
        public static final String LUCENE_STAR_QUERY = "*";

        /** The name of the collection to search. */
        private final String collection;
        /** The lucene search query. */
        private String query;
        /** The number of search results to retrieve. */
        private int limit;
        /** The offset to start search results at. */
        private int offset;

        private Builder(final String collection) {
            assert (collection != null);

            this.collection = collection;
            this.query = LUCENE_STAR_QUERY;
            this.limit = 10;
            this.offset = 0;
        }

        private Builder(final SearchOperation searchOp) {
            assert (searchOp != null);

            this.collection = searchOp.builder.collection;
            this.query = searchOp.builder.query;
            this.limit = searchOp.builder.limit;
            this.offset = searchOp.builder.offset;
        }

        // TODO document this
        public Builder query(final String luceneQuery) {
            if (luceneQuery == null) {
                throw new IllegalArgumentException("'luceneQuery' cannot be null.");
            }
            if (luceneQuery.length() < 1) {
                throw new IllegalArgumentException("'luceneQuery' cannot be empty.");
            }
            this.query = luceneQuery;
            return this;
        }

        // TODO document this
        public Builder limit(final int limit) {
            if (limit < 0) {
                throw new IllegalArgumentException("'limit' cannot be negative.");
            }
            if (limit > 100) {
                final String message =
                        "'limit' cannot be greater than 100. " +
                                "Currently the Orchestrate.io service limits this value.";
                throw new IllegalArgumentException(message);
            }
            this.limit = limit;
            return this;
        }

        // TODO document this
        public Builder offset(final int offset) {
            if (offset < 0) {
                throw new IllegalArgumentException("'offset' cannot be negative.");
            }
            this.offset = offset;
            return this;
        }

        /**
         * Creates a new {@code SearchOperation}, search results are returned
         * as raw JSON.
         *
         * @return A new {@link SearchOperation}.
         */
        public SearchOperation<String> build() {
            return new SearchOperation<String>(this, String.class);
        }

        /**
         * Creates a new {@code SearchOperation}.
         *
         * @param clazz The converter to deserialize search results with.
         * @return A new {@link SearchOperation}.
         */
        public <T> SearchOperation<T> build(final Class<T> clazz) {
            return new SearchOperation<T>(this, clazz);
        }

    }

}
