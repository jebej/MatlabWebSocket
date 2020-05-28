package io.github.jebej.matlabwebsocket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.framing.PongFrame;
import org.java_websocket.enums.Opcode;

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

    // Method handler when a text message has been received by the server
    @Override
    public void onMessage( WebSocket conn, String message ) {
        MatlabEvent matlab_event = new MatlabEvent( this, conn, message );
        for (MatlabListener _listener : _listeners) {
            (_listener).TextMessage(matlab_event);
        }
    }

    // Method handler when a binary message has been received by the server
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
		Collection<WebSocket> conns = getConnections();
		synchronized ( conns ) {
			for ( WebSocket conn : conns ) {
				if (conn.hashCode() == hashCode) {
                    return conn;
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
		WebSocket conn = getConnection( hashCode );
		sendSplit( conn, blob );
	}

    // Send binary message to a connection identified by a hashcode
    public void sendTo( int hashCode, byte[] bytes ) {
		sendTo( hashCode, ByteBuffer.wrap( bytes ) );
	}

    // Send text message to all clients
    public void sendToAll( String message ) {
		Collection<WebSocket> conns = getConnections();
		synchronized ( conns ) {
			for( WebSocket conn : conns ) {
				conn.send( message );
			}
		}
	}

    // Send binary message to all clients
    public void sendToAll( ByteBuffer blob ) {
		Collection<WebSocket> conns = getConnections();
		synchronized ( conns ) {
			for( WebSocket conn : conns ) {
				sendSplit( conn, blob );
			}
		}
	}

    // Send binary message to all clients
    public void sendToAll( byte[] bytes ) {
        sendToAll( ByteBuffer.wrap( bytes ) );
	}
	
	// Method to send large messages in fragments if needed
	public void sendSplit( WebSocket conn, ByteBuffer blob ) {
		int FRAG_SIZE = 5*1024*1024; // 5MB
		int blobSize = blob.capacity();
		// Only send as fragments if message is larger than FRAG_SIZE
		if ( blobSize <= FRAG_SIZE ) {
			conn.send( blob );
		} else {
			int numFrags = (blobSize + FRAG_SIZE - 1)/FRAG_SIZE;
			blob.rewind();
			for ( int i = 0; i<numFrags; i++ ) {
				blob.position( i*FRAG_SIZE );
				blob.limit( Math.min( (i+1)*FRAG_SIZE, blobSize ) );
				conn.sendFragmentedFrame( Opcode.BINARY, blob, (i+1)==numFrags );
				// Send a ping AND an unpromted pong to keep connection alive
				conn.sendPing();
				conn.sendFrame( new PongFrame() );
			}
			assert( blob.position() == blobSize );
		}
	}

    // Close connection identified by a hashcode
    public void close( int hashCode ) {
		getConnection( hashCode ).close();
	}

    // Close all connections
    public void closeAll() {
		Collection<WebSocket> conns = getConnections();
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
