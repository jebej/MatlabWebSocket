classdef webSocketServerLab < handle
    %WEBSOCKETSERVER websocketserver is an object that allows matlab to
    %start a java-websocket server instance.
    %   Detailed explanation goes here
    
    properties (SetAccess = private) % properties can only be Set by class methods (Get is still public)
        server % Java-WebSocket server object
        status % Server status (when the server starts, this value is set to 1)
        message % Latest message received from websocket
        conns % Stores an array of java connection objects
        connIDs % Stores active connections' hash and address
        log % Server log
    end
    
    methods
        function obj = webSocketServerLab(port)
            % Constructor
            % Import java library (remember to add the jar to the STATIC java path)
            import org.java_websocket.matlabserverbridge.*
            % Create the java server object in matlab with specified port
            obj.server = MatlabWebSocketServerBridge(port);
            % Start the server
            obj.server.start();
            % Set status to 1
            obj.status = 1;
            % Set callbacks
            set(obj.server, 'OnOpenCallback', @(h,e) obj.open_callback(h,e));
            set(obj.server, 'OnMessageCallback', @(h,e) obj.message_callback(h,e));
            set(obj.server, 'OnErrorCallback', @(h,e) obj.error_callback(h,e));
            set(obj.server, 'OnCloseCallback', @(h,e) obj.close_callback(h,e));
        end
        
        
        
        function send(~, conn, message)
            % Send a message to the client specified by conn
            try
                conn.send(message);
                pause(0.005);
            catch
                warning('The message could not be sent')
            end
        end
        
        
        function sendAll(obj, message)
            % Send message to all connected clients
            n = numel(obj.conns);
            for idx = 1:n
                obj.send(obj.conns(idx),message);
            end
        end
        
        
        function delete(obj)
            % Destructor
            % Remove the onclosecallback that would otherwise produce an
            % error due to missing objects
            set(obj.server, 'OnCloseCallback', '');
            % Stops the server with a 1 sec delay to properly close
            % connections
            obj.server.stop(1000);
        end
    end
    
    
    % Protected methods triggered by the callbacks defined above. This is
    % where the reactive behaviour of the server is defined.
    methods (Access = private)
        function open_callback(obj, ~, e)
            % This function gets executed on an open connection event
            obj.localUpdateProperties(e.message,e.conn,e.conns);
            %obj.onOpen(e.message,e.conn); % Define behavior here
        end
        
        
        function message_callback(obj, ~, e)
            % This function gets executed on a message event
            obj.localUpdateProperties('Message received',e.conn,e.conns);
            obj.onMessage(e.message,e.conn); % Define behavior here
        end
        
        
        function error_callback(obj, ~, e)
            % This function gets executed on an error event
            obj.localUpdateProperties(e.message,e.conn,e.conns);
            %obj.onError(e.message,e.conn); % Define behavior here
        end
        
        
        function close_callback(obj, ~, e)
            % This function gets executed on a close connection event
            obj.localUpdateProperties(e.message,e.conn,e.conns);
            %obj.onClose(e.message,e.conn); % Define behavior here
        end
    end
    
    % Implement concrete methods for a superclass
    methods (Abstract, Access = protected)
        %onOpen(obj,message,conn)
        onMessage(obj,message,conn)
        %onError(obj,message,conn)
        %onClose((obj,message,conn)
    end
    
    
    % Some routine methods
    methods (Access = private)
        function localUpdateProperties(obj,messageIn,connIn,connsIn)
            % This function updates the matlab server object properties to
            % keep them current
            % Append log property
            c = cell(1); h=c;
            c = cellstr(dec2hex(connIn.hashCode));
            htest = connIn.getRemoteSocketAddress;
            if ~isempty(htest)
                h = cell(htest.toString);
            end
            if ischar(messageIn)
                messageIn=cellstr(messageIn);
            end
            m = cell(messageIn);
            obj.localLogAppend(c,h,m);
            % Update connIDs property
            connsArray = connsIn.toArray;
            n = numel(connsArray);
            connIDsHashes = cell(n,1);
            connIDsHost = connIDsHashes;
            for idx = 1:n
                connIDsHashes(idx) = cellstr(dec2hex(connsArray(idx).hashCode));
                connIDsHost(idx) = connsArray(idx).getRemoteSocketAddress.toString;
            end
            obj.connIDs = [connIDsHashes connIDsHost];
            % Update conns property
            obj.conns = connsArray;
            % Update message property (last message in)
            obj.message = messageIn;
        end
        
        
        function localLogAppend(obj,code,host,message)
            % This function keeps a log of server event
            if ~isa(code,'cell')
                code = cellstr(code);
            end
            if ~isa(host,'cell')
                host = cellstr(host);
            end
            if ~isa(message,'cell')
                message = cellstr(message);
            end
            time = cellstr(datestr(now));
            newlogentry = [time code host message];
            obj.log = [ newlogentry ; obj.log];
        end
    end
end