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
import com.artipie.http.headers.WwwAuthenticate;
import java.util.stream.StreamSupport;

/**
 * Generic authenticator that performs authentication using username and password.
 * Authentication is done if requested by server using required scheme.
 *
 * @since 0.3
 */
public final class GenericAuthenticator implements Authenticator {

    /**
     * Basic authenticator used when required.
     */
    private final Authenticator.Basic basic;

    /**
     * Ctor.
     *
     * @param username Username.
     * @param password Password.
     */
    public GenericAuthenticator(final String username, final String password) {
        this.basic = new Authenticator.Basic(username, password);
    }

    @Override
    public Headers authenticate(final Headers headers) {
        return StreamSupport.stream(headers.spliterator(), false)
            .filter(header -> header.getKey().equals(WwwAuthenticate.NAME))
            .findAny()
            .map(
                header -> this.authenticate(
                    new WwwAuthenticate(header.getValue())
                ).authenticate(headers)
            )
            .orElse(Headers.EMPTY);
    }

    /**
     * Get authorization headers.
     *
     * @param header WWW-Authenticate to use for authorization.
     * @return Authorization headers.
     */
    public Authenticator authenticate(final WwwAuthenticate header) {
        final String scheme = header.scheme();
        if ("Basic".equals(scheme)) {
            return this.basic;
        }
        throw new IllegalArgumentException(String.format("Unsupported scheme: %s", scheme));
    }
}
