package io.github.jebej.matlabwebsocket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;

@WebSocket
public class MatlabBridgeServerSocket {

    // Method handler when a new connection has been opened
    @OnWebSocketConnect
    public void onOpen( Session conn ) {
        String add = conn.getRemoteAddress().getHostName() + ":" + conn.getRemoteAddress().getPort();
        String openMessage = "Client " + conn.getRemoteAddress().hashCode() + " at " + add + " opened a connection";
        MatlabEvent matlab_event = new MatlabEvent( this, conn, openMessage );
        Iterator<MatlabListener> listeners = _listeners.iterator();
        while (listeners.hasNext() ) {
            ( (MatlabListener) listeners.next() ).Open( matlab_event );
        }
    }

    // Method handler when a text message has been received from the client
    @OnWebSocketMessage
    public void onMessage( Session conn, String message ) {
        MatlabEvent matlab_event = new MatlabEvent( this, conn, message );
        Iterator<MatlabListener> listeners = _listeners.iterator();
        while (listeners.hasNext() ) {
            ( (MatlabListener) listeners.next() ).TextMessage( matlab_event );
        }
    }

    // Method handler when a binary message has been received from the client
    @OnWebSocketMessage
    public void onMessage( Session conn, byte[] buf, int offset, int length ) {
        ByteBuffer blob = ByteBuffer.wrap( buf, offset, length);
        MatlabEvent matlab_event = new MatlabEvent( this, conn, blob );
        Iterator<MatlabListener> listeners = _listeners.iterator();
        while (listeners.hasNext() ) {
            ( (MatlabListener) listeners.next() ).BinaryMessage( matlab_event );
        }
    }

    // Method handler when an error has occurred
    @OnWebSocketError
    public void onError( Session conn, Throwable cause ) {
        MatlabEvent matlab_event = new MatlabEvent( this, conn, cause.getMessage() );
        Iterator<MatlabListener> listeners = _listeners.iterator();
        while (listeners.hasNext() ) {
            ( (MatlabListener) listeners.next() ).Error( matlab_event );
        }
    }

    // Method handler when a connection has been closed
    @OnWebSocketClose
    public void onClose( Session conn, int code, String reason ) {
        String add = conn.getRemoteAddress().getHostName() + ":" + conn.getRemoteAddress().getPort();
        String closeMessage = "Closed connection to client " + conn.getRemoteAddress().hashCode() + " at " + add;
        MatlabEvent matlab_event = new MatlabEvent( this, conn, closeMessage );
        Iterator<MatlabListener> listeners = _listeners.iterator();
        while (listeners.hasNext() ) {
            ( (MatlabListener) listeners.next() ).Close( matlab_event );
        }
    }

    // Methods for handling MATLAB as a listener, automatically managed
    private List<MatlabListener> _listeners = new ArrayList<MatlabListener>();
    public synchronized void addMatlabListener( MatlabListener lis ) {
        _listeners.add( lis );
    }
    public synchronized void removeMatlabListener( MatlabListener lis ) {
        _listeners.remove( lis );
    }

}
