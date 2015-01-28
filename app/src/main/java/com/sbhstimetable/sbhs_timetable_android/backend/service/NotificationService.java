package com.sbhstimetable.sbhs_timetable_android.backend.service;


import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
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
		Toast.makeText(this.getApplicationContext(), "Hi there!", Toast.LENGTH_SHORT).show();
		Intent me = new Intent(this, this.getClass());
		PendingIntent soon = PendingIntent.getService(this, 0, me, 0);
		AlarmManager am = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);

		am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 5000, soon);
		Log.i(TAG, "Set alarm for 5 seconds");
		return START_STICKY;
	}

	@Override
	public void onCreate() {

		Log.w(TAG, "Created!");

	}
}
