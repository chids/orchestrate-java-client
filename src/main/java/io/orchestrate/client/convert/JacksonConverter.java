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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

// TODO document this
public final class JacksonConverter<T> implements Converter<T> {

    /** The shared object mapper to use to convert types. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** The type to deserialize JSON to. */
    private final Class<T> clazz;

    // TODO document this
    public JacksonConverter(final Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("'clazz' cannot be null.");
        }
        this.clazz = clazz;
    }

    /** {@inheritDoc} */
    @Override
    public T toDomain(final String json) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (final JsonMappingException e) {
            throw new ConversionException(e);
        } catch (final JsonParseException e) {
            throw new ConversionException(e);
        } catch (final IOException e) {
            throw new ConversionException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String fromDomain(final T domainObject) {
        try {
            return MAPPER.writeValueAsString(domainObject);
        } catch (final JsonProcessingException e) {
            throw new ConversionException(e);
        }
    }

}
