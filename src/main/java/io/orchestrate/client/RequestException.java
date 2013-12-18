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
 * An object that stores information about a failed {@code Client} request.
 */
@ToString
@EqualsAndHashCode(callSuper=false)
@SuppressWarnings("serial")
public final class RequestException extends RuntimeException {

    /** The HTTP status code from the request. */
    private final int statusCode;
    /** The HTTP content from the request. */
    private final String message;
    /** The HTTP response ID from the request. */
    private final String requestId;

    RequestException(
            final int statusCode, final String message, final String requestId) {
        assert (statusCode >= 0);
        assert (message != null);
        assert (message.length() > 0);
        assert (requestId != null);
        assert (requestId.length() > 0);

        this.statusCode = statusCode;
        this.message = message;
        this.requestId = requestId;
    }

    /**
     * Returns the HTTP status code from the failed request.
     *
     * @return The HTTP status code from the failed request.
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the HTTP content from the failed request.
     *
     * @return The HTTP content from the failed request.
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * Returns the HTTP response ID from the failed request.
     *
     * <p>The response ID is used to help with debugging the query in the
     * Orchestrate.io service.
     *
     * @return The HTTP response ID from the failed request.
     */
    public String getRequestId() {
        return requestId;
    }

}
