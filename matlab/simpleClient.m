classdef simpleClient < matWebSocketClient
    %CLIENT Summary of this class goes here
    %   Detailed explanation goes here
    
    properties
    end
    
    methods
        function obj = simpleClient(URI)
            %Constructor
            obj@matWebSocketClient(URI);
        end
    end
    
    methods (Access = protected)
        function onMessage(~,message,~)
            % This function simply displays the message received
            fprintf('Message received:\n%s\n\n',message);
        end
        
        function onOpen(~,message,~)
            fprintf('Connection opened:\n%s\n\n',message);
        end
        
        function onClose(~,message,~)
            fprintf('Server closed connection:\n%s\n\n',message);
        end
        
        function onError(~,message,~)
            fprintf('Error:\n %s\n\n',message);
        end
    end
end

