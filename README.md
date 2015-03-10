MatlabWebSocketServer
===============

MatlabWebSocketServer is a simple websocket server for Matlab built on [Java-WebSockets](https://github.com/TooTallNate/Java-WebSocket), a java implementation of the websocket protocol by Nathan Rajlich.

Installation
------------
The required java library `matlabwebsocketserver.jar` located in `/dist/` must be placed on the static java class path in Matlab. See the [Matlab Documentation](http://www.mathworks.com/help/matlab/matlab_external/bringing-java-classes-and-methods-into-matlab-workspace.html).

You must also add the `webSocketServerLab.m` file to the Matlab path.

The `webSocketServerLab.m` file is an abstract Matlab class. The behaviour of the server must therefore be defined by creating a subclass that implements the following methods:

```matlab
        onOpen(obj,message,conn)
        onMessage(obj,message,conn)
        onError(obj,message,conn)
        onClose((obj,message,conn)
```

Example
------
The example is an echo server, it only implements the 'onMessage' method.

Run the echo server by making sure that the file is on the Matlab path and executing:
```matlab
        obj = echoServer(30000);
```
to start the server on port 30000.

To close the server, type:
```matlab
        delete(obj);
		clear obj;
```

Acknowledgments
-------

This work was inspired by a websocket client matlab implementation [matlab-websockets](https://github.com/mingot/matlab-websockets).

It relies on the [Java-WebSockets](https://github.com/TooTallNate/Java-WebSocket) library.


License
-------

The code in this repository is licensed under the MIT license. See the `LICENSE` file for details.