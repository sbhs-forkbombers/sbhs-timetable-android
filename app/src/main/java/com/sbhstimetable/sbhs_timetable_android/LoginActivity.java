package com.sbhstimetable.sbhs_timetable_android;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import static com.sbhstimetable.sbhs_timetable_android.backend.ApiAccessor.baseURL;

import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.backend.ApiAccessor;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView wv = new WebView(this);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        final Activity me = this;
        wv.getSettings().setJavaScriptEnabled(true);
        wv.setWebChromeClient(new WebChromeClient() {
            public void onProgressChange(WebView view, int progress) {
                me.setProgress(progress * 1000);
            }
        });
        wv.setWebViewClient(new WebViewClient() {
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.i("timetable", url + " baseURL is " + baseURL);
                if (url.contains(baseURL + "/mobile_loading")) {
                    // this would be our JSON!
                    String[] parts = url.split("/mobile_loading\\?sessionID=");
                    if (parts.length > 1) {
                        String sessionID = parts[1];
                        Log.e("timetable", "I GOTTSS IT! THE PRECIOUSSS! " + sessionID);
                        Toast.makeText(me, "Got your Session ID - " + sessionID, Toast.LENGTH_LONG);
                        ApiAccessor.finishedLogin(me, sessionID);
                        NavUtils.navigateUpFromSameTask(me);
                    }

                }
            }
        });
        setContentView(wv);
        wv.loadUrl(baseURL + "/try_do_oauth?app=1");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
