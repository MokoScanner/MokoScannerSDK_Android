package com.moko.support.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.moko.support.MokoConstants;
import com.moko.support.entity.DeviceResponse;
import com.moko.support.handler.BaseMessageHandler;


public class SocketService extends Service {
    private Handler mInHandle;

    private Handler mOutHandle;

    private SocketThread mSocketThread;


    public class InHandler extends BaseMessageHandler<SocketService> {

        public InHandler(SocketService socketService) {
            super(socketService);
        }

        @Override
        protected void handleMessage(SocketService socketService, Message msg) {
            if (msg.obj != null) {
                DeviceResponse response = (DeviceResponse) msg.obj;
                // 获取设备信息
                Intent intent = new Intent(MokoConstants.ACTION_AP_SET_DATA_RESPONSE);
                intent.putExtra(MokoConstants.EXTRA_AP_SET_DATA_RESPONSE, response);
                SocketService.this.sendBroadcast(intent);
            }
        }
    }

    public class OutHandler extends BaseMessageHandler<SocketService> {

        public OutHandler(SocketService socketService) {
            super(socketService);
        }

        @Override
        protected void handleMessage(SocketService socketService, Message msg) {
            int code = msg.what;
            Intent intent = new Intent(MokoConstants.ACTION_AP_CONNECTION);
            intent.putExtra(MokoConstants.EXTRA_AP_CONNECTION, code);
            SocketService.this.sendBroadcast(intent);
        }
    }

    public void startSocket() {
        mSocketThread = new SocketThread(mInHandle, mOutHandle, this);
        mSocketThread.start();
    }

    public void closeSocket() {
        mSocketThread.close();
    }

    public void sendMessage(String message) {
        mSocketThread.send(message);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mInHandle = new InHandler(this);
        mOutHandle = new OutHandler(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    private IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;
        }

    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
