package com.ganterpore.simplediet.Controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WeeklyMeals implements DailyMealsInterface{
    private List<DailyMeals> week;
    private  List<WeeklyMealsInterface> listeners;

    private int weeklyCheats;

    public WeeklyMeals(WeeklyMealsInterface listener) {
        this(listener, 0);
    }
    public WeeklyMeals(WeeklyMealsInterface listener, int daysOffset) {
        listeners = new ArrayList<>();
        addListener(listener);

        week = new ArrayList<>();
        for(int daysAgo=daysOffset;daysAgo<7+daysOffset;daysAgo++) {
            week.add(new DailyMeals(this, daysAgo));
        }
    }

    public List<DailyMeals> getWeek() {
        return week;
    }

    public int getWeeklyCheats() {
        return weeklyCheats;
    }

    @Override
    public void updateDailyMeals(DailyMeals updatedDay) {
        weeklyCheats = 0;
        for(DailyMeals day : week) {
            weeklyCheats += day.getTotalCheats();
        }
        updateListeners();
    }

    public void addListener(WeeklyMealsInterface listener) {
        if(listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * update all listeners to the weekly meals of a change
     */
    private void updateListeners() {
        for(WeeklyMealsInterface listener : listeners) {
            listener.updateWeeklyMeals(this);
        }
    }
}
