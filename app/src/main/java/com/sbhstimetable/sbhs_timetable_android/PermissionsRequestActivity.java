package com.sbhstimetable.sbhs_timetable_android;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.PrefUtil;
import com.sbhstimetable.sbhs_timetable_android.gapis.GoogleApiHelper;

public class PermissionsRequestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions_request);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!GoogleApiHelper.checkPermission(this, null)) {
            // need to request permission
            GoogleApiHelper.checkPermission(this, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == GoogleApiHelper.MY_PERMS_GEOFENCING_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                GoogleApiHelper.initialise(this);
            } else {
                Toast.makeText(this, "You cannot enable scan in reminders unless you grant SBHS Timetable location access.", Toast.LENGTH_LONG).show();
                PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(PrefUtil.GEOFENCING_ACTIVE, false).apply();
            }
        }
        finish();
    }

}
