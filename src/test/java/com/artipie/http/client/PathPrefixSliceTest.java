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

import com.artipie.asto.Content;
import com.artipie.asto.ext.PublisherAs;
import com.artipie.http.Headers;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RequestLineFrom;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.StandardRs;
import java.util.concurrent.CompletableFuture;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PathPrefixSlice}.
 *
 * @since 0.3
 */
final class PathPrefixSliceTest {

    @Test
    void shouldAddPrefixToPathAndPreserveEverythingElse() {
        final RqMethod method = RqMethod.GET;
        final Headers headers = new Headers.From("X-Header", "The Value");
        final byte[] body = "request body".getBytes();
        new PathPrefixSlice(
            (line, rqheaders, rqbody) -> {
                MatcherAssert.assertThat(
                    "Path is prefixed",
                    new RequestLineFrom(line).uri().getPath(),
                    new IsEqual<>("/prefix/path")
                );
                MatcherAssert.assertThat(
                    "Method is preserved",
                    new RequestLineFrom(line).method(),
                    new IsEqual<>(method)
                );
                MatcherAssert.assertThat(
                    "Headers are preserved",
                    rqheaders,
                    new IsEqual<>(headers)
                );
                MatcherAssert.assertThat(
                    "Body is preserved",
                    new PublisherAs(rqbody).bytes().toCompletableFuture().join(),
                    new IsEqual<>(body)
                );
                return StandardRs.OK;
            },
            "/prefix"
        ).response(
            new RequestLine(method, "/path").toString(),
            headers,
            new Content.From(body)
        ).send(
            (status, rsheaders, rsbody) -> CompletableFuture.allOf()
        );
    }
}
