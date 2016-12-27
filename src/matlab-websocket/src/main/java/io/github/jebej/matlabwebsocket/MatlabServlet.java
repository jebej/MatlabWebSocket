package io.github.jebej.matlabwebsocket;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

@SuppressWarnings("serial")
public class MatlabServlet extends WebSocketServlet
{
    @Override
    public void configure(WebSocketServletFactory factory)
    {
        // Set a 10 second timeout
        factory.getPolicy().setIdleTimeout(10000);
        // Register MatlabBridgeServerSocket
        factory.register(MatlabBridgeServerSocket.class);
    }
}
