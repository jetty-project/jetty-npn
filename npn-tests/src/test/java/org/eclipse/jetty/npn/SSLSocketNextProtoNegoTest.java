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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import org.junit.Assert;
import org.junit.Test;

public class SSLSocketNextProtoNegoTest
{
    @Test
    public void testSSLSocket() throws Exception
    {
        SSLContext context = SSLSupport.newSSLContext();

        final int readTimeout = 5000;
        final String data = "data";
        final String protocolName = "test";
        final AtomicReference<CountDownLatch> latch = new AtomicReference<>(new CountDownLatch(4));
        final SSLServerSocket server = (SSLServerSocket)context.getServerSocketFactory().createServerSocket();
        server.bind(new InetSocketAddress("localhost", 0));
        final CountDownLatch handshakeLatch = new CountDownLatch(2);
        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    SSLSocket socket = (SSLSocket)server.accept();
                    socket.setUseClientMode(false);
                    socket.setSoTimeout(readTimeout);
                    NextProtoNego.put(socket, new NextProtoNego.ServerProvider()
                    {
                        @Override
                        public void unsupported()
                        {
                        }

                        @Override
                        public List<String> protocols()
                        {
                            latch.get().countDown();
                            return Arrays.asList(protocolName);
                        }

                        @Override
                        public void protocolSelected(String protocol)
                        {
                            Assert.assertEquals(protocolName, protocol);
                            latch.get().countDown();
                        }
                    });
                    socket.addHandshakeCompletedListener(new HandshakeCompletedListener()
                    {
                        @Override
                        public void handshakeCompleted(HandshakeCompletedEvent event)
                        {
                            handshakeLatch.countDown();
                        }
                    });
                    socket.startHandshake();

                    InputStream serverInput = socket.getInputStream();
                    for (int i = 0; i < data.length(); ++i)
                    {
                        int read = serverInput.read();
                        Assert.assertEquals(data.charAt(i), read);
                    }

                    OutputStream serverOutput = socket.getOutputStream();
                    serverOutput.write(data.getBytes("UTF-8"));
                    serverOutput.flush();

                    for (int i = 0; i < data.length(); ++i)
                    {
                        int read = serverInput.read();
                        Assert.assertEquals(data.charAt(i), read);
                    }

                    serverOutput.write(data.getBytes("UTF-8"));
                    serverOutput.flush();

                    // Re-handshake
                    socket.startHandshake();

                    for (int i = 0; i < data.length(); ++i)
                    {
                        int read = serverInput.read();
                        Assert.assertEquals(data.charAt(i), read);
                    }

                    serverOutput.write(data.getBytes("UTF-8"));
                    serverOutput.flush();

                    Assert.assertEquals(4, latch.get().getCount());

                    socket.close();
                }
                catch (Exception x)
                {
                    x.printStackTrace();
                }
            }
        }.start();

        SSLSocket client = (SSLSocket)context.getSocketFactory().createSocket("localhost", server.getLocalPort());
        client.setUseClientMode(true);
        client.setSoTimeout(readTimeout);
        NextProtoNego.put(client, new NextProtoNego.ClientProvider()
        {
            @Override
            public boolean supports()
            {
                latch.get().countDown();
                return true;
            }

            @Override
            public void unsupported()
            {
            }

            @Override
            public String selectProtocol(List<String> protocols)
            {
                Assert.assertEquals(1, protocols.size());
                String protocol = protocols.get(0);
                Assert.assertEquals(protocolName, protocol);
                latch.get().countDown();
                return protocol;
            }
        });

        client.addHandshakeCompletedListener(new HandshakeCompletedListener()
        {
            @Override
            public void handshakeCompleted(HandshakeCompletedEvent event)
            {
                handshakeLatch.countDown();
            }
        });

        client.startHandshake();

        Assert.assertTrue(latch.get().await(5, TimeUnit.SECONDS));
        Assert.assertTrue(handshakeLatch.await(5, TimeUnit.SECONDS));

        // Check whether we can write real data to the connection
        OutputStream clientOutput = client.getOutputStream();
        clientOutput.write(data.getBytes("UTF-8"));
        clientOutput.flush();

        InputStream clientInput = client.getInputStream();
        for (int i = 0; i < data.length(); ++i)
        {
            int read = clientInput.read();
            Assert.assertEquals(data.charAt(i), read);
        }

        // Re-handshake
        latch.set(new CountDownLatch(4));
        client.startHandshake();
        Assert.assertEquals(4, latch.get().getCount());

        clientOutput.write(data.getBytes("UTF-8"));
        clientOutput.flush();

        for (int i = 0; i < data.length(); ++i)
        {
            int read = clientInput.read();
            Assert.assertEquals(data.charAt(i), read);
        }

        clientOutput.write(data.getBytes("UTF-8"));
        clientOutput.flush();

        for (int i = 0; i < data.length(); ++i)
        {
            int read = clientInput.read();
            Assert.assertEquals(data.charAt(i), read);
        }

        int read = clientInput.read();
        Assert.assertEquals(-1, read);

        client.close();

        server.close();
    }
}
