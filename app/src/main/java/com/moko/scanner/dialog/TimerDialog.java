package com.moko.scanner.dialog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.moko.scanner.R;
import com.moko.scanner.base.BaseDialog;
import com.moko.scanner.view.WheelView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * @Date 2018/6/21
 * @Author wenzheng.liu
 * @Description 倒计时弹框
 * @ClassPath com.moko.scanner.dialog.TimerDialog
 */
public class TimerDialog extends BaseDialog<Boolean> {
    @Bind(R.id.tv_switch_state)
    TextView tvSwitchState;
    @Bind(R.id.wv_hour)
    WheelView wvHour;
    @Bind(R.id.wv_minute)
    WheelView wvMinute;

    public TimerDialog(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_timer;
    }

    @Override
    protected void renderConvertView(View convertView, Boolean on_off) {
        tvSwitchState.setText(on_off ? R.string.countdown_timer_off : R.string.countdown_timer_on);
        initWheelView();
    }

    private void initWheelView() {
        ArrayList<String> hour = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            if (i > 1) {
                hour.add(i + " hours");
            } else {
                hour.add(i + " hour");
            }
        }
        wvHour.setData(hour);
        wvHour.setDefault(0);
        ArrayList<String> minute = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            if (i > 1) {
                minute.add(i + " mins");
            } else {
                minute.add(i + " min");

            }
        }
        wvMinute.setData(minute);
        wvMinute.setDefault(0);
    }

    public int getWvHour() {
        return wvHour.getSelected();
    }

    public int getWvMinute() {
        return wvMinute.getSelected();
    }

    @OnClick({R.id.tv_back, R.id.tv_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                dismiss();
                break;
            case R.id.tv_confirm:
                listener.onConfirmClick(this);
                break;
        }
    }

    private TimerListener listener;

    public void setListener(TimerListener listener) {
        this.listener = listener;
    }

    public interface TimerListener {
        void onConfirmClick(TimerDialog dialog);
    }
}
