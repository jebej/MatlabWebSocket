classdef WebSocketClient < handle
    %WEBSOCKETCLIENT WebSocketClient is an ABSTRACT class that allows
    %MATLAB to start a java-websocket client instance and connect to a
    %WebSocket server.
    %
    %   In order to make a valid implementation of the class, some methods
    %   must be defined in the superclass:
    %    onOpen(obj,message)
    %    onTextMessage(obj,message)
    %    onBinaryMessage(obj,bytearray)
    %    onError(obj,message)
    %    onClose((obj,message)
    %   The "callback" behavior of the client can be defined there. If
    %   the client needs to perform actions that are not responses to a
    %   server-caused event, these actions must be performed outside of the
    %   callback methods.
    
    properties (SetAccess = private)
        URI % The URI of the server
        httpHeaders = containers.Map % Map of additional http headers
        Secure = false % True if the connection is using WebSocketSecure
        Status = false % Status of the connection, true if the connection is open
        ClientObj % Java-WebSocket client object
    end
    
    properties (Access = private)
        UseKeyStore = false
        KeyStore % Location of the keystore
        StorePassword % Keystore password
        KeyPassword % Key password
    end
    
    methods
        function obj = WebSocketClient(URI,varargin)
            % WebSocketClient Constructor
            % Creates a java client to connect to the designated server.
            % The URI must be of the form 'ws://some.server.org:30000'.
            obj.URI = lower(URI);
            if strfind(obj.URI,'wss')
                obj.Secure = true;
            end
            if nargin == 2
                obj.httpHeaders = varargin{1};
            elseif obj.Secure && (nargin == 4 || nargin == 5)
                obj.UseKeyStore = true;
                obj.KeyStore = keyStore;
                obj.StorePassword = storePassword;
                obj.KeyPassword = keyPassword;
                if nargin == 5
                    obj.httpHeaders = httpHeaders;
                end
            elseif obj.Secure && nargin > 2
                error('Invalid number of arguments for secure connection with keystore!');
            elseif ~obj.Secure && nargin > 2
                warning(['You are passing a keystore, but the given '...
                    'server URI does not start with "wss". '...
                    'The connection will not be secure.']);
            end
            % Connect the client to the server
            obj.open();
        end
        
        function status = get.Status(obj)
            % Get the status of the connection
            if isempty(obj.ClientObj)
                status = false;
            else
                status = obj.ClientObj.isOpen();
            end
        end
        
        function open(obj)
            % Open the connection to the server
            % Create the java client object in with specified URI
            if obj.Status; warning('Connection is already open!');return; end
            import io.github.jebej.matlabwebsocket.*
            uri = handle(java.net.URI(obj.URI));
            headers = handle(java.util.HashMap());
            for key = keys(obj.httpHeaders)
                headers.put(key{1}, obj.httpHeaders(key{1}));
            end            
            if obj.Secure && ~obj.UseKeyStore
                obj.ClientObj = handle(MatlabWebSocketSSLClient(uri, headers),'CallbackProperties');
            elseif obj.Secure && obj.UseKeyStore
                obj.ClientObj = handle(MatlabWebSocketSSLClient(uri,...
                    obj.KeyStore,obj.StorePassword,obj.KeyPassword, headers),'CallbackProperties');
            else
                obj.ClientObj = handle(MatlabWebSocketClient(uri, headers),'CallbackProperties');
            end
            % Set callbacks
            set(obj.ClientObj,'OpenCallback',@obj.openCallback);
            set(obj.ClientObj,'TextMessageCallback',@obj.textMessageCallback);
            set(obj.ClientObj,'BinaryMessageCallback',@obj.binaryMessageCallback);
            set(obj.ClientObj,'ErrorCallback',@obj.errorCallback);
            set(obj.ClientObj,'CloseCallback',@obj.closeCallback);
            % Connect to the websocket server
            obj.ClientObj.connectBlocking();
        end
        
        function close(obj)
            % Close the websocket connection and explicitely delete the
            % java client object
            if ~obj.Status; warning('Connection is already closed!');return; end
            obj.ClientObj.closeBlocking()
            delete(obj.ClientObj);
            obj.ClientObj = [];
        end
        
        function delete(obj)
            % Destructor
            % Closes the websocket if it's open.
            if obj.Status
                obj.close();
            end
        end
        
        function send(obj,message)
            % Send a message to the server
            if ~obj.Status; warning('Connection is closed!');return; end
            if ~ischar(message) && ~isa(message,'int8') && ~isa(message,'uint8')
                error('You can only send character arrays or byte arrays!');
            end
            obj.ClientObj.send(message);
        end
    end
    
    % Implement these methods in a subclass.
    methods (Abstract, Access = protected)
        onOpen(obj,message)
        onTextMessage(obj,message)
        onBinaryMessage(obj,bytearray)
        onError(obj,message)
        onClose(obj,message)
    end
    
    % Private methods triggered by the callbacks defined above.
    methods (Access = private)
        function openCallback(obj,~,e)
            % Define behavior in an onOpen method of a subclass
            obj.onOpen(char(e.message));
        end
        
        function textMessageCallback(obj,~,e)
            % Define behavior in an onTextMessage method of a subclass
            obj.onTextMessage(char(e.message));
        end
        
        function binaryMessageCallback(obj,~,e)
            % Define behavior in an onBinaryMessage method of a subclass
            obj.onBinaryMessage(e.blob.array);
        end
        
        function errorCallback(obj,~,e)
            % Define behavior in an onError method of a subclass
            obj.onError(char(e.message));
        end
        
        function closeCallback(obj,~,e)
            % Define behavior in an onClose method of a subclass
            obj.onClose(char(e.message));
            % Delete java client object if needed
            if ~isvalid(obj); return; end
            delete(obj.ClientObj);
            obj.ClientObj = [];
        end
    end
end