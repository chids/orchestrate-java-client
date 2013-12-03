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

// TODO document this
@SuppressWarnings("serial")
public final class ConversionException extends RuntimeException {

    // TODO document this
    public ConversionException(final String message) {
        super(message);
    }

    // TODO document this
    public ConversionException(final Throwable cause) {
        super(cause);
    }

    // TODO document this
    public ConversionException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
