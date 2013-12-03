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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.http.*;
import org.glassfish.grizzly.http.util.Base64Utils;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.io.IOException;

import static org.glassfish.grizzly.attributes.DefaultAttributeBuilder.DEFAULT_ATTRIBUTE_BUILDER;

/**
 * A filter to handle HTTP operations and apply the Orchestrate.io
 * authentication header.
 */
@Slf4j
final class ClientFilter extends BaseFilter {

    /** The name of the filter attribute for a HTTP response. */
    public static final String HTTP_RESPONSE_ATTR = "orchestrate-client-response";
    /** The attribute for the HTTP response. */
    @Getter(AccessLevel.PACKAGE)
    private final Attribute<AbstractOperation> httpResponseAttr;

    /** The header value to authenticate with the Orchestrate.io service */
    private final String authHeaderValue;
    /** The hostname for the Orchestrate.io service. */
    private final String host;
    /** The version of the Orchestrate.io API to use. */
    private final String version;

    ClientFilter(final String host, final String apiKey, final String version) {
        assert (host != null);
        assert (host.length() > 0);
        assert (apiKey != null);
        assert (apiKey.length() > 0);
        assert (version != null);
        assert (version.length() > 0);

        this.httpResponseAttr =
                DEFAULT_ATTRIBUTE_BUILDER.createAttribute(HTTP_RESPONSE_ATTR);
        this.authHeaderValue =
                "Basic ".concat(Base64Utils.encodeToString(apiKey.getBytes(), true));
        this.host = host;
        this.version = version;
    }

    @SuppressWarnings("unchecked")
    @Override
    public NextAction handleRead(final FilterChainContext ctx) throws IOException {
        final Object object = ctx.getMessage();
        if (!(object instanceof HttpContent) || (object instanceof HttpTrailer)) {
            // pass the message to the next filter
            return ctx.getInvokeAction();
        }

        final HttpContent content = (HttpContent) object;
        log.info("{}", content.getClass());
        log.info("Received content: {}", content.getHttpHeader());
        final AbstractOperation message =
                httpResponseAttr.get(ctx.getConnection().getAttributes());

        final HttpStatus status = ((HttpResponsePacket) content.getHttpHeader()).getHttpStatus();
        message.getFuture().setResult(message.decode(content, content.getHttpHeader(), status));

        ctx.setMessage(null);
        return ctx.getStopAction();
    }

    @Override
    public NextAction handleWrite(final FilterChainContext ctx) throws IOException {
        final Object message = ctx.getMessage();
        assert (message instanceof HttpContent);

        final HttpContent request = (HttpContent) message;
        final HttpRequestPacket httpHeader = (HttpRequestPacket) request.getHttpHeader();

        // add version information
        final String uriWithPrefix = "/"
                .concat(version)
                .concat("/")
                .concat(httpHeader.getRequestURI());

        // adjust the HTTP request to include standard headers
        httpHeader.setProtocol(Protocol.HTTP_1_1);
        httpHeader.setHeader(Header.Host, host);
        httpHeader.setRequestURI(uriWithPrefix);

        // add basic auth information
        httpHeader.addHeader(Header.Authorization, authHeaderValue);

        log.info("Sending request: {}", httpHeader);
        ctx.write(request);

        return ctx.getInvokeAction();
    }

}
