package com.example.mygpsapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigationrail.NavigationRailView;

public abstract class BaseActivity extends AppCompatActivity {

    protected NavigationRailView navigationRail;
    protected FloatingActionButton fabMainAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void initNavigationRail() {
        navigationRail = findViewById(R.id.navigation_rail);

        if (navigationRail != null && navigationRail.getHeaderView() != null) {
            fabMainAction = navigationRail.getHeaderView().findViewById(R.id.fab_main_action);
        }

        setupNavigationRail();
    }

    protected void setupToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
    }

    private void setupNavigationRail() {
        if (navigationRail == null) return;

        navigationRail.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_map) {
                navigateToMap();
            } else if (itemId == R.id.nav_weather) {
                navigateToWeather();
            } else if (itemId == R.id.nav_send_sms) {
                onSendSmsClicked();
            } else if (itemId == R.id.nav_save_map) {
                onSaveMapClicked();
            } else if (itemId == R.id.nav_share) {
                onShareClicked();
            }

            return true;
        });

        if (fabMainAction != null) {
            fabMainAction.setOnClickListener(v -> onFabClicked());
        }
    }

    private void navigateToMap() {
        if (!(this instanceof MainActivity)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
    }

    private void navigateToWeather() {
        if (!(this instanceof MyWeather)) {
            Intent intent = new Intent(this, MyWeather.class);
            startActivity(intent);
        }
    }

    protected void onSendSmsClicked() {
    }

    protected void onSaveMapClicked() {
    }

    protected void onShareClicked() {
    }

    protected void onFabClicked() {
    }

    protected void setNavigationItemSelected(int itemId) {
        if (navigationRail != null) {
            navigationRail.setSelectedItemId(itemId);
        }
    }
}
