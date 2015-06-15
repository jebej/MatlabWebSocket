package org.java_websocket.matlabbridge;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class MatlabWebSocketClientBridge extends WebSocketClient {

	private List<MatlabListener> _listeners = new ArrayList<MatlabListener>();
	
	/**
	 * This open a websocket connection as specified by rfc6455
	 */
	public MatlabWebSocketClientBridge(URI serverURI) {
		super(serverURI);
	}

	/**
	 * This function gets executed when the connection with the server is opened
	 */
	@Override
	public void onOpen(ServerHandshake handshakedata) {
		String openMessage = "Connection opened succefully."; 
		//System.out.println( openMessage );

		MatlabEvent matlab_event = new MatlabEvent( this, openMessage);
		Iterator<MatlabListener> listeners = _listeners.iterator();
		while (listeners.hasNext() ) {
			( (MatlabListener) listeners.next() ).onOpen( matlab_event );
		}
	}

	/** 
	 * This function gets executed on string message receipt
	 */
	@Override
	public void onMessage(String message) {
		MatlabEvent matlab_event = new MatlabEvent( this, message );
		Iterator<MatlabListener> listeners = _listeners.iterator();
		while (listeners.hasNext() ) {
			( (MatlabListener) listeners.next() ).onMessage( matlab_event );
		}
	}


	/**
	 * Method handler when a byte message has been received from the client, including matlab java callback.
	 */
	@Override
	public void onMessage( ByteBuffer blob) {
		//System.out.println("Received: " + message);

		MatlabEvent matlab_event = new MatlabEvent( this, blob);
		Iterator<MatlabListener> listeners = _listeners.iterator();
		while (listeners.hasNext() ) {
			( (MatlabListener) listeners.next() ).onMessage( matlab_event );
		}
	}


	/** 
	 * This function gets executed when the websocket connection is closed. The close codes are documented in class org.java_websocket.framing.CloseFrame
	 */
	@Override
	public void onClose(int code, String reason, boolean remote) {
		String closeMessage = "Connection closed by " + ( remote ? "remote peer." : "us.") + " Reason: " + reason;
		//System.out.println( closeMessage );

		MatlabEvent matlab_event = new MatlabEvent( this, closeMessage);
		Iterator<MatlabListener> listeners = _listeners.iterator();
		while (listeners.hasNext() ) {
			( (MatlabListener) listeners.next() ).onClose( matlab_event );
		}
	}

	/** 
	 * This method gets executed on error
	 */
	@Override
	public void onError(Exception ex) {
		String errorMessage = "Error received: " +  ex;
		//System.out.println( errorMessage );
		
		MatlabEvent matlab_event = new MatlabEvent( this, errorMessage);
		Iterator<MatlabListener> listeners = _listeners.iterator();
		while (listeners.hasNext() ) {
			( (MatlabListener) listeners.next() ).onError( matlab_event );
		}
		// If the error is fatal then onClose will be called additionally
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
		private static final long serialVersionUID = 1822498536266601113L;
		public String message;
		public ByteBuffer blob;
		public MatlabEvent( Object obj, String message) {
			super( obj );
			this.message = message;
		}
		public MatlabEvent( Object obj, ByteBuffer blob) {
			super( obj );
			this.blob = blob;
		}
	}
}