package com.mooring.mh.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.machtalk.sdk.connect.MachtalkSDK;
import com.machtalk.sdk.connect.MachtalkSDKConstant;
import com.machtalk.sdk.connect.MachtalkSDKListener;
import com.machtalk.sdk.domain.Result;
import com.mooring.mh.R;
import com.mooring.mh.activity.ConfirmationPswActivity;
import com.mooring.mh.activity.MainActivity;
import com.mooring.mh.app.InitApplicationHelper;
import com.mooring.mh.db.DbXUtils;
import com.mooring.mh.db.User;
import com.mooring.mh.utils.CommonUtils;
import com.mooring.mh.utils.MConstants;

import org.xutils.DbManager;
import org.xutils.common.util.LogUtil;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

/**
 * 登陆
 * <p/>
 * Created by Will on 16/3/30.
 */
public class LoginFragment extends BaseFragment implements View.OnClickListener {

    private EditText edit_userName;
    private EditText edit_userPwd;
    private TextView tv_forget_psw;
    private TextView tv_login_btn;
    private TextView tv_login_error;
    private ImageView imgView_sina;
    private ImageView imgView_weChart;
    private ImageView imgView_QQ;
    private ImageView imgView_facebook;

    private String userName;//手机号码
    private String userPwd;//密码
    private DbManager dbManager;

    private MySDKListener listener;
    private SharedPreferences.Editor editor;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_login;
    }

    @Override
    protected void initView() {

        editor = InitApplicationHelper.sp.edit();
        listener = new MySDKListener();

        edit_userName = (EditText) rootView.findViewById(R.id.edit_phone);
        edit_userPwd = (EditText) rootView.findViewById(R.id.edit_psw);
        tv_forget_psw = (TextView) rootView.findViewById(R.id.tv_forget_psw);
        tv_login_btn = (TextView) rootView.findViewById(R.id.tv_login_btn);
        tv_login_error = (TextView) rootView.findViewById(R.id.tv_login_error);

        imgView_sina = (ImageView) rootView.findViewById(R.id.imgView_sina);
        imgView_weChart = (ImageView) rootView.findViewById(R.id.imgView_weChart);
        imgView_QQ = (ImageView) rootView.findViewById(R.id.imgView_QQ);
        imgView_facebook = (ImageView) rootView.findViewById(R.id.imgView_facebook);


        tv_forget_psw.setOnClickListener(this);
        tv_login_btn.setOnClickListener(this);
        imgView_sina.setOnClickListener(this);
        imgView_weChart.setOnClickListener(this);
        imgView_QQ.setOnClickListener(this);
        imgView_facebook.setOnClickListener(this);

        //--------------暂时使用,真实情况去除!!!!!!!!---------------------------
        edit_userName.setText("13661498824");
        edit_userPwd.setText("123456");

    }

    class MySDKListener extends MachtalkSDKListener {
        @Override
        public void onServerConnectStatusChanged(MachtalkSDKConstant.ServerConnStatus serverConnStatus) {
            super.onServerConnectStatusChanged(serverConnStatus);
            if (serverConnStatus == MachtalkSDKConstant.ServerConnStatus.LOGOUT_KICKOFF) {
                context.finish();
                return;
            }
        }

        @Override
        public void onUserLogin(Result result, String user) {
            int success = Result.FAILED;
            String errMsg = null;
            if (result != null) {
                success = result.getSuccess();
                errMsg = result.getErrorMessage();
            }
            if (success == Result.SUCCESS) {
                editor.putString(MConstants.SP_KEY_USERNAME, userName);
                editor.putString(MConstants.SP_KEY_PASSWORD, userPwd);
                editor.commit();

                LogUtil.i("store username: " + userName + " password: " + userPwd);

                startActivity(new Intent(context, MainActivity.class));
                context.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
                context.finish();
            } else {
                if (errMsg == null) {
                    errMsg = getResources().getString(R.string.network_exception);
                }
                CommonUtils.showToast(context, errMsg);
            }
        }
    }

    @Override
    protected void lazyLoad() {

    }

    @Override
    protected MachtalkSDKListener setSDKListener() {

        return listener;
    }

    /**
     * 登陆
     */
    private void login() {


        MachtalkSDK.getInstance().userLogin("13661498824", "123456", null);

//        MachtalkSDK.getInstance().userLogin("18136093612", "123456", null);

        /*RequestParams params = CommonUtils.getBaseParams(MConstants.LOGIN_BY_MOBILE_PHONE);
        params.addParameter("mobile_userName", userName);
        params.addParameter("password", userPwd);
        x.http().post(params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                if (result != null) {

                    //登陆成功   检查本地是否有用户

                    checkHasLocalUser();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.e(ex.getMessage(), ex);
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.e(cex.getMessage(), cex);
            }

            @Override
            public void onFinished() {
                LogUtil.e("onFinished");
            }
        });*/

    }

    /**
     * 检查本地是否有用户
     */
    private void checkHasLocalUser() {
        DbManager.DaoConfig config = DbXUtils.getDaoConfig(getActivity());
        dbManager = x.getDb(config);

        List<User> users = new ArrayList<User>();
        try {
            users = dbManager.findAll(User.class);
            if (users.size() == 0) {
                //跳转添加用户界面
            } else {
                //跳转到首页
            }
        } catch (DbException e) {
            e.printStackTrace();
        }

    }


    /**
     * 登陆前检索
     *
     * @return
     */
    private boolean checkLogin() {
        if ("".equals(userName)) {
            setError(getResources().getString(R.string.error_login_name_empty));
            return false;
        }
        if ("".equals(userPwd)) {
            setError(getResources().getString(R.string.error_login_psw_empty));
            return false;
        }
        if (!(CommonUtils.isMobileNO(userName) || CommonUtils.isEmail(userName))) {
            setError(getResources().getString(R.string.error_with_num_email));
            return false;
        }
        return true;
    }

    private void setError(String error) {
        tv_login_error.setVisibility(View.VISIBLE);
        tv_login_error.setText(error);
    }

    /**
     * 第三方登陆
     */
    private void SSO() {
        CommonUtils.showToast(context, CommonUtils.getSP("token"));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_forget_psw:
                Intent it = new Intent();
                it.setClass(getActivity(), ConfirmationPswActivity.class);
                getActivity().startActivity(it);
                break;
            case R.id.tv_login_btn:
                userName = edit_userName.getText().toString().trim();
                userPwd = edit_userPwd.getText().toString().trim();


                /*if (checkLogin()) {
                    login();
                }*/

                login();


                break;
            case R.id.imgView_sina:
                SSO();
                break;
            case R.id.imgView_weChart:
                SSO();
                break;
            case R.id.imgView_QQ:
                SSO();
                break;
            case R.id.imgView_facebook:
                SSO();
                break;
        }
    }
}
