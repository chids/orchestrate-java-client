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
import org.glassfish.grizzly.http.util.HttpStatus;

import javax.annotation.Nullable;

// TODO document this
public final class DeleteOperation extends AbstractOperation<Boolean> {

    /**  */
    private final String collection;
    /**  */
    private final String key;

    public DeleteOperation(final String collection) {
        this(collection, null);
    }

    public DeleteOperation(final String collection, @Nullable final String key) {
        // TODO add input checking
        this.collection = collection;
        this.key = key;
    }

    /** {@inheritDoc} */
    @Override
    HttpContent encode() {
        String uri = collection;
        if (key != null) {
            uri = uri.concat("/").concat(key);
        }

        final HttpRequestPacket.Builder httpHeaderBuilder = HttpRequestPacket.builder()
                .method(Method.DELETE)
                .uri(uri);
        if (key == null) {
            httpHeaderBuilder.query("force=true");
        }
        final HttpRequestPacket httpHeader = httpHeaderBuilder.build();

        return httpHeader.httpContentBuilder()
                .httpHeader(httpHeader)
                .build();
    }

    /** {@inheritDoc} */
    @Override
    Boolean decode(final HttpContent content, final HttpHeader header, final HttpStatus status) {
        switch (status.getStatusCode()) {
            case 204:
                return Boolean.TRUE;
            default:
                // FIXME do better with this error handling
                throw new RuntimeException();
        }
    }

}
