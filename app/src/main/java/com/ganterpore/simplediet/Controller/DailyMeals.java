package com.ganterpore.simplediet.Controller;

import android.text.format.DateUtils;

import com.ganterpore.simplediet.Model.Meal;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DailyMeals {
    private static final String TAG = "DailyMeals";
    public static final String MEALS = "Meals";

    private List<Meal> todaysMeals;

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

    private double totalCheats; //accumulated cheats for this day

    private Date date;

    /**
     * Constructor for getting todays meals
     */
    DailyMeals(MealDataSorter data) {
        //todays meals, aka zero days ago
        this(0, data);
    }

    /**
     * Constructor for getting meals from previous days
     * @param daysAgo, the number of days ago to get meals from
     */
    DailyMeals(int daysAgo, MealDataSorter data) {
        this.date = getStartOfDay(new Date(System.currentTimeMillis() - (daysAgo * DateUtils.DAY_IN_MILLIS)));
        if(data != null) {
            todaysMeals = data.getMealsOnDay(daysAgo);
        } else {
            todaysMeals = new ArrayList<>();
        }
        updateData();
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
        //iterate through all todaysMeals and add the days data
        for(Meal meal : todaysMeals) {
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

    public List<Meal> getMeals() {
        return todaysMeals;
    }

    public double getTotalServes() {
        return vegCount + proteinCount + dairyCount + grainCount
                + fruitCount + excessServes;
    }

    public double getServesOf(Meal.FoodType foodType) {
        switch (foodType) {
            case VEGETABLE:
                return getVegCount();
            case MEAT:
                return getProteinCount();
            case MILK:
            case DAIRY:
                return getDairyCount();
            case GRAIN:
                return getGrainCount();
            case FRUIT:
                return getFruitCount();
            case EXCESS:
                return getExcessServes();
            case WATER:
                return getWaterCount();
            case CAFFEINE:
                return getCaffieneCount();
            case ALCOHOL:
                return getAlcoholCount();
        }
        return 0;
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

    public double getExcessServes() {
        return excessServes;
    }

    public double getTotalCheats() {
        return totalCheats;
    }

    public Date getDate() {
        return date;
    }
}
