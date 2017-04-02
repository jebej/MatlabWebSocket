package io.github.jebej.matlabwebsocket;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class MatlabWebSocketClient extends WebSocketClient {
    // This open a websocket connection as specified by RFC6455
    public MatlabWebSocketClient( URI serverURI ) {
        super( serverURI );
    }

    // This function gets executed when the connection is opened
    @Override
    public void onOpen( ServerHandshake handshakedata ) {
        String openMessage = "Connected to server at " + getURI();
        MatlabEvent matlab_event = new MatlabEvent( this, openMessage );
        for (MatlabListener _listener : _listeners) {
            (_listener).Open(matlab_event);
        }
    }

    // This function gets executed on text message receipt
    @Override
    public void onMessage( String message ) {
        MatlabEvent matlab_event = new MatlabEvent( this, message );
        for (MatlabListener _listener : _listeners) {
            (_listener).TextMessage(matlab_event);
        }
    }

    // Method handler when a byte message has been received from the client
    @Override
    public void onMessage( ByteBuffer blob ) {
        MatlabEvent matlab_event = new MatlabEvent( this, blob );
        for (MatlabListener _listener : _listeners) {
            (_listener).BinaryMessage(matlab_event);
        }
    }

    // This method gets executed on error
    @Override
    public void onError( Exception ex ) {
        MatlabEvent matlab_event = new MatlabEvent( this, ex.getMessage() );
        for (MatlabListener _listener : _listeners) {
            (_listener).Error(matlab_event);
        }
        // If the error is fatal, onClose will be called automatically
    }

    // This function gets executed when the websocket connection is closed,
    // close codes are documented in org.java_websocket.framing.CloseFrame
    @Override
    public void onClose( int code, String reason, boolean remote ) {
        String closeMessage = "Disconnected from server at " + getURI();
        MatlabEvent matlab_event = new MatlabEvent( this, closeMessage );
        for (MatlabListener _listener : _listeners) {
            (_listener).Close(matlab_event);
        }
    }

    // Methods for handling MATLAB as a listener, automatically managed.
    private final List<MatlabListener> _listeners = new ArrayList<MatlabListener>();
    public synchronized void addMatlabListener( MatlabListener lis ) {
        _listeners.add( lis );
    }
    public synchronized void removeMatlabListener( MatlabListener lis ) {
        _listeners.remove( lis );
    }
}
