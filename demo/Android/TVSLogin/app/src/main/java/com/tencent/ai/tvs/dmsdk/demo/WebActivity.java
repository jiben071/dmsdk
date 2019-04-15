package com.tencent.ai.tvs.dmsdk.demo;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.tencent.ai.dobbydemo.BuildConfig;
import com.tencent.ai.dobbydemo.R;
import com.tencent.ai.tvs.ConstantValues;
import com.tencent.ai.tvs.LoginProxy;
import com.tencent.ai.tvs.core.common.TVSDevice;
import com.tencent.ai.tvs.env.ELoginPlatform;
import com.tencent.ai.tvs.env.EUserAttrType;
import com.tencent.ai.tvs.ui.AndroidBug5497Workaround;
import com.tencent.ai.tvs.web.TVSWebController;
import com.tencent.ai.tvs.web.TVSWebView;

import org.json.JSONObject;

public class WebActivity extends ModuleActivity {
    private String LOG_TAG = "DMSDK_WebActivity";

    public static final int ACTIVITY_RESULT_CODE_FILECHOOSER = 1000;

    private ProgressBar mProgressBar;
    private EditText mURLEditText;

    private TVSWebController mWebViewController;
    private ImageButton mPresetButton;
    private ImageButton mGoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "onCreate");
        setContentView(R.layout.activity_web);
        mProgressBar = findViewById(R.id.progressBar);
        mURLEditText = findViewById(R.id.urlEditText);
        mPresetButton = findViewById(R.id.presetButton);
        registerForContextMenu(mPresetButton);
        mPresetButton.setOnClickListener(this::openContextMenu);
        mPresetButton.setLongClickable(false);
        mGoButton = findViewById(R.id.goButton);
        mGoButton.setOnClickListener(v -> mWebViewController.loadURL(mURLEditText.getText().toString()));

        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        AndroidBug5497Workaround.assistActivity(this);

        mWebViewController = ((TVSWebView) findViewById(R.id.tvsWebView)).getController();
        // TODO: Change hard-coded parameters here before running your demo
        String deviceType = "";
        String deviceOEM = "";
//        mWebViewController.setDeviceInfo(TVSDeviceBindType.TVS_SPEAKER, deviceType, deviceOEM, DemoConstant.PRODUCT_ID, DemoConstant.DSN);
        Intent intent = getIntent();
        TVSDevice tvsDevice = (TVSDevice) intent.getSerializableExtra("devInfo");
        String targetUrl = intent.getStringExtra("targetUrl");
        mURLEditText.setText(targetUrl == null ? "http://dingdang.qq.com": targetUrl);
        mWebViewController.setDeviceInfo(tvsDevice);
        mWebViewController.setUIEventListener(new DemoUIEventListener());
        mWebViewController.setBusinessEventListener(new DemoBusinessEventListener());
        mWebViewController.loadURL(mURLEditText.getText().toString());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.v(LOG_TAG, "onNewIntent");
        mWebViewController.loadURL(ConstantValues.VALID_USERCENTER_TESTENV_URL + ConstantValues.TSKAUTHMGR_URL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (LoginProxy.getInstance().handleQQOpenIntent(requestCode, resultCode, data)) {
            return;
        }
        if(requestCode == ACTIVITY_RESULT_CODE_FILECHOOSER) {
            mWebViewController.onPickFileResult(resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_web, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.context_web_preset, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!mWebViewController.goBack()) {
                    hideInputMethod();
                    finish();
                }
                return true;
            case R.id.backwardMenuItem:
                mWebViewController.goBack();
                return true;
            case R.id.stopMenuItem:
                mWebViewController.stop();
                return true;
            case R.id.reloadMenuItem:
                mWebViewController.reload();
                return true;
            case R.id.forwardMenuItem:
                mWebViewController.goForward();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // TODO: Change URL EditText
        switch (item.getItemId()) {
            case R.id.authMenuItem:
                mWebViewController.toPresetURL(EUserAttrType.AUTH);
                return true;
            case R.id.profileMenuItem:
                mWebViewController.toPresetURL(EUserAttrType.HOMEPAGE);
                return true;
            case R.id.memberRequestMenuItem:
                mWebViewController.toPresetURL(EUserAttrType.CPOPERATION);
                return true;
            case R.id.memberPurchaseMenuItem:
                mWebViewController.toPresetURL(EUserAttrType.RECHARGE);
                return true;
            case R.id.phoneMenuItem:
                mWebViewController.toPresetURL(EUserAttrType.INFOSETTING);
                return true;
            case R.id.qqMusicMenuItem:
                mWebViewController.toPresetURL(EUserAttrType.QQ_MUSIC);
                return true;
            case R.id.iotMenuItem:
                mWebViewController.toPresetURL(EUserAttrType.IOT);
                return true;
            case R.id.tskAuthMgrItem:
                mWebViewController.toPresetURL(EUserAttrType.TSKAUTHMGR);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!mWebViewController.goBack()) {
                finish();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        Log.v(LOG_TAG, "onDestroy");
        mWebViewController.onDestroy();
        super.onDestroy();
    }

    private void hideInputMethod() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mURLEditText.getWindowToken(), 0);
        }
    }

    private class DemoUIEventListener implements TVSWebController.UIEventListener {
        @Override
        public void requireUISettings(String settings) {
        }

        @Override
        public boolean shouldOverrideUrlLoading(String url) {
            return false;
        }

        @Override
        public void onReceiveTitle(String title) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setSubtitle(title);
            }
        }

        @Override
        public void onLoadProgress(int progress) {
            mProgressBar.setProgress(progress);
        }

        @Override
        public void onLoadStarted(String url) {
            mURLEditText.setText(url);
            mProgressBar.setAlpha(1);
        }

        @Override
        public void onLoadFinished(String url) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mProgressBar, "alpha", 0);
            animator.setDuration(500);
            animator.start();
        }

        @Override
        public void onLoadError() {
        }
    }

    private class DemoBusinessEventListener implements TVSWebController.BusinessEventListener {
        @Override
        public void requireCloseWebView() {
            finish();
        }

        @Override
        public void onPickFile(Intent fileChooser) {
            startActivityForResult(fileChooser, ACTIVITY_RESULT_CODE_FILECHOOSER);
        }

        @Override
        public void onReceiveProxyData(JSONObject data) {
        }

        @Override
        public void onLoginResult(ELoginPlatform platform, int errorCode) {
        }

        @Override
        public void onTokenRefreshResult(ELoginPlatform platform, int errorCode) {
        }
    }
}