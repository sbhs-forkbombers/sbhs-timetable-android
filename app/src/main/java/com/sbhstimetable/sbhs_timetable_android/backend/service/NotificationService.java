package com.sbhstimetable.sbhs_timetable_android.backend.service;


import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class NotificationService extends Service {
	private static final String TAG = "NotificationService";

	@Override
	public IBinder onBind(Intent intent) {
		// nope
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "Received start id " + startId + ": " + intent);

		return START_NOT_STICKY;
	}

	@Override
	public void onCreate() {
		//Toast.makeText(this.getApplicationContext(), "Hi there!", Toast.LENGTH_SHORT).show();
		Log.w(TAG, "Created!");
	}
}
