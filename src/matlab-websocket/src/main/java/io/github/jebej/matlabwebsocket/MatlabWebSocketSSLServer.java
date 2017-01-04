package io.github.jebej.matlabwebsocket;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;

import fi.iki.elonen.NanoHTTPD;

public class MatlabWebSocketSSLServer extends MatlabWebSocketServer {

    // The constructor creates a new MatlabWebSocketSSLServer with the wildcard
    // IP, accepting all connections on the specified port
    public MatlabWebSocketSSLServer( int port, String keystore, String storePassword, String keyPassword ) throws Exception {
        super( port );

        String STORETYPE = "JKS";

        // Load the key store
        KeyStore ks = KeyStore.getInstance( STORETYPE );
        File kf = new File( keystore );
        ks.load( new FileInputStream( kf ), storePassword.toCharArray() );
        // Load KeyManagerFactory
        KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
        kmf.init( ks, keyPassword.toCharArray() );
        // Make server secure
        this.makeSecure(NanoHTTPD.makeSSLSocketFactory(ks,kmf), null);
    }
}
