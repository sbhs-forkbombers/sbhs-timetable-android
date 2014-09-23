package com.sbhstimetable.sbhs_timetable_android.backend.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.backend.ApiAccessor;
import com.sbhstimetable.sbhs_timetable_android.backend.DateTimeHelper;
import com.sbhstimetable.sbhs_timetable_android.backend.json.BelltimesJson;
import com.sbhstimetable.sbhs_timetable_android.backend.json.TodayJson;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class NotificationService extends IntentService {
    private static final String ACTION_CHECK_UPDATES = "com.sbhstimetable.sbhs_timetable_android.backend.service.action.CHECK_UPDATES";
    private static final String ACTION_UPDATE_NOTIFICATION = "com.sbhstimetable.sbhs_timetable_android.backend.service.action.UPDATE_NOTIFICATION";

    private static final String EXTRA_SESSID = "com.sbhstimetable.sbhs_timetable_android.backend.service.extra.PARAM1";
    private static final String EXTRA_SHOULD_DO_ALL = "com.sbhstimetable.sbhs_timetable_android.backend.service.extra.PARAM2";

    public static final int NOTIFICATION_NEXT = 1;

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService

    public static void startCheckUpdate(Context context, String sessID, boolean doEverything) {
        Intent intent = new Intent(context, NotificationService.class);
        intent.setAction(ACTION_CHECK_UPDATES);
        intent.putExtra(EXTRA_SESSID, sessID);
        intent.putExtra(EXTRA_SHOULD_DO_ALL, doEverything);
        context.startService(intent);
    }*/

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startUpdatingNotification(Context context, String param1) {
        Intent intent = new Intent(context, NotificationService.class);
        intent.setAction(ACTION_UPDATE_NOTIFICATION);
        intent.putExtra(EXTRA_SESSID, param1);
        context.startService(intent);
    }

    public NotificationService() {
        super("NotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CHECK_UPDATES.equals(action)) {
                final boolean doAll = intent.getBooleanExtra(EXTRA_SHOULD_DO_ALL, false);
                handleCheckUpdate(doAll);
            } else if (ACTION_UPDATE_NOTIFICATION.equals(action)) {
                final String sessID = intent.getStringExtra(EXTRA_SESSID);
                handleNotificationUpdate(sessID);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleCheckUpdate(boolean all) {
        ApiAccessor.load(this);
        ApiAccessor.getToday(this);
        if (all) {
            ApiAccessor.getNotices(this);
            ApiAccessor.getBelltimes(this);
        }
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleNotificationUpdate(final String param1) {
        NotificationManager m = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("notifications_enable", false)) {
            // only show a notification if it's configured
            m.cancel(NOTIFICATION_NEXT);
            return;
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher);
        if (BelltimesJson.getInstance() == null) return;
        BelltimesJson.Bell next = BelltimesJson.getInstance().getNextBell();
        if (next == null) {
            // Don't show anything?
            m.cancel(NOTIFICATION_NEXT);
            return;
        }
        String title = next.getLabel();
        Integer[] b = next.getBell();
        b[0] = b[0] % 12;
        if (b[0] == 0) b[0] = 12;
        String subText = "at " + String.format("%02d:%02d", b);
        subText += (next.getBell()[0] >= 12 ? "pm" : "am");
        if (next.isPeriod() && TodayJson.getInstance() != null) {
            TodayJson.Period p = TodayJson.getInstance().getPeriod(next.getPeriodNumber());
            title = p.name() + " in " + p.room();
            subText += " with " + p.fullTeacher();
        }
        builder.setContentTitle(title);
        builder.setContentText(subText);


        ScheduledExecutorService ses = new ScheduledThreadPoolExecutor(1);
        m.notify(NOTIFICATION_NEXT, builder.build());
        final Context con = this;
        ses.schedule(new Runnable() {
            @Override
            public void run() {
                NotificationService.startUpdatingNotification(con, param1);
            }
        }, DateTimeHelper.milliSecondsUntilNextEvent(), TimeUnit.MILLISECONDS);
    }
}
