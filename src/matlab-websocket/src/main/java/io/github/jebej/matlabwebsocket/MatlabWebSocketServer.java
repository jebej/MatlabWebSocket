package io.github.jebej.matlabwebsocket;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class MatlabWebSocketServer {
    // Store the Jetty Server object
    private Server server;

    // The constructor creates a new MatlabWebSocketServer with the wildcard IP,
    // accepting all connections on the specified port
    public MatlabWebSocketServer( int port ) {
        this.server = new Server(port);

        // Setup the basic application "context" for this application at "/"
        // This is also known as the handler tree (in jetty speak)
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // Add websocket servlet to root path
        ServletHolder wsHolder = new ServletHolder("MATLAB", MatlabServlet.class );
        context.addServlet(wsHolder,"/");
    }

    // Start server method
    public void start() {
        try {
            this.server.start();
            this.server.dump( System.err );
            this.server.join();
        }
        catch (Exception e) {
            e.printStackTrace( System.err );
        }
    }

    public static void main(String[] args) {
        MatlabWebSocketServer ms = new MatlabWebSocketServer(30000);
        ms.start();
    }
}
