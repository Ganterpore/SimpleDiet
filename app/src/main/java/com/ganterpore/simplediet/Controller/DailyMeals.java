package com.ganterpore.simplediet.Controller;

import android.support.annotation.NonNull;
import android.text.format.DateUtils;
import android.util.Log;

import com.ganterpore.simplediet.Model.Meal;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

public class DailyMeals {
    private static final String TAG = "DailyMeals";
    public static final String MEALS = "Meals";
    private FirebaseFirestore db;
    private List<DailyMealsInterface> listeners;

    private List<Meal> todaysMeals;
//    private List<Meal> yesterdaysMeals;
//    private List<Meal> theDayBeforesMeals;
    private List<Meal> thisWeeksMeals;
//    private List<Meal> tomorrowsMeals;

    private double vegCount;
    private double proteinCount;
    private double dairyCount;
    private double grainCount;
    private double fruitCount;
    private double waterCount;
    private double excessServes;

    private double totalCheats; //accumulated cheats for this day
    private double weeklyCheats; //accumulated cheats for the past week

    private Date date;

    /**
     * Constructor for getting todays meals
     */
    DailyMeals(DailyMealsInterface listener, String user) {
        //todays meals, aka zero days ago
        this(listener, user, 0);
    }

    /**
     * Constructor for getting meals from previous days
     * @param daysAgo, the number of days ago to get meals from
     */
    DailyMeals(DailyMealsInterface listener, String user, int daysAgo) {
        this(listener, user, new Date(System.currentTimeMillis() - (daysAgo * DateUtils.DAY_IN_MILLIS)));
    }

    /**
     * Get the daily update from meals on the given date
     * @param day, date to look at meals from
     */
    DailyMeals(DailyMealsInterface listener, String user, Date day) {
        listeners = new ArrayList<>();
        addListener(listener);

        //update date to start of the day, and get other important dates
        day = getStartOfDay(day);
        final Date nextDay = new Date(day.getTime() + DateUtils.DAY_IN_MILLIS);
        final Date endOfNextDay = new Date(day.getTime() + 2*DateUtils.DAY_IN_MILLIS);
        final Date yesterDay = new Date(day.getTime() - DateUtils.DAY_IN_MILLIS);
        final Date dayBeforeYest = new Date(day.getTime() - 2*DateUtils.DAY_IN_MILLIS);
        final Date weekAgo = new Date(day.getTime() - 7*DateUtils.DAY_IN_MILLIS);

        this.date = day;

        db = FirebaseFirestore.getInstance();
        CollectionReference mealsCollection = db.collection(MEALS);
        Query allMeals = mealsCollection.whereGreaterThan("day", weekAgo.getTime())
                            .whereLessThan("day", endOfNextDay.getTime())
                            .whereEqualTo("user", FirebaseAuth.getInstance().getCurrentUser().getUid());

        //add a snapshot listener to update the meals when the databse updates
        final Date finalDay = day;
        allMeals.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e != null) {
                    Log.e(TAG, "onEvent: exception " + e.getLocalizedMessage());
                } else {
                    List<DocumentSnapshot> meals = queryDocumentSnapshots.getDocuments();
                    sortMeals(meals, finalDay, nextDay, yesterDay, dayBeforeYest, weekAgo, endOfNextDay);
                    updateData();
                    updateListeners();
                }
            }
        });

        //get the query, and update when data recieved
        allMeals.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    List<DocumentSnapshot> meals = task.getResult().getDocuments();
                    sortMeals(meals, finalDay, nextDay, yesterDay, dayBeforeYest, weekAgo, endOfNextDay);
                    updateData();
                    updateListeners();
                }
            }
        });
//        dietPlan = new DietPlanWrapper(this, user);
    }

    private void sortMeals(List<DocumentSnapshot> meals, Date finalDay, Date nextDay, Date yesterDay, Date dayBeforeYest, Date weekAgo, Date endOfNextDay) {
        todaysMeals = new ArrayList<>();
//        yesterdaysMeals =  new ArrayList<>();
//        theDayBeforesMeals = new ArrayList<>();
        thisWeeksMeals = new ArrayList<>();
//        tomorrowsMeals = new ArrayList<>();
        for(DocumentSnapshot mealDS : meals) {
            Meal meal = new Meal(mealDS);
            if(meal.getDay() >= finalDay.getTime() && meal.getDay() < nextDay.getTime()) {
                todaysMeals.add(meal);
            }
//            if(meal.getDay() >= yesterDay.getTime() && meal.getDay() < finalDay.getTime()) {
//                yesterdaysMeals.add(meal);
//            }
//            if(meal.getDay() >= dayBeforeYest.getTime() && meal.getDay() < yesterDay.getTime()) {
//                theDayBeforesMeals.add(meal);
//            }
            if(meal.getDay() >= weekAgo.getTime() && meal.getDay() < nextDay.getTime()) {
                thisWeeksMeals.add(meal);
            }
//            if(meal.getDay() >= nextDay.getTime() && meal.getDay() < endOfNextDay.getTime()) {
//                tomorrowsMeals.add(meal);
//            }
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
            excessServes += meal.getExcessServes();
        }
        weeklyCheats = 0;
        int i =0;
        for(Meal meal : thisWeeksMeals) {
            i++;
            weeklyCheats += meal.calculateTotalCheats();
        }
    }

    public void addListener(DailyMealsInterface listener) {
        if(listener != null) {
            listeners.add(listener);
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

    public double getExcessServes() {
        return excessServes;
    }

    public double getTotalCheats() {
        return totalCheats;
    }

    public Date getDate() {
        return date;
    }

    public double getWeeklyCheats() {
        return weeklyCheats;
    }

    /**
     * update all listeners to te daily meals of a change
     */
    private void updateListeners() {
        for(DailyMealsInterface listener : listeners) {
            listener.updateDailyMeals(this);
        }
    }

    public interface DailyMealsInterface {
        /**
         * This method is called whenever the day object is updated
         * @param day, the day object that was updated
         */
        void updateDailyMeals(DailyMeals day);
    }



}
