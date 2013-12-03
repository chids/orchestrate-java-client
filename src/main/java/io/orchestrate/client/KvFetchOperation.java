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

import org.glassfish.grizzly.http.*;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.http.util.HttpStatus;

import javax.annotation.Nullable;
import java.io.IOException;

// TODO document this
public final class KvFetchOperation<T> extends AbstractOperation<KvObject<T>> {

    /**  */
    private final String collection;
    /**  */
    private final String key;
    /**  */
    private final String ref;
    /**  */
    private final Class<T> clazz;

    public KvFetchOperation(final String collection, final String key, final Class<T> clazz) {
        this(collection, key, clazz, null);
    }

    public KvFetchOperation(final String collection, final String key, final Class<T> clazz, @Nullable final String ref) {
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
        if (clazz == null) {
            throw new IllegalArgumentException("'converter' cannot be null.");
        }
        this.collection = collection;
        this.key = key;
        this.clazz = clazz;
        this.ref = ref;
    }

    /** {@inheritDoc} */
    @Override
    HttpContent encode() {
        String uri = collection.concat("/").concat(key);
        if (ref != null) {
            uri = uri.concat("/refs/").concat(ref);
        }

        final HttpRequestPacket httpHeader = HttpRequestPacket.builder()
                .method(Method.GET)
                .uri(uri)
                .build();

        return httpHeader.httpContentBuilder()
                .httpHeader(httpHeader)
                .build();
    }

    /** {@inheritDoc} */
    @Override
    KvObject<T> decode(final HttpContent content, final HttpHeader header, final HttpStatus status) {
        switch (status.getStatusCode()) {
            case 200:
                final String json = content.getContent().toStringContent();
                final T value;
                try {
                    value = Client.MAPPER.readValue(json, clazz);
                } catch (final IOException e) {
                    throw new ConversionException(e);
                }

                final String ref = header.getHeader(Header.ETag)
                        .replace("\"", "")
                        .replace("-gzip", "");
                return new KvObject<T>(collection, key, ref, value, json);
            case 404:
                return null;
            default:
                // FIXME do better with this error handling
                throw new RuntimeException();
        }
    }

}
