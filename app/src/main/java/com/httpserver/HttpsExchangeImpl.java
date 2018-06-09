/*
 * @(#)HttpsExchangeImpl.java	1.9 07/01/02
 *
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.httpserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

import javax.net.ssl.SSLSession;

class HttpsExchangeImpl extends HttpsExchange {

    ExchangeImpl impl;

    HttpsExchangeImpl(ExchangeImpl impl) throws IOException {
        this.impl = impl;
    }

    public Headers getRequestHeaders() {
        return impl.getRequestHeaders();
    }

    public Headers getResponseHeaders() {
        return impl.getResponseHeaders();
    }

    public URI getRequestURI() {
        return impl.getRequestURI();
    }

    public String getRequestMethod() {
        return impl.getRequestMethod();
    }

    public HttpContextImpl getHttpContext() {
        return impl.getHttpContext();
    }

    public void close() {
        impl.close();
    }

    public InputStream getRequestBody() {
        return impl.getRequestBody();
    }

    public int getResponseCode() {
        return impl.getResponseCode();
    }

    public OutputStream getResponseBody() {
        return impl.getResponseBody();
    }


    public void sendResponseHeaders(int rCode, long contentLen)
            throws IOException {
        impl.sendResponseHeaders(rCode, contentLen);
    }

    public InetSocketAddress getRemoteAddress() {
        return impl.getRemoteAddress();
    }

    public InetSocketAddress getLocalAddress() {
        return impl.getLocalAddress();
    }

    public String getProtocol() {
        return impl.getProtocol();
    }

    public SSLSession getSSLSession() {
        return impl.getSSLSession();
    }

    public Object getAttribute(String name) {
        return impl.getAttribute(name);
    }

    public void setAttribute(String name, Object value) {
        impl.setAttribute(name, value);
    }

    public void setStreams(InputStream i, OutputStream o) {
        impl.setStreams(i, o);
    }

    public HttpPrincipal getPrincipal() {
        return impl.getPrincipal();
    }

    ExchangeImpl getExchangeImpl() {
        return impl;
    }
}
