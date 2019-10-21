package com.moko.scanner.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moko.scanner.R;
import com.moko.scanner.base.BaseAdapter;
import com.moko.scanner.entity.MokoDevice;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * @Date 2018/6/8
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.scanner.adapter.DeviceAdapter
 */
public class DeviceAdapter extends BaseAdapter<MokoDevice> {


    public DeviceAdapter(Context context) {
        super(context);
    }

    @Override
    protected void bindViewHolder(int position, ViewHolder viewHolder, View convertView, ViewGroup parent) {
        final DeviceViewHolder holder = (DeviceViewHolder) viewHolder;
        final MokoDevice device = getItem(position);
        setView(holder, device);
    }

    private void setView(DeviceViewHolder holder, final MokoDevice device) {
        if (!device.isOnline) {
            holder.tvDeviceStatus.setText(mContext.getString(R.string.device_state_offline));
            holder.tvDeviceStatus.setTextColor(ContextCompat.getColor(mContext, R.color.grey_b3b3b3));
        } else {
            holder.tvDeviceStatus.setText(mContext.getString(R.string.device_state_online));
            holder.tvDeviceStatus.setTextColor(ContextCompat.getColor(mContext, R.color.blue_0188cc));
        }
        holder.tvDeviceName.setText(device.nickName);

        holder.rlDeviceDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.deviceDetailClick(device);
            }
        });
    }

    @Override
    protected ViewHolder createViewHolder(int position, LayoutInflater inflater, ViewGroup parent) {
        final View convertView = inflater.inflate(R.layout.device_item, parent, false);
        return new DeviceViewHolder(convertView);
    }

    public void setListener(AdapterClickListener listener) {
        this.listener = listener;
    }

    static class DeviceViewHolder extends ViewHolder {
        @Bind(R.id.tv_device_name)
        TextView tvDeviceName;
        @Bind(R.id.tv_device_status)
        TextView tvDeviceStatus;
        @Bind(R.id.rl_device_detail)
        RelativeLayout rlDeviceDetail;

        public DeviceViewHolder(View convertView) {
            super(convertView);
            ButterKnife.bind(this, convertView);
        }
    }

    private AdapterClickListener listener;

    public interface AdapterClickListener {
        void deviceDetailClick(MokoDevice device);
    }
}
