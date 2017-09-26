MatlabWebSocket
===============

MatlabWebSocket is a simple library consisting of a websocket server and client for MATLAB  built on [Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket), a java implementation of the websocket protocol. Encryption is supported with self-signed certificates made with the java keytool.

Installation and Uninstallation
------------
First, download the latest release on GitHub or MATLAB Central and extract to contents where you want.

The required java library is a `jar` file located in the `/jar/` folder. It must be placed on the static java class path in MATLAB. For example, if the location of the jar file is `C:\MatlabWebSocket\jar\matlab-websocket-1.3.jar`, then open the static class path file with the following command:
```matlab
edit(fullfile(prefdir,'javaclasspath.txt'))
```
and add the line `C:\MatlabWebSocket\jar\matlab-websocket-1.3.jar` to it. Make sure that there are no other lines with a `matlab-websocket-*` jar.

See the [MATLAB  Documentation](http://www.mathworks.com/help/matlab/matlab_external/static-path.html) for more information on the static java class path. Note that after adding the jar file to the java class path, MATLAB will need to be restarted.

You must now add the `/src/` folder to the MATLAB  path. If you want to run the examples, add the `/examples/` folder as well.

Simply undo these operations to uninstall MatlabWebSocket.

Usage
------------

The `WebSocketServer.m` file is an abstract MATLAB  class. The behaviour of the server must therefore be defined by creating a subclass that implements the following methods:

```matlab
onOpen(obj,conn,message)
onTextMessage(obj,conn,message)
onBinaryMessage(obj,conn,message)
onError(obj,conn,message)
onClose(obj,conn,message)
```

 * `obj` is the object instance of the subclass, it is implicitly passed by MATLAB (see the [object-oriented programming documentation of MATLAB](http://www.mathworks.com/help/matlab/object-oriented-programming.html)).
 * `message` is the message received by the server. It will usually be a character array, except for the `onBinaryMessage` method, in which case it will be an `int8` array
 * `conn` is a WebSocketConnection object representing the client connection that caused the event. For example, if a message is received, the `conn` object will represent the client that sent this message. You can send messages to that client through this object.

The `WebSocketClient.m` class is very similar to the server, except that no `conn` object is passed to the `onSomething` methods.

These methods will be automatically called when the corresponding event (connection is opened, message received, etc...) occurs. In this way, a reactive behavior can be defined.

The server supports a variety of methods to help talk to clients, look in the MATLAB class file to see what methods are available.

When you are done, do not forget to `delete` the clients and/or servers.

See the `EchoServer.m` and `SimpleClient.m` files in the `examples` folder for an implementation example. A good resource on classes is the [MATLAB object-oriented documentation](http://www.mathworks.com/help/matlab/object-oriented-programming.html).

Example
------
The example is an echo server, it returns to the client whatever was received.

Run the echo server by making sure that the file is on the MATLAB path and executing
```matlab
server = EchoServer(30000)
```
to start the server on port 30000.

To test the server, make a client object from the `SimpleClient.m` class:
```matlab
>> client = SimpleClient('ws://localhost:30000');
Connected to server at ws://localhost:30000
Client 1063680447 at 127.0.0.1:42520 opened a connection
```

You can now connect and send messages (`client.send('hi!')`). If the server is working properly, you will receive messages identical to the ones you send.

The server can enumerate clients that are connected, just type:
```matlab
server.Connections % view the result as a table: struct2table(server.Connections)
```

This allows you to send a message to the client via its identifying `HashCode`:
```matlab
>> clientCode = server.Connections(1).HashCode;
>> server.sendTo(clientCode,'hi, this is the server!')
Message received:
hi, this is the server!
```

The server can be stopped and restarted (this will disconnect clients):
```matlab
>> server.stop
Disconnected from server at ws://localhost:30000
```

To delete the server, type:
```matlab
delete(server);
clear server;
```

SSL / WebSocket Secure (wss)
-------

To enable SSL, you must first have a certificate. A self-signed key store can be generated with the java `keytool`, but you should always use a valid certificate in production. From there, open the server by passing the location of the store, the store password, and the key password. With the EchoServer, for example:

```matlab
PORT = 8887; % choose an other port!
STORE = 'C:\keystore.jks';
STOREPASSWORD = 'storepassword';
KEYPASSWORD = 'keypassword';
s = EchoServer(PORT,STORE,STOREPASSWORD,KEYPASSWORD);
```

The client can then connect to it:
```matlab
URI = sprintf(wss://localhost:%d',PORT);
c = SimpleClient(URI,STORE,STOREPASSWORD,KEYPASSWORD);
```

If a valid certificate is used, the default java keystore can be used. For example, we can connect a client directly the the secured [websocket.org](`https://www.websocket.org/echo.html`) test server:

```matlab
>> c2 = SimpleClient('wss://echo.websocket.org');
Connected to server at wss://echo.websocket.org
>> c2.send('hi, this communication is secured!')
Message received:
hi, this communication is secured!
```

Acknowledgments
-------

This work was inspired by a websocket client MATLAB implementation:  [matlab-websockets](https://github.com/mingot/matlab-websockets).

It relies on the [Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket) library.

License
-------

The code in this repository is licensed under the MIT license. See the `LICENSE` file for details.
