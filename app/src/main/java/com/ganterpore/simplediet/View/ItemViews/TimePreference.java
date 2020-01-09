package com.ganterpore.simplediet.View.ItemViews;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TimePicker;

import androidx.preference.DialogPreference;

import com.ganterpore.simplediet.R;

import java.util.Calendar;

/**
 * Preference object for use in the setting screen that creates a TimePicker object, when a settings
 * preferences needs to get a time.
 */
public class TimePreference extends DialogPreference implements TimePickerDialog.OnTimeSetListener {
    public final static String TAG = "TimePreference";
    private int lastHour;
    private int lastMinute;
    private int defaultHour;
    private int defaultMinute;
    private SharedPreferences preferences;

    public TimePreference(Context ctxt, AttributeSet attrs){//, int defStyle) {
        super(ctxt, attrs);

        TypedArray hourAttr = ctxt.obtainStyledAttributes(attrs, R.styleable.start_hour, 0, 0);
        TypedArray minuteAttr = ctxt.obtainStyledAttributes(attrs, R.styleable.start_minute , 0, 0);
        defaultHour = hourAttr.getInt(R.styleable.start_hour_start_hour, 0);
        defaultMinute = minuteAttr.getInt(R.styleable.start_minute_start_minute, 0);

        setPositiveButtonText("Set");
        setNegativeButtonText("Cancel");

        hourAttr.recycle();
        minuteAttr.recycle();
    }

    @Override
    protected void onClick() {
        TimePickerDialog dialog = new TimePickerDialog(getContext(), this, lastHour,lastMinute,DateFormat.is24HourFormat(getContext()));
        dialog.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        lastHour = hourOfDay;
        lastMinute = minute;
        preferences = getSharedPreferences();
        preferences.edit().putInt(getKey()+"_hour", hourOfDay)
                          .putInt(getKey()+"_minute",minute).apply();

        lastHour = preferences.getInt(getKey()+"_hour", 0);
        lastMinute = preferences.getInt(getKey()+"_minute", 0);
        callChangeListener(lastHour);
        notifyChanged();
    }

    @Override
    public CharSequence getSummary() {
        preferences = getSharedPreferences();
        lastHour = preferences.getInt(getKey()+"_hour", defaultHour);
        lastMinute = preferences.getInt(getKey()+"_minute", defaultMinute);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, lastHour);
        calendar.set(Calendar.MINUTE, lastMinute);
        return DateFormat.getTimeFormat(getContext()).format(calendar.getTime());
    }
}