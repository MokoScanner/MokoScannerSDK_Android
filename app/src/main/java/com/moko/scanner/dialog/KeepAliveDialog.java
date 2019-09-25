package com.moko.scanner.dialog;

import android.text.TextUtils;
import android.view.View;

import com.moko.scanner.R;
import com.moko.scanner.view.WheelView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class KeepAliveDialog extends MokoBaseDialog {

    @Bind(R.id.wv_keep_alive)
    WheelView wvKeepAlive;
    private int selected;

    @Override
    public int getLayoutRes() {
        return R.layout.dialog_keep_alive;
    }

    @Override
    public void bindView(View v) {
        ButterKnife.bind(this, v);

        wvKeepAlive.setData(createData());
        wvKeepAlive.setDefault(selected - 10);
    }

    private ArrayList<String> createData() {
        ArrayList<String> data = new ArrayList<>();
        for (int i = 10; i <= 120; i++) {
            data.add(i + "");
        }
        return data;
    }


    @Override
    public float getDimAmount() {
        return 0.7f;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick({R.id.tv_cancel, R.id.tv_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                dismiss();
                break;
            case R.id.tv_confirm:
                dismiss();
                if (TextUtils.isEmpty(wvKeepAlive.getSelectedText())) {
                    return;
                }
                if (wvKeepAlive.getSelected() < 0) {
                    return;
                }
                if (listener != null) {
                    listener.onDataSelected(wvKeepAlive.getSelectedText());
                }
                break;
        }
    }

    private OnDataSelectedListener listener;

    public void setListener(OnDataSelectedListener listener) {
        this.listener = listener;
    }

    public interface OnDataSelectedListener {
        void onDataSelected(String data);
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }
}
