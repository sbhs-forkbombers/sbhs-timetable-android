package com.sbhstimetable.sbhs_timetable_android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class FeedbackDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final Context c = getActivity();
        builder.setMessage(R.string.pls2feedback).setPositiveButton(R.string.no_show, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                PreferenceManager.getDefaultSharedPreferences(c).edit().putBoolean(TimetableActivity.PREF_DISABLE_DIALOG, true).apply();
            }
        }).setNegativeButton(R.string.yes_show, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        return builder.create();
    }
}
