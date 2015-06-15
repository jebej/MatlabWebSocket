classdef matWebSocketClient < handle
    %MATWEBSOCKETCLIENT matWebSocketClient is an ABSTRAT class that allows matlab to
    %start a java-websocket client instance.
    %   In order to make a valid implementation of the class, some methods
    %   must be defined in the superclass:
    %    onOpen(obj,message,conn)
    %    onMessage(obj,message,conn)
    %    onError(obj,message,conn)
    %    onClose((obj,message,conn)
    %   The "callback" behaviour of the client can be defined there. If
    %   the client needs to perform actions that are not responses to a
    %   client event, these actions must be performed outside of the
    %   client superclass.
    
    properties% (SetAccess = protected) % properties can only be Set by class and subclass methods (Get is still public)
        client % Java-WebSocket client object
        URI % The URI of the server
        status % 0 means the client is not connected to the server
        log % Client log
    end
    
    methods
        function obj = matWebSocketClient(URI)
            % URI in the form 'ws://localhost:30000'
            % Constructor
            obj.URI=URI;
            % Connect the client to the server
            obj.open();
        end
        
        
        function send(obj, message)
            % Sends the message to the client specified by conn
            try
                obj.client.send(message);
                pause(0.005); % Small pause for the java method
                %fprintf('%s\n', ['Message sent: ' message]);
                obj.localLogAppend(message,'sent');
            catch err;
                warning('The message could not be sent!')
                fprintf('%s',getReport(err,'basic'));
            end
        end
        
        
        function open(obj)
            % Create the java client object in with specified URI
            javaURI = javaObject('java.net.URI',obj.URI);
            javaObj = javaObject('org.java_websocket.matlabbridge.MatlabWebSocketClientBridge',javaURI);
            % Assing the java object to a handle to avoid memory leaks
            obj.client = handle(javaObj,'CallbackProperties');
            % Explicitely delete the reference to the raw java object
            javaObj = [];clear javaObj;
            % Set callbacks
            set(obj.client, 'OnOpenCallback', @(h,e) obj.open_callback(h,e));
            set(obj.client, 'OnMessageCallback', @(h,e) obj.message_callback(h,e));
            set(obj.client, 'OnErrorCallback', @(h,e) obj.error_callback(h,e));
            set(obj.client, 'OnCloseCallback', @(h,e) obj.close_callback(h,e));
            % Connect to the websocket server
            obj.status = obj.client.connectBlocking();
        end
        
        
        function close(obj)
            % Close the websocket connection and explicitely delete the client object
            obj.client.close()
            obj.client = [];
            clear obj.client;
            obj.status = 0;
        end
        
        
        function delete(obj)
            % Destructor
            % Remove the onclosecallback that would otherwise produce an
            % error due to missing objects
            set(obj.client, 'OnCloseCallback', '');
            % Closes the websocket if it's open.
            if obj.status==1
                obj.close();
            end
        end
    end
    
    
    % Implement these methods in a superclass. Don't forget to call these
    % functions from the callbacks below!
    methods (Abstract, Access = protected)
        onOpen(obj,message,conn)
        onMessage(obj,message,conn)
        onError(obj,message,conn)
        onClose(obj,message,conn)
    end
    
    
    % Private methods triggered by the callbacks defined above. This is
    % where the reactive behaviour of the client is defined.
    methods (Access = private)
        function open_callback(obj, ~, e)
            % This function gets executed on an open connection event
            thismessage = char(e.message); e=[]; clear e;
            % Update the properties of the client with new info
            obj.localLogAppend(thismessage,'received');
            % Define behavior here
            obj.onOpen(thismessage);
        end
        
        
        function message_callback(obj, ~, e)
            % This function gets executed on a message event
            if ~isempty(e.blob) % Received bytes message
                thismessage = typecast(e.blob.array,'uint8');
            elseif ~isempty(e.message) % Received string message
                thismessage = char(e.message);
            else
                thismessage = [];
            end            
            e=[]; clear e;
            % Update the properties of the client with new info
            obj.localLogAppend(thismessage,'received');
            % Define behavior here
            obj.onMessage(thismessage);
        end
        
        
        function error_callback(obj, ~, e)
            % This function gets executed on an error event
            thismessage = char(e.message); e=[]; clear e;
            % Update the properties of the client with new info
            obj.localLogAppend(thismessage,'received');
            % Define behavior here
            obj.onError(thismessage);
        end
        
        
        function close_callback(obj, ~, e)
            % This function gets executed on a close connection event
            thismessage = char(e.message); e=[]; clear e;
            % Update the properties of the client with new info
            obj.localLogAppend(thismessage,'received');
            % Define behavior here
            obj.onClose(thismessage);
            obj.close();
        end
    end
    
    
    % Some routine methods, they can be removed if unnecessary. They update
    % the client properties with information about the current connections
    % and the client history.
    methods (Access = protected)
        function localLogAppend(obj,message,sentReceived)
            % This function keeps a log of client event
            if isa(message,'uint8')
                message = cellstr('Binary data message');
            elseif ~isa(message,'cell')
                message = cellstr(message);
            end
            time = cellstr(datestr(now));
            newlogentry = [time sentReceived message];
            obj.log = [ newlogentry ; obj.log];
        end
    end
end