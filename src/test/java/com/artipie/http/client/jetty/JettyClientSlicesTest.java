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
 */
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
}
