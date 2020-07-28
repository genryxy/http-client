/*
 * MIT License
 *
 * Copyright (c) 2020 Artipie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.artipie.http.client.jetty;

import com.artipie.http.Headers;
import com.artipie.http.Slice;
import com.artipie.http.rs.StandardRs;
import com.artipie.vertx.VertxSliceServer;
import io.reactivex.Flowable;
import io.vertx.reactivex.core.Vertx;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.jetty.client.HttpClient;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.StringStartsWith;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for {@link JettyClientSlice}.
 *
 * @since 0.1
 */
final class JettyClientSliceTest {

    /**
     * Vert.x instance used for test server.
     */
    private Vertx vertx;

    /**
     * Test server.
     */
    private VertxSliceServer server;

    /**
     * Reference to fake slice used in test.
     */
    private AtomicReference<Slice> fake;

    /**
     * HTTP client used in tests.
     */
    private HttpClient client;

    /**
     * HTTP client sliced being tested.
     */
    private JettyClientSlice slice;

    @BeforeEach
    void setUp() throws Exception {
        this.fake = new AtomicReference<>();
        this.vertx = Vertx.vertx();
        this.server = new VertxSliceServer(
            this.vertx,
            (line, headers, body) -> this.fake.get().response(line, headers, body)
        );
        final int port = this.server.start();
        this.client = new HttpClient();
        this.client.start();
        this.slice = new JettyClientSlice(this.client, false, "localhost", port);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (this.vertx != null) {
            this.vertx.close();
        }
        if (this.server != null) {
            this.server.close();
        }
        if (this.client != null) {
            this.client.stop();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "PUT /",
        "GET /index.html",
        "POST /path?param1=value&param2=something",
        "HEAD /my%20path?param=some%20value"
    })
    void shouldSendRequestLine(final String line) {
        final AtomicReference<String> actual = new AtomicReference<>();
        this.fake.set(
            (rqline, rqheaders, rqbody) -> {
                actual.set(rqline);
                return StandardRs.EMPTY;
            }
        );
        this.slice.response(
            String.format("%s HTTP/1.1", line),
            Headers.EMPTY,
            Flowable.empty()
        ).send((status, headers, body) -> CompletableFuture.allOf()).toCompletableFuture().join();
        MatcherAssert.assertThat(
            actual.get(),
            new StringStartsWith(false, String.format("%s HTTP", line))
        );
    }
}
