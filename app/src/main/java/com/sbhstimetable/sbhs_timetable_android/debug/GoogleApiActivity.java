package com.sbhstimetable.sbhs_timetable_android.debug;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.PrefUtil;
import com.sbhstimetable.sbhs_timetable_android.gapis.GoogleApiHelper;

public class GoogleApiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_api);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Button btn = ((Button)findViewById(R.id.checkstatus));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GoogleApiHelper.ready()) {
                    log("API is ready! " + GoogleApiHelper.ready());
                } else {
                    log("API is not ready.");
                }
            }
        });
        final Activity a = this;
        findViewById(R.id.initconn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log("Initialising...");
                if (GoogleApiHelper.checkPermission(view.getContext(), a)) {
                    GoogleApiHelper.initialise(view.getContext()); // else we're waiting for the user to grant permission
                    log("build() complete");
                } else {
                    log("Waiting for permission from user...");
                }
            }
        });

        findViewById(R.id.resetdate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreferenceManager.getDefaultSharedPreferences(view.getContext()).edit().putString(PrefUtil.GEOFENCE_LAST_NOTIFIED_DATE, "1970-01-01").commit();
                log("Commited change");
            }
        });

        findViewById(R.id.getapi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log("GoogleApiHelper.client = " + GoogleApiHelper.getClient());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == GoogleApiHelper.MY_PERMS_GEOFENCING_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                log("Permission granted!");
                GoogleApiHelper.initialise(this);
                log("Build complete");
            } else {
                log("Permission denied! Aborting.");
                Toast.makeText(this, "You cannot enable scan in reminders unless you grant SBHS Timetable location access.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void log(String s) {
        TextView t = ((TextView)findViewById(R.id.status));
        String newVal = t.getText() + "\n" + s;
        t.setText(newVal);
    }

}
