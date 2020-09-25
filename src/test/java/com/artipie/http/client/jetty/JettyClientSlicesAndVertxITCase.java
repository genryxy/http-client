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

import com.artipie.asto.Content;
import com.artipie.http.Headers;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.async.AsyncResponse;
import com.artipie.http.client.ClientSlices;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.RsFull;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.slice.LoggingSlice;
import com.artipie.vertx.VertxSliceServer;
import io.vertx.reactivex.core.Vertx;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;

/**
 * Tests for {@link JettyClientSlices} and vertx.
 *
 * @since 0.3
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@Disabled
final class JettyClientSlicesAndVertxITCase {

    /**
     * Vertx instance.
     */
    private static final Vertx VERTX = Vertx.vertx();

    /**
     * Clients.
     */
    private final JettyClientSlices clients = new JettyClientSlices();

    /**
     * Server port.
     */
    private int port;

    /**
     * Vertx slice server instance.
     */
    private VertxSliceServer server;

    @BeforeEach
    void setUp() throws Exception {
        this.clients.start();
        this.server = new VertxSliceServer(
            JettyClientSlicesAndVertxITCase.VERTX,
            new LoggingSlice(new ProxySlice(this.clients))
        );
        this.port = this.server.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        this.clients.stop();
        this.server.close();
    }

    @Test
    void getsSomeContent() throws IOException {
        final HttpURLConnection con = (HttpURLConnection)
            new URL(String.format("http://localhost:%s", this.port)).openConnection();
        con.setRequestMethod("GET");
        MatcherAssert.assertThat(
            con.getResponseCode(),
            new IsEqual<>(Integer.parseInt(RsStatus.OK.code()))
        );
    }

    /**
     * Test proxy slice.
     * @since 0.3
     */
    static final class ProxySlice implements Slice {

        /**
         * Client.
         */
        private final ClientSlices client;

        /**
         * Ctor.
         * @param client Http client
         */
        ProxySlice(final ClientSlices client) {
            this.client = client;
        }

        @Override
        public Response response(
            final String line,
            final Iterable<Map.Entry<String, String>> headers,
            final Publisher<ByteBuffer> pub
        ) {
            final CompletableFuture<Response> promise = new CompletableFuture<>();
            return new AsyncResponse(
                this.client.https("yandex.ru").response(
                    new RequestLine(
                        RqMethod.GET, "/"
                    ).toString(),
                    Headers.EMPTY,
                    Content.EMPTY
                ).send(
                    (status, rsheaders, body) -> {
                        promise.complete(new RsFull(status, rsheaders, body));
                        return CompletableFuture.allOf();
                    }
                ).thenCompose(ignored -> promise)
            );
        }
    }
}
