package com.ganterpore.simplediet.View.Activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.SwitchPreferenceCompat;

import com.ganterpore.simplediet.Controller.NotificationReciever;
import com.ganterpore.simplediet.R;

import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {
    public static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //before leaving the settings, make sure the notifications are set
        updateNotifications();
    }

    /**
     * function is used to update the notifications when a user exits the settings page.
     * This will cancel any notifications that have been turned off, and set any turned on.
     */
    private void updateNotifications() {
        SharedPreferences preferences = getSharedPreferences(MainActivity.SHARED_PREFS_LOC, MODE_PRIVATE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if(preferences.getBoolean(NotificationReciever.MORNING_NOTIFICATION_CHANNEL, false)) {
            //if we are doing morning notifications, then set it up
            //creating notification intent
            Intent intent = new Intent(this, NotificationReciever.class);
            intent.putExtra("id", NotificationReciever.MORNING_NOTIFICATION_CHANNEL);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, NotificationReciever.MORNING_NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            //getting the time set for morning notifications
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, preferences.getInt("morning_notification_time_hour", 8));
            calendar.set(Calendar.MINUTE, preferences.getInt("morning_notification_time_minute", 30));
            calendar.set(Calendar.SECOND, 0);
            //if the time has already happened, set the day to tomorrow
            if(calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.setTimeInMillis(calendar.getTimeInMillis() + DateUtils.DAY_IN_MILLIS);
            }

            //setting up the notifications
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_HOUR*8, pendingIntent);
        } else {
            //if we are not doing morning alarms, confirm they are cancelled
            Intent intent = new Intent(this, NotificationReciever.class);
            intent.putExtra("id", NotificationReciever.MORNING_NOTIFICATION_CHANNEL);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, NotificationReciever.MORNING_NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.cancel(pendingIntent);
        }

        if(preferences.getBoolean(NotificationReciever.EVENING_NOTIFICATION_CHANNEL, false)) {
            //if we are doing morning notifications, then set it up
            //creating notification intent
            Intent intent = new Intent(this, NotificationReciever.class);
            intent.putExtra("id", NotificationReciever.EVENING_NOTIFICATION_CHANNEL);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, NotificationReciever.EVENING_NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            //getting the time set for morning notifications
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, preferences.getInt("evening_notification_time_hour", 20));
            calendar.set(Calendar.MINUTE, preferences.getInt("evening_notification_time_minute", 0));
            calendar.set(Calendar.SECOND, 0);
            //if the time has already happened, set the day to tomorrow
            if(calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.setTimeInMillis(calendar.getTimeInMillis() + DateUtils.DAY_IN_MILLIS);
            }

            //setting up the notifications
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        } else {
            //if we are not doing evening alarms, confirm they are cancelled
            Intent intent = new Intent(this, NotificationReciever.class);
            intent.putExtra("id", NotificationReciever.EVENING_NOTIFICATION_CHANNEL);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, NotificationReciever.EVENING_NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.cancel(pendingIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            PreferenceManager preferenceManager = getPreferenceManager();
            preferenceManager.setSharedPreferencesName(MainActivity.SHARED_PREFS_LOC);
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            final SwitchPreferenceCompat track_alcohol = findPreference("track_alcohol");
            final SwitchPreferenceCompat track_caffeine = findPreference("track_caffeine");
            findPreference("track_water").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if(!((Boolean) newValue)) {
                        track_alcohol.setChecked(false);
//                        getPreferenceManager().getSharedPreferences().edit().putBoolean("track_alcohol", false).apply();
                        track_caffeine.setChecked(false);
//                        getPreferenceManager().getSharedPreferences().edit().putBoolean("track_caffeine", false).apply();
                    } else {
                        track_alcohol.setChecked(true);
//                        getPreferenceManager().getSharedPreferences().edit().putBoolean("track_alcohol", true).apply();
                        track_caffeine.setChecked(true);
//                        getPreferenceManager().getSharedPreferences().edit().putBoolean("track_caffeine", true).apply();
                    }
                    return true;
                }
            });
        }


    }
}