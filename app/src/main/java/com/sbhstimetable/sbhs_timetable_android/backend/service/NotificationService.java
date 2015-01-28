package com.sbhstimetable.sbhs_timetable_android.backend.service;


import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.sbhstimetable.sbhs_timetable_android.R;

public class NotificationService extends Service {
	private static final String TAG = "NotificationService";
	private NotificationManager mNM;
	private PendingIntent alarm;
	private int NOTIFICATION = R.string.app_name; // it's unique, amirite?

	@Override
	public IBinder onBind(Intent intent) {
		// nope
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "Received start id " + startId + ": " + intent);
		this.showNotification();
		Toast.makeText(this.getApplicationContext(), "Hi there!", Toast.LENGTH_SHORT).show();
		Intent me = new Intent(this, this.getClass());


		AlarmManager am = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
		if (alarm != null) am.cancel(alarm);
		PendingIntent soon = PendingIntent.getService(this, 0, me, 0);
		alarm = soon;
		am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 5000, soon);
		Log.i(TAG, "Set alarm for 5 seconds");
		return START_STICKY;
	}

	@Override
	public void onCreate() {
		mNM = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		Log.w(TAG, "Created!");

	}

	private void showNotification() {
		Notification b = new NotificationCompat.Builder(this).setContentTitle("Swag").setContentInfo("Last updated at " + System.currentTimeMillis()).setSmallIcon(R.mipmap.ic_launcher).setOngoing(true).setAutoCancel(false).build();
		mNM.notify(NOTIFICATION ,b);
	}

	@Override
	public void onDestroy() {
		Log.w(TAG, "Destroyed!");
		mNM.cancel(NOTIFICATION);
		if (alarm != null) {
			((AlarmManager)this.getSystemService(Context.ALARM_SERVICE)).cancel(alarm);
			alarm = null;
		}
	}
}
