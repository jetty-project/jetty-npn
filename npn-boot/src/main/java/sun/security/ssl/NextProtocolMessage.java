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

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;

public class NextProtocolMessage extends HandshakeMessage
{
    public static final int ID = 67;

    private final byte[] protocolBytes;
    private final byte[] paddingBytes;
    private final String protocol;

    public NextProtocolMessage(String protocol)
    {
        protocolBytes = protocol.getBytes(Charset.forName("UTF-8"));
        paddingBytes = new byte[32 - ((1 + protocolBytes.length + 1) % 32)];
        this.protocol = protocol;
    }

    public NextProtocolMessage(HandshakeInStream input) throws IOException
    {
        protocolBytes = input.getBytes8();
        paddingBytes = input.getBytes8();
        protocol = new String(protocolBytes, "UTF-8");
    }

    public String getProtocol()
    {
        return protocol;
    }

    @Override
    int messageType()
    {
        return ID;
    }

    @Override
    int messageLength()
    {
        return 1 + protocolBytes.length + 1 + paddingBytes.length;
    }

    @Override
    void send(HandshakeOutStream output) throws IOException
    {
        output.putBytes8(protocolBytes);
        output.putBytes8(paddingBytes);
    }

    @Override
    void print(PrintStream stream) throws IOException
    {
        stream.printf("*** NextProtocol(%s)%n", protocol);
    }
}
