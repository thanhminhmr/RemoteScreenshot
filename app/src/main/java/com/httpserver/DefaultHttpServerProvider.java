/*
 * @(#)DefaultHttpServerProvider.java	1.4 07/01/02
 *
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.httpserver;

import java.io.IOException;
import java.net.InetSocketAddress;

public class DefaultHttpServerProvider extends HttpServerProvider {
    public HttpServer createHttpServer(InetSocketAddress addr, int backlog) throws IOException {
        return new HttpServerImpl(addr, backlog);
    }

    public HttpsServer createHttpsServer(InetSocketAddress addr, int backlog) throws IOException {
        return new HttpsServerImpl(addr, backlog);
    }
}
