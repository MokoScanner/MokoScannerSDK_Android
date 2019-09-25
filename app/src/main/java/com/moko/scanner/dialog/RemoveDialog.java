package com.moko.scanner.dialog;

import android.content.Context;
import android.view.View;

import com.moko.scanner.R;
import com.moko.scanner.base.BaseDialog;

import butterknife.OnClick;

/**
 * @Date 2018/6/21
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.scanner.dialog.RemoveDialog
 */
public class RemoveDialog extends BaseDialog<Boolean> {

    public RemoveDialog(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_remove_device;
    }

    @Override
    protected void renderConvertView(View convertView, Boolean on_off) {

    }


    @OnClick({R.id.tv_cancel, R.id.tv_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                dismiss();
                break;
            case R.id.tv_confirm:
                listener.onConfirmClick(this);
                break;
        }
    }

    private RemoveListener listener;

    public void setListener(RemoveListener listener) {
        this.listener = listener;
    }

    public interface RemoveListener {
        void onConfirmClick(RemoveDialog dialog);
    }
}
