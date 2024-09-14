package com.example.waterlanders.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.waterlanders.R;

public abstract class BaseActivity extends AppCompatActivity {

    private Handler handler;
    private Runnable retryRunnable;
    private final BroadcastReceiver networkChangeReceiver = new NetworkChangeReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isInternetAvailable()) {
            showNoInternetScreen();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkChangeReceiver);
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        }

        @SuppressLint("MissingPermission") NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    private void showNoInternetScreen() {
        // Inflate the no internet screen layout
        View noInternetView = LayoutInflater.from(this).inflate(R.layout.activity_no_internet_screen, null);

        // Set up the retry button
        Button retryButton = noInternetView.findViewById(R.id.button_retry);
        retryButton.setOnClickListener(v -> startRetryMechanism(noInternetView));

        // Add the no internet view to the activity
        ViewGroup contentView = findViewById(android.R.id.content);
        contentView.addView(noInternetView);
    }

    private void startRetryMechanism(View noInternetView) {
        if (handler == null) {
            handler = new Handler();
        }

        retryRunnable = new Runnable() {
            @Override
            public void run() {
                if (isInternetAvailable()) {
                    ViewGroup contentView = findViewById(android.R.id.content);
                    contentView.removeView(noInternetView);
                    recreate();
                } else {
                    handler.postDelayed(this, 3000);
                }
            }
        };

        handler.postDelayed(retryRunnable, 3000);
    }

    public void updateNetworkStatus() {
        if (!isInternetAvailable()) {
            showNoInternetScreen();
        } else {
            // Remove the no internet screen if internet becomes available
            ViewGroup contentView = findViewById(android.R.id.content);
            View noInternetView = contentView.findViewById(R.id.no_internet_layout); // Ensure this ID matches your no internet screen's ID

            if (noInternetView != null) {
                contentView.removeView(noInternetView);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && retryRunnable != null) {
            handler.removeCallbacks(retryRunnable);
        }
    }
}
