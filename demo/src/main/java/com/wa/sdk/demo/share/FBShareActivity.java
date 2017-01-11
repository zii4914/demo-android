package com.wa.sdk.demo.share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.wa.sdk.WAConstants;
import com.wa.sdk.common.WACommonProxy;
import com.wa.sdk.common.WASharedPrefHelper;
import com.wa.sdk.common.model.WACallback;
import com.wa.sdk.common.model.WACallbackManagerImpl;
import com.wa.sdk.common.utils.FileUtil;
import com.wa.sdk.common.utils.LogUtil;
import com.wa.sdk.demo.R;
import com.wa.sdk.demo.WADemoConfig;
import com.wa.sdk.demo.base.BaseActivity;
import com.wa.sdk.demo.widget.TitleBar;
import com.wa.sdk.social.WASocialProxy;
import com.wa.sdk.social.model.WAShareLinkContent;
import com.wa.sdk.social.model.WAShareOpenGraphAction;
import com.wa.sdk.social.model.WAShareOpenGraphContent;
import com.wa.sdk.social.model.WAShareOpenGraphObject;
import com.wa.sdk.social.model.WASharePhoto;
import com.wa.sdk.social.model.WASharePhotoContent;
import com.wa.sdk.social.model.WAShareResult;
import com.wa.sdk.social.model.WAShareVideo;
import com.wa.sdk.social.model.WAShareVideoContent;

import java.io.File;

/**
 * Facebook分享页面
 * Created by yinglovezhuzhu@gmail.com on 2016/7/14.
 */
public class FBShareActivity extends BaseActivity {
    private static final String TAG = LogUtil.TAG + "_DEMO_SHARE";

    private boolean mShareWithApi = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fb_share);

        TitleBar tb = (TitleBar) findViewById(R.id.tb_fb_share);
        tb.setLeftButton(android.R.drawable.ic_menu_revert, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tb.setTitleText(R.string.share);
        tb.setTitleTextColor(R.color.color_white);

        WASharedPrefHelper sharedPrefHelper = WASharedPrefHelper.newInstance(this, WADemoConfig.SP_CONFIG_FILE_DEMO);
//        if (sharedPrefHelper.getBoolean("enable_logcat", true)) {
//            Logcat.enableLogcat(this);
//        }

    }

    private WACallback<WAShareResult> mShareCallback = new WACallback<WAShareResult>() {
        @Override
        public void onSuccess(int code, String message, WAShareResult result) {

            LogUtil.i(TAG, "Code:" + code + "<> Message:" + message + "Result:" + result.toString());
            Toast.makeText(FBShareActivity.this, "Code:" + code + "<> Message:" + message
                    + "Result:" + result.toString(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCancel() {
            Toast.makeText(FBShareActivity.this, "FacebookShare canceled", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onError(int code, String message, WAShareResult result, Throwable throwable) {
            Toast.makeText(FBShareActivity.this, "FacebookShare error: Code:" + code + "<>Message:"
                            + message + (null == throwable ? "" : "<>Throwable:" + throwable.toString()),
                    Toast.LENGTH_LONG).show();
            LogUtil.e(TAG, "FacebookShare error:" + (null == throwable ? "" : LogUtil.getStackTrace(throwable)));
        }
    };

    public void fbShareLink(View view) {
        mShareWithApi = Boolean.valueOf((String) view.getTag());
        WAShareLinkContent shareLinkContent = new WAShareLinkContent.Builder()
                .setContentTitle("Test share") // 分享标题
                .setContentDescription("Test Ghw share with facebook") // 分享文本内容
//                .setImageUri(Uri.parse("https://www.baidu.com/img/bdlogo.png"))
//                .setContentUri(Uri.parse("https://www.baidu.com/"))
                .setImageUri(Uri.parse("http://assets.bmob.cn/images/2014-data-service-icon.png")) // 缩略图地址
                .setContentUri(Uri.parse("http://www.bmob.cn/")) // 分享的链接
                .build();

        WASocialProxy.share(this, WAConstants.CHANNEL_FACEBOOK, shareLinkContent, mShareWithApi, null, mShareCallback);

    }

    /**
     * 分享图片
     *
     * @param view
     */
    public void fbSharePicture(View view) {
        mShareWithApi = Boolean.valueOf((String) view.getTag());
        pickImage();
    }

    /**
     * 分享视频
     *
     * @param view
     */
    public void fbShareVideo(View view) {
        mShareWithApi = Boolean.valueOf((String) view.getTag());
        pickVideo();
    }

    /**
     * 分享OpenGraph
     *
     * @param view
     */
    public void fbShareOpenGraph(View view) {
        mShareWithApi = Boolean.valueOf((String) view.getTag());

        // 构建一个OpenGraphObject对象
        WAShareOpenGraphObject object = new WAShareOpenGraphObject.Builder()
                .putString("og:type", "com_ghw_sdk:level")
                .putString("og:title", "A Game of Thrones")
                .putString("og:description", "In the frozen wastes to the north of Winterfell, sinister and supernatural forces are mustering.")
//                .putString("books:isbn", "0-553-57340-3")
                .putString("og:image", "http://pic.miercn.com/uploads/allimg/150907/85-150ZF92058.jpg")
                .build();

//        WASharePhoto photo = new WASharePhoto.Builder()
//                .setBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
//                .setUserGenerated(true)
//                .build();

        // 构建一个OpenGraphAction对象
        WAShareOpenGraphAction action = new WAShareOpenGraphAction.Builder()
                .setActionType("com_ghw_sdk:reach")
                .putObject("level", object)
//                .putPhoto("image", photo)
                .build();

        // 构建一个OpenGraphContent对象
        WAShareOpenGraphContent content = new WAShareOpenGraphContent.Builder()
                .setPreviewPropertyName("level")
                .setAction(action)
                .build();

        WASocialProxy.share(this, WAConstants.CHANNEL_FACEBOOK, content, mShareWithApi, null, mShareCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(WACallbackManagerImpl.RequestCodeOffset.PickImage.toRequestCode() == requestCode) {
            if(RESULT_OK == resultCode) {
                Uri uri = data.getData();
                if(null == uri) {
                    showShortToast("Share error: image data is null");
                    return;
                }
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                File imgFile = FileUtil.parseUriToFile(FBShareActivity.this, uri);
                Bitmap bm = BitmapFactory.decodeFile(imgFile.getPath(), options);
                if(null == bm) {
                    Toast.makeText(FBShareActivity.this, "Bitmap is null", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(FBShareActivity.this, "Bitmap is ：" + bm.getByteCount(), Toast.LENGTH_LONG).show();
                    LogUtil.e("Bitmap", "Bitmap byte count:" + bm.getByteCount());
                }
                // 构造图片内容对象
                WASharePhoto photo = new WASharePhoto.Builder()
                                .setImageUri(uri) // 图片本地Uri
//                                .setImageUri(Uri.fromFile(imgFile))
//                        .setImageUri(Uri.parse("http://attach.bbs.miui.com/forum/201311/24/215445jhkmukdk1p3urpur.jpg"))
//                                                .setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ghw_sdk_ic_anonymous))
//                        .setBitmap(bm)
                        .build();
                // 构造分享图片对象
                WASharePhotoContent sharePhotoContent = new WASharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();
                WASocialProxy.share(FBShareActivity.this, WAConstants.CHANNEL_FACEBOOK,
                        sharePhotoContent, mShareWithApi, null, mShareCallback);
            } else {
                showShortToast("Share canceled!");
            }
        } else if(WACallbackManagerImpl.RequestCodeOffset.PickVideo.toRequestCode() == requestCode) {
            if(RESULT_OK == resultCode) {
                Uri uri = data.getData();
                if (null == uri) {
                    showShortToast("Share error: video data is null");
                    return;
                }
                // 构建视频内容对象
                WAShareVideo video = new WAShareVideo.Builder()
                        .setLocalUri(uri)
                        .build();
                // 构建缩略图内容对象
                WASharePhoto photo = new WASharePhoto.Builder()
                        .setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                        .build();
                // 构建分享视频对象
                WAShareVideoContent shareVideoContent = new WAShareVideoContent.Builder()
                        .setContentTitle("TestVideo") // 标题
                        .setContentDescription("Test share video to facebook") // 文本内容描述
                        .setVideo(video) // 视频内容
                        .setPreviewPhoto(photo) // 缩略图内容
                        .build();
                WASocialProxy.share(FBShareActivity.this, WAConstants.CHANNEL_FACEBOOK,
                        shareVideoContent, mShareWithApi, null, mShareCallback);
            } else {
                showShortToast("Share canceled!");
            }
        } else {
            WACommonProxy.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, WACallbackManagerImpl.RequestCodeOffset.PickImage.toRequestCode());
    }

    private void pickVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, WACallbackManagerImpl.RequestCodeOffset.PickVideo.toRequestCode());
    }
}
