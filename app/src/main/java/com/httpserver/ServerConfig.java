/*
 * @(#)ServerConfig.java	1.6 07/01/02
 *
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.httpserver;

/**
 * Parameters that users will not likely need to set
 * but are useful for debugging
 */

class ServerConfig {
    private static final int defaultClockTick = 10000; // 10 sec.
    /* These values must be a reasonable multiple of clockTick */
    private static final long defaultReadTimeout = 20; // 20 sec.
    private static final long defaultWriteTimeout = 60; // 60 sec.
    private static final long defaultIdleInterval = 300; // 5 min
    private static final long defaultSelCacheTimeout = 120;  // seconds
    private static final int defaultMaxIdleConnections = 200;
    private static final long defaultDrainAmount = 64 * 1024;

    private static final int clockTick;
    private static final long readTimeout;
    private static final long writeTimeout;
    private static final long idleInterval;
    private static final long selCacheTimeout;
    private static final long drainAmount;    // max # of bytes to drain from an inputstream
    private static final int maxIdleConnections;
    private static final boolean debug = false;

    static {
        idleInterval = defaultIdleInterval * 1000;

        clockTick = defaultClockTick;

        maxIdleConnections = defaultMaxIdleConnections;

        readTimeout = defaultReadTimeout * 1000;

        selCacheTimeout = defaultSelCacheTimeout * 1000;

        writeTimeout = defaultWriteTimeout * 1000;

        drainAmount = defaultDrainAmount;
    }

    static long getReadTimeout() {
        return readTimeout;
    }

    static long getSelCacheTimeout() {
        return selCacheTimeout;
    }

    static boolean debugEnabled() {
        return debug;
    }

    static long getIdleInterval() {
        return idleInterval;
    }

    static int getClockTick() {
        return clockTick;
    }

    static int getMaxIdleConnections() {
        return maxIdleConnections;
    }

    static long getWriteTimeout() {
        return writeTimeout;
    }

    static long getDrainAmount() {
        return drainAmount;
    }
}
