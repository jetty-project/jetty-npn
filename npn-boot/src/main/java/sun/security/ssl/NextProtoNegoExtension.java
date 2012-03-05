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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.SSLProtocolException;

public class NextProtoNegoExtension extends HelloExtension
{
    private static final int ID = 0x3374;

    private final List<String> protocols = new ArrayList<>();
    private final byte[] content;

    public NextProtoNegoExtension() throws SSLProtocolException
    {
        this(Collections.<String>emptyList());
    }

    public NextProtoNegoExtension(List<String> protocols) throws SSLProtocolException
    {
        super(ExtensionType.get(ID));
        this.protocols.addAll(protocols);
        content = init();
    }

    public NextProtoNegoExtension(HandshakeInStream input, int length) throws IOException
    {
        super(ExtensionType.get(ID));
        while (length > 0)
        {
            byte[] protoBytes = input.getBytes8();
            protocols.add(new String(protoBytes, "UTF-8"));
            length -= 1 + protoBytes.length;
        }
        content = init();
    }

    private byte[] init() throws SSLProtocolException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        for (String protocol : protocols)
        {
            byte[] protocolBytes = protocol.getBytes(Charset.forName("UTF-8"));
            int length = protocolBytes.length;
            if (length > Byte.MAX_VALUE)
                throw new SSLProtocolException("Invalid protocol " + protocol);
            bytes.write(length);
            bytes.write(protocolBytes, 0, length);
        }
        return bytes.toByteArray();
    }

    public List<String> getProtocols()
    {
        return protocols;
    }

    @Override
    int length()
    {
        return 2 + 2 + content.length;
    }

    @Override
    void send(HandshakeOutStream out) throws IOException
    {
        out.putInt16(ID);
        out.putInt16(content.length);
        out.write(content, 0, content.length);
    }

    @Override
    public String toString()
    {
        return "Extension " + type + ", protocols: " + protocols;
    }
}
