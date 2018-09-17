function runtests( TEST_SSL )
%RUNTESTS These tests need the example folder to be on the path to work
if nargin==0; TEST_SSL = 0; end

PROTOCOL = 'ws';
PORT = randi([49152 65535],1);
STORE  = fullfile(fileparts(which('WebSocketServer')),'matlab-websocket','keystore.jks');
STOREPASSWORD = 'storepassword';
KEYPASSWORD = 'keypassword';
if TEST_SSL; PROTOCOL = 'wss'; end
URI = sprintf('%s://localhost:%d',PROTOCOL,PORT);

try
    % First, create a server
    if TEST_SSL
        s = EchoServer(PORT,STORE,STOREPASSWORD,KEYPASSWORD);
    else
        s = EchoServer(PORT);
    end
    
    % Then, create a client
    c = SimpleClient(URI,STORE,STOREPASSWORD,KEYPASSWORD);
    
    % Send a message to the server and wait to receive the echo back
    c.send('hi!'); pause(0.1);
    
    % Close and reopen the client connection
    c.close(); pause(0.1);
    c.open(); pause(0.1);
    
    % Send a message again
    c.send('hi a second time!'); pause(0.1);
    
    % Restart server to see if that works
    s.stop(); pause(0.1);
    s.start(); pause(0.1);
    
    % Client should be disconnected, reconnect
    if c.Status == 0
        c.open(); pause(0.1);
    else
        error('The client did not disconnect from a stopped server, not good!');
    end
    
    % Large file transfer test
    A = randi([-128 127],3*10^7,1,'int8');
    c.send(A); pause(1);
    
    % Connect many clients
    N = 100;
    clients = cell(N,1);
    for i=1:N; clients{i} = SimpleClient(URI,STORE,STOREPASSWORD,KEYPASSWORD); end
    pause(1);
    
    % Send a message from each client
    for i=1:N; clients{i}.send(A(i*(1:10^5))); end
    pause(1);
    
    % Disconnect all the clients
    for i=1:N; delete(clients{i}); end
    pause(1);
    
catch err
    delete(c);
    delete(s);
    rethrow(err);
end

% Kill the client and server
delete(c);
delete(s);
end
