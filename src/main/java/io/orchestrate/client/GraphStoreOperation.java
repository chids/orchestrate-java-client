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

import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpHeader;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.util.HttpStatus;

// TODO document this
public final class GraphStoreOperation extends AbstractOperation<Boolean> {

    private final String collection;
    private final String key;
    private final String toCollection;
    private final String toKey;
    private final String kind;

    public GraphStoreOperation(
            final String collection, final String key, final String toCollection, final String toKey, final String kind) {
        // TODO add input checking
        this.collection = collection;
        this.key = key;
        this.toCollection = toCollection;
        this.toKey = toKey;
        this.kind = kind;
    }

    public GraphStoreOperation(final KvObject<?> sourceObj, final String kind, final KvObject<?> toObj) {
        this(sourceObj.getCollection(), sourceObj.getKey(), kind, toObj.getCollection(), toObj.getKey());
    }

    /** {@inheritDoc} */
    @Override
    HttpContent encode() {
        final String uri = collection.concat("/").concat(key)
                .concat("/relation/").concat(kind).concat("/")
                .concat(toCollection).concat("/").concat(toKey);
        final HttpRequestPacket httpHeader = HttpRequestPacket.builder()
                .method(Method.PUT)
                .uri(uri)
                .build();
        return httpHeader.httpContentBuilder()
                .httpHeader(httpHeader)
                .build();
    }

    /** {@inheritDoc} */
    @Override
    Boolean decode(
            final HttpContent content, final HttpHeader header, final HttpStatus status) {
        switch (status.getStatusCode()) {
            case 204:
                return Boolean.TRUE;
            default:
                // FIXME do better with this error handling
                throw new RuntimeException();
        }
    }

}
