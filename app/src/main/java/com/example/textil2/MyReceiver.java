package com.example.textil2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
    new NotificationHandler(context).send("Ideje várásolni");
    }
}