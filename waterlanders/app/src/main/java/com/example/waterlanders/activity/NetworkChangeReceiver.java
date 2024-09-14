package com.example.waterlanders.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (context instanceof BaseActivity) {
            ((BaseActivity) context).updateNetworkStatus();
        }
    }
}
