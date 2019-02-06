package me.tseng.studios.tchores.java;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class TChoresBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Start the service that will manage the alarms

            TChoresService.enqueueSetAllChoreAlarms(context);;

        }
    }

}
