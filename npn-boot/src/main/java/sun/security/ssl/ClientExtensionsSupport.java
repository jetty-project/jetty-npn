/*
 * Copyright (c) 2011, Mort Bay Consulting Pty. Ltd. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Mort Bay Consulting designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Mort Bay Consulting in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 */

package sun.security.ssl;

import java.util.ArrayList;
import java.util.List;

public final class ClientExtensionsSupport {
    public static List<String> getHostNames(SSLEngineImpl engine) {
        try {
            ServerNameExtension e = (ServerNameExtension) engine.clientExtensions.get(ExtensionType.EXT_SERVER_NAME);
            ArrayList<String> result = new ArrayList<>();
            for (ServerNameExtension.ServerName sn : e.getNames()) {
                result.add(sn.hostname);
            }
            return result;
        } catch (Exception e) {
            return new ArrayList<String>();
        }
    }
}
