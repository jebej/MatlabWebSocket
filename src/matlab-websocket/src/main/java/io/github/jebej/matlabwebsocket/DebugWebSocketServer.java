package io.github.jebej.matlabwebsocket;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import fi.iki.elonen.NanoWSD;
import fi.iki.elonen.NanoWSD.WebSocketFrame.CloseCode;

public class DebugWebSocketServer extends NanoWSD {

    /**
     * logger to log to.
     */
    private static final Logger LOG = Logger.getLogger(DebugWebSocketServer.class.getName());

    private final boolean debug;

    public DebugWebSocketServer(int port, boolean debug) {
        super(port);
        this.debug = debug;
    }

    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        return new DebugWebSocket(this, handshake);
    }

    private static class DebugWebSocket extends WebSocket {

        private final DebugWebSocketServer server;

        public DebugWebSocket(DebugWebSocketServer server, IHTTPSession handshakeRequest) {
            super(handshakeRequest);
            this.server = server;
        }

        @Override
        protected void onOpen() {
        }

        @Override
        protected void onClose(CloseCode code, String reason, boolean initiatedByRemote) {
            if (server.debug) {
                System.out.println("C [" + (initiatedByRemote ? "Remote" : "Self") + "] " + (code != null ? code : "UnknownCloseCode[" + code + "]")
                        + (reason != null && !reason.isEmpty() ? ": " + reason : ""));
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
        protected void onPong(WebSocketFrame pong) {
            if (server.debug) {
                System.out.println("P " + pong);
            }
        }

        @Override
        protected void onException(IOException exception) {
            DebugWebSocketServer.LOG.log(Level.SEVERE, "exception occured", exception);
        }

        @Override
        protected void debugFrameReceived(WebSocketFrame frame) {
            if (server.debug) {
                System.out.println("R " + frame);
            }
        }

        @Override
        protected void debugFrameSent(WebSocketFrame frame) {
            if (server.debug) {
                System.out.println("S " + frame);
            }
        }
    }
}
