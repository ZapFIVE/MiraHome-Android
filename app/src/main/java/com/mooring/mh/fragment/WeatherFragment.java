package com.mooring.mh.fragment;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.mooring.mh.BuildConfig;
import com.mooring.mh.R;
import com.mooring.mh.activity.SetWifiActivity;
import com.mooring.mh.activity.SleepDetailActivity;
import com.mooring.mh.utils.MConstants;
import com.mooring.mh.utils.MUtils;
import com.mooring.mh.views.CircleProgress.CircleDisplay;
import com.mooring.mh.views.WeatherView.WeatherView;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.common.util.LogUtil;
import org.xutils.ex.HttpException;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.text.DecimalFormat;

/**
 * 天气Fragment
 * <p/>
 * Created by Will on 16/3/24.
 */
public class WeatherFragment extends BaseFragment implements View.OnClickListener,
        AMapLocationListener, SwitchUserObserver {

    private CircleDisplay cp_deep_sleep;//深睡眠
    private CircleDisplay cp_light_sleep;//浅睡眠
    private CircleDisplay cp_rem;//REM
    private CircleDisplay cp_wake_up;//清醒
    private TextView tv_sleep_detail;//详情

    private View no_network_data;//网络连接失败,暂无网络数据
    private View no_data_conn_mooring;//先连接mooring,暂无数据
    private View no_mooring_data;//无设备数据,但已连接上
    private View layout_sleep_data;//所有睡眠数据
    private View layout_weather_data;//天气相关数据
    private View tv_error_no_weather_data;//无天气数据
    /**
     * --------------天气---------------
     */
    private AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationOption = null;
    private double lon;//经度
    private double lat;//纬度
    private int weatherId;
    private double temp;//当前温度
    private int humidity;//单位%
    private double wind_speed;//米每秒
    private String sunrise;//日出
    private String sunset;//日落

    private int weatherKind = 0;
    private WeatherView weather_view;//天气
    private View layout_weather_bg;//天气背景
    private ImageView imgView_cloud_1;//云1
    private ImageView imgView_cloud_2;//云2
    private ImageView imgView_cloud_3;//云3
    private ImageView imgView_cloud_4;//云4
    private ImageView imgView_cloud_5;//云5

    private TextView tv_curr_temp;
    private TextView tv_wind_speed;
    private TextView tv_humidity;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_weather;
    }

    @Override
    protected void initFragment() {
    }

    @Override
    protected void initView() {
        /**
         * 天气相关数据
         */
        layout_weather_data = rootView.findViewById(R.id.layout_weather_data);
        tv_error_no_weather_data = rootView.findViewById(R.id.tv_error_no_weather_data);
        weather_view = (WeatherView) rootView.findViewById(R.id.weather_view);
        layout_weather_bg = rootView.findViewById(R.id.layout_weather_bg);
        imgView_cloud_1 = (ImageView) rootView.findViewById(R.id.imgView_cloud_1);
        imgView_cloud_2 = (ImageView) rootView.findViewById(R.id.imgView_cloud_2);
        imgView_cloud_3 = (ImageView) rootView.findViewById(R.id.imgView_cloud_3);
        imgView_cloud_4 = (ImageView) rootView.findViewById(R.id.imgView_cloud_4);
        imgView_cloud_5 = (ImageView) rootView.findViewById(R.id.imgView_cloud_5);
        tv_curr_temp = (TextView) rootView.findViewById(R.id.tv_curr_temp);
        tv_wind_speed = (TextView) rootView.findViewById(R.id.tv_wind_speed);
        tv_humidity = (TextView) rootView.findViewById(R.id.tv_humidity);
        /**
         * 睡眠数据相关
         */
        tv_sleep_detail = (TextView) rootView.findViewById(R.id.tv_sleep_detail);
        cp_deep_sleep = (CircleDisplay) rootView.findViewById(R.id.cp_deep_sleep);
        cp_light_sleep = (CircleDisplay) rootView.findViewById(R.id.cp_light_sleep);
        cp_rem = (CircleDisplay) rootView.findViewById(R.id.cp_rem);
        cp_wake_up = (CircleDisplay) rootView.findViewById(R.id.cp_wake_up);
        layout_sleep_data = rootView.findViewById(R.id.layout_sleep_data);
        tv_sleep_detail.setOnClickListener(this);

//        getLatestSleepData();

        getWeatherData();
    }

    /**
     * 从服务器获取昨日睡眠数据
     */
    private void getLatestSleepData() {
        RequestParams params = MUtils.getBaseParams(MConstants.DAILY_REPORT);
        params.addParameter("member_id", "");//为当前User的id
        params.addParameter("year_month", MUtils.getCurrDate());
        x.http().get(params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                //解析日参数数据
                if (result != null) {
                    //当日参数为空时展示无数据界面
//                    showNetworkErrorView();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                if (ex instanceof HttpException) { // 网络错误
                    //提示网络原因导致错误
                    showNetworkErrorView();
                } else { // 其他错误
                    LogUtil.e("onError message: " + ex.getMessage());
                }
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.w("onCancel message: " + cex.getMessage());
                showNetworkErrorView();
            }

            @Override
            public void onFinished() {
                LogUtil.i("  onFinished  ");
            }
        });

    }

    /**
     * 获取天气数据
     */
    private void getWeatherData() {
        //初始化当前白天/黑夜
        LogUtil.w("此时是白天吗:  " + MUtils.judgeCurrIsDayTime());
        if (MUtils.judgeCurrIsDayTime()) {
            layout_weather_bg.setBackgroundResource(R.drawable.img_weather_bg_night);
            imgView_cloud_1.setImageResource(R.drawable.ic_night_cloud_1);
            imgView_cloud_2.setImageResource(R.drawable.ic_night_cloud_2);
            imgView_cloud_3.setImageResource(R.drawable.ic_night_cloud_3);
            imgView_cloud_4.setImageResource(R.drawable.ic_night_cloud_4);
            imgView_cloud_5.setImageResource(R.drawable.ic_night_cloud_5);
        }

        if (Build.VERSION.SDK_INT >= 23) {
            LogUtil.w("______这里是WeatherFragment_____申请了权限");
            int check = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (check != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MConstants.PERMISSIONS_LOCATION);
                return;
            }
        }

        getLatAndLon();
    }

    /**
     * 高德定位
     * 获取经纬度
     */
    private void getLatAndLon() {
        LogUtil.w("_____发起定位请求_____");
        mLocationClient = new AMapLocationClient(context.getApplicationContext());
        mLocationOption = new AMapLocationClientOption();
        // 设置定位模式为高精度模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        // 设置不返回地址信息
        mLocationOption.setNeedAddress(false);
        //单次定位
        mLocationOption.setOnceLocation(true);
        // 设置定位监听
        mLocationClient.setLocationListener(this);
        // 设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        // 启动定位
        mLocationClient.startLocation();
    }

    /**
     * 设置圆形进度
     *
     * @param c
     * @param value
     */
    private void setCircleDisplay(CircleDisplay c, float value) {
        c.setFormatDigits(0);
        c.setAnimDuration(3000);
        c.setUnit("%");
        c.showValue(value, 100f, true);
    }

    /**
     * 展示无网络数据错误
     */
    private void showNetworkErrorView() {
        layout_sleep_data.setVisibility(View.INVISIBLE);
        if (no_data_conn_mooring != null) {
            no_data_conn_mooring.setVisibility(View.GONE);
        }
        if (no_mooring_data != null) {
            no_mooring_data.setVisibility(View.GONE);
        }
        if (no_network_data == null) {
            ViewStub viewStub = (ViewStub) rootView.findViewById(R.id.VStub_no_network_data);
            no_network_data = viewStub.inflate();
        } else {
            no_network_data.setVisibility(View.VISIBLE);
        }
        View view = rootView.findViewById(R.id.layout_data_fail);
        View imgView_network_reload = view.findViewById(R.id.imgView_network_reload);
        imgView_network_reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //重新获取睡眠数据
                getLatestSleepData();
            }
        });
    }

    /**
     * 隐藏无网络数据错误
     */
    private void hideNetworkErrorView() {
        layout_sleep_data.setVisibility(View.VISIBLE);
        if (no_network_data != null) {
            no_network_data.setVisibility(View.GONE);
        }
    }

    /**
     * 展示提示连接mooring设备
     */
    private void showConnectMooringView() {
        layout_sleep_data.setVisibility(View.INVISIBLE);
        if (no_network_data != null) {
            no_network_data.setVisibility(View.GONE);
        }
        if (no_mooring_data != null) {
            no_mooring_data.setVisibility(View.GONE);
        }
        if (no_data_conn_mooring == null) {
            ViewStub viewStub = (ViewStub) rootView.findViewById(R.id.VStub_connect_mooring);
            no_data_conn_mooring = viewStub.inflate();
        } else {
            no_data_conn_mooring.setVisibility(View.VISIBLE);
        }
        View view = rootView.findViewById(R.id.layout_connect_mooring);
        View imgView_conn_mooring = view.findViewById(R.id.imgView_conn_mooring);
        imgView_conn_mooring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //连接设备
                startActivity(new Intent(context, SetWifiActivity.class));
            }
        });
    }

    /**
     * 隐藏连接mooring设备的提示
     */
    private void hideConnectMooringView() {
        layout_sleep_data.setVisibility(View.VISIBLE);
        if (no_data_conn_mooring != null) {
            no_data_conn_mooring.setVisibility(View.GONE);
        }
    }

    /**
     * 展示数据为空的情况提示
     */
    private void showNoMooringDataView() {
        layout_sleep_data.setVisibility(View.INVISIBLE);
        if (no_data_conn_mooring != null) {
            no_data_conn_mooring.setVisibility(View.GONE);
        }
        if (no_network_data != null) {
            no_network_data.setVisibility(View.GONE);
        }
        if (no_mooring_data == null) {
            ViewStub viewStub = (ViewStub) rootView.findViewById(R.id.VStub_no_mooring_data);
            no_mooring_data = viewStub.inflate();
        } else {
            no_mooring_data.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏数据为空的情况提示
     */
    private void hideNoMooringDataView() {
        layout_sleep_data.setVisibility(View.VISIBLE);
        if (no_mooring_data != null) {
            no_mooring_data.setVisibility(View.GONE);
        }
    }

    /**
     * 获取天气数据
     */
    private void getLatestWeather() {
        LogUtil.d("____发起了天气数据请求_______");
        RequestParams params = new RequestParams(MConstants.WEATHER_SERVER);
        params.addParameter("lon", lon);
        params.addParameter("lat", lat);
        params.addParameter("units", MUtils.getCurrTempUnit() ? "metric" : "imperial");
        params.addParameter("appId", MConstants.OPEN_WEATHER_ID);
        x.http().get(params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                JSONArray weather = result.optJSONArray("weather");
                try {
                    weatherId = weather.optJSONObject(0).getInt("id");
                    JSONObject main = result.optJSONObject("main");
                    humidity = main.optInt("humidity");
                    temp = main.optDouble("temp");
                    JSONObject wind = result.optJSONObject("wind");
                    wind_speed = wind.optDouble("speed");
                    JSONObject sys = result.optJSONObject("sys");
                    sunrise = MUtils.translateTime(sys.optLong("sunrise"));
                    sunset = MUtils.translateTime(sys.optLong("sunset"));

                    tv_wind_speed.setText(new DecimalFormat("#0.0").format(wind_speed * 3.6)
                            + getString(R.string.unit_km_h));
                    tv_humidity.setText(humidity + getString(R.string.unit_percent));
                    tv_curr_temp.setText(Math.round(temp) + (MUtils.getCurrTempUnit() ?
                            getString(R.string.unit_celsius) : getString(R.string.unit_fahrenheit)));
                    judgeWeather(weatherId);
                    weather_view.switchWeather(weatherKind);
                    editor.putInt("beforeWeather", weatherKind);
                    editor.putInt("beforeWeatherId", weatherId);
                    editor.apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.e("ex  " + ex.getMessage() + "  ");
                tv_error_no_weather_data.setVisibility(View.VISIBLE);
                layout_weather_data.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.w("cex  " + cex.getMessage() + "  ");
                tv_error_no_weather_data.setVisibility(View.VISIBLE);
                layout_weather_data.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFinished() {
                LogUtil.d("onFinished ");
            }
        });
    }

    /**
     * 判定当前天气
     *
     * @param id
     */
    private void judgeWeather(int id) {
        if (id == 951 || id == 800) {
            //晴
            changeWeatherIcon(R.drawable.ic_calm);
            switchBackground(WeatherView.CALM);
            return;
        }
        if (id == 952 || id == 953 || id == 954 || id == 955 || id == 956 || id == 905) {
            //微风
            changeWeatherIcon(R.drawable.ic_light_breeze);
            switchBackground(WeatherView.LIGHT_BREEZE);
            return;
        }
        if (id == 957 || id == 958 || id == 959 || id == 960 || id == 961 || id == 962) {
            //大风
            changeWeatherIcon(R.drawable.ic_high_wind);
            switchBackground(WeatherView.HIGH_WIND);
            return;
        }
        if (id == 900 || id == 901 || id == 902 || id == 781) {
            //龙卷风
            changeWeatherIcon(R.drawable.ic_tornado);
            switchBackground(WeatherView.TORNADO);
            return;
        }
        if (id == 903) {
            //冷
            changeWeatherIcon(R.drawable.ic_clod);
            switchBackground(WeatherView.COLD);
            return;
        }
        if (id == 904) {
            //热
            changeWeatherIcon(R.drawable.ic_hot);
            switchBackground(WeatherView.HOT);
            return;
        }
        if (id == 906) {
            //冰雹
            changeWeatherIcon(R.drawable.ic_hail);
            switchBackground(WeatherView.HAIL);
            return;
        }
        if (id == 801 || id == 802 || id == 803 || id == 804) {
            //多云
            changeWeatherIcon(R.drawable.ic_few_clouds);
            switchBackground(WeatherView.FEW_CLOUDS);
            return;
        }
        if (id == 701 || id == 711 || id == 721) {
            //雾霾
            changeWeatherIcon(R.drawable.ic_mist);
            switchBackground(WeatherView.MIST);
            return;
        }
        if (id == 731 || id == 741 || id == 751 || id == 761 || id == 762 || id == 771) {
            //沙尘暴
            changeWeatherIcon(R.drawable.ic_mist);
            switchBackground(WeatherView.DUST_WHIRLS);
            return;
        }
        if (id == 600 || id == 601) {
            //小雪
            changeWeatherIcon(R.drawable.ic_light_snow);
            switchBackground(WeatherView.LIGHT_SNOW);
            return;
        }
        if (id == 602) {
            //大雪
            changeWeatherIcon(R.drawable.ic_heavy_snow);
            switchBackground(WeatherView.HEAVY_SNOW);
            return;
        }
        if (id == 611 || id == 612 || id == 615 || id == 616 || id == 620 || id == 621 || id == 622) {
            //雨夹雪
            changeWeatherIcon(R.drawable.ic_sleet);
            switchBackground(WeatherView.SLEET);
            return;
        }
        if (id == 300 || id == 301 || id == 302 || id == 310 || id == 311 || id == 312 || id == 500
                || id == 313 || id == 314 || id == 321 || id == 501) {
            //小雨
            changeWeatherIcon(R.drawable.ic_light_rain);
            switchBackground(WeatherView.LIGHT_RAIN);
            return;
        }
        if (id == 502 || id == 503 || id == 504) {
            //大雨
            changeWeatherIcon(R.drawable.ic_heavy_rain);
            switchBackground(WeatherView.HEAVY_RAIN);
            return;
        }
        if (id == 511) {
            //雷雨伴冰雹
            changeWeatherIcon(R.drawable.ic_freezing_rain);
            switchBackground(WeatherView.FREEZING_RAIN);
            return;
        }
        if (id == 200 || id == 201 || id == 202 || id == 210 || id == 211 || id == 212 || id == 221
                || id == 230 || id == 231 || id == 232 || id == 520 || id == 521 || id == 522 ||
                id == 531) {
            //雷雨
            changeWeatherIcon(R.drawable.ic_shower_rain);
            switchBackground(WeatherView.SHOWER_RAIN);
            return;
        }
    }

    /**
     * 切换天气图标
     *
     * @param id
     */
    private void changeWeatherIcon(int id) {
        tv_curr_temp.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(id), null, null, null);
    }

    /**
     * 切换天气对应的背景
     *
     * @param kind
     */
    private void switchBackground(int kind) {
        this.weatherKind = kind;
        if (!MUtils.judgeTimeInterval(sunrise, sunset)) {
            return;
        }
        switch (kind) {
            case WeatherView.CALM:
                layout_weather_bg.setBackgroundResource(R.drawable.img_weather_bg_calm);
                break;
            case WeatherView.CLOUDY_DAY:
            case WeatherView.FEW_CLOUDS:
            case WeatherView.MIST:
                layout_weather_bg.setBackgroundResource(R.drawable.img_weather_bg_cloudy_day);
                break;
            case WeatherView.LIGHT_SNOW:
            case WeatherView.HEAVY_SNOW:
            case WeatherView.SLEET:
                layout_weather_bg.setBackgroundResource(R.drawable.img_weather_bg_sleet);
                break;
            case WeatherView.LIGHT_RAIN:
            case WeatherView.HEAVY_RAIN:
            case WeatherView.HAIL:
            case WeatherView.SHOWER_RAIN:
            case WeatherView.FREEZING_RAIN:
                layout_weather_bg.setBackgroundResource(R.drawable.img_weather_bg_and_hail);
                break;
            case WeatherView.LIGHT_BREEZE:
                layout_weather_bg.setBackgroundResource(R.drawable.img_weather_bg_light_breeze);
                break;
            case WeatherView.HIGH_WIND:
            case WeatherView.TORNADO:
                layout_weather_bg.setBackgroundResource(R.drawable.img_weather_bg_tornado);
                break;
            case WeatherView.DUST_WHIRLS:
                layout_weather_bg.setBackgroundResource(R.drawable.img_weather_bg_mist);
                break;
            case WeatherView.COLD:
                layout_weather_bg.setBackgroundResource(R.drawable.img_weather_bg_cold);
                break;
            case WeatherView.HOT:
                layout_weather_bg.setBackgroundResource(R.drawable.img_weather_bg_hot);
                break;
        }

    }

    /**
     * 跳转到设置界面的Dialog提示
     *
     * @param msg
     */
    private void showDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg);
        builder.setTitle(getString(R.string.tip_access_request));
        builder.setPositiveButton(getString(R.string.tip_go_setting), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                intent.setData(uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(getString(R.string.tv_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_sleep_detail:
                Intent it = new Intent();
                it.setClass(getActivity(), SleepDetailActivity.class);
                context.startActivity(it);
                break;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            weather_view.setRuning(false);
        } else {
            if (tv_error_no_weather_data.getVisibility() == View.VISIBLE) {
                getLatestWeather();
            }
            weather_view.setRuning(true);
        }
    }

    @Override
    public void onSwitch(String userId, int location, String fTag) {
        if (MConstants.OBSERVER_TEMP_UNIT == location) {
            if (fTag.equals(MConstants.DEGREES_C + "")) {
                temp = MUtils.F2C((float) temp);
                tv_curr_temp.setText(Math.round(temp) + getString(R.string.unit_celsius));
            } else if (fTag.equals(MConstants.DEGREES_F + "")) {
                temp = MUtils.C2F((float) temp);
                tv_curr_temp.setText(Math.round(temp) + getString(R.string.unit_fahrenheit));
            }
        }
        if (!isVisible()) return;
        if (!TextUtils.isEmpty(userId)) {//切换头像时
            if (!MUtils.isCurrDeviceOnline()) return;
            //请求切换后用户日报数据
        } else {
            switch (location) {
                case MConstants.OBSERVER_DEVICE_STATUS:
                    if (fTag.equals(MConstants.DEVICE_ONLINE + "")) {
                        //设备上线
                        hideConnectMooringView();
                    } else if (fTag.equals(MConstants.DEVICE_OFFLINE + "")) {
                        //设备下线
                        showConnectMooringView();
                    }
                    break;
            }
        }
        LogUtil.e("________切换了User_________");
        //切换时要切换对应用户数据
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] permissions, int[] grantResults) {
        if (code != MConstants.PERMISSIONS_LOCATION) {
            return;
        }
        if (grantResults.length <= 0) {
            LogUtil.v(getString(R.string.error_permission_failed));
            return;
        }
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //允许
            getLatAndLon();
        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                //弹出Dialog,告知用户为什么要去申请该权限
                showDialog(getString(R.string.permission_location));
            } else {
                MUtils.showToast(context, getString(R.string.error_permission_failed));
            }
        }
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        LogUtil.w("______执行了定位监听________");
        if (null != amapLocation) {
            if (amapLocation.getErrorCode() == 0) {
                lat = amapLocation.getLatitude();//获取纬度
                lon = amapLocation.getLongitude();//获取经度
                LogUtil.w("定位来源: " + amapLocation.getLocationType()
                        + " 精度信息 " + amapLocation.getAccuracy());
                getLatestWeather();
                mLocationClient.stopLocation();//停止定位,这对每次启动只定位一次
            } else {
                LogUtil.e("location Error, ErrCode:" + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());
            }
        }
    }

    @Override
    protected void OnResume() {
        weather_view.setRuning(true);
        MobclickAgent.onPageStart("Weather");
    }

    @Override
    protected void OnPause() {
        weather_view.setRuning(false);
        MobclickAgent.onPageEnd("Weather");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mLocationClient) {
            mLocationClient.onDestroy();//销毁客户端
            mLocationClient = null;
            mLocationOption = null;
        }
    }
}