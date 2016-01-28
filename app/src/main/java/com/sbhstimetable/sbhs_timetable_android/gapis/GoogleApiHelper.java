package com.sbhstimetable.sbhs_timetable_android.gapis;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Arrays;

public class GoogleApiHelper implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {
    private static GoogleApiHelper INSTANCE;
    private Context cxt;
    private GoogleApiClient client;
    private static final double SBHS_LAT = -33.892398;
    private static final double SBHS_LON = 151.210911;
    private static final int FENCE_RADIUS = 100;
    public static final int MY_PERMS_GEOFENCING_REQUEST = 4; // chosen by fair dice roll. Guaranteed to be random.
    private Geofence sbhsGeofence;

    /**
     *
     * @param c - a context
     * @param a - null if you just want to know if we have permission
     * @return false if calling activity must wait to initialise, true if permissions are granted and we can go straight away
     */
    public static boolean checkPermission(Context c, Activity a) {
        if (ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (a == null) return false;
            // show an explanation
            Toast.makeText(c, "To get scan in reminders, you need to allow SBHS Timetable access to your location.", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(a, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMS_GEOFENCING_REQUEST);
            return false; // activity must wait to call initialise()
        }
        return true;
    }

    public static void initialise(Context c) {
        Log.i("GoogleApiHelper", "initialise(c).");

        INSTANCE = new GoogleApiHelper(c);
        Log.i("GoogleApiHelper", "INSTANCE = " + INSTANCE);
    }

    private GoogleApiHelper(Context c) {
        cxt = c;
        if (c == null) throw new IllegalArgumentException();
        Log.i("GoogleApiHelper", "In constructor");
        buildGoogleApiClient();
        //INSTANCE = this;

    }

    protected synchronized void buildGoogleApiClient() {
        Log.i("GoogleApiHelper", "Building API Client...");
        client = new GoogleApiClient.Builder(cxt)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        client.connect();
        Log.i("GoogleApiHelper", "" + client);
    }

    public static boolean ready() {
        return INSTANCE != null && INSTANCE.client.isConnected();
    }

    public static void disableApiClient() {
        if (ready()) {
            NotificationManager nm = (NotificationManager)INSTANCE.cxt.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(GeofencingIntentService.GEOFENCING_NOTIFICATION_ID);
            LocationServices.GeofencingApi.removeGeofences(INSTANCE.client, Arrays.asList(new String[] {"sbhs"}));
            INSTANCE.client.disconnect();
        }
    }

    public static GoogleApiClient getClient() {
        if (INSTANCE == null) return null;
        return INSTANCE.client;
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(sbhsGeofence);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(cxt, GeofencingIntentService.class);
        return PendingIntent.getService(cxt, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i("GoogleApiHelper", "Connected to Google APIs");
        // setup geofence
        sbhsGeofence = new Geofence.Builder()
                .setRequestId("sbhs")
                .setCircularRegion(
                        SBHS_LAT,
                        SBHS_LON,
                        FENCE_RADIUS
                ).setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();
        try {
            LocationServices.GeofencingApi.addGeofences(
                    client,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        } catch (SecurityException e) {
            // kek
            GeofencingIntentService.postPermissionsNotification(this.cxt);
        }
        Log.i("GoogleApiHelper", "Setup geofences!");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("GoogleApiHelper", "Connection suspended: " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("GoogleApiHelper", "Connection to Google APIs failed: " + connectionResult.getErrorMessage());
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.i("GoogleApiHelper", status + ", " + status.getStatusMessage());
    }
}
