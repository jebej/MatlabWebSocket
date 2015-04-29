MatlabWebSocket
===============

MatlabWebSocket is a simple library consisting of a websocket server and client for Matlab built on [Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket), a java implementation of the websocket protocol by Nathan Rajlich. It currently does not support encryption.

Installation
------------
The required java library `matlabwebsocket.jar` located in `/dist/` must be placed on the static java class path in Matlab. See the [Matlab Documentation](http://www.mathworks.com/help/matlab/matlab_external/bringing-java-classes-and-methods-into-matlab-workspace.html).

You must also add the `webSocketServer.m` and/or `webSocketClient.m` files located in `/matlab/` file to the Matlab path.

Usage
------------

The `webSocketServer.m` file is an abstract Matlab class. The behaviour of the server must therefore be defined by creating a subclass that implements the following methods:

```matlab
        onOpen(obj,message,conn)
        onMessage(obj,message,conn)
        onError(obj,message,conn)
        onClose(obj,message,conn)
```

`obj` is the object instance of the subclass, it is implicitly passed by Matlab.

`message` is the message received by the server.

`conn` is a java object representing the client connection that cause the event. For example, if a message is received, the `conn` object will represent the client that sent the message.

See the `echoServer.m` file for an implementation example.

Example
------
The example is an echo server, it only implements the 'onMessage' method.

Run the echo server by making sure that the file is on the Matlab path and executing:
```matlab
        obj = echoServer(30000);
```
to start the server on port 30000.

To test the server, open the `client.html` in the `/client/` folder in a modern web browser (really anything released after 2013). The port should already be set to 30000.

You can now connect and send messages. If the server is working properly, you will receive messages identical to the ones you send.

To close the server, go back to Matlab and type:
```matlab
        delete(obj);
		clear obj;
```

Acknowledgments
-------

This work was inspired by a websocket client matlab implementation [matlab-websockets](https://github.com/mingot/matlab-websockets).

It relies on the [Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket) library.

The html client was taken from the [cwebsocket]https://github.com/m8rge/cwebsocket repository.

License
-------

Apart from the client, the code in this repository is licensed under the MIT license. See the `LICENSE` file for details.

The client is licensed under the GPLv3 license. See the `CLIENTLICENSE` file in the `/client/` folder for details.