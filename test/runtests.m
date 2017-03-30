function runtests(  )
%RUNTESTS Summary of this function goes here
%   Detailed explanation goes here

PORT = randi([49152 65535],1);

% First, create a server
s = EchoServer(PORT);

% Then, create a client
c = SimpleClient(sprintf('ws://localhost:%d',PORT));

% Send a message to the server and wait to receive the echo back
c.send('hi!'); pause(0.1);

% Close and reopen the connection
c.close(); pause(0.1);
c.open(); pause(0.1);

% Send a message again
c.send('hi a second time!'); pause(0.1);

% Connect many clients


% Kill the client and server
delete(c);
delete(s);
end
