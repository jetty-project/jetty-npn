## Next Protocol Negotiation Implementation ##

This project implements the [Next Protocol Negotiation (NPN)
Specification](http://technotes.googlecode.com/git/nextprotoneg.html)
for OpenJDK 7 and greater.

The 'master' branch contains few new classes and modifications to few OpenJDK
classes that implement the NPN functionalities in the 'npn-boot' module.

In order to make your application NPN enabled, you need to start the JVM
with the option:

    java -Xbootclasspath/p:<path_to_npn_boot_jar> ...

The 'javaagent' branch contains the same few new classes, but the OpenJDK
classes are modified on-the-fly via bytecode transformation.
This solution is more complex and does not avoid the need to put the few
new classes in the boot classpath anyway, so its usage is discouraged.

### Next Protocol Negotiation API ###

A simple API provides NPN support to applications.
First, applications needs to register either a `SSLSocket` or a `SSLEngine`
instance with a NPN provider.

The API offers a client provider and a server provider, depending whether
the application is a client application that communicates with a NPN enabled
server, or a server application that communicates with a NPN enabled client.

#### Client Example ####

    SSLContext sslContext = ...;
    SSLSocket sslSocket = (SSLSocket)context.getSocketFactory()
            .createSocket("localhost", server.getLocalPort());

    NextProtoNego.put(sslSocket, new NextProtoNego.ClientProvider()
    {
        @Override
        public boolean supports()
        {
            return true;
        }

        @Override
        public String selectProtocol(List<String> protocols)
        {
            return protocols.get(0);
        }
    });

Methods `supports()` and `selectProtocol(List<String>)` will be called by
the NPN implementation, so that the application can, respectively, decide
whether to support NPN and select one of the protocols supported by
the server.

The example for `SSLEngine` is identical, and just needs to replace the
`SSLSocket` instance with a `SSLEngine` instance.

#### Server Example ####

    SSLSocket sslSocket = ...;
    NextProtoNego.put(sslSocket, new NextProtoNego.ServerProvider()
    {
        @Override
        public List<String> protocols()
        {
            return Arrays.asList("http/1.1");
        }

        @Override
        public void protocolSelected(String protocol)
        {
            System.out.println("Protocol Selected is: " + protocol);
        }
    });

Methods `protocols()` and `protocolSelected(String)` will be called by the
NPN implementation, so that the application can, respectively, provide the
list of protocols supported by the server, and know what is the protocol
chosen by the client.
