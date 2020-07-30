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
import com.artipie.http.client.Settings;
import com.artipie.http.hm.RsHasBody;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.RsWithBody;
import com.artipie.vertx.VertxSliceServer;
import io.reactivex.Flowable;
import io.vertx.reactivex.core.Vertx;
import java.nio.ByteBuffer;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsInstanceOf;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link JettyClientSlices}.
 *
 * @since 0.1
 * @todo #1:30min Improve tests for `JettyClientSlices`.
 *  Unit tests in `JettyClientSlicesTest` check that `JettyClientSlices` produce
 *  `JettyClientSlice` instances, but do not check that they are configured
 *  with expected host, port and secure flag.
 *  These important properties should be covered with tests.
 * @todo #5:30min Test support for secure proxy in `JettyClientSlices`.
 *  There is a test in `JettyClientSlicesTest` checking that
 *  non-secure proxy works in `JettyClientSlices`. It's needed to test
 *  support for proxy working over HTTPS protocol.
 * @todo #5:30min Test support for `trustAll` setting in `JettyClientSlices`.
 *  There is no test checking that `trustAll` setting is supported by `JettyClientSlices`.
 *  To check that it is required to start an HTTP server with self-signed certificate
 *  and see that client connects successfully and does not fail verification.
 *  It should also be validated that client with `trustAll` setting disabled does not connect
 *  to such server.
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class JettyClientSlicesTest {

    @Test
    void shouldProduceHttp() {
        MatcherAssert.assertThat(
            new JettyClientSlices().http("example.com"),
            new IsInstanceOf(JettyClientSlice.class)
        );
    }

    @Test
    void shouldProduceHttpWithPort() {
        final int port = 8080;
        MatcherAssert.assertThat(
            new JettyClientSlices().http("localhost", port),
            new IsInstanceOf(JettyClientSlice.class)
        );
    }

    @Test
    void shouldProduceHttps() {
        MatcherAssert.assertThat(
            new JettyClientSlices().http("artipie.com"),
            new IsInstanceOf(JettyClientSlice.class)
        );
    }

    @Test
    void shouldProduceHttpsWithPort() {
        final int port = 9876;
        MatcherAssert.assertThat(
            new JettyClientSlices().http("www.artipie.com", port),
            new IsInstanceOf(JettyClientSlice.class)
        );
    }

    @Test
    void shouldSupportProxy() throws Exception {
        final byte[] response = "response from proxy".getBytes();
        final Slice slice = (line, headers, body) -> new RsWithBody(
            Flowable.just(ByteBuffer.wrap(response))
        );
        try (VertxSliceServer server = new VertxSliceServer(Vertx.vertx(), slice)) {
            final int port = server.start();
            final JettyClientSlices client = new JettyClientSlices(
                new Settings.WithProxy(new Settings.Proxy.Simple(false, "localhost", port))
            );
            try {
                client.start();
                MatcherAssert.assertThat(
                    client.http("artipie.com").response(
                        new RequestLine(RqMethod.GET, "/").toString(),
                        Headers.EMPTY,
                        Flowable.empty()
                    ),
                    new RsHasBody(response)
                );
            } finally {
                client.stop();
            }
        }
    }
}
