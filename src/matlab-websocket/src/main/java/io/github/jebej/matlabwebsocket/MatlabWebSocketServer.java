package io.github.jebej.matlabwebsocket;

import java.io.IOException;

public class MatlabWebSocketServer {
    // Store the Jetty Server object
    private DebugWebSocketServer server;

    // The constructor creates a new MatlabWebSocketServer with the wildcard IP,
    // accepting all connections on the specified port
    public MatlabWebSocketServer( int port ) {
        this.server = new DebugWebSocketServer(port, true);
    }

    // Start server method
    public void start() throws IOException {
        this.server.start();
    }

    // Start server method
    public void stop() {
        this.server.stop();
    }

    public static void main(String[] args) throws IOException {
        MatlabWebSocketServer ms = new MatlabWebSocketServer(30000);
        ms.start();

        System.out.println("Server started, hit Enter to stop.\n");
        try {
            System.in.read();
        } catch (IOException ignored) {
        }

        ms.stop();
        System.out.println("Server stopped.\n");
    }
}
