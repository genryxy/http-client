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

import com.artipie.http.Headers;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.rs.RsStatus;
import com.google.common.collect.Iterables;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import org.reactivestreams.Publisher;

/**
 * Slice augmenting requests with authentication when needed.
 *
 * @since 0.3
 */
public final class AuthClientSlice implements Slice {

    /**
     * Origin slice.
     */
    private final Slice origin;

    /**
     * Authenticator.
     */
    private final Authenticator auth;

    /**
     * Ctor.
     *
     * @param origin Origin slice.
     * @param auth Authenticator.
     */
    public AuthClientSlice(final Slice origin, final Authenticator auth) {
        this.origin = origin;
        this.auth = auth;
    }

    @Override
    public Response response(
        final String line,
        final Iterable<Map.Entry<String, String>> headers,
        final Publisher<ByteBuffer> body) {
        return connection -> this.origin.response(
            line,
            new Headers.From(headers, this.auth.authenticate(Headers.EMPTY)),
            body
        ).send(
            (rsstatus, rsheaders, rsbody) -> {
                final CompletionStage<Void> sent;
                if (rsstatus == RsStatus.UNAUTHORIZED) {
                    final Headers second = this.auth.authenticate(rsheaders);
                    if (Iterables.isEmpty(second)) {
                        sent = connection.accept(rsstatus, rsheaders, rsbody);
                    } else {
                        sent = this.origin.response(
                            line,
                            new Headers.From(headers, second),
                            body
                        ).send(connection);
                    }
                } else {
                    sent = connection.accept(rsstatus, rsheaders, rsbody);
                }
                return sent;
            }
        );
    }
}
