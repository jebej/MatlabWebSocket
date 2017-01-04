package io.github.jebej.matlabwebsocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fi.iki.elonen.NanoWSD;
import fi.iki.elonen.NanoWSD.WebSocketFrame.CloseCode;

public class MatlabWebSocketServer extends NanoWSD {
    // To debug or not to debug
    private final boolean debug;
    // The constructor creates a new MatlabWebSocketServer with the wildcard IP,
    // accepting all connections on the specified port
    public MatlabWebSocketServer( int port ) {
        super( port );
        this.debug = true;
    }

    @Override
    public WebSocket openWebSocket( IHTTPSession handshake ) {
        return new MatlabSocket( this, handshake );
    }

    // The following private class defines the interaction with MATLAB
    public class MatlabSocket extends WebSocket {
        public final MatlabWebSocketServer server;

        public MatlabSocket( MatlabWebSocketServer server, IHTTPSession handshakeRequest ) {
            super(handshakeRequest);
            this.server = server;
        }

        @Override
        public void onOpen() {
            //String add = this.getRemoteAddress().getHostName() + ":" + conn.getRemoteAddress().getPort();
            //String openMessage = "Client " + this.getRemoteAddress().hashCode() + " at " + add + " opened a connection";
            MatlabEvent matlab_event = new MatlabEvent( server, this, "hiiiii!" );
            Iterator<MatlabListener> listeners = _listeners.iterator();
            while (listeners.hasNext() ) {
                ( (MatlabListener) listeners.next() ).Open( matlab_event );
            }
        }

        @Override
        protected void onMessage(WebSocketFrame message) {
            try {
                message.setUnmasked();
                sendFrame(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void onException(IOException exception) {
        }

        @Override
        protected void onClose(CloseCode code, String reason, boolean initiatedByRemote) {
            if (server.debug) {
                System.out.println("C [" + (initiatedByRemote ? "Remote" : "Self") + "] " + (code != null ? code : "UnknownCloseCode[" + code + "]")
                + (reason != null && !reason.isEmpty() ? ": " + reason : ""));
            }
        }

        @Override
        protected void onPong(WebSocketFrame pong) {
            if (server.debug) {
                System.out.println("P " + pong);
            }
        }
    }

    // Methods for handling MATLAB as a listener, automatically managed
    public List<MatlabListener> _listeners = new ArrayList<MatlabListener>();
    public synchronized void addMatlabListener( MatlabListener lis ) {
        _listeners.add( lis );
    }
    public synchronized void removeMatlabListener( MatlabListener lis ) {
        _listeners.remove( lis );
    }
}
