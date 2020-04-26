package com.ganterpore.simplediet.View.Activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.ganterpore.simplediet.Controller.NotificationReciever;
import com.ganterpore.simplediet.R;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class SettingsActivity extends Fragment {
    public static final String TAG = "SettingsActivity";

    private View settingsView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        settingsView = inflater.inflate(R.layout.activity_settings, container, false);
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        return settingsView;
    }

    /**
     * function is used to update the notifications when a user exits the settings page.
     * This will cancel any notifications that have been turned off, and set any turned on.
     */
    private static void updateNotifications(Activity activity) {
        SharedPreferences preferences = activity.getSharedPreferences(MainActivity.SHARED_PREFS_LOC, MODE_PRIVATE);
        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(ALARM_SERVICE);
        if(preferences.getBoolean(NotificationReciever.MORNING_NOTIFICATION_CHANNEL, false)) {
            //if we are doing morning notifications, then set it up
            //creating notification intent
            Intent intent = new Intent(activity, NotificationReciever.class);
            intent.putExtra("id", NotificationReciever.MORNING_NOTIFICATION_CHANNEL);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, NotificationReciever.MORNING_NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

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
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        } else {
            //if we are not doing morning alarms, confirm they are cancelled
            Intent intent = new Intent(activity, NotificationReciever.class);
            intent.putExtra("id", NotificationReciever.MORNING_NOTIFICATION_CHANNEL);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, NotificationReciever.MORNING_NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.cancel(pendingIntent);
        }

        if(preferences.getBoolean(NotificationReciever.EVENING_NOTIFICATION_CHANNEL, false)) {
            //if we are doing morning notifications, then set it up
            //creating notification intent
            Intent intent = new Intent(activity, NotificationReciever.class);
            intent.putExtra("id", NotificationReciever.EVENING_NOTIFICATION_CHANNEL);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, NotificationReciever.EVENING_NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

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
            Intent intent = new Intent(activity, NotificationReciever.class);
            intent.putExtra("id", NotificationReciever.EVENING_NOTIFICATION_CHANNEL);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, NotificationReciever.EVENING_NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.cancel(pendingIntent);
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
                        track_caffeine.setChecked(false);
                    } else {
                        track_alcohol.setChecked(true);
                        track_caffeine.setChecked(true);
                    }
                    return true;
                }
            });

            //making notification services update when preferences are changed
            Preference.OnPreferenceChangeListener updateNotificationsOnChange = new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    updateNotifications(getActivity());
                    return true;
                }
            };
            findPreference("morning_notifications").setOnPreferenceChangeListener(updateNotificationsOnChange);
            findPreference("morning_notification_time").setOnPreferenceChangeListener(updateNotificationsOnChange);
            findPreference("evening_notifications").setOnPreferenceChangeListener(updateNotificationsOnChange);
            findPreference("evening_notification_time").setOnPreferenceChangeListener(updateNotificationsOnChange);
        }


    }
}