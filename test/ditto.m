function client = ditto()
DITTO_IP = 'ditto.eclipse.org';
DITTO_PORT = '80';

THING_FEATURE = '';
THING_USR = 'demo1';
THING_PWD = 'demo';

address = get_address(DITTO_IP, DITTO_PORT, THING_FEATURE);
headers = get_headers(THING_USR, THING_PWD);

client = SimpleClient(address, headers);
client.send('START-SEND-MESSAGES');
client.send('START-SEND-EVENTS');
end

function address = get_address(ip, port, feature)
address = ['ws://', ip, ':', port, '/ws/1', feature];
end

function headers = get_headers(username, password)
import matlab.net.base64encode;
authorization = ['Basic ' base64encode([username ':' password])];
headers = {'Authorization', authorization};
end
