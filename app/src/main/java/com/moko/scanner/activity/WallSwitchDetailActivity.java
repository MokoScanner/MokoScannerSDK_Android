package com.moko.scanner.activity;

import com.moko.scanner.base.BaseActivity;

/**
 * @Date 2018/6/7
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.scanner.activity.WallSwitchDetailActivity
 */
public class WallSwitchDetailActivity extends BaseActivity {
//    @Bind(R.id.iv_wall_switch_1)
//    ImageView ivWallSwitch1;
//    @Bind(R.id.tv_wall_switch_1_edit)
//    TextView tvWallSwitch1Edit;
//    @Bind(R.id.tv_wall_switch_1_timer_state)
//    TextView tvWallSwitch1TimerState;
//    @Bind(R.id.rl_wall_switch_1)
//    RelativeLayout rlWallSwitch1;
//    @Bind(R.id.iv_wall_switch_2)
//    ImageView ivWallSwitch2;
//    @Bind(R.id.tv_wall_switch_2_edit)
//    TextView tvWallSwitch2Edit;
//    @Bind(R.id.tv_wall_switch_2_timer_state)
//    TextView tvWallSwitch2TimerState;
//    @Bind(R.id.rl_wall_switch_2)
//    RelativeLayout rlWallSwitch2;
//    @Bind(R.id.iv_wall_switch_3)
//    ImageView ivWallSwitch3;
//    @Bind(R.id.tv_wall_switch_3_edit)
//    TextView tvWallSwitch3Edit;
//    @Bind(R.id.tv_wall_switch_3_timer_state)
//    TextView tvWallSwitch3TimerState;
//    @Bind(R.id.rl_wall_switch_3)
//    RelativeLayout rlWallSwitch3;
//
//    private MokoDevice mokoDevice;
//    private int deviceType;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_wall_switch_detail);
//        ButterKnife.bind(this);
//        if (getIntent().getExtras() != null) {
//            mokoDevice = (MokoDevice) getIntent().getSerializableExtra(AppConstants.EXTRA_KEY_DEVICE);
//            deviceType = Integer.parseInt(mokoDevice.type);
//            changeSwitchState();
//        }
//        // 注册广播接收器
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(MokoConstants.ACTION_MQTT_CONNECTION);
//        filter.addAction(MokoConstants.ACTION_MQTT_RECEIVE);
//        filter.addAction(MokoConstants.ACTION_MQTT_PUBLISH);
//        filter.addAction(AppConstants.ACTION_MODIFY_NAME);
//        filter.addAction(AppConstants.ACTION_DEVICE_STATE);
//        registerReceiver(mReceiver, filter);
//    }
//
//    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (MokoConstants.ACTION_MQTT_CONNECTION.equals(action)) {
//                int state = intent.getIntExtra(MokoConstants.EXTRA_MQTT_CONNECTION_STATE, 0);
//            }
//            if (MokoConstants.ACTION_MQTT_RECEIVE.equals(action)) {
//                String topic = intent.getStringExtra(MokoConstants.EXTRA_MQTT_RECEIVE_TOPIC);
//                if (topic.equals(mokoDevice.getDeviceTopicSwitchState())) {
//                    mokoDevice.isOnline = true;
//                    String message = intent.getStringExtra(MokoConstants.EXTRA_MQTT_RECEIVE_MESSAGE);
//                    JsonObject object = new JsonParser().parse(message).getAsJsonObject();
//                    String switch_state_1;
//                    String switch_state_2;
//                    String switch_state_3;
//                    switch (deviceType) {
//                        case 1:
//                            switch_state_1 = object.get("switch_state_01").getAsString();
//                            if (!switch_state_1.equals(mokoDevice.on_off_1 ? "on" : "off")) {
//                                mokoDevice.on_off_1 = "on".equals(switch_state_1);
//                                tvWallSwitch1TimerState.setVisibility(View.GONE);
//                            }
//                            break;
//                        case 2:
//                            switch_state_1 = object.get("switch_state_01").getAsString();
//                            if (!switch_state_1.equals(mokoDevice.on_off_1 ? "on" : "off")) {
//                                mokoDevice.on_off_1 = "on".equals(switch_state_1);
//                                tvWallSwitch1TimerState.setVisibility(View.GONE);
//                            }
//                            switch_state_2 = object.get("switch_state_02").getAsString();
//                            if (!switch_state_2.equals(mokoDevice.on_off_2 ? "on" : "off")) {
//                                mokoDevice.on_off_2 = "on".equals(switch_state_2);
//                                tvWallSwitch2TimerState.setVisibility(View.GONE);
//                            }
//                            break;
//                        case 3:
//                            switch_state_1 = object.get("switch_state_01").getAsString();
//                            if (!switch_state_1.equals(mokoDevice.on_off_1 ? "on" : "off")) {
//                                mokoDevice.on_off_1 = "on".equals(switch_state_1);
//                                tvWallSwitch1TimerState.setVisibility(View.GONE);
//                            }
//                            switch_state_2 = object.get("switch_state_02").getAsString();
//                            if (!switch_state_2.equals(mokoDevice.on_off_2 ? "on" : "off")) {
//                                mokoDevice.on_off_2 = "on".equals(switch_state_2);
//                                tvWallSwitch2TimerState.setVisibility(View.GONE);
//                            }
//                            switch_state_3 = object.get("switch_state_03").getAsString();
//                            if (!switch_state_3.equals(mokoDevice.on_off_3 ? "on" : "off")) {
//                                mokoDevice.on_off_3 = "on".equals(switch_state_3);
//                                tvWallSwitch3TimerState.setVisibility(View.GONE);
//                            }
//                            break;
//                    }
//                    changeSwitchState();
//                }
//                if (topic.equals(mokoDevice.getDeviceTopicDelayTime())) {
//                    String message = intent.getStringExtra(MokoConstants.EXTRA_MQTT_RECEIVE_MESSAGE);
//                    JsonObject object = new JsonParser().parse(message).getAsJsonObject();
//                    String delay_time_01;
//                    String delay_time_02;
//                    String delay_time_03;
//                    switch (deviceType) {
//                        case 1:
//                            delay_time_01 = object.get("delay_time_01").getAsString();
//                            if ("0:0:0".equals(delay_time_01)) {
//                                tvWallSwitch1TimerState.setVisibility(View.GONE);
//                            } else {
//                                tvWallSwitch1TimerState.setVisibility(View.VISIBLE);
//                                String timer = String.format("%s after %s", !mokoDevice.on_off_1 ? "on" : "off", delay_time_01);
//                                tvWallSwitch1TimerState.setText(timer);
//                            }
//                            break;
//                        case 2:
//                            delay_time_01 = object.get("delay_time_01").getAsString();
//                            if ("0:0:0".equals(delay_time_01)) {
//                                tvWallSwitch1TimerState.setVisibility(View.GONE);
//                            } else {
//                                tvWallSwitch1TimerState.setVisibility(View.VISIBLE);
//                                String timer = String.format("%s after %s", !mokoDevice.on_off_1 ? "on" : "off", delay_time_01);
//                                tvWallSwitch1TimerState.setText(timer);
//                            }
//                            delay_time_02 = object.get("delay_time_02").getAsString();
//                            if ("0:0:0".equals(delay_time_02)) {
//                                tvWallSwitch2TimerState.setVisibility(View.GONE);
//                            } else {
//                                tvWallSwitch2TimerState.setVisibility(View.VISIBLE);
//                                String timer = String.format("%s after %s", !mokoDevice.on_off_2 ? "on" : "off", delay_time_02);
//                                tvWallSwitch2TimerState.setText(timer);
//                            }
//                            break;
//                        case 3:
//                            delay_time_01 = object.get("delay_time_01").getAsString();
//                            if ("0:0:0".equals(delay_time_01)) {
//                                tvWallSwitch1TimerState.setVisibility(View.GONE);
//                            } else {
//                                tvWallSwitch1TimerState.setVisibility(View.VISIBLE);
//                                String timer = String.format("%s after %s", !mokoDevice.on_off_1 ? "on" : "off", delay_time_01);
//                                tvWallSwitch1TimerState.setText(timer);
//                            }
//                            delay_time_02 = object.get("delay_time_02").getAsString();
//                            if ("0:0:0".equals(delay_time_02)) {
//                                tvWallSwitch2TimerState.setVisibility(View.GONE);
//                            } else {
//                                tvWallSwitch2TimerState.setVisibility(View.VISIBLE);
//                                String timer = String.format("%s after %s", !mokoDevice.on_off_2 ? "on" : "off", delay_time_02);
//                                tvWallSwitch2TimerState.setText(timer);
//                            }
//                            delay_time_03 = object.get("delay_time_03").getAsString();
//                            if ("0:0:0".equals(delay_time_03)) {
//                                tvWallSwitch3TimerState.setVisibility(View.GONE);
//                            } else {
//                                tvWallSwitch3TimerState.setVisibility(View.VISIBLE);
//                                String timer = String.format("%s after %s", !mokoDevice.on_off_3 ? "on" : "off", delay_time_03);
//                                tvWallSwitch3TimerState.setText(timer);
//                            }
//                            break;
//                    }
//                }
//            }
//            if (MokoConstants.ACTION_MQTT_PUBLISH.equals(action)) {
//                int state = intent.getIntExtra(MokoConstants.EXTRA_MQTT_STATE, 0);
//                dismissLoadingProgressDialog();
//            }
//            if (AppConstants.ACTION_DEVICE_STATE.equals(action)) {
//                String topic = intent.getStringExtra(MokoConstants.EXTRA_MQTT_RECEIVE_TOPIC);
//                if (topic.equals(mokoDevice.getDeviceTopicSwitchState())) {
//                    mokoDevice.isOnline = false;
//                    mokoDevice.on_off_1 = false;
//                    mokoDevice.on_off_2 = false;
//                    mokoDevice.on_off_3 = false;
//                    tvWallSwitch1TimerState.setVisibility(View.GONE);
//                    tvWallSwitch2TimerState.setVisibility(View.GONE);
//                    tvWallSwitch3TimerState.setVisibility(View.GONE);
//                    changeSwitchState();
//                }
//            }
//            if (AppConstants.ACTION_MODIFY_NAME.equals(action)) {
//                MokoDevice device = DBTools.getInstance(WallSwitchDetailActivity.this).selectDevice(mokoDevice.mac);
//                mokoDevice.nickName = device.nickName;
//            }
//        }
//    };
//
//    private void changeSwitchState() {
//        tvWallSwitch1Edit.setText(mokoDevice.switchName1);
//        tvWallSwitch2Edit.setText(mokoDevice.switchName2);
//        tvWallSwitch3Edit.setText(mokoDevice.switchName3);
//        if (deviceType == 2) {
//            rlWallSwitch2.setVisibility(View.VISIBLE);
//        } else if (deviceType == 3) {
//            rlWallSwitch2.setVisibility(View.VISIBLE);
//            rlWallSwitch3.setVisibility(View.VISIBLE);
//        }
//        rlWallSwitch1.setBackgroundColor(ContextCompat.getColor(WallSwitchDetailActivity.this, mokoDevice.on_off_1 ? R.color.white_ffffff : R.color.grey_e0e0e0));
//        ivWallSwitch1.setImageDrawable(ContextCompat.getDrawable(WallSwitchDetailActivity.this, mokoDevice.on_off_1 ? R.drawable.wall_switch_switch_on : R.drawable.wall_switch_switch_off));
//        rlWallSwitch2.setBackgroundColor(ContextCompat.getColor(WallSwitchDetailActivity.this, mokoDevice.on_off_2 ? R.color.white_ffffff : R.color.grey_e0e0e0));
//        ivWallSwitch2.setImageDrawable(ContextCompat.getDrawable(WallSwitchDetailActivity.this, mokoDevice.on_off_2 ? R.drawable.wall_switch_switch_on : R.drawable.wall_switch_switch_off));
//        rlWallSwitch3.setBackgroundColor(ContextCompat.getColor(WallSwitchDetailActivity.this, mokoDevice.on_off_3 ? R.color.white_ffffff : R.color.grey_e0e0e0));
//        ivWallSwitch3.setImageDrawable(ContextCompat.getDrawable(WallSwitchDetailActivity.this, mokoDevice.on_off_3 ? R.drawable.wall_switch_switch_on : R.drawable.wall_switch_switch_off));
//    }
//
//    public void back(View view) {
//        finish();
//    }
//
//    public void more(View view) {
//        Intent intent = new Intent(this, MoreActivity.class);
//        intent.putExtra(AppConstants.EXTRA_KEY_DEVICE, mokoDevice);
//        startActivity(intent);
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        unregisterReceiver(mReceiver);
//    }
//
//    public void onClickAllOff(View view) {
//        if (isWindowLocked()) {
//            return;
//        }
//        if (!MokoSupport.getInstance().isConnected()) {
//            ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.network_error);
//            return;
//        }
//        if (!mokoDevice.isOnline) {
//            ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.device_offline);
//            return;
//        }
//        showLoadingProgressDialog(getString(R.string.wait));
//        LogModule.i("切换开关");
//        JsonObject json = new JsonObject();
//        switch (deviceType) {
//            case 1:
//                json.addProperty("switch_state_01", "off");
//                break;
//            case 2:
//                json.addProperty("switch_state_01", "off");
//                json.addProperty("switch_state_02", "off");
//                break;
//            case 3:
//                json.addProperty("switch_state_01", "off");
//                json.addProperty("switch_state_02", "off");
//                json.addProperty("switch_state_03", "off");
//                break;
//        }
//        String mqttConfigAppStr = SPUtiles.getStringValue(WallSwitchDetailActivity.this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");
//        MQTTConfig appMqttConfig = new Gson().fromJson(mqttConfigAppStr, MQTTConfig.class);
//        MqttMessage message = new MqttMessage();
//        message.setPayload(json.toString().getBytes());
//        message.setQos(appMqttConfig.qos);
//        try {
//            MokoSupport.getInstance().publish(mokoDevice.getAppTopicSwitchState(), message);
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void onClickAllOn(View view) {
//        if (isWindowLocked()) {
//            return;
//        }
//        if (!MokoSupport.getInstance().isConnected()) {
//            ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.network_error);
//            return;
//        }
//        if (!mokoDevice.isOnline) {
//            ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.device_offline);
//            return;
//        }
//        showLoadingProgressDialog(getString(R.string.wait));
//        LogModule.i("切换开关");
//        JsonObject json = new JsonObject();
//        switch (deviceType) {
//            case 1:
//                json.addProperty("switch_state_01", "on");
//                break;
//            case 2:
//                json.addProperty("switch_state_01", "on");
//                json.addProperty("switch_state_02", "on");
//                break;
//            case 3:
//                json.addProperty("switch_state_01", "on");
//                json.addProperty("switch_state_02", "on");
//                json.addProperty("switch_state_03", "on");
//                break;
//        }
//        String mqttConfigAppStr = SPUtiles.getStringValue(WallSwitchDetailActivity.this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");
//        MQTTConfig appMqttConfig = new Gson().fromJson(mqttConfigAppStr, MQTTConfig.class);
//        MqttMessage message = new MqttMessage();
//        message.setPayload(json.toString().getBytes());
//        message.setQos(appMqttConfig.qos);
//        try {
//            MokoSupport.getInstance().publish(mokoDevice.getAppTopicSwitchState(), message);
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @OnClick({R.id.tv_wall_switch_1_schedule, R.id.tv_wall_switch_2_schedule, R.id.tv_wall_switch_3_schedule})
//    public void onViewClicked(View view) {
//        switch (view.getId()) {
//            case R.id.tv_wall_switch_1_schedule:
//                if (!MokoSupport.getInstance().isConnected()) {
//                    ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.network_error);
//                    return;
//                }
//                if (!mokoDevice.isOnline) {
//                    ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.device_offline);
//                    return;
//                }
//                ToastUtils.showToast(this, R.string.device_detail_schedule_tips);
//                break;
//            case R.id.tv_wall_switch_2_schedule:
//                if (!MokoSupport.getInstance().isConnected()) {
//                    ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.network_error);
//                    return;
//                }
//                if (!mokoDevice.isOnline) {
//                    ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.device_offline);
//                    return;
//                }
//                ToastUtils.showToast(this, R.string.device_detail_schedule_tips);
//                break;
//            case R.id.tv_wall_switch_3_schedule:
//                if (!MokoSupport.getInstance().isConnected()) {
//                    ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.network_error);
//                    return;
//                }
//                if (!mokoDevice.isOnline) {
//                    ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.device_offline);
//                    return;
//                }
//                ToastUtils.showToast(this, R.string.device_detail_schedule_tips);
//                break;
//        }
//    }
//
//    public void onClickWallSwitch1(View view) {
//        if (isWindowLocked()) {
//            return;
//        }
//        if (!MokoSupport.getInstance().isConnected()) {
//            ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.network_error);
//            return;
//        }
//        if (!mokoDevice.isOnline) {
//            ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.device_offline);
//            return;
//        }
//        showLoadingProgressDialog(getString(R.string.wait));
//        LogModule.i("切换开关");
//        JsonObject json = new JsonObject();
//        switch (deviceType) {
//            case 1:
//                json.addProperty("switch_state_01", mokoDevice.on_off_1 ? "off" : "on");
//                break;
//            case 2:
//                json.addProperty("switch_state_01", mokoDevice.on_off_1 ? "off" : "on");
//                json.addProperty("switch_state_02", mokoDevice.on_off_2 ? "on" : "off");
//                break;
//            case 3:
//                json.addProperty("switch_state_01", mokoDevice.on_off_1 ? "off" : "on");
//                json.addProperty("switch_state_02", mokoDevice.on_off_2 ? "on" : "off");
//                json.addProperty("switch_state_03", mokoDevice.on_off_3 ? "on" : "off");
//                break;
//        }
//        String mqttConfigAppStr = SPUtiles.getStringValue(WallSwitchDetailActivity.this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");
//        MQTTConfig appMqttConfig = new Gson().fromJson(mqttConfigAppStr, MQTTConfig.class);
//        MqttMessage message = new MqttMessage();
//        message.setPayload(json.toString().getBytes());
//        message.setQos(appMqttConfig.qos);
//        try {
//            MokoSupport.getInstance().publish(mokoDevice.getAppTopicSwitchState(), message);
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void onClickWallSwitch2(View view) {
//        if (isWindowLocked()) {
//            return;
//        }
//        if (!MokoSupport.getInstance().isConnected()) {
//            ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.network_error);
//            return;
//        }
//        if (!mokoDevice.isOnline) {
//            ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.device_offline);
//            return;
//        }
//        showLoadingProgressDialog(getString(R.string.wait));
//        LogModule.i("切换开关");
//        JsonObject json = new JsonObject();
//        switch (deviceType) {
//            case 2:
//                json.addProperty("switch_state_01", mokoDevice.on_off_1 ? "on" : "off");
//                json.addProperty("switch_state_02", mokoDevice.on_off_2 ? "off" : "on");
//                break;
//            case 3:
//                json.addProperty("switch_state_01", mokoDevice.on_off_1 ? "on" : "off");
//                json.addProperty("switch_state_02", mokoDevice.on_off_2 ? "off" : "on");
//                json.addProperty("switch_state_03", mokoDevice.on_off_3 ? "on" : "off");
//                break;
//        }
//        String mqttConfigAppStr = SPUtiles.getStringValue(WallSwitchDetailActivity.this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");
//        MQTTConfig appMqttConfig = new Gson().fromJson(mqttConfigAppStr, MQTTConfig.class);
//        MqttMessage message = new MqttMessage();
//        message.setPayload(json.toString().getBytes());
//        message.setQos(appMqttConfig.qos);
//        try {
//            MokoSupport.getInstance().publish(mokoDevice.getAppTopicSwitchState(), message);
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void onClickWallSwitch3(View view) {
//        if (isWindowLocked()) {
//            return;
//        }
//        if (!MokoSupport.getInstance().isConnected()) {
//            ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.network_error);
//            return;
//        }
//        if (!mokoDevice.isOnline) {
//            ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.device_offline);
//            return;
//        }
//        showLoadingProgressDialog(getString(R.string.wait));
//        LogModule.i("切换开关");
//        JsonObject json = new JsonObject();
//        json.addProperty("switch_state_01", mokoDevice.on_off_1 ? "on" : "off");
//        json.addProperty("switch_state_02", mokoDevice.on_off_2 ? "on" : "off");
//        json.addProperty("switch_state_03", mokoDevice.on_off_3 ? "off" : "on");
//        String mqttConfigAppStr = SPUtiles.getStringValue(WallSwitchDetailActivity.this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");
//        MQTTConfig appMqttConfig = new Gson().fromJson(mqttConfigAppStr, MQTTConfig.class);
//        MqttMessage message = new MqttMessage();
//        message.setPayload(json.toString().getBytes());
//        message.setQos(appMqttConfig.qos);
//        try {
//            MokoSupport.getInstance().publish(mokoDevice.getAppTopicSwitchState(), message);
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void onClickTimer1(View view) {
//        if (isWindowLocked()) {
//            return;
//        }
//        if (!MokoSupport.getInstance().isConnected()) {
//            ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.network_error);
//            return;
//        }
//        if (!mokoDevice.isOnline) {
//            ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.device_offline);
//            return;
//        }
//        TimerDialog dialog = new TimerDialog(this);
//        dialog.setData(mokoDevice.on_off_1);
//        dialog.setListener(new TimerDialog.TimerListener() {
//            @Override
//            public void onConfirmClick(TimerDialog dialog) {
//                if (!MokoSupport.getInstance().isConnected()) {
//                    ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.network_error);
//                    return;
//                }
//                if (!mokoDevice.isOnline) {
//                    ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.device_offline);
//                    return;
//                }
//                showLoadingProgressDialog(getString(R.string.wait));
//                JsonObject json = new JsonObject();
//                json.addProperty("delay_hour_01", dialog.getWvHour());
//                json.addProperty("delay_minute_01", dialog.getWvMinute());
//                String mqttConfigAppStr = SPUtiles.getStringValue(WallSwitchDetailActivity.this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");
//                MQTTConfig appMqttConfig = new Gson().fromJson(mqttConfigAppStr, MQTTConfig.class);
//                MqttMessage message = new MqttMessage();
//                message.setPayload(json.toString().getBytes());
//                message.setQos(appMqttConfig.qos);
//                try {
//                    MokoSupport.getInstance().publish(mokoDevice.getAppTopicDelayTime1(), message);
//                } catch (MqttException e) {
//                    e.printStackTrace();
//                }
//                dialog.dismiss();
//            }
//        });
//        dialog.show();
//    }
//
//
//    public void onClickTimer2(View view) {
//        if (isWindowLocked()) {
//            return;
//        }
//        if (!MokoSupport.getInstance().isConnected()) {
//            ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.network_error);
//            return;
//        }
//        if (!mokoDevice.isOnline) {
//            ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.device_offline);
//            return;
//        }
//        TimerDialog dialog = new TimerDialog(this);
//        dialog.setData(mokoDevice.on_off_2);
//        dialog.setListener(new TimerDialog.TimerListener() {
//            @Override
//            public void onConfirmClick(TimerDialog dialog) {
//                if (!MokoSupport.getInstance().isConnected()) {
//                    ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.network_error);
//                    return;
//                }
//                if (!mokoDevice.isOnline) {
//                    ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.device_offline);
//                    return;
//                }
//                showLoadingProgressDialog(getString(R.string.wait));
//                JsonObject json = new JsonObject();
//                json.addProperty("delay_hour_02", dialog.getWvHour());
//                json.addProperty("delay_minute_02", dialog.getWvMinute());
//                String mqttConfigAppStr = SPUtiles.getStringValue(WallSwitchDetailActivity.this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");
//                MQTTConfig appMqttConfig = new Gson().fromJson(mqttConfigAppStr, MQTTConfig.class);
//                MqttMessage message = new MqttMessage();
//                message.setPayload(json.toString().getBytes());
//                message.setQos(appMqttConfig.qos);
//                try {
//                    MokoSupport.getInstance().publish(mokoDevice.getAppTopicDelayTime2(), message);
//                } catch (MqttException e) {
//                    e.printStackTrace();
//                }
//                dialog.dismiss();
//            }
//        });
//        dialog.show();
//    }
//
//
//    public void onClickTimer3(View view) {
//        if (isWindowLocked()) {
//            return;
//        }
//        if (!MokoSupport.getInstance().isConnected()) {
//            ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.network_error);
//            return;
//        }
//        if (!mokoDevice.isOnline) {
//            ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.device_offline);
//            return;
//        }
//        TimerDialog dialog = new TimerDialog(this);
//        dialog.setData(mokoDevice.on_off_3);
//        dialog.setListener(new TimerDialog.TimerListener() {
//            @Override
//            public void onConfirmClick(TimerDialog dialog) {
//                if (!MokoSupport.getInstance().isConnected()) {
//                    ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.network_error);
//                    return;
//                }
//                if (!mokoDevice.isOnline) {
//                    ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.device_offline);
//                    return;
//                }
//                showLoadingProgressDialog(getString(R.string.wait));
//                JsonObject json = new JsonObject();
//                json.addProperty("delay_hour_03", dialog.getWvHour());
//                json.addProperty("delay_minute_03", dialog.getWvMinute());
//                String mqttConfigAppStr = SPUtiles.getStringValue(WallSwitchDetailActivity.this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");
//                MQTTConfig appMqttConfig = new Gson().fromJson(mqttConfigAppStr, MQTTConfig.class);
//                MqttMessage message = new MqttMessage();
//                message.setPayload(json.toString().getBytes());
//                message.setQos(appMqttConfig.qos);
//                try {
//                    MokoSupport.getInstance().publish(mokoDevice.getAppTopicDelayTime3(), message);
//                } catch (MqttException e) {
//                    e.printStackTrace();
//                }
//                dialog.dismiss();
//            }
//        });
//        dialog.show();
//    }
//
//    private InputFilter filter = new InputFilter() {
//        @Override
//        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
//            if (source.equals(" ") || source.toString().contentEquals("\n")) return "";
//            else return null;
//        }
//    };
//
//    public void onClickEdit1(final View view) {
//        View content = LayoutInflater.from(this).inflate(R.layout.modify_name, null);
//        final EditText etDeviceName = ButterKnife.findById(content, R.id.et_device_name);
//        final TextView tvModifyTitle = ButterKnife.findById(content, R.id.tv_modify_title);
//        tvModifyTitle.setText(R.string.more_modify_switch_title);
//        String switchName = ((TextView) view).getText().toString();
//        etDeviceName.setText(switchName);
//        etDeviceName.setSelection(switchName.length());
//        etDeviceName.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(20)});
//        CustomDialog dialog = new CustomDialog.Builder(this)
//                .setContentView(content)
//                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                })
//                .setNegativeButton(R.string.save, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        String nickName = etDeviceName.getText().toString();
//                        if (TextUtils.isEmpty(nickName)) {
//                            ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.more_modify_switch_name_tips);
//                            return;
//                        }
//                        mokoDevice.switchName1 = nickName.toString();
//                        DBTools.getInstance(WallSwitchDetailActivity.this).updateDevice(mokoDevice);
//                        Intent intent = new Intent(AppConstants.ACTION_MODIFY_NAME);
//                        WallSwitchDetailActivity.this.sendBroadcast(intent);
//                        ((TextView) view).setText(nickName);
//                        dialog.dismiss();
//                    }
//                })
//                .create();
//        dialog.show();
//        ivWallSwitch1.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                showKeyboard(etDeviceName);
//            }
//        }, 300);
//    }
//
//    public void onClickEdit2(final View view) {
//        View content = LayoutInflater.from(this).inflate(R.layout.modify_name, null);
//        final EditText etDeviceName = ButterKnife.findById(content, R.id.et_device_name);
//        final TextView tvModifyTitle = ButterKnife.findById(content, R.id.tv_modify_title);
//        tvModifyTitle.setText(R.string.more_modify_switch_title);
//        String switchName = ((TextView) view).getText().toString();
//        etDeviceName.setText(switchName);
//        etDeviceName.setSelection(switchName.length());
//        etDeviceName.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(20)});
//        CustomDialog dialog = new CustomDialog.Builder(this)
//                .setContentView(content)
//                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                })
//                .setNegativeButton(R.string.save, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        String nickName = etDeviceName.getText().toString();
//                        if (TextUtils.isEmpty(nickName)) {
//                            ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.more_modify_switch_name_tips);
//                            return;
//                        }
//                        mokoDevice.switchName2 = nickName.toString();
//                        DBTools.getInstance(WallSwitchDetailActivity.this).updateDevice(mokoDevice);
//                        Intent intent = new Intent(AppConstants.ACTION_MODIFY_NAME);
//                        WallSwitchDetailActivity.this.sendBroadcast(intent);
//                        ((TextView) view).setText(nickName);
//                        dialog.dismiss();
//                    }
//                })
//                .create();
//        dialog.show();
//        ivWallSwitch1.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                showKeyboard(etDeviceName);
//            }
//        }, 300);
//    }
//
//    public void onClickEdit3(final View view) {
//        View content = LayoutInflater.from(this).inflate(R.layout.modify_name, null);
//        final EditText etDeviceName = ButterKnife.findById(content, R.id.et_device_name);
//        final TextView tvModifyTitle = ButterKnife.findById(content, R.id.tv_modify_title);
//        tvModifyTitle.setText(R.string.more_modify_switch_title);
//        String switchName = ((TextView) view).getText().toString();
//        etDeviceName.setText(switchName);
//        etDeviceName.setSelection(switchName.length());
//        etDeviceName.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(20)});
//        CustomDialog dialog = new CustomDialog.Builder(this)
//                .setContentView(content)
//                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                })
//                .setNegativeButton(R.string.save, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        String nickName = etDeviceName.getText().toString();
//                        if (TextUtils.isEmpty(nickName)) {
//                            ToastUtils.showToast(WallSwitchDetailActivity.this, R.string.more_modify_switch_name_tips);
//                            return;
//                        }
//                        mokoDevice.switchName3 = nickName.toString();
//                        DBTools.getInstance(WallSwitchDetailActivity.this).updateDevice(mokoDevice);
//                        Intent intent = new Intent(AppConstants.ACTION_MODIFY_NAME);
//                        WallSwitchDetailActivity.this.sendBroadcast(intent);
//                        ((TextView) view).setText(nickName);
//                        dialog.dismiss();
//                    }
//                })
//                .create();
//        dialog.show();
//        ivWallSwitch1.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                showKeyboard(etDeviceName);
//            }
//        }, 300);
//    }
//
//    //弹出软键盘
//    public void showKeyboard(EditText editText) {
//        //其中editText为dialog中的输入框的 EditText
//        if (editText != null) {
//            //设置可获得焦点
//            editText.setFocusable(true);
//            editText.setFocusableInTouchMode(true);
//            //请求获得焦点
//            editText.requestFocus();
//            //调用系统输入法
//            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            inputManager.showSoftInput(editText, 0);
//        }
//    }
}
