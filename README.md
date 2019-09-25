## 1.Import and use SDK
### 1.1	Import module project mokosupport
### 1.2	Configure settings.gradle file and call mokosupport project:

	include ':app',':mokosupport'

### 1.3	Edit the build.gradle file of the main project:

	dependencies {
	    implementation fileTree(dir: 'libs', include: ['*.jar'])
	    implementation project(path: ': mokosupport')
	}

### 1.4	Import SDK during project initialization:

	public class BaseApplication extends Application {
	    @Override
	    public void onCreate() {
	        super.onCreate();
	        // initialization
	        MokoSupport.getInstance().init(getApplicationContext());
	    }
	}


## 2.Function Introduction

- The methods provided in SDK include: Socket communication with WIFI device, MQTT connection service, disconnection, subscription topic, unsubscribe topic, post topic, log record, etc.
- Socket communication is called by `SocketService`;
- MQTT communication can be called by `MokoSupport.getInstance()`;

### 2.1 SocketService

Before creating a Socket connection, you need to confirm whether the APP is connected to the WIFI of the device. Connect the default IP address `192.168.4.1`, the default port number is `8266`, which can be modified in `SocketThread`.

#### 2.1.1 Initialization

	bindService(new Intent(this, SocketService.class), mServiceConnection, BIND_AUTO_CREATE);

Start SocketService, and get the SocketService object, call `mService.startSocket()` to create a Socket thread, connect the device, and the thread waits for the message to be sent after the connection is successful;

#### 2.1.2 Get connection status and response

1. Get the connection status by registering the broadcast:

Broadcast ACTION：`MokoConstants.ACTION_AP_CONNECTION`

Connection status：

- Connection successful：`MokoConstants.CONN_STATUS_SUCCESS`
- connecting：`MokoConstants.CONN_STATUS_CONNECTING`
- Connection failed：`MokoConstants.CONN_STATUS_FAILED`
- Connection timeout：`MokoConstants.CONN_STATUS_TIMEOUT`

2、Get the Socket communication response by registering the broadcast :

Broadcast ACTION：`MokoConstants.ACTION_AP_SET_DATA_RESPONSE`

Get a response：

	DeviceResponse response = (DeviceResponse) intent.getSerializableExtra(MokoConstants.EXTRA_AP_SET_DATA_RESPONSE);

#### 2.1.3 Socket

Send data only accepts strings in JSON format

eg:

1、Get device information：

	{ 
	          "header" : 4001
	 }
	 
response：

	 { 
	     "code" : 0, 
	     "message" : "success", 
	     "result" : { 
	          "header" : 4001, 
	          "device_function" : "iot_plug", 
	          "device_name" : "plug_one", 
	          "device_specifications" : "us", 
	          "device_mac" : "11:22:33:44:55:66",
	          "device_type" : "1"
	     } 
	 }
	 
2、	Send MQTT server information

	{ 
	          "header" : 4002, 
	          "host" : "45.32.33.42", 
	          "port" : 1883, 
	          "connect_mode" : 0, 
	          "username" : "DVES_USER", 
	          "password" : "DVES_PASS", 
	          "keepalive" : 120, 
	          "qos" : 2, 
	          "clean_session" :1
	 }
	 
response：

	{ 
	     "code" : 0, 
	     "message" : "success", 
	     "result" : { 
	         "header" : 4002
	     } 
	 }
	 
3、Send a WIFI network with a specific SSID

	{ 
	          "header" : 4003, 
	          "wifi_ssid" : "Fitpolo", 
	          "wifi_pwd" : "fitpolo1234.", 
	          "wifi_security" : 3 
	 }
	 
response:

	{ 
	     "code" : 0, 
	     "message" : "success", 
	     "result" : { 
	       "header" : 4003
	     } 
	 }



### 2.2	MokoSupport

#### 2.2.1 Connect to the MQTT server

1、Create `MqttAndroidClient`

	public void creatClient(String host, String port, String clientId, boolean tlsConnection)
	
2、Connect to the server

	public void connectMqtt(MqttConnectOptions options)
	
 Get the creation status according to `MqttCallbackHandler` and receive the return data form server

	@Override
    public void connectComplete(boolean reconnect, String serverURI) {
        ...
    }
    @Override
    public void connectionLost(Throwable cause) {
        ...
    }
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        ...
    }
    
3、Get the connection status by registering the broadcast:

Broadcast ACTION：`MokoConstants.ACTION_MQTT_CONNECTION`

Connection status：

- Connection success：`MokoConstants.MQTT_CONN_STATUS_SUCCESS`
- Disconnect：`MokoConstants.MQTT_CONN_STATUS_LOST`

4、Receive the return data from server by registering the broadcast

Broadcast ACTION：`MokoConstants.ACTION_MQTT_RECEIVE`

Return data：

- Return data Topic：`MokoConstants.EXTRA_MQTT_RECEIVE_TOPIC`
- Return data Message：`MokoConstants.EXTRA_MQTT_RECEIVE_MESSAGE`

The return data is in JSON format,eg：

	{ 
	          "company_name" : "moko", 
	          "production_date" : "201801", 
	          "product_model" : "plug_one", 
	          "firmware_version" : "000001" 
	          "device_mac" : "11:22:33:44:55:66"
	 }

#### 2.2.2 Action monitor

MQTT communication contains four kinds of Actions. To execute each Action, you need to set `ActionListener` to monitor the state of the Action:

	public enum Action {
	        /**
	         * Connect Action
	         **/
	        CONNECT,
	        /**
	         * Subscribe Action
	         **/
	        SUBSCRIBE,
	        /**
	         * Publish Action
	         **/
	        PUBLISH,
	        /**
	         * UnSubscribe Action
	         **/
	        UNSUBSCRIBE
	    }
	    
	    
Get the Action status by registering the broadcast:

1、CONNECT
	
Broadcast ACTION：`MokoConstants.ACTION_MQTT_CONNECTION`

- Connection failed：`MokoConstants.MQTT_CONN_STATUS_FAILED`

2、SUBSCRIBE

Broadcast ACTION：`MokoConstants.ACTION_MQTT_SUBSCRIBE`

- Subscribe Topic：`MokoConstants.EXTRA_MQTT_RECEIVE_TOPIC`
- Subscribe status：`MokoConstants.EXTRA_MQTT_STATE`

3、PUBLISH

Broadcast ACTION：`MokoConstants.ACTION_MQTT_PUBLISH`

- Publish status：`MokoConstants.EXTRA_MQTT_STATE`

4、UNSUBSCRIBE

Broadcast ACTION：`MokoConstants.ACTION_MQTT_UNSUBSCRIBE`

- Unsubscribe status：`MokoConstants.EXTRA_MQTT_STATE`

#### 2.2.3 Subscribe topic

	MokoSupport.getInstance().subscribe(String topic, int qos)
	
#### 2.2.4 Publish information

	MokoSupport.getInstance().publish(String topic, MqttMessage message)

#### 2.2.5 Unsubscribe topic

	MokoSupport.getInstance().unSubscribe(String topic)
	
#### 2.2.6 Determine whether the MQTT is connected

	MokoSupport.getInstance().isConnected()
	
#### 2.2.7 Disconnection

	MokoSupport.getInstance().disconnectMqtt()
	

## 3.Save Log to SD Card

- SDK integrates the Log saved to the SD card function, is called [https://github.com/elvishew/xLog](https://github.com/elvishew/xLog "XLog")
- initialization method in `MokoSupport.getInstance().init(getApplicationContext())`
- The folder name and file name saved on the SD card can be modified.

		public class LogModule {
			private static final String TAG = "mokoLife";// file name 
		    private static final String LOG_FOLDER = "mokoLife";// folder name
			...
		}

- Storage strategy: only store the data of the day and the data of the day before , the file is suffixed with.bak
- call method：
	- LogModule.v("log info");
	- LogModule.d("log info");
	- LogModule.i("log info");
	- LogModule.w("log info");
	- LogModule.e("log info");


