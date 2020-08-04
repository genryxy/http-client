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
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.RsWithBody;
import com.artipie.vertx.VertxSliceServer;
import io.reactivex.Flowable;
import io.vertx.reactivex.core.Vertx;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Tests checking for leaks in {@link JettyClientSlice}.
 *
 * @since 0.1
 */
final class JettyClientSliceLeakTest {

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

    @Test
    @Disabled
    void shouldNotLeakConnectionsIfBodyNotRead() throws Exception {
        this.fake.set(
            (line, headers, body) -> new RsWithBody(
                Flowable.just(ByteBuffer.wrap("data".getBytes()))
            )
        );
        final int total = 100;
        for (int count = 0; count < total; count += 1) {
            this.slice.response(
                new RequestLine(RqMethod.GET, "/").toString(),
                Headers.EMPTY,
                Flowable.empty()
            ).send(
                (status, headers, body) -> CompletableFuture.allOf()
            ).toCompletableFuture().get(1, TimeUnit.SECONDS);
        }
    }

    @Test
    @Disabled
    void shouldNotLeakConnectionsIfSendFails() throws Exception {
        this.fake.set(
            (line, headers, body) -> new RsWithBody(
                Flowable.just(ByteBuffer.wrap("data2".getBytes()))
            )
        );
        final int total = 100;
        for (int count = 0; count < total; count += 1) {
            final CompletionStage<Void> sent = this.slice.response(
                new RequestLine(RqMethod.GET, "/").toString(),
                Headers.EMPTY,
                Flowable.empty()
            ).send(
                (status, headers, body) -> {
                    final CompletableFuture<Void> future = new CompletableFuture<>();
                    future.completeExceptionally(new IllegalStateException());
                    return future;
                }
            );
            try {
                sent.toCompletableFuture().get(1, TimeUnit.SECONDS);
            } catch (final ExecutionException expected) {
            }
        }
    }
}
