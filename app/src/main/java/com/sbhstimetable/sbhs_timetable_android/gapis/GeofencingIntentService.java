package com.sbhstimetable.sbhs_timetable_android.gapis;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.sbhstimetable.sbhs_timetable_android.R;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class GeofencingIntentService extends IntentService {


    public GeofencingIntentService() {
        super("GeofencingIntentService");
        Log.i("GeofencingIntentService", "DOOT DOOT222");
    }

    private final String TAG="GeofencingIntent";

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("GeofencingIntentService", "DOOT DOOT");
        if (intent != null) {
            /*final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }*/
            Log.i(TAG, "Got intent: " + intent.getAction());
            GeofencingEvent ev = GeofencingEvent.fromIntent(intent);
            if (ev.hasError()) {
                String msg = GeofenceStatusCodes.getStatusCodeString(ev.getErrorCode());
                Log.e(TAG, msg);
                return;
            }
            int transition = ev.getGeofenceTransition();

            NotificationManager m = (NotificationManager)this.getSystemService(NOTIFICATION_SERVICE);
            m.notify(987123, makeNotification(transition));

        }
    }

    private Notification makeNotification(int transition) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        switch (transition) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                builder.setContentTitle("Enter").setContentText("You have entered the danger zone").setSmallIcon(R.drawable.swag);
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                builder.setContentTitle("Dwell").setContentTitle("You are dwelling in the danger zone").setSmallIcon(R.drawable.swag);
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                builder.setContentTitle("Exit").setContentTitle("You have left the danger zone").setSmallIcon(R.drawable.swag);
                break;
        }
        return builder.build();
    }


}
