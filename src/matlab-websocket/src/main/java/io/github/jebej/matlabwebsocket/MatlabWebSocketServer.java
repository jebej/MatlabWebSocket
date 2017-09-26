package io.github.jebej.matlabwebsocket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class MatlabWebSocketServer extends WebSocketServer {
    // The constructor creates a new WebSocketServer with the wildcard IP,
    // accepting all connections on the specified port
    public MatlabWebSocketServer( int port ) {
        super( new InetSocketAddress( port ) );
    }

    // Server start
    @Override
	public void onStart() {
		//System.out.println( "Server started!" );
	}

    // Method handler when a new connection has been opened
    @Override
    public void onOpen( WebSocket conn, ClientHandshake handshake ) {
        String add = conn.getRemoteSocketAddress().getHostName() + ":" + conn.getRemoteSocketAddress().getPort();
        String openMessage = "Client " + conn.hashCode() + " at " + add + " opened a connection";
        MatlabEvent matlab_event = new MatlabEvent( this, conn, openMessage );
        for (MatlabListener _listener : _listeners) {
            (_listener).Open(matlab_event);
        }
    }

    // Method handler when a text message has been received from the client
    @Override
    public void onMessage( WebSocket conn, String message ) {
        MatlabEvent matlab_event = new MatlabEvent( this, conn, message );
        for (MatlabListener _listener : _listeners) {
            (_listener).TextMessage(matlab_event);
        }
    }

    // Method handler when a binary message has been received from the client
    @Override
    public void onMessage( WebSocket conn, ByteBuffer blob ) {
        MatlabEvent matlab_event = new MatlabEvent( this, conn, blob );
        for (MatlabListener _listener : _listeners) {
            (_listener).BinaryMessage(matlab_event);
        }
    }

    // Method handler when an error has occurred
    @Override
    public void onError( WebSocket conn, Exception ex ) {
        MatlabEvent matlab_event = new MatlabEvent( this, conn, ex.getMessage() );
        for (MatlabListener _listener : _listeners) {
            (_listener).Error(matlab_event);
        }
    }

    // Method handler when a connection has been closed
    @Override
    public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
        String add = conn.getRemoteSocketAddress().getHostName() + ":" + conn.getRemoteSocketAddress().getPort();
        String closeMessage = remote ? "Client " + conn.hashCode() + " at "+ add + " closed the connection" : "Closed connection to client " + conn.hashCode() + " at " + add;
        MatlabEvent matlab_event = new MatlabEvent( this, conn, closeMessage );
        for (MatlabListener _listener : _listeners) {
            (_listener).Close(matlab_event);
        }
    }

    // Retrieve a connection by hashcode
    public WebSocket getConnection( int hashCode ) {
		Collection<WebSocket> conns = connections();
		synchronized ( conns ) {
			for( WebSocket c : conns ) {
				if (c.hashCode() == hashCode) {
                    return c;
                }
			}
		}
        throw new IllegalArgumentException("No connection has this HashCode!");
	}

    // Send text message to a connection identified by a hashcode
    public void sendTo( int hashCode, String message ) {
		getConnection( hashCode ).send( message );
	}

    // Send binary message to a connection identified by a hashcode
    public void sendTo( int hashCode, ByteBuffer blob ) {
		getConnection( hashCode ).send( blob );
	}

    // Send binary message to a connection identified by a hashcode
    public void sendTo( int hashCode, byte[] bytes ) {
		sendTo( hashCode, ByteBuffer.wrap( bytes ) );
	}

    // Send text message to all clients
    public void sendToAll( String message ) {
		Collection<WebSocket> conns = connections();
		synchronized ( conns ) {
			for( WebSocket c : conns ) {
				c.send( message );
			}
		}
	}

    // Send binary message to all clients
    public void sendToAll( ByteBuffer blob ) {
		Collection<WebSocket> conns = connections();
		synchronized ( conns ) {
			for( WebSocket c : conns ) {
				c.send( blob );
			}
		}
	}

    // Send binary message to all clients
    public void sendToAll( byte[] bytes ) {
        sendToAll( ByteBuffer.wrap( bytes ) );
	}

    // Close connection identified by a hashcode
    public void close( int hashCode ) {
		getConnection( hashCode ).close();
	}

    // Close all connections
    public void closeAll() {
		Collection<WebSocket> conns = connections();
		synchronized ( conns ) {
			for( WebSocket c : conns ) {
				c.close();
			}
		}
	}

    // Methods for handling MATLAB as a listener, automatically managed
    private final List<MatlabListener> _listeners = new ArrayList<MatlabListener>();
    public synchronized void addMatlabListener( MatlabListener lis ) {
        _listeners.add( lis );
    }
    public synchronized void removeMatlabListener( MatlabListener lis ) {
        _listeners.remove( lis );
    }

}
