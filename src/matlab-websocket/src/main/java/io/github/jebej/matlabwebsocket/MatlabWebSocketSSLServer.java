package io.github.jebej.matlabwebsocket;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.java_websocket.WebSocketImpl;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;

public class MatlabWebSocketSSLServer extends MatlabWebSocketServer {
    // The constructor creates a new SSL WebSocketServer with the wildcard IP,
    // accepting all connections on the specified port
    public MatlabWebSocketSSLServer( int port, String keystore, String storePassword, String keyPassword ) throws Exception {
        super( port );
        String STORETYPE = "JKS";
        //WebSocketImpl.DEBUG = false;

        // Load up the key store
        KeyStore ks = KeyStore.getInstance( STORETYPE );
        File kf = new File( keystore );
        ks.load( new FileInputStream( kf ), storePassword.toCharArray() );
        // Initialize KMF and TMF
        KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
        kmf.init( ks, keyPassword.toCharArray() );
        TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
        tmf.init( ks );
        // Initialize SSLContext
        SSLContext sslContext = null;
        sslContext = SSLContext.getInstance( "TLS" );
        sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null );
        // Apply SSL context to server
        this.setWebSocketFactory( new DefaultSSLWebSocketServerFactory( sslContext ) );
    }
}
