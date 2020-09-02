package com.wa.sdk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.wa.sdk.WAConstants;
import com.wa.sdk.common.WACommonProxy;
import com.wa.sdk.common.WASharedPrefHelper;
import com.wa.sdk.common.model.WACallback;
import com.wa.sdk.common.utils.LogUtil;
import com.wa.sdk.core.WACoreProxy;
import com.wa.sdk.demo.base.BaseActivity;
import com.wa.sdk.demo.widget.TitleBar;
import com.wa.sdk.user.WAUserProxy;
import com.wa.sdk.user.model.WALoginResult;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 测试Login
 * Created by yinglovezhuzhu@gmail.com on 2016/1/4.
 */
public class LoginActivity extends BaseActivity {

    private TitleBar mTitlebar;

    private WASharedPrefHelper mSharedPrefHelper;

    private int mResultCode = RESULT_CANCELED;

    private boolean mAutoFinish = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Demo的初始化，跟SDK无关
        WASdkDemo.getInstance().initialize(this);

        setContentView(R.layout.activity_login);
        mSharedPrefHelper = WASharedPrefHelper.newInstance(this, WADemoConfig.SP_CONFIG_FILE_DEMO);

        Intent intent = getIntent();
        if (intent.hasExtra("auto_finish")) {
            mAutoFinish = intent.getBooleanExtra("auto_finish", false);
        }

        initView();


        Bundle metaData = null;
        boolean apiKey;
        try {
            android.content.pm.ApplicationInfo ai = this.getPackageManager()
                    .getApplicationInfo(this.getPackageName(), android.content.pm.PackageManager.GET_META_DATA);
            if (null != ai) {
                metaData = ai.metaData;
            }
            if (null != metaData) {
                apiKey = metaData.getBoolean("com.wa.sdk.android_ad");
                boolean a = apiKey;
            }
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
        }
    }


    @Override
    public void onBackPressed() {
        exit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        WACommonProxy.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (WACommonProxy.onRequestPermissionsResult(this, requestCode, permissions, grantResults)) {
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelLoadingDialog();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_fb_login:
                fbLogin();
                break;
            case R.id.btn_gg_login:
                googleLogin();
//                aggLogin();
                break;
            case R.id.btn_anonymous_login:
                anonymousLogin();
                break;
            case R.id.btn_app_login:
                appLogin();
                break;
            case R.id.btn_vk_login:
                vkLogin();
                break;
            case R.id.btn_twitter_login:
                twitterLogin();
                break;
            case R.id.btn_instagram_login:
                instagramLogin();
                break;
            case R.id.btn_logout:
                logout();
                break;
            case R.id.btn_login_form:
                WAUserProxy.loginUI(LoginActivity.this,
                        mSharedPrefHelper.getBoolean(WADemoConfig.SP_KEY_ENABLE_LOGIN_CACHE, false),
                        mLoginCallback);
                break;
            case R.id.btn_clear_login_cache:
                WAUserProxy.clearLoginCache();
                showShortToast(R.string.clean_login_cache);
                break;
            default:
                break;
        }
    }

    private void initView() {
        mTitlebar = (TitleBar) findViewById(R.id.tb_login);
        mTitlebar.setTitleText(R.string.login);
        mTitlebar.setLeftButton(android.R.drawable.ic_menu_revert, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });
        mTitlebar.setTitleTextColor(R.color.color_white);

        ToggleButton loginFlowType = (ToggleButton) findViewById(R.id.tbtn_login_flow_type);
        int flowType = WASdkDemo.getInstance().getLoginFlowType();
        WAUserProxy.setLoginFlowType(flowType);
        if (WAConstants.LOGIN_FLOW_TYPE_DEFAULT == flowType) {
            loginFlowType.setChecked(false);
        } else if (WAConstants.LOGIN_FLOW_TYPE_REBIND == flowType) {
            loginFlowType.setChecked(true);
        }
        loginFlowType.setOnCheckedChangeListener(mOnCheckedChangeListener);

        ToggleButton loginCache = (ToggleButton) findViewById(R.id.tbtn_enable_login_cache);
        loginCache.setChecked(mSharedPrefHelper.getBoolean(WADemoConfig.SP_KEY_ENABLE_LOGIN_CACHE, false));
        loginCache.setOnCheckedChangeListener(mOnCheckedChangeListener);
    }


    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.tbtn_login_flow_type:
                    int flowType = isChecked ? WAConstants.LOGIN_FLOW_TYPE_REBIND : WAConstants.LOGIN_FLOW_TYPE_DEFAULT;
                    WASdkDemo.getInstance().setLoginFlowType(flowType);
                    break;
                case R.id.tbtn_enable_login_cache:
                    mSharedPrefHelper.saveBoolean(WADemoConfig.SP_KEY_ENABLE_LOGIN_CACHE, isChecked);
                    break;
                default:
                    break;
            }

        }
    };

    private WACallback<WALoginResult> mLoginCallback = new WACallback<WALoginResult>() {
        @Override
        public void onSuccess(int code, String message, WALoginResult result) {
            String text = "code:" + code + "\nmessage:" + message;
            if (null == result) {
                text = "Login failed->" + text;
            } else {
                text = "Login success->" + text
                        + "\nplatform:" + result.getPlatform()
                        + "\nuserId:" + result.getUserId()
                        + "\ntoken:" + result.getToken()
                        + "\nplatformUserId:" + result.getPlatformUserId()
                        + "\nplatformToken:" + result.getPlatformToken()
                        + "\nisBindMobile: " + result.isBindMobile()
                        + "\nisFistLogin: " + result.isFirstLogin();

                // 数据收集
                //qa
                WACoreProxy.setServerId("server2");
//                WACoreProxy.setGameUserId("server2-role1-59473005");
//                WACoreProxy.setNickname("wing_test");
                //pre
                WACoreProxy.setServerId("server2");
                WACoreProxy.setGameUserId("server2-role1-59473005");
                WACoreProxy.setNickname("青铜server2-59473005");

//                WAEvent event = new WAEvent.Builder()
//                        .setDefaultEventName(WAEventType.LOGIN)
//                        .addDefaultEventValue(WAEventParameterName.LEVEL, 140)
//                        .build();
//                event.track(LoginActivity.this);

            }

            LogUtil.i(LogUtil.TAG, text);
            Toast.makeText(LoginActivity.this, text, Toast.LENGTH_LONG).show();
            cancelLoadingDialog();

            WASdkDemo.getInstance().updateLoginAccount(result);

            mResultCode = RESULT_OK;

            if (mAutoFinish) {
                exit();
            }
        }

        @Override
        public void onCancel() {
            cancelLoadingDialog();
            LogUtil.i(LogUtil.TAG, "Login canceled");
            Toast.makeText(LoginActivity.this, "Login canceled", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onError(int code, String message, WALoginResult result, Throwable throwable) {
            cancelLoadingDialog();
            String text = "code:" + code + "\nmessage:" + message;
            LogUtil.i(LogUtil.TAG, "Login failed->" + text);
            Toast.makeText(LoginActivity.this, "Login failed->" + text, Toast.LENGTH_LONG).show();
        }
    };

    public void exit() {
        setResult(mResultCode, new Intent());
        finish();
    }


    /**
     * Facebook登陆点击
     */
    public void fbLogin() {

        showLoadingDialog("正在登陆Facebook", null);

        WAUserProxy.login(this, WAConstants.CHANNEL_FACEBOOK, mLoginCallback, null);

//        JSONObject extInfoJson = new JSONObject();
//        try {
//            extInfoJson.putOpt("permissionType", "read");
//            JSONArray permissions = new JSONArray();
//            permissions.put("public_profile");
//            permissions.put("user_friends");
//            extInfoJson.putOpt("permissions", permissions);
////            extInfoJson.putOpt("permissionTYpe", "publish");
////            JSONArray permissions = new JSONArray();
////            permissions.put("publish_actions");
////            extInfoJson.putOpt("permissions", permissions);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        WAUserProxy.login(this, WAConstants.CHANNEL_FACEBOOK, mLoginCallback, extInfoJson.toString());
    }

    /**
     * Google登陆点击
     */
    public void googleLogin() {
        showLoadingDialog("正在登陆Google", null);
        WAUserProxy.login(this, WAConstants.CHANNEL_GOOGLE, mLoginCallback, null);
    }

    /**
     * 匿名登录点击
     */
    public void anonymousLogin() {
        showLoadingDialog("正在匿名登录", null);
        WAUserProxy.login(this, WAConstants.CHANNEL_WA, mLoginCallback, null);
    }

    /**
     * VK平台登录
     */
    public void vkLogin() {
        showLoadingDialog("正在登录VK", null);
        WAUserProxy.login(LoginActivity.this, WAConstants.CHANNEL_VK, mLoginCallback, null);
    }

    /**
     * Twitter平台登录
     */
    public void twitterLogin() {
        showLoadingDialog("正在登录Twitter", null);
        WAUserProxy.login(this, WAConstants.CHANNEL_TWITTER, mLoginCallback, null);
    }

    /**
     * Instagram平台登录
     */
    public void instagramLogin() {
        showLoadingDialog("正在登录Instagram", null);
        WAUserProxy.login(this, WAConstants.CHANNEL_INSTAGRAM, mLoginCallback, null);
    }

    /**
     * 应用内登录
     */
    public void appLogin() {
        JSONObject extObject = new JSONObject();
        try {
            extObject.putOpt("appSelfLogin", true);
            extObject.putOpt("puserId", "12345");
            extObject.putOpt("accessToken", "o1akkfjia81FMvFSO8kxC96TgQYlhEEr");
            extObject.putOpt("extInfo", "extInfo String");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        showLoadingDialog("应用内登录", null);
        WAUserProxy.login(this, WAConstants.CHANNEL_WA, mLoginCallback, extObject.toString());
//        WAUserProxy.login(this, "APPSELF", mLoginCallback, extObject.toString());
    }

    /**
     * 登出点击
     */
    public void logout() {
        Toast.makeText(getApplicationContext(), "Logout", Toast.LENGTH_LONG).show();
        WAUserProxy.logout();
        WASdkDemo.getInstance().logout();
//        WALoginResult loginAccount = WASdkDemo.getInstance().getLoginAccount();
//        if(null == loginAccount) {
//            return;
//        }
//        Map<String, Object> eventValues = new HashMap<>();
//        eventValues.put(GhwParameterName.USER_ID,  loginAccount.getUserId());
//        eventValues.put(GhwParameterName.SERVER_ID, "165");
//        eventValues.put(GhwParameterName.LEVEL, 0);
//
//        GhwTrackingSDK.track(this, new GhwEvent.Builder().setDefaultEventName("logout")
//                .setDefaultEventValues(eventValues).build());
//
//        GhwSdkDemo.getInstance().updateLoginAccount(null);

    }


}
