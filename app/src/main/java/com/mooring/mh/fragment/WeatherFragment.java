package com.mooring.mh.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.mooring.mh.R;
import com.mooring.mh.activity.MoreActivity;
import com.mooring.mh.app.InitApplicationHelper;
import com.mooring.mh.utils.MConstants;
import com.mooring.mh.utils.MUtils;
import com.mooring.mh.views.CircleProgress.CircleDisplay;
import com.mooring.mh.views.WeatherView.WeatherView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.common.util.LogUtil;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.Locale;

/**
 * 天气Fragment
 * <p/>
 * Created by Will on 16/3/24.
 */
public class WeatherFragment extends BaseFragment implements View.OnClickListener,
        AMapLocationListener, SwitchUserObserver {

    private CircleDisplay circle_progress_1;
    private CircleDisplay circle_progress_2;
    private CircleDisplay circle_progress_3;
    private CircleDisplay circle_progress_4;

    private TextView tv_more;

    private View layout_show;
    private View layout_data_fail;


    /**
     * --------------天气---------------
     */
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    private double lon;//经度
    private double lat;//纬度
    private int weatherId;
    private double temp;//当前温度
    private int humidity;//单位%
    private double wind_speed;//米每秒
    private String sunrise = MUtils.getCurrDate() + " 06:00:00";//日出
    private String sunset = MUtils.getCurrDate() + " 18:00:00";//日落

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


        circle_progress_1 = (CircleDisplay) rootView.findViewById(R.id.circle_progress_1);
        circle_progress_2 = (CircleDisplay) rootView.findViewById(R.id.circle_progress_2);
        circle_progress_3 = (CircleDisplay) rootView.findViewById(R.id.circle_progress_3);
        circle_progress_4 = (CircleDisplay) rootView.findViewById(R.id.circle_progress_4);


        setmCircleDisplay(circle_progress_1, 66f);
        setmCircleDisplay(circle_progress_2, 20f);
        setmCircleDisplay(circle_progress_3, 68f);
        setmCircleDisplay(circle_progress_4, 90f);

        tv_more = (TextView) rootView.findViewById(R.id.tv_more);
        tv_more.setOnClickListener(this);

//        initCloud();

        int beforeWeather = InitApplicationHelper.sp.getInt("beforeWeather", -1);
        int beforeWeatherId = InitApplicationHelper.sp.getInt("beforeWeatherId", -1);
        if (beforeWeatherId != -1 && beforeWeather != -1) {
//            judgeWeather(beforeWeatherId);
//            weather_view.switchWeather(beforeWeather);

            //------------直接切换天气有问题
        }

        if (Build.VERSION.SDK_INT >= 23) {
            int check = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION);
            if (check != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MConstants.PERMISSIONS_LOCATION);
                return;
            }
        }

        getLatAndLon();

        System.out.printf(translateTime(1461320856));
        if (!MUtils.judgeTimeInterval(sunrise, sunset)) {
            layout_weather_bg.setBackgroundResource(R.drawable.img_weather_bg_night);
            imgView_cloud_1.setImageResource(R.drawable.ic_night_cloud_1);
            imgView_cloud_2.setImageResource(R.drawable.ic_night_cloud_2);
            imgView_cloud_3.setImageResource(R.drawable.ic_night_cloud_3);
            imgView_cloud_4.setImageResource(R.drawable.ic_night_cloud_4);
            imgView_cloud_5.setImageResource(R.drawable.ic_night_cloud_5);
        }

    }

    /**
     * 初始化晃动云彩-------有待于修改
     */
    private void initCloud() {
        TranslateAnimation translate = new TranslateAnimation(0, -50, 0, 0);
        translate.setInterpolator(new OvershootInterpolator());
        translate.setDuration(3000);
        translate.setRepeatCount(-1);
        translate.setRepeatMode(Animation.REVERSE);
        TranslateAnimation translate2 = new TranslateAnimation(-50, 0, 0, 0);
        translate2.setInterpolator(new OvershootInterpolator());
        translate2.setDuration(3000);
        translate2.setRepeatCount(-1);
        translate2.setRepeatMode(Animation.REVERSE);

        imgView_cloud_1.startAnimation(translate);
        imgView_cloud_2.startAnimation(translate2);
        imgView_cloud_3.startAnimation(translate);
        imgView_cloud_4.startAnimation(translate2);
        imgView_cloud_5.startAnimation(translate);

    }

    /**
     * 设置圆形进度
     *
     * @param c
     * @param value
     */
    private void setmCircleDisplay(CircleDisplay c, float value) {
        c.setFormatDigits(0);
        c.setAnimDuration(3000);
        c.setUnit("%");
        c.showValue(value, 100f, true);

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            weather_view.setRuning(false);
        } else {
            weather_view.setRuning(true);
        }
    }


    private void getLatestWeather() {
        RequestParams params = new RequestParams(MConstants.WEATHER_SERVER);
        params.addParameter("lon", lon);
        params.addParameter("lat", lat);
        params.addParameter("APPID", MConstants.OPEN_WEATHER_ID);
        x.http().get(params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                JSONArray weather = result.optJSONArray("weather");
                try {

                    weatherId = weather.optJSONObject(0).getInt("id");
                    JSONObject main = result.optJSONObject("main");
                    humidity = main.optInt("humidity");
                    temp = main.optDouble("temp") - 272.15;
                    JSONObject wind = result.optJSONObject("wind");
                    wind_speed = wind.optDouble("speed");
                    JSONObject sys = result.optJSONObject("sys");
                    sunrise = translateTime(sys.optLong("sunrise"));
                    sunset = translateTime(sys.optLong("sunset"));

                    tv_wind_speed.setText((float) (Math.round((wind_speed * 3.6) * 10)) / 10 + "km/h");
                    tv_humidity.setText(humidity + "%");
                    tv_curr_temp.setText((int) temp + "℃");
                    judgeWeather(weatherId);
                    weather_view.switchWeather(weatherKind);
                    SharedPreferences.Editor editor = InitApplicationHelper.sp.edit();
                    editor.putInt("beforeWeather", weatherKind);
                    editor.putInt("beforeWeatherId", weatherId);
                    editor.commit();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.e("ex  " + ex.getMessage() + "  ");
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.e("cex  " + cex.getMessage() + "  ");
            }

            @Override
            public void onFinished() {
                LogUtil.e("onFinished ");
            }
        });
    }


    /**
     * 转换时间格式
     *
     * @param time
     */
    private String translateTime(long time) {
        String date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).
                format(new java.util.Date(time * 1000L));
        return date;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_more:
                Intent it = new Intent();
                it.setClass(getActivity(), MoreActivity.class);
                getActivity().startActivity(it);
                break;
        }
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
        tv_curr_temp.setCompoundDrawablesWithIntrinsicBounds(
                getResources().getDrawable(id), null, null, null);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        weather_view.setRuning(false);
    }

    @Override
    public void onStop() {
        super.onStop();
        weather_view.setRuning(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        weather_view.setRuning(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MConstants.PERMISSIONS_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //允许
                    getLatAndLon();
                } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        //弹出Dialog,告知用户为什么要去申请该权限


                    } else {
                        MUtils.showToast(context, "位置权限请求失败");
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    /**
     * 高德定位
     * 获取经纬度
     */
    private void getLatAndLon() {
        locationClient = new AMapLocationClient(context);
        locationOption = new AMapLocationClientOption();
        // 设置定位模式为高精度模式
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        // 设置定位监听
        locationClient.setLocationListener(this);
        //单次定位
        locationOption.setOnceLocation(true);
        // 设置定位参数
        locationClient.setLocationOption(locationOption);
        // 启动定位
        locationClient.startLocation();
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (null != amapLocation) {
            if (amapLocation.getErrorCode() == 0) {
                lat = amapLocation.getLatitude();//获取纬度
                lon = amapLocation.getLongitude();//获取经度

                getLatestWeather();

                LogUtil.i("定位来源: " + amapLocation.getLocationType()
                        + " 精度信息 " + amapLocation.getAccuracy());
            } else {
                LogUtil.e("location Error, ErrCode:" + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());
            }
        }
    }

    @Override
    public void onSwitch(String userId,int location,String fTag) {

        LogUtil.e("________切换了User_________");
    }
}