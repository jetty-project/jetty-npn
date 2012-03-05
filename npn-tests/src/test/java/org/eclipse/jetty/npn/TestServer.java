/*
 * Copyright (c) 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.jetty.npn;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

/**
 * Server that just accepts socket connections and advertises spdy/2.
 * This is useful to test with Chromium and see if Chromium actually
 * selects the spdy/2 protocol, and if our implementation works.
 */
public class TestServer
{
    public static void main(String[] args) throws Exception
    {
        SSLContext context = SSLSupport.newSSLContext();
        SSLServerSocket server = (SSLServerSocket)context.getServerSocketFactory().createServerSocket(8443);
        while (true)
        {
            SSLSocket socket = (SSLSocket)server.accept();
            socket.setUseClientMode(false);
            NextProtoNego.put(socket, new NextProtoNego.ServerProvider()
            {
                @Override
                public void unsupported()
                {
                }

                @Override
                public List<String> protocols()
                {
                    return Arrays.asList("spdy/2", "http/1.1");
                }

                @Override
                public void protocolSelected(String protocol)
                {
                    System.err.println("protocol = " + protocol);
                }
            });
            try
            {
                socket.startHandshake();
            }
            catch (IOException x)
            {
                x.printStackTrace();
            }
        }
    }
}
