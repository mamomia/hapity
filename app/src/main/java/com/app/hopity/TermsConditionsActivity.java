package com.app.hopity;


import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;


public class TermsConditionsActivity extends Activity implements View.OnClickListener {

    WebView legalWebView;
    private ImageView img_back;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_conditions);

        legalWebView = (WebView) findViewById(R.id.webView1);
        legalWebView.loadUrl("file:///android_asset/index.html");

        img_back = (ImageView) findViewById(R.id.img_terms_back);
        img_back.setOnClickListener(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onStop() {
        super.onStop();
        finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void onClick(View v) {
        finish();
    }
}
