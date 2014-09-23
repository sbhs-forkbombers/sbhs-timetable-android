package com.sbhstimetable.sbhs_timetable_android;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.sbhstimetable.sbhs_timetable_android.backend.ApiAccessor;

import static com.sbhstimetable.sbhs_timetable_android.backend.ApiAccessor.baseURL;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setTintColor(Color.parseColor("#455ede"));
        tintManager.setStatusBarTintEnabled(true);
        setContentView(R.layout.activity_login);
        WebView wv = (WebView) findViewById(R.id.loginview);
        wv.setBackgroundColor(Color.parseColor("#000000"));
        wv.getSettings().setSaveFormData(true);
        final Activity me = this;
        //wv.getSettings().setJavaScriptEnabled(true);
        wv.setWebChromeClient(new WebChromeClient() {
            public void onProgressChange(WebView view, int progress) {
                me.setProgress(progress * 1000);
            }
        });
        wv.setWebViewClient(new WebViewClient() {
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (url.startsWith(baseURL) && (url.endsWith("/") || url.contains("mobile_loading"))) {
                    // this would be our website!
                    String[] cookies = CookieManager.getInstance().getCookie(baseURL).split("[;]");
                    for (String i : cookies) {
                        if (i.contains("SESSID")) {
                            String sessionID = i.split("=")[1];
                            ApiAccessor.finishedLogin(me, sessionID);
                            NavUtils.navigateUpFromSameTask(me);
                            view.clearCache(true);
                        }
                    }

                }
            }
        });
        //setContentView(wv);
        wv.loadUrl(baseURL + "/try_do_oauth?app=1");
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }
}
