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
 * A container for the search result and its associated KV data.
 *
 * @param <T> The deserializable type for the value of the KV data belonging
 *            to this search result.
 */
@ToString
@EqualsAndHashCode
public class Result<T> {

    /** The KV object for this search result. */
    private final KvObject<T> kvObject;
    /** The score for this result. */
    private final double score;

    Result(final KvObject<T> kvObject, final double score) {
        assert (kvObject != null);
        assert (score >= 0);

        this.kvObject = kvObject;
        this.score = score;
    }

    /**
     * Returns the KV object for this search result.
     *
     * @return The KV object for this result.
     */
    public final KvObject<T> getKvObject() {
        return kvObject;
    }

    /**
     * Returns the score of this search results.
     *
     * @return The score for this result.
     */
    public final double getScore() {
        return score;
    }

}
