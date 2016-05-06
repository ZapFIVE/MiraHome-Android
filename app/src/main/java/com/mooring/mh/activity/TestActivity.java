package com.mooring.mh.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.mooring.mh.R;
import com.mooring.mh.model.SleepTimeInfo;
import com.mooring.mh.views.AlarmDaySelectView;
import com.mooring.mh.views.ChartView.MyChartView;
import com.mooring.mh.views.ChartView.XXChartView;
import com.mooring.mh.views.ChartView.YAxisView;
import com.mooring.mh.views.CircleProgress.DoubleCircleView;
import com.mooring.mh.views.WeatherView.RandomGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于测试
 * Created by Will on 16/4/27.
 */
public class TestActivity extends AppCompatActivity {

    DoubleCircleView doubleCircle;

    Button test_date_picker;

    RandomGenerator mRandom;

    List<SleepTimeInfo> outDatas;
    List<SleepTimeInfo> innDatas;

    private YAxisView allChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


        myChartView = (MyChartView) findViewById(R.id.myChartView);
        xdata = new ArrayList<>();
        ydata = new ArrayList<>();



        alarmDaySelect = (AlarmDaySelectView) findViewById(R.id.alarmDaySelect);

    }

    AlarmDaySelectView alarmDaySelect;
    List<Integer> x = new ArrayList<>();
    XXChartView xxChartView;

    MyChartView myChartView;
    List<String> xdata;
    List<String> ydata;

    private float oldX = 0;
    private float oldY = 0;

    private float X = 0;
    private float Y = 0;
}
