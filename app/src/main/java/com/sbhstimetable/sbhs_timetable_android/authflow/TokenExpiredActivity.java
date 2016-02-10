package com.sbhstimetable.sbhs_timetable_android.authflow;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.api.ApiWrapper;

public class TokenExpiredActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ApiWrapper.hasLaunchedTokenExpired = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_expired);
        if (this.getIntent().getBooleanExtra("firstTime", false)) {
            // change text
            ((TextView) this.findViewById(R.id.title)).setText(R.string.title_first_login);
            ((TextView) this.findViewById(R.id.explanation)).setText(R.string.first_login_explain);
            ((ImageView) this.findViewById(R.id.notameme)).setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.swag));
        }
        this.findViewById(R.id.button_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(new Intent(v.getContext(), LoginActivity.class));

            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ApiWrapper.hasLaunchedTokenExpired = false;
    }
}
