package com.moko.support;

import android.content.Context;

import com.moko.support.callback.ActionListener;
import com.moko.support.handler.MqttCallbackHandler;
import com.moko.support.log.LogModule;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * @Date 2017/12/7 0007
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.MokoSupport
 */
public class MokoSupport {

    private static volatile MokoSupport INSTANCE;

    private Context mContext;

    MqttAndroidClient mqttAndroidClient;


    private MokoSupport() {
    }

    public static MokoSupport getInstance() {
        if (INSTANCE == null) {
            synchronized (MokoSupport.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MokoSupport();
                }
            }
        }
        return INSTANCE;
    }

    public void init(Context context) {
        LogModule.init(context);
        mContext = context;
    }

    public MqttAndroidClient creatClient(String host, String port, String clientId, boolean tlsConnection) {
        String uri;
        if (tlsConnection) {
            uri = "ssl://" + host + ":" + port;
        } else {
            uri = "tcp://" + host + ":" + port;
        }
        mqttAndroidClient = new MqttAndroidClient(mContext, uri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackHandler(mContext));
        return mqttAndroidClient;
    }

    public void connectMqtt(MqttConnectOptions options) throws MqttException {
        if (mqttAndroidClient != null) {
            mqttAndroidClient.connect(options, null, new ActionListener(mContext, ActionListener.Action.CONNECT));
        }
    }


    public void disconnectMqtt() throws MqttException {
        if (mqttAndroidClient != null) {
            mqttAndroidClient.disconnect();
            mqttAndroidClient = null;
        }
    }

    public void subscribe(String topic, int qos) throws MqttException {
        if (mqttAndroidClient != null) {
            mqttAndroidClient.subscribe(topic, qos, null, new ActionListener(mContext, ActionListener.Action.SUBSCRIBE));
        }
    }

    public void unSubscribe(String topic) throws MqttException {
        if (mqttAndroidClient != null) {
            mqttAndroidClient.unsubscribe(topic, null, new ActionListener(mContext, ActionListener.Action.UNSUBSCRIBE));
        }
    }

    public void publish(String topic, MqttMessage message) throws MqttException {
        if (mqttAndroidClient != null) {
            mqttAndroidClient.publish(topic, message, null, new ActionListener(mContext, ActionListener.Action.PUBLISH));
        }
    }

    public boolean isConnected() {
        if (mqttAndroidClient != null) {
            return mqttAndroidClient.isConnected();
        }
        return false;
    }
}
