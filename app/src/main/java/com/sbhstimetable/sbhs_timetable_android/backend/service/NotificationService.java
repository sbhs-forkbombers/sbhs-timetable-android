package com.sbhstimetable.sbhs_timetable_android.backend.service;


import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.TimetableActivity;
import com.sbhstimetable.sbhs_timetable_android.api.ApiWrapper;
import com.sbhstimetable.sbhs_timetable_android.api.DateTimeHelper;
import com.sbhstimetable.sbhs_timetable_android.api.Day;
import com.sbhstimetable.sbhs_timetable_android.api.FullCycleWrapper;
import com.sbhstimetable.sbhs_timetable_android.api.Lesson;
import com.sbhstimetable.sbhs_timetable_android.api.StorageCache;
import com.sbhstimetable.sbhs_timetable_android.api.gson.Belltimes;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.PrefUtil;
import com.sbhstimetable.sbhs_timetable_android.event.BellsEvent;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatterBuilder;

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
	private FullCycleWrapper cycle;
	private StorageCache cache;
	private DateTimeHelper dth;
	private EventListener eventListener;
    public static boolean running;
	@Override
	public IBinder onBind(Intent intent) {
		// nope
		return null;
	}

	private void updateAllTheThings() {
		if (cache.shouldReloadBells())
			ApiWrapper.requestBells(this);
		if (cache.shouldReloadToday())
			ApiWrapper.requestToday(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			Log.wtf(TAG, "Started without an intent?");
			return START_NOT_STICKY;
		}
        running = true;
		Log.d(TAG, "Received start id " + startId + ": " + intent.getAction());
		if (intent.getAction().equals(ACTION_INITIALISE)) {
            Log.d(TAG, "init");
			this.showLoadingNotification();
			this.updateAllTheThings();
			this.showAppropriateNotification();
		} else if (intent.getAction().equals(ACTION_BELLTIMES)) {
            Log.d(TAG, "bells - " + dth.hasBells());
			dth.setBells(cache.loadBells());
			showAppropriateNotification();
		} else if (intent.getAction().equals(ACTION_UPDATE)) {
            Log.d(TAG, "update!");
			if (!dth.hasBells()) {
				this.updateAllTheThings();
				this.showLoadingNotification();
				return START_NOT_STICKY;
			}
			if (dth.getNextBell().isPeriodStart()) {
				// TODO I'm not entirely sure this is necessary
				showNextPeriodNotification();
			}
			int nextPeriod = (dth.getNextPeriod() == null ? 1 : dth.getNextPeriod().getPeriodNumber());
            /*if (dth.getNextBell() != null && dth.getNextPeriod() == null) {
                showEndOfDayNotification();
            } else */if (nextPeriod == 1 && !dth.hasBells()) {
				this.updateAllTheThings();
				this.showLoadingNotification();
			} else {
				showAppropriateNotification();
			}
		}

		//Toast.makeText(this.getApplicationContext(), "Hi there!", Toast.LENGTH_SHORT).show();
		Intent me = new Intent(this, this.getClass());
		me.setAction(ACTION_UPDATE);


		AlarmManager am = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
		if (alarm != null) am.cancel(alarm);
		PendingIntent soon = PendingIntent.getService(this, 0, me, 0);
		alarm = soon;
		Log.d(TAG, "Will wake up in " + (dth.getNextEvent().toDateTime().getMillis() - DateTime.now().getMillis() /*+ 5 * 60 * 1000*/) / 1000 + " seconds to update notification.");
		int type = AlarmManager.ELAPSED_REALTIME;
		long updateTime = SystemClock.elapsedRealtime() + (dth.getNextEvent().toDateTime().getMillis() - DateTime.now().getMillis());/*+ 5 * 60 * 1000*/
		am.set(type, updateTime, soon);
		return START_NOT_STICKY;
	}

	@Override
	public void onCreate() {
		mNM = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		this.cycle = new FullCycleWrapper(this);
		this.cache = new StorageCache(this);
		this.dth = new DateTimeHelper(this);
		this.eventListener = new EventListener(this);
		ApiWrapper.getEventBus().register(this.eventListener);
	}

	private void showAppropriateNotification() {
		if (!dth.hasBells()) return;
		if (dth.getNextPeriod() == null || dth.getNextPeriod().getPeriodNumber() == 1 && (!DateTimeHelper.after(LocalDateTime.now(), 9, 5) || DateTimeHelper.after(LocalDateTime.now(), 15, 15))) {
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
		return new NotificationCompat.Builder(this).setOngoing(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PrefUtil.NOTIFICATIONS_PERSISTENT, true))
				.setAutoCancel(false).setSmallIcon(R.mipmap.ic_notification_icon).setContentIntent(getStartAppPendingIntent())
				.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
	}

	private void showLoadingNotification() {
		NotificationCompat.Builder b = getBaseNotification();
		b.setProgress(3, 1, true);
		b.setContentTitle(getResources().getString(R.string.app_name));
		b.setContentText(getResources().getString(R.string.loading_data));
		b.setContentInfo("¯\\_(ツ)_/¯");
		b.setPriority(NotificationCompat.PRIORITY_MIN);
		mNM.notify(NOTIFICATION, b.build());
	}

	private void showNextPeriodNotification() {
		if (!this.dth.hasBells()) return; // belltimes are always needed to show a notification.
		NotificationCompat.Builder b = getBaseNotification();
		String topLine, bottomLine, sideLine = "";
		Belltimes.Bell nextPeriod;
		if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PrefUtil.NOTIFICATION_INCLUDE_BREAKS, false)) {
			nextPeriod = this.dth.getNextPeriod();
		} else {
			nextPeriod = this.dth.getNextBell();
		}
        if (nextPeriod == null) {
            showEndOfDayNotification();
            return;
        }
		if (this.cycle.ready() && nextPeriod.isPeriodStart()) {
			Lesson next = this.cycle.getToday().getPeriod(nextPeriod.getPeriodNumber());
			if (next.cancelled()) {
				topLine = "Free";
				bottomLine = "at " + nextPeriod.getBellDisplay();
			} else {
				topLine = next.getSubject() + " in " + next.getRoom();
				bottomLine = next.getTeacher() + " at " + nextPeriod.getBellDisplay();
			}
			sideLine = nextPeriod.getBellName();
		} else if (!nextPeriod.isPeriodStart()) {
			topLine = nextPeriod.getBellName();
			bottomLine = nextPeriod.getBellDisplay();
			sideLine = this.dth.getBells().getDayName() + " " + this.dth.getBells().getWeek();
		} else {
			topLine = nextPeriod.getBellName() + " at " + nextPeriod.getBellDisplay();
			bottomLine = getResources().getString(R.string.not_all_data);
		}

		b.setContentTitle(topLine);
		b.setContentText(bottomLine);
		b.setContentInfo(sideLine);
		b.setWhen(nextPeriod.getBellTime().getMillis());
		mNM.notify(NOTIFICATION, b.build());

	}

    private void showEndOfDayNotification() {
        if (!this.dth.hasBells()) return;
        NotificationCompat.Builder b = getBaseNotification();
        Belltimes.Bell eod = this.dth.getNextBell();
        b.setContentTitle(eod.getBellName());
        b.setContentText("at " + eod.getBellDisplay());
		b.setWhen(eod.getBellTime().getMillis());
        mNM.notify(NOTIFICATION, b.build());
    }

	private void showTomorrowNotification() {
		if (!this.dth.hasBells()) return;
		NotificationCompat.Builder b = getBaseNotification().setPriority(NotificationCompat.PRIORITY_MIN);
		String topLine, bottomLine, sideLine = "";
		//topLine = this.today.getDayName() + " Week " + this.today.get
		if (this.dth.getBells().isStatic()) {
			String day = new DateTimeFormatterBuilder().appendDayOfWeekText().toFormatter().print(dth.getNextSchoolDay());
			topLine = day + " " + cache.loadWeek();
		} else {
			topLine = this.dth.getBells().getDayName() + " Week " + this.dth.getBells().getWeek();
		}
		bottomLine = "";
		if (this.cycle.getToday() != null) {
			Day today = this.cycle.getToday();
			for (int i = 1; i < 6; i++) {
				 if (!today.getPeriod(i).isTimetabledFree()) {
					 Lesson p = today.getPeriod(i);
					 bottomLine += p.getSubject();
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
		b.setWhen(this.dth.getBells().getBellIndex(0).getBellTime().getMillis());
		mNM.notify(NOTIFICATION, b.build());
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "Destroyed.");
		mNM.cancel(NOTIFICATION);
		if (alarm != null) {
			((AlarmManager)this.getSystemService(Context.ALARM_SERVICE)).cancel(alarm);
			alarm = null;
		}
		ApiWrapper.getEventBus().unregister(this.eventListener);
        running = false;

	}

	public class EventListener {
		private Context con;
		public EventListener(Context c) {
			this.con = c;
		}
		private void startService(String action, Context c) {
			Intent i = new Intent(c, NotificationService.class);
			i.setAction(action);
			c.startService(i);
		}
		public void onEvent(BellsEvent b) {
			if (b.successful()) {
				this.startService(ACTION_BELLTIMES, con);
			}
		}
	}
}
