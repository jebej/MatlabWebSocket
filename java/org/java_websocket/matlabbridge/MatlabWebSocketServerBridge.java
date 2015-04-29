package org.java_websocket.matlabbridge;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class MatlabWebSocketServerBridge extends WebSocketServer {

	/** The default web socket port number */
	private static int PORT = 30000;

	private Set<WebSocket> conns;
	private List<MatlabListener> _listeners = new ArrayList<MatlabListener>();

	/**
	 * The constructor creates a new WebSocketServer with the wildcard IP accepting all connections on the specified port.
	 */
	public MatlabWebSocketServerBridge( int port ) {
		super(new InetSocketAddress(port));
		conns = new HashSet<>();
	}

	/**
	 * The constructor creates a new WebSocketServer with the wildcard IP accepting all connections on default port 30000.
	 */
	public MatlabWebSocketServerBridge() {
		super(new InetSocketAddress(PORT));
		conns = new HashSet<>();
	}

	/**
	 * Method handler when a new connection has been opened, including matlab java callback.
	 */
	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		conns.add(conn);
		String openMessage = "New connection from " + conn.getRemoteSocketAddress().getAddress().getHostAddress();
		//System.out.println(openMessage);

		MatlabEvent matlab_event = new MatlabEvent( this, openMessage, conn, conns);
		Iterator<MatlabListener> listeners = _listeners.iterator();
		while (listeners.hasNext() ) {
			( (MatlabListener) listeners.next() ).onOpen( matlab_event );
		}
	}

	/**
	 * Method handler when a connection has been closed, including matlab java callback.
	 */
	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		conns.remove(conn);
		String closeMessage = "Closed connection to " + conn.getRemoteSocketAddress().getAddress().getHostAddress();
		//System.out.println(closeMessage);

		MatlabEvent matlab_event = new MatlabEvent( this, closeMessage, conn, conns);
		Iterator<MatlabListener> listeners = _listeners.iterator();
		while (listeners.hasNext() ) {
			( (MatlabListener) listeners.next() ).onClose( matlab_event );
		}
	}

	/**
	 * Method handler when a message has been received from the client, including matlab java callback.
	 */
	@Override
	public void onMessage(WebSocket conn, String message) {
		//System.out.println("Received: " + message);

		MatlabEvent matlab_event = new MatlabEvent( this, message, conn, conns);
		Iterator<MatlabListener> listeners = _listeners.iterator();
		while (listeners.hasNext() ) {
			( (MatlabListener) listeners.next() ).onMessage( matlab_event );
		}
	}


	/**
	 * Method handler when an error has occurred.
	 */
	@Override
	public void onError(WebSocket conn, Exception ex) {
		conns.remove(conn);
		String errorMessage = "ERROR from " + conn.getRemoteSocketAddress().getAddress().getHostAddress();
		//System.out.println(errorMessage);
		ex.printStackTrace();

		MatlabEvent matlab_event = new MatlabEvent( this, errorMessage, conn, conns);
		Iterator<MatlabListener> listeners = _listeners.iterator();
		while (listeners.hasNext() ) {
			( (MatlabListener) listeners.next() ).onError( matlab_event );
		}
	}

	/**
	 * Methods for handling matlab as a listener. Automatically managed by matlab.
	 */
	public synchronized void addMatlabListener( MatlabListener lis ) {
		_listeners.add( lis );
	}

	public synchronized void removeMatlabListener( MatlabListener lis ) {
		_listeners.remove( lis );
	}   

	/**
	 * Methods that define callbacks in Matlab.
	 * Inside Matlab, they need to be referenced for example as 'OnOpenCallback'
	 */
	public interface MatlabListener extends java.util.EventListener {
		void onOpen( MatlabEvent event );
		void onMessage( MatlabEvent event );
		void onError( MatlabEvent event );
		void onClose( MatlabEvent event );
	}

	/**
	 * Object given to Matlab when an event occur. The "this.something" assignments define which arguments are passed to matlab.
	 */
	public class MatlabEvent extends java.util.EventObject {
		private static final long serialVersionUID = -4346315089398115565L;
		public String message;
		public WebSocket conn;
		public Set<WebSocket> conns;
		public MatlabEvent( Object obj, String message, WebSocket conn, Set<WebSocket> conns) {
			super( obj );
			this.message = message;
			this.conn = conn;
			this.conns = conns;
		}
	}
}