package io.github.jebej.matlabwebsocket;

import java.net.URI;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.java_websocket.WebSocketImpl;

public class MatlabWebSocketSSLClient extends MatlabWebSocketClient {
    // The constructor creates a new SSL WebSocketServer with the wildcard IP,
    // accepting all connections on the specified port
    public MatlabWebSocketSSLClient( URI serverURI, Map<String,String> httpHeaders, String keystore, String storePassword, String keyPassword ) throws Exception {
        super( serverURI, httpHeaders );
        String STORETYPE = "JKS";
        //WebSocketImpl.DEBUG = true;

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
        SSLSocketFactory factory = sslContext.getSocketFactory();
        // Apply SSL context to client
        this.setSocket( factory.createSocket() );
    }

    public MatlabWebSocketSSLClient( URI serverURI, Map<String,String> httpHeaders ) throws Exception {
        super( serverURI, httpHeaders );
        //WebSocketImpl.DEBUG = true;

        // Initialize SSLContext with java's default key and trust store
        SSLContext sslContext = null;
        sslContext = SSLContext.getInstance( "TLS" );
        sslContext.init( null, null, null );
        SSLSocketFactory factory = sslContext.getSocketFactory();
        // Apply SSL context to client
        this.setSocket( factory.createSocket() );
    }
}
