package com.sbhstimetable.sbhs_timetable_android.gapis;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.sbhstimetable.sbhs_timetable_android.PermissionsRequestActivity;
import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.api.DateTimeHelper;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.PrefUtil;
import com.sbhstimetable.sbhs_timetable_android.backend.service.NotificationDismissReceiver;

import org.joda.time.LocalDateTime;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class GeofencingIntentService extends IntentService {
    public static final int GEOFENCING_NOTIFICATION_ID = 987123;
    public static final int NOTIFICATION_NEED_PERMS_ID = 67;

    public GeofencingIntentService() {
        super("GeofencingIntentService");
        Log.i("GeofencingIntentService", "DOOT DOOT222");
    }

    private final String TAG="GeofencingIntent";

    private String getYYYYMMDD() {
        return DateTimeHelper.getYYYYMMDDFormatter().print(LocalDateTime.now());
    }

    private boolean hasNotifiedToday() {
        Log.i(TAG, getYYYYMMDD());
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getString(PrefUtil.GEOFENCE_LAST_NOTIFIED_DATE, "1970-01-01")
                .equals(getYYYYMMDD());

    }

    private void setNotifiedToday() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit().putString(PrefUtil.GEOFENCE_LAST_NOTIFIED_DATE, getYYYYMMDD())
                .apply();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("GeofencingIntentService", "DOOT DOOT");
        if (intent != null) {
            Log.i(TAG, "Got intent: " + intent.getAction());
            GeofencingEvent ev = GeofencingEvent.fromIntent(intent);
            if (ev.hasError()) {
                String msg = GeofenceStatusCodes.getStatusCodeString(ev.getErrorCode());
                Log.e(TAG, msg);
                return;
            }
            int transition = ev.getGeofenceTransition();
            if (!hasNotifiedToday()) {
                NotificationManager m = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
                m.notify(GEOFENCING_NOTIFICATION_ID, makeNotification());
                setNotifiedToday();
            } else {
                Log.i(TAG, "Already notified today, not going to do it again");
            }

        }
    }


    public static void fakeNotification(Context c) {
        NotificationManager m = (NotificationManager) c.getSystemService(NOTIFICATION_SERVICE);
        m.notify(GEOFENCING_NOTIFICATION_ID, makeNotification(c));
    }

    private Notification makeNotification() {
        return makeNotification(this);
    }

    private static Notification makeNotification(Context c) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(c)
                .setSmallIcon(R.drawable.barcode)
                .setLargeIcon(BitmapFactory.decodeResource(c.getResources(), R.mipmap.ic_launcher))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentTitle(c.getResources().getString(R.string.notification_scan_on_title))
                .setContentText(c.getResources().getString(R.string.notification_scan_on_text))
                .setContentIntent(
                        PendingIntent.getBroadcast(c, 0,
                                new Intent(NotificationDismissReceiver.ACTION_DISMISS_NOTIFICATION, null, c, NotificationDismissReceiver.class), 0)
                )
                .setOngoing(true);
        /*switch (transition) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                builder.setContentTitle("Enter").setContentText("You have entered the danger zone");
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                builder.setContentTitle("Dwell").setContentText("You are dwelling in the danger zone");
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                builder.setContentTitle("Exit").setContentText("You have left the danger zone");
                break;
        }*/
        String sound = PreferenceManager.getDefaultSharedPreferences(c).getString(PrefUtil.GEOFENCE_SOUND, "");
        if (!sound.equals("")) {
            builder.setSound(Uri.parse(sound));
        }
        if (PreferenceManager.getDefaultSharedPreferences(c).getBoolean(PrefUtil.GEOFENCE_VIBRATE, true)) {
            builder.setVibrate(new long[] {
                    0,
                    500,
                    150,
                    500,
                    100,
                    200,
                    100,
                    200,
                    100,
                    200
            });
        }
        return builder.build();
    }

    public static void postPermissionsNotification(Context c) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent i = new Intent(c, PermissionsRequestActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        PendingIntent pi = PendingIntent.getActivity(c, 0, i, 0);
        NotificationCompat.Builder n = new NotificationCompat.Builder(c)
                .setContentText(c.getString(R.string.notification_no_permissions_text))
                .setContentTitle(c.getString(R.string.notification_no_permissions_title))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setLargeIcon(BitmapFactory.decodeResource(c.getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.drawable.ic_warning_white_48dp);

        nm.notify(NOTIFICATION_NEED_PERMS_ID, n.build());
    }


}
