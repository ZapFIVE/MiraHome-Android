package com.mooring.mh.fragment;

import android.content.Intent;
import android.view.View;
import android.view.ViewStub;

import com.mooring.mh.R;
import com.mooring.mh.activity.DryingControlActivity;
import com.mooring.mh.activity.HeatingControlActivity;
import com.mooring.mh.activity.SetWifiActivity;
import com.mooring.mh.utils.MConstants;
import com.umeng.analytics.MobclickAgent;

/**
 * 第二个fragment 负责控制仪器的温度
 * <p/>
 * Created by Will on 16/3/24.
 */
public class ControlFragment extends BaseFragment implements View.OnClickListener, SwitchUserObserver {

    private View layout_control;
    private View layout_no_device;//无设备去连接
    private View layout_heating;
    private View layout_drying;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_control;
    }

    @Override
    protected void initFragment() {
    }

    @Override
    protected void initView() {

        layout_control = rootView.findViewById(R.id.layout_control);
        layout_heating = rootView.findViewById(R.id.layout_heating);
        layout_drying = rootView.findViewById(R.id.layout_drying);

        layout_heating.setOnClickListener(this);
        layout_drying.setOnClickListener(this);

        judgeDeviceIsOnline();
    }

    /**
     * 判断设备是否在线
     */
    private void judgeDeviceIsOnline() {
        if (sp.getBoolean(MConstants.DEVICE_LAN_ONLINE, false) ||
                sp.getBoolean(MConstants.DEVICE_ONLINE, false)) {
            hideNoDeviceView();
        } else {
            showNoDeviceView();
        }
    }

    /**
     * 显示无设备去连接界面
     */
    private void showNoDeviceView() {
        layout_control.setVisibility(View.GONE);
        if (layout_no_device == null) {
            ViewStub viewStub = (ViewStub) rootView.findViewById(R.id.VStub_no_device);
            layout_no_device = viewStub.inflate();
        } else {
            layout_no_device.setVisibility(View.VISIBLE);
        }
        View view = rootView.findViewById(R.id.no_device_to_conn);
        View imgView_device_connect = view.findViewById(R.id.imgView_device_connect);
        imgView_device_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //连接设备
                context.startActivity(new Intent(context, SetWifiActivity.class));
            }
        });
    }

    /**
     * 隐藏无设备去连接界面
     */
    private void hideNoDeviceView() {
        layout_control.setVisibility(View.VISIBLE);
        if (layout_no_device != null) {
            layout_no_device.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        Intent it = new Intent();
        switch (v.getId()) {
            case R.id.layout_heating:
                it.setClass(context, HeatingControlActivity.class);
                context.startActivity(it);
                break;
            case R.id.layout_drying:
                it.setClass(context, DryingControlActivity.class);
                context.startActivity(it);
                break;
        }
    }

    @Override
    public void onSwitch(String userId, int location, String fTag) {
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            judgeDeviceIsOnline();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("Control");
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("Control");
    }
}

