package com.ganterpore.simplediet.Controller;

import android.text.format.DateUtils;

import com.ganterpore.simplediet.Model.DietPlan;
import com.ganterpore.simplediet.Model.Meal;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class WeeklyIntake {
    private double vegCount;
    private double proteinCount;
    private double dairyCount;
    private double grainCount;
    private double fruitCount;
    private double excessServes;

    private double waterCount;
    private double caffieneCount;
    private double alcoholCount;
    private double hydrationScore;

    private double totalCheats; //accumulated cheats for this week

    private double weeklyLimitVeg;
    private double weeklyLimitProtein;
    private double weeklyLimitDairy;
    private double weeklyLimitGrain;
    private double weeklyLimitFruit;

    private double weeklyLimitCaffiene;
    private double weeklyLimitAlcohol;
    private double weeklyLimitHydration;

    private double weeklyLimitCheats;
    private ArrayList<Meal> thisWeeksMeals;

    public WeeklyIntake(List<DocumentSnapshot> data, DietPlan dietPlan) {
        this(data, dietPlan, 0);
    }

    public WeeklyIntake(List<DocumentSnapshot> data, DietPlan dietPlan, int weeksAgo) {
        Date endDate = new Date(System.currentTimeMillis() - (weeksAgo * DateUtils.WEEK_IN_MILLIS) + DateUtils.DAY_IN_MILLIS);
        endDate = getStartOfDay(endDate);
        Date startDate = new Date(System.currentTimeMillis() - ((weeksAgo + 1) * DateUtils.WEEK_IN_MILLIS) + DateUtils.DAY_IN_MILLIS);
        startDate = getStartOfDay(startDate);

        setLimits(dietPlan);
        sortMeals(data, startDate, endDate);
        updateData();
    }

    private void setLimits(DietPlan dietPlan) {
        this.weeklyLimitVeg = dietPlan.getDailyVeges() * 7;
        this.weeklyLimitProtein = dietPlan.getDailyProtein() * 7;
        this.weeklyLimitDairy = dietPlan.getDailyDairy() * 7;
        this.weeklyLimitGrain = dietPlan.getDailyGrain() * 7;
        this.weeklyLimitFruit = dietPlan.getDailyFruit() * 7;
        this.weeklyLimitCaffiene = dietPlan.getWeeklyCaffeine();
        this.weeklyLimitAlcohol = dietPlan.getWeeklyAlcohol();
        this.weeklyLimitHydration = dietPlan.getDailyHydration() * 7;
        this.weeklyLimitCheats = dietPlan.getDailyCheats() * 7;//TODO get an actual weekly
    }

    /**
     * Takes a list of meals, and a list of days, and sorts the meals into the different days
     */
    private void sortMeals(List<DocumentSnapshot> meals, Date startDate, Date endDate) {
        thisWeeksMeals = new ArrayList<>();
        if(meals.isEmpty()) {
            return;
        }
        for(DocumentSnapshot mealDS : meals) {
            Meal meal = new Meal(mealDS);
            if(meal.getDay() >= startDate.getTime() && meal.getDay() < endDate.getTime()) {
                thisWeeksMeals.add(meal);
            }
        }
    }

    /**
     * given a list of documents of all the docMeals in the day, update the data
     * to reflect what is in the documents
     */
    private void updateData() {
        //reset all numbers to 0
        vegCount = 0;
        proteinCount = 0;
        dairyCount = 0;
        grainCount = 0;
        fruitCount = 0;
        waterCount = 0;
        caffieneCount = 0;
        alcoholCount = 0;
        hydrationScore = 0;
        excessServes = 0;
        totalCheats = 0;
        //iterate through all thisWeeksMeals and add the days data
        for(Meal meal : thisWeeksMeals) {
            totalCheats += meal.calculateTotalCheats();
            vegCount += meal.getVegCount();
            proteinCount += meal.getProteinCount();
            dairyCount += meal.getDairyCount();
            grainCount += meal.getGrainCount();
            fruitCount += meal.getFruitCount();
            waterCount += meal.getWaterCount();
            caffieneCount += meal.getCaffeineCount();
            alcoholCount += meal.getAlcoholStandards();
            hydrationScore += meal.getHydrationScore();
            excessServes += meal.getExcessServes();
        }
    }

    /**
     * get a date representing the start time of a given day
     * @param date, the date with which you want the start time of the day from
     * @return the date at the start of the given day
     */
    private Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        calendar.set(year, month, day, 0, 0, 0);
        return calendar.getTime();
    }

    public double getVegCount() {
        return vegCount;
    }

    public double getProteinCount() {
        return proteinCount;
    }

    public double getDairyCount() {
        return dairyCount;
    }

    public double getGrainCount() {
        return grainCount;
    }

    public double getFruitCount() {
        return fruitCount;
    }

    public double getExcessServes() {
        return excessServes;
    }

    public double getWaterCount() {
        return waterCount;
    }

    public double getCaffieneCount() {
        return caffieneCount;
    }

    public double getAlcoholCount() {
        return alcoholCount;
    }

    public double getHydrationScore() {
        return hydrationScore;
    }

    public double getTotalCheats() {
        return totalCheats;
    }

    public double getWeeklyLimitVeg() {
        return weeklyLimitVeg;
    }

    public double getWeeklyLimitProtein() {
        return weeklyLimitProtein;
    }

    public double getWeeklyLimitDairy() {
        return weeklyLimitDairy;
    }

    public double getWeeklyLimitGrain() {
        return weeklyLimitGrain;
    }

    public double getWeeklyLimitFruit() {
        return weeklyLimitFruit;
    }

    public double getWeeklyLimitCaffiene() {
        return weeklyLimitCaffiene;
    }

    public double getWeeklyLimitAlcohol() {
        return weeklyLimitAlcohol;
    }

    public double getWeeklyLimitHydration() {
        return weeklyLimitHydration;
    }

    public double getWeeklyLimitCheats() {
        return weeklyLimitCheats;
    }

    public ArrayList<Meal> getThisWeeksMeals() {
        return thisWeeksMeals;
    }
}
