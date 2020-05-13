package com.moko.scanner.dialog;

import android.content.Context;
import android.view.View;

import com.moko.scanner.R;
import com.moko.scanner.base.BaseDialog;

import butterknife.OnClick;

/**
 * @Date 2020/5/11
 * @Author wenzheng.liu
 * @Description 
 * @ClassPath com.moko.scanner.dialog.FilterDelDialog
 */
public class FilterDelDialog extends BaseDialog<Object> {

    public FilterDelDialog(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_filter_del;
    }

    @Override
    protected void renderConvertView(View convertView, Object object) {

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

    private FilterDelListener listener;

    public void setListener(FilterDelListener listener) {
        this.listener = listener;
    }

    public interface FilterDelListener {
        void onConfirmClick(FilterDelDialog dialog);
    }
}
