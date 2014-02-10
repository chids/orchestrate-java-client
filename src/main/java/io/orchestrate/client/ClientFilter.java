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
import org.glassfish.grizzly.http.util.UEncoder;

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
    private final Attribute<OrchestrateFutureImpl> httpResponseAttr;

    /** The header value to authenticate with the Orchestrate.io service */
    private final String authHeaderValue;
    /** The header value to indicate the client and version queried with. */
    private final String userAgentValue;
    /** The hostname for the Orchestrate.io service. */
    private final String host;
    /** The version of the Orchestrate.io API to use. */
    private final String version;
    /** The mapper to use when deserializing responses from JSON. */
    private final JacksonMapper mapper;

    ClientFilter(final ClientBuilder builder) {
        assert (builder != null);

        assert (builder.getHost() != null);
        assert (builder.getHost().toString().length() > 0);
        assert (builder.getApiKey() != null);
        assert (builder.getApiKey().length() > 0);
        assert (builder.getVersion() != null);
        assert (builder.getVersion().name().length() > 0);
        assert (builder.getMapper() != null);

        this.httpResponseAttr =
                DEFAULT_ATTRIBUTE_BUILDER.createAttribute(HTTP_RESPONSE_ATTR);
        this.authHeaderValue =
                "Basic ".concat(Base64Utils.encodeToString(builder.getApiKey().getBytes(), true));
        this.userAgentValue =
                "Orchestrate Java Client/" + getClass().getPackage().getImplementationVersion();
        this.host = builder.getHost().toString();
        this.version = builder.getVersion().name();
        this.mapper = builder.getMapper();
    }

    @Override
    @SuppressWarnings("unchecked")
    public NextAction handleRead(final FilterChainContext ctx) throws IOException {
        final HttpContent content = ctx.getMessage();
        if (!content.isLast()) {
            return ctx.getStopAction(content);
        }

        final HttpHeader header = content.getHttpHeader();
        final HttpStatus status = ((HttpResponsePacket) header).getHttpStatus();
        final int statusCode = status.getStatusCode();

        log.info("Received content: {}", header);
        final String contentString = content.getContent().toStringContent();
        final OrchestrateFutureImpl future =
                httpResponseAttr.get(ctx.getConnection().getAttributes());

        switch (statusCode) {
            case 200:   // intentional fallthrough
            case 201:   // intentional fallthrough
            case 204:   // intentional fallthrough
            case 404:   // intentional fallthrough
            case 412:
                final Object result = future.getOperation()
                        .fromResponse(statusCode, header, contentString, mapper);
                future.setResult(result);
                break;
            default:
                final String reqId = header.getHeader("x-orchestrate-req-id");
                future.setException(new RequestException(statusCode, contentString, reqId));
        }

        return ctx.getStopAction();
    }

    @Override
    public NextAction handleWrite(final FilterChainContext ctx) throws IOException {
        final Object message = ctx.getMessage();
        if (!(message instanceof HttpPacket)) {
            return ctx.getInvokeAction();
        }

        final HttpPacket request = (HttpPacket) message;
        final HttpRequestPacket httpHeader = (HttpRequestPacket) request.getHttpHeader();

        final UEncoder urlEncoder = new UEncoder();
        urlEncoder.addSafeCharacter('/');
        final String uriWithPrefix = "/"
                .concat(version)    // add version information
                .concat("/")
                .concat(httpHeader.getRequestURI());

        // adjust the HTTP request to include standard headers
        httpHeader.setProtocol(Protocol.HTTP_1_1);
        httpHeader.setHeader(Header.UserAgent, userAgentValue);
        httpHeader.setHeader(Header.Host, host);
        httpHeader.setRequestURI(urlEncoder.encodeURL(uriWithPrefix));

        // add basic auth information
        httpHeader.addHeader(Header.Authorization, authHeaderValue);

        log.info("Sending request: {}", httpHeader);
        ctx.write(request);

        return ctx.getStopAction();
    }

}
