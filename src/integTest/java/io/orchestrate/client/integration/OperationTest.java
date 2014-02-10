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
package io.orchestrate.client.integration;

import io.orchestrate.client.Client;
import io.orchestrate.client.ClientBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;

/**
 * A base test class for testing Orchestrate client operations.
 */
public abstract class OperationTest {

    /** The name of the collection used with all integration tests. */
    public static final String TEST_COLLECTION = "oio-client-integration-tests";

    /** The client instance to use with requests to the Orchestrate service. */
    private static Client client;

    protected Client client() {
        return client;
    }

    @BeforeClass
    public static void setUpBeforeClass() {
        final String apiKey = System.getProperty("orchestrate.apiKey");
        if (apiKey == null || apiKey.length() < 1) {
            throw new IllegalStateException("Cannot run integration tests, 'apiKey' is blank.");
        }

        client = new ClientBuilder(apiKey)
                .poolSize(5)
                .maxPoolSize(Integer.MAX_VALUE)
                .build();
    }

    @AfterClass
    public static void tearDownAfterClass() throws IOException {
        client.stop();
    }

    static String generateString() {
        return Long.toHexString(Double.doubleToLongBits(Math.random()));
    }

}
