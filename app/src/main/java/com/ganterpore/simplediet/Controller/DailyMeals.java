package com.ganterpore.simplediet.Controller;

import android.support.annotation.NonNull;
import android.text.format.DateUtils;
import android.util.Log;

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
    private FirebaseFirestore db;
    private List<DailyMealsInterface> listeners;
    private List<DocumentSnapshot> meals;

    private double vegCount;
    private double proteinCount;
    private double dairyCount;
    private double grainCount;
    private double fruitCount;
    private double waterCount;
    private double excessServes;

    private double totalCheats;

    /**
     * Constructor for getting todays meals
     */
    public DailyMeals(DailyMealsInterface listener) {
        //todays meals, aka zero days ago
        this(listener, 0);
    }

    /**
     * Constructor for getting meals from previous days
     * @param daysAgo, the number of days ago to get meals from
     */
    public DailyMeals(DailyMealsInterface listener, int daysAgo) {
        this(listener, new Date(System.currentTimeMillis() - (daysAgo * DateUtils.DAY_IN_MILLIS)));
    }

    /**
     * Get the daily update from meals on the given date
     * @param day, date to look at meals from
     */
    public DailyMeals(DailyMealsInterface listener, Date day) {
        listeners = new ArrayList<>();
        addListener(listener);

        //get tomorrows date
        Date nextDay = new Date(day.getTime() + DateUtils.DAY_IN_MILLIS);
        //update the days to the start of each day
        day = getStartOfDay(day);
        nextDay = getStartOfDay(nextDay);

        //access documents from the database from between these two times
        db = FirebaseFirestore.getInstance();
        CollectionReference meals = db.collection("Meals");
        Query todaysMeals = meals.whereGreaterThanOrEqualTo("day", day.getTime())
                .whereLessThan("day", nextDay.getTime()).whereEqualTo("user", FirebaseAuth.getInstance().getCurrentUser().getUid());

        //add a snapshot listener to update the meals when the databse updates
        todaysMeals.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e != null) {
                    Log.e(TAG, "onEvent: exception " + e.getLocalizedMessage());
                } else {
                    List<DocumentSnapshot> meals = queryDocumentSnapshots.getDocuments();
                    getDataFromDocuments(meals);
                    updateListeners();
                }
            }
        });
        //get the query, and update when data recieved
        todaysMeals.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    List<DocumentSnapshot> meals = task.getResult().getDocuments();
                    getDataFromDocuments(meals);
                    updateListeners();
                }
            }
        });
    }

    public void addListener(DailyMealsInterface listener) {
        if(listener != null) {
            listeners.add(listener);
        }
    }

    public List<DocumentSnapshot> getMeals() {
        return meals;
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

    /**
     * given a list of documents of all the meals in the day, update the data
     * to reflect what is in the documents
     * @param meals, a list of FirestoreDcument's of the days meals
     */
    private void getDataFromDocuments(List<DocumentSnapshot> meals) {
        this.meals = meals;
        //reset all numbers to 0
        vegCount = 0;
        proteinCount = 0;
        dairyCount = 0;
        grainCount = 0;
        fruitCount = 0;
        waterCount = 0;
        excessServes = 0;
        totalCheats = 0;
        //iterate through all meals and add the days data
        for(DocumentSnapshot meal : meals) {
            double serveCount = 0;
            double cheatScore = meal.getDouble("cheatScore");
            double mealVegCount  = meal.getDouble("vegCount");
            double mealProteinCount = meal.getDouble("proteinCount");
            double mealDairyCount = meal.getDouble("dairyCount");
            double mealGrainCount = meal.getDouble("grainCount");
            double mealFruitCount = meal.getDouble("fruitCount");
            double mealWaterCount = meal.getDouble("waterCount");
            double mealExcessServes = meal.getDouble("excessServes");

            serveCount = mealVegCount + mealProteinCount + mealDairyCount
                    + mealGrainCount + mealFruitCount + mealWaterCount + mealExcessServes;
            totalCheats += cheatScore * serveCount;
            vegCount += mealVegCount;
            proteinCount += mealProteinCount;
            dairyCount += mealDairyCount;
            grainCount += mealGrainCount;
            fruitCount += mealFruitCount;
            waterCount += mealWaterCount;
            excessServes += mealExcessServes;
        }
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
}
