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
package com.artipie.http.client.auth;

import com.artipie.asto.Content;
import com.artipie.http.Headers;
import com.artipie.http.client.FakeClientSlices;
import com.artipie.http.headers.Header;
import com.artipie.http.headers.WwwAuthenticate;
import com.artipie.http.rq.RequestLineFrom;
import com.artipie.http.rs.RsWithBody;
import com.artipie.http.rs.StandardRs;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link BearerAuthenticator}.
 *
 * @since 0.4
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle MagicNumberCheck (500 lines)
 */
class BearerAuthenticatorTest {

    @Test
    void shouldRequestTokenFromRealm() {
        final AtomicReference<String> path = new AtomicReference<>();
        final FakeClientSlices fake = new FakeClientSlices(
            (rsline, rqheaders, rqbody) -> {
                path.set(new RequestLineFrom(rsline).uri().getRawPath());
                return StandardRs.OK;
            }
        );
        new BearerAuthenticator(
            fake,
            bytes -> "token"
        ).authenticate(
            new Headers.From(
                new WwwAuthenticate("Bearer realm=\"https://artipie.com:321/get_token\"")
            )
        ).toCompletableFuture().join();
        MatcherAssert.assertThat(
            "Scheme is correct",
            fake.capturedSecure(),
            new IsEqual<>(true)
        );
        MatcherAssert.assertThat(
            "Host is correct",
            fake.capturedHost(),
            new IsEqual<>("artipie.com")
        );
        MatcherAssert.assertThat(
            "Port is correct",
            fake.capturedPort(),
            new IsEqual<>(321)
        );
        MatcherAssert.assertThat(
            "Path is correct",
            path.get(),
            new IsEqual<>("/get_token")
        );
    }

    @Test
    void shouldProduceBearerHeaderUsingTokenFormat() {
        final byte[] response = "{\"access_token\":\"mF_9.B5f-4.1JqM\"}".getBytes();
        final AtomicReference<byte[]> captured = new AtomicReference<>();
        final Headers headers = new BearerAuthenticator(
            new FakeClientSlices(
                (rqline, rqheaders, rqbody) -> new RsWithBody(new Content.From(response))
            ),
            bytes -> {
                captured.set(bytes);
                return "mF_9.B5f-4.1JqM";
            }
        ).authenticate(
            new Headers.From(new WwwAuthenticate("Bearer realm=\"http://localhost\""))
        ).toCompletableFuture().join();
        MatcherAssert.assertThat(
            "Token response sent to token format",
            captured.get(),
            new IsEqual<>(response)
        );
        MatcherAssert.assertThat(
            "Result headers contains authorization",
            StreamSupport.stream(
                headers.spliterator(),
                false
            ).map(Header::new).collect(Collectors.toList()),
            Matchers.contains(new Header("Authorization", "Bearer mF_9.B5f-4.1JqM"))
        );
    }
}
