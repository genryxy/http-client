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
package com.artipie.http.client;

import java.util.Optional;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SettingsTest}.
 *
 * @since 0.1
 */
final class SettingsTest {

    @Test
    void defaultProxy() {
        MatcherAssert.assertThat(
            new Settings.Default().proxy().isPresent(),
            new IsEqual<>(false)
        );
    }

    @Test
    void defaultTrustAll() {
        MatcherAssert.assertThat(
            new Settings.Default().trustAll(),
            new IsEqual<>(false)
        );
    }

    @Test
    void proxyFrom() {
        final boolean secure = true;
        final String host = "proxy.com";
        final int port = 8080;
        final Settings.Proxy.Simple proxy = new Settings.Proxy.Simple(secure, host, port);
        MatcherAssert.assertThat(
            "Wrong secure flag",
            proxy.secure(),
            new IsEqual<>(secure)
        );
        MatcherAssert.assertThat(
            "Wrong host",
            proxy.host(),
            new IsEqual<>(host)
        );
        MatcherAssert.assertThat(
            "Wrong port",
            proxy.port(),
            new IsEqual<>(port)
        );
    }

    @Test
    void withProxy() {
        final Settings.Proxy proxy = new Settings.Proxy.Simple(false, "example.com", 80);
        MatcherAssert.assertThat(
            new Settings.WithProxy(new Settings.Default(), proxy).proxy(),
            new IsEqual<>(Optional.of(proxy))
        );
    }
}
