package com.wa.sdk.demo.invite;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.wa.sdk.WAConstants;
import com.wa.sdk.common.WACommonProxy;
import com.wa.sdk.common.WASharedPrefHelper;
import com.wa.sdk.common.model.WACallback;
import com.wa.sdk.common.model.WAResult;
import com.wa.sdk.common.utils.LogUtil;
import com.wa.sdk.demo.R;
import com.wa.sdk.demo.WADemoConfig;
import com.wa.sdk.demo.base.BaseActivity;
import com.wa.sdk.demo.widget.TitleBar;
import com.wa.sdk.social.WASocialProxy;
import com.wa.sdk.social.model.WARequestSendResult;
import com.wa.sdk.user.model.WAUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Facebook邀请页面
 * Created by yinglovezhuzhu@gmail.com on 2016/7/14.
 */
public class FBInviteActivity extends BaseActivity {
    private static final String TAG = LogUtil.TAG + "_FBINVITE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fb_invite);

        TitleBar titleBar = (TitleBar) findViewById(R.id.tb_fb_invite);
        titleBar.setTitleText(R.string.fb_invite);
        titleBar.setLeftButton(android.R.drawable.ic_menu_revert, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        titleBar.setTitleTextColor(R.color.color_white);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(!WACommonProxy.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_fb_app_invite:
                String appLinkUrl = "https://fb.me/1666843733570117";
                String previewImageUrl = "https://lh3.googleusercontent.com/Q83SJEGMmWz_OBunjahU-RUtyJZJG8P3lbKAvrPB7q4F6UwMdWkvInvkCwTfYElanq4=w300-rw";
                WASocialProxy.appInvite(FBInviteActivity.this, WAConstants.CHANNEL_FACEBOOK,
                        appLinkUrl, previewImageUrl, new WACallback<WAResult>() {
                            @Override
                            public void onSuccess(int code, String message, WAResult result) {
                                showShortToast(null == message ? "App invite success" : message);
                            }

                            @Override
                            public void onCancel() {
                                showShortToast("App invite cancel");
                            }

                            @Override
                            public void onError(int code, String message, WAResult result, Throwable throwable) {
                                showShortToast("App invite error.\ncode: " + code
                                        + "\nmessage: " + (null == message ? "App invite error" : message));
                            }
                        });
                break;
            case R.id.btn_fb_game_service_invite:
//                startActivity(new Intent(FBInviteActivity.this, FBInviteFriendsActivity.class));
                WASocialProxy.sendRequest(this, WAConstants.CHANNEL_FACEBOOK, WAConstants.REQUEST_INVITE,
                        "Your friends invite you to join it",
                        "This is game is very funning, come and join with me!", null, null,
                         new WACallback<WARequestSendResult>() {
                            @Override
                            public void onSuccess(int code, String message, WARequestSendResult result) {
                                cancelLoadingDialog();
                                Toast.makeText(FBInviteActivity.this, "Invite send success", Toast.LENGTH_LONG).show();
                                WASocialProxy.createInviteRecord(WAConstants.CHANNEL_FACEBOOK,
                                        result.getRequestId(), result.getRecipients(), null);
                            }

                            @Override
                            public void onCancel() {
                                cancelLoadingDialog();
                                Toast.makeText(FBInviteActivity.this, "Invite canceled", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onError(int code, String message, WARequestSendResult result, Throwable throwable) {
                                cancelLoadingDialog();
                                Toast.makeText(FBInviteActivity.this, "Invite error:" + message, Toast.LENGTH_LONG).show();
                            }
                        }, null);
                break;
            case R.id.btn_fb_invite_install_reward:
                inviteInstallReward();
                break;
            case R.id.btn_fb_invite_event_reward:
                inviteEventReward();
                break;
            default:
                break;
        }
    }

    private void inviteInstallReward() {
        // 测试邀请奖励检查，邀请奖励检查必须是在Facebook登录的前提下
        showLoadingDialog(getString(R.string.loading), null);
        WASocialProxy.inviteInstallReward(FBInviteActivity.this, WAConstants.CHANNEL_FACEBOOK, new WACallback<WAResult>() {
            @Override
            public void onSuccess(int code, String message, WAResult result) {
                LogUtil.i(TAG, "Install invite reward success: " + message);
                cancelLoadingDialog();
                showShortToast(message);
            }

            @Override
            public void onCancel() {
                cancelLoadingDialog();
                LogUtil.i(TAG, "Install invite reward canceled: ");
            }

            @Override
            public void onError(int code, String message, WAResult result, Throwable throwable) {
                LogUtil.i(TAG, "Install invite reward error: " + message);
                cancelLoadingDialog();
                showShortToast("code:" + code
                        + "\nmessage:" + message);
            }
        });
    }

    private void inviteEventReward() {
        showLoadingDialog(getString(R.string.loading), null);
        WASocialProxy.inviteEventReward(WAConstants.CHANNEL_FACEBOOK, "purchase500", new WACallback<WAResult>() {
            @Override
            public void onSuccess(int code, String message, WAResult result) {
                LogUtil.i(TAG, "Invite event reward success: " + message);
                cancelLoadingDialog();
                showShortToast(message);
            }

            @Override
            public void onCancel() {
                LogUtil.i(TAG, "Invite event reward canceled: ");
                cancelLoadingDialog();
            }

            @Override
            public void onError(int code, String message, WAResult result, Throwable throwable) {
                LogUtil.i(TAG, "Invite event reward error: " + message);
                cancelLoadingDialog();
                showShortToast("code:" + code
                        + "\nmessage:" + message);
            }
        });
    }
}
