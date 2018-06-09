/*
 * @(#)TimeSource.java	1.4 07/01/02
 *
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.httpserver;

interface TimeSource {
    long getTime();
}
