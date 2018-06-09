/*
 * @(#)HttpServerProvider.java	1.4 07/01/02
 *
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.httpserver;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Service provider class for HttpServer.
 * Sub-classes of HttpServerProvider provide an implementation of {@link HttpServer} and
 * associated classes. Applications do not normally use this class.
 * See {@link #provider()} for how providers are found and loaded.
 */
public abstract class HttpServerProvider {

    private static HttpServerProvider provider = null;

    /**
     * Initializes a new instance of this class.
     */
    protected HttpServerProvider() {
    }

    /**
     * Returns the default HttpServerProvider if no other HttpServerProvider is set
     *
     * @return HttpServerProvider
     */
    public static HttpServerProvider provider() {
        if (provider != null)
            return provider;
        return new DefaultHttpServerProvider();
    }

    /**
     * Set new HttpServerProvider
     */
    public static void setProvider(HttpServerProvider newProvider) {
        provider = newProvider;
    }

    /**
     * creates a HttpServer from this provider
     *
     * @param addr    the address to bind to. May be <code>null</code>
     * @param backlog the socket backlog. A value of <code>zero</code> means the systems default
     */
    public abstract HttpServer createHttpServer(InetSocketAddress addr, int backlog) throws IOException;

    /**
     * creates a HttpsServer from this provider
     *
     * @param addr    the address to bind to. May be <code>null</code>
     * @param backlog the socket backlog. A value of <code>zero</code> means the systems default
     */
    public abstract HttpsServer createHttpsServer(InetSocketAddress addr, int backlog) throws IOException;

}
