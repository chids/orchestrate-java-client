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
import io.orchestrate.client.convert.ConversionException;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpHeader;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// TODO document this
public final class GraphFetchOperation extends AbstractOperation<Iterable<KvObject<String>>> {

    private final String collection;
    private final String key;
    private final String[] kinds;

    public GraphFetchOperation(
            final String collection, final String key, final String... kinds) {
        // TODO add input checking
        this.collection = collection;
        this.key = key;
        this.kinds = kinds;
    }

    /** {@inheritDoc} */
    @Override
    HttpContent encode() {
        String uri = collection.concat("/").concat(key).concat("/relations");
        for (final String kind : kinds) {
            uri = uri.concat("/").concat(kind);
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
    Iterable<KvObject<String>> decode(
            final HttpContent content, final HttpHeader header, final HttpStatus status) {
        switch (status.getStatusCode()) {
            case 200:
                // TODO allow this deserialization to *not* depend on Jackson
                ObjectMapper mapper = new ObjectMapper();
                final JsonNode jsonNode;
                try {
                    jsonNode = mapper.readTree(content.getContent().toStringContent());
                } catch (final IOException e) {
                    throw new ConversionException(e);
                }

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
            default:
                // FIXME do better with this error handling
                throw new RuntimeException();
        }
    }

}
