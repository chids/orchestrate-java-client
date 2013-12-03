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
package io.orchestrate.client.convert;

// TODO document this
public final class NoOpConverter implements Converter<String> {

    /** The default instance of the converter. */
    public static final NoOpConverter INSTANCE = new NoOpConverter();

    /** {@inheritDoc} */
    @Override
    public String toDomain(final String json) {
        return json;
    }

    /** {@inheritDoc} */
    @Override
    public String fromDomain(final String domainObject) {
        return domainObject;
    }

}
