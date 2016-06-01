package com.mooring.mh.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.mooring.mh.R;
import com.mooring.mh.adapter.LoginAndSignUpPagerAdapter;
import com.mooring.mh.utils.MConstants;

import java.util.Calendar;

/**
 * 登录和注册Activity
 * <p/>
 * Created by Will on 16/3/30.
 */
public class LoginAndSignUpActivity extends AppCompatActivity {
    private ViewPager login_viewPager;
    private TabLayout login_tabLayout;
    private LoginAndSignUpPagerAdapter pagerAdapter;
    private String flag;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_sign);

        Intent it = getIntent();
        flag = it.getStringExtra(MConstants.ENTRANCE_FLAG);

        initView();

        initData();

        if (MConstants.LOGOUT_KICKOFF.equals(flag)) {
            dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_logout_kickoff);
            dialog.setCanceledOnTouchOutside(false);
            Calendar cal = Calendar.getInstance();
            ((TextView) dialog.findViewById(R.id.tv_tip_time)).setText(
                    String.format(getString(R.string.tip_kicked_out),
                            doubleTime(cal.get(Calendar.HOUR_OF_DAY)) + ":" + doubleTime(cal.get(Calendar.MINUTE))));
            dialog.findViewById(R.id.tv_back).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                }
            });
            dialog.findViewById(R.id.tv_login).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //申请去重新登录

                    dialog.cancel();
                }
            });
            dialog.show();
        }
    }

    private void initView() {

        login_viewPager = (ViewPager) findViewById(R.id.login_viewPager);
        login_tabLayout = (TabLayout) findViewById(R.id.login_tabLayout);

    }

    private void initData() {
        pagerAdapter = new LoginAndSignUpPagerAdapter(this, getSupportFragmentManager());
        login_viewPager.setAdapter(pagerAdapter);
        login_tabLayout.setupWithViewPager(login_viewPager);//在设定Adapter之后才可执行
        login_tabLayout.setTabMode(TabLayout.MODE_FIXED);
    }

    private String doubleTime(int data) {
        if (data < 10) {
            return "0" + data;
        }
        return "" + data;
    }
}
