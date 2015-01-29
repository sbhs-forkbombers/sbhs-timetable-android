package com.sbhstimetable.sbhs_timetable_android.backend.service;


import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonParser;
import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.TimetableActivity;
import com.sbhstimetable.sbhs_timetable_android.backend.ApiAccessor;
import com.sbhstimetable.sbhs_timetable_android.backend.DateTimeHelper;
import com.sbhstimetable.sbhs_timetable_android.backend.json.BelltimesJson;
import com.sbhstimetable.sbhs_timetable_android.backend.json.TimetableJson;
import com.sbhstimetable.sbhs_timetable_android.backend.json.TodayJson;
// TODO retry later if no internet connection
// TODO respect sync on/off setting
public class NotificationService extends Service {
	public static final String ACTION_INITIALISE = "com.sbhstimetable.action.NotificationService.init";
	public static final String ACTION_BELLTIMES = "com.sbhstimetable.action.NotificationService.belltimes";
	public static final String ACTION_TIMETABLE = "com.sbhstimetable.action.NotificationService.timetable";
	public static final String ACTION_TODAY = "com.sbhstimetable.action.NotificationService.today";

	public static final String EXTRA_DATA = "com.sbhstimetable.data.json";

	private static final String ACTION_UPDATE = "com.sbhstimetable.action.NotificationService.update";
	private static final String TAG = "NotificationService";


	private NotificationManager mNM;
	private PendingIntent alarm;
	private int NOTIFICATION = R.string.app_name; // it's unique, amirite?
	private TodayJson today;
	private BelltimesJson belltimes;
	private TimetableJson timetable;
	private IntentReceiver intentReceiver;
	@Override
	public IBinder onBind(Intent intent) {
		// nope
		return null;
	}

	private void updateAllTheThings() {
		ApiAccessor.getToday(this);
		ApiAccessor.getBelltimes(this);
		ApiAccessor.getTimetable(this, true);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			Log.wtf(TAG, "Started without an intent?");
			return START_STICKY;
		}
		Log.i(TAG, "Received start id " + startId + ": " + intent.getAction());
		if (intent.getAction().equals(ACTION_INITIALISE)) {
			this.showLoadingNotification();
			this.updateAllTheThings();
		} else if (intent.getAction().equals(ACTION_BELLTIMES)) {
			this.belltimes = new BelltimesJson(new JsonParser().parse(intent.getStringExtra(EXTRA_DATA)).getAsJsonObject());
			DateTimeHelper.bells = this.belltimes;
			showAppropriateNotification();
		} else if (intent.getAction().equals(ACTION_TIMETABLE)) {
			this.timetable = new TimetableJson(new JsonParser().parse(intent.getStringExtra(EXTRA_DATA)).getAsJsonObject());
			showAppropriateNotification();
		} else if (intent.getAction().equals(ACTION_TODAY)) {
			this.today = new TodayJson(new JsonParser().parse(intent.getStringExtra(EXTRA_DATA)).getAsJsonObject());
			showAppropriateNotification();
		} else if (intent.getAction().equals(ACTION_UPDATE)) {
			if (belltimes == null || DateTimeHelper.getDateString(null) == null) {
				this.updateAllTheThings();
				this.showLoadingNotification();
				return START_STICKY;
			}
			int nextPeriod = belltimes.getNextPeriod().getPeriodNumber();
			if (nextPeriod == 1 && !DateTimeHelper.getDateString(null).equals(belltimes.getDateString())) {
				this.updateAllTheThings();
				this.showLoadingNotification();
			} else {
				showNextPeriodNotification();
			}
		}

		//Toast.makeText(this.getApplicationContext(), "Hi there!", Toast.LENGTH_SHORT).show();
		Intent me = new Intent(this, this.getClass());
		me.setAction(ACTION_UPDATE);


		AlarmManager am = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
		if (alarm != null) am.cancel(alarm);
		PendingIntent soon = PendingIntent.getService(this, 0, me, 0);
		alarm = soon;
		Log.i(TAG, "Will wake up in " + DateTimeHelper.milliSecondsUntilNextEvent() / 1000 + " seconds to update notification.");
		am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + DateTimeHelper.milliSecondsUntilNextEvent(), soon);
		return START_STICKY;
	}

	@Override
	public void onCreate() {
		mNM = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		IntentFilter filter = new IntentFilter(ApiAccessor.ACTION_TIMETABLE_JSON);
		filter.addAction(ApiAccessor.ACTION_TODAY_JSON);
		filter.addAction(ApiAccessor.ACTION_BELLTIMES_JSON);
		this.intentReceiver = new IntentReceiver();
		LocalBroadcastManager.getInstance(this).registerReceiver(this.intentReceiver, filter);


	}

	private void showAppropriateNotification() {
		if (belltimes == null) return;
		if (belltimes.getNextPeriod().getPeriodNumber() == 1) {
			showTomorrowNotification();
		} else {
			showNextPeriodNotification();
		}
	}

	private PendingIntent getStartAppPendingIntent() {
		Intent i = new Intent(this, TimetableActivity.class);
		return PendingIntent.getActivity(this, 0, i, 0);
	}

	private NotificationCompat.Builder getBaseNotification() {
		return new NotificationCompat.Builder(this).setOngoing(true).setAutoCancel(false)
				.setSmallIcon(R.mipmap.ic_notification_icon).setContentIntent(getStartAppPendingIntent())
				.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
	}

	private void showLoadingNotification() {
		NotificationCompat.Builder b = getBaseNotification();
		b.setProgress(3, 1, true);
		b.setContentTitle(getResources().getString(R.string.app_name));
		b.setContentText(getResources().getString(R.string.loading_data));
		b.setContentInfo("¯\\_(ツ)_/¯");
		mNM.notify(NOTIFICATION, b.build());
	}

	private void showNextPeriodNotification() {
		if (this.belltimes == null) return; // belltimes are always needed to show a notification.
		NotificationCompat.Builder b = getBaseNotification();
		String topLine, bottomLine, sideLine = "";
		BelltimesJson.Bell nextPeriod = belltimes.getNextPeriod();
		if (this.today != null) {
			TodayJson.Period next = this.today.getPeriod(nextPeriod.getPeriodNumber());
			topLine = next.name() + " in room " + next.room();
			bottomLine = next.teacher() + " at " + String.format("%02d:%02d", nextPeriod.getBell());
			sideLine = nextPeriod.getLabel();
		} else {
			topLine = nextPeriod.getLabel() + " at " + String.format("%02d:%02d", nextPeriod.getBell());
			bottomLine = getResources().getString(R.string.not_all_data);
		}

		b.setContentTitle(topLine);
		b.setContentText(bottomLine);
		b.setContentInfo(sideLine);
		mNM.notify(NOTIFICATION, b.build());

	}

	private void showTomorrowNotification() {
		if (this.belltimes == null) return;
		NotificationCompat.Builder b = getBaseNotification();
		String topLine, bottomLine, sideLine = "";
		topLine = this.belltimes.getDayName() + " Week " + this.belltimes.getWeekInTerm() + this.belltimes.getWeekLetter();
		bottomLine = "";
		if (this.today != null) {
			for (int i = 1; i < 6; i++) {
				 if (!today.getPeriod(i).isFree()) {
					 TodayJson.Period p = today.getPeriod(i);
					 bottomLine += p.name();
				 } else {
					 bottomLine += "Free";
				 }
				if (i < 5) {
					bottomLine += ", ";
				}
				if (i == 4) {
					bottomLine += "and ";
				}
			}
		}
		b.setContentTitle(topLine);
		b.setContentText(bottomLine);
		b.setContentInfo(sideLine);
		mNM.notify(NOTIFICATION, b.build());
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "Destroyed.");
		mNM.cancel(NOTIFICATION);
		if (alarm != null) {
			((AlarmManager)this.getSystemService(Context.ALARM_SERVICE)).cancel(alarm);
			alarm = null;
		}
		LocalBroadcastManager.getInstance(this).unregisterReceiver(this.intentReceiver);
		this.intentReceiver = null;
	}

	/**
	 * This class receives intents when belltimes, today and timetable are available and tells the NotificationService to update itself accordingly
	 */
	public class IntentReceiver extends BroadcastReceiver {
		private void startService(String action, String data, Context c) {
			Intent i = new Intent(c, NotificationService.class);
			i.setAction(action);
			i.putExtra(EXTRA_DATA, data);
			c.startService(i);
		}
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("Intentreceiver", "Got intent " + intent);
			if (intent.getAction().equals(ApiAccessor.ACTION_TODAY_JSON)) {
				startService(ACTION_TODAY, intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA), context);
			} else if (intent.getAction().equals(ApiAccessor.ACTION_BELLTIMES_JSON)) {
				startService(ACTION_BELLTIMES, intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA), context);
			} else if (intent.getAction().equals(ApiAccessor.ACTION_TIMETABLE_JSON)) {
				startService(ACTION_TIMETABLE, intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA), context);
			}
		}
	}
}
