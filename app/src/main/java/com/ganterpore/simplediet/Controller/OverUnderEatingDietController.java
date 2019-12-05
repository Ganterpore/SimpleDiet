package com.ganterpore.simplediet.Controller;

import android.util.SparseArray;

import com.ganterpore.simplediet.Model.DietPlan;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

public class OverUnderEatingDietController implements DietController, DailyMeals.DailyMealsInterface {
    private DietControllerListener listener;
    private String user;
    private DailyMeals todaysMeals;
    private DietPlan overallDiet;
    private FirebaseFirestore db;
    private SparseArray<DailyMeals> daysAgoMeals;
    private SparseArray<DietPlan> daysAgoDiets;

    public OverUnderEatingDietController(DietControllerListener listener) {
        //initialising variables
        this.db = FirebaseFirestore.getInstance();
        this.listener = listener;
        this.user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        todaysMeals = new DailyMeals(this, user);
        overallDiet = new DietPlan();
        getCurrentDietPlanFromDB();
        daysAgoDiets = new SparseArray<>();
        daysAgoMeals = new SparseArray<>();
        daysAgoMeals.append(0, todaysMeals);
        //adding the diet plan to be tracked (todays), then refresh diet plans so it updates.
        daysAgoDiets.append(0, new DietPlan());
        refreshDietPlans();
    }

    /**
     * Updates the diet plan to the current one on the db for the user.
     */
    private void getCurrentDietPlanFromDB() {
        //document reference to the diet plan
        DocumentReference dietPlanQuery = db.collection(DietPlan.COLLECTION_NAME).document(user);
        //getting the data and updating
        dietPlanQuery.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        overallDiet = documentSnapshot.toObject(DietPlan.class);
                        updateListener();
                    }
                });
        //making sure any changes to the data are tracked
        dietPlanQuery.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                overallDiet = documentSnapshot.toObject(DietPlan.class);
                updateListener();
            }
        });
    }

    @Override
    public DailyMeals getTodaysMeals() {
        return todaysMeals;
    }
    @Override
    public DailyMeals getDaysMeals(int nDaysAgo) {
        DailyMeals daysMeal = daysAgoMeals.get(nDaysAgo);
        if(daysMeal == null) {
            daysMeal = new DailyMeals(this, user, nDaysAgo);
            daysAgoMeals.append(nDaysAgo, daysMeal);
        }
        return daysMeal;
    }

    @Override
    public DietPlan getOverallDietPlan() {
        return overallDiet; //For the BasicDietController, both todays and the overall diet plan are the same
    }
    @Override
    public DietPlan getTodaysDietPlan() {
        return getDaysDietPlan(0);
    }
    @Override
    public DietPlan getDaysDietPlan(int nDaysAgo) {
        DietPlan daysDiet = daysAgoDiets.get(nDaysAgo);
        if(daysDiet == null) {
            daysAgoDiets.append(nDaysAgo, new DietPlan());
            refreshDietPlans();
        }
        return daysAgoDiets.get(nDaysAgo);
    }

    /**
     * updates all the DietPlans that have been looked at so far by the Controller, and updates their values.
     * should be called when an update to the overall DietPlan or any DailyMeals has occurred.
     */
    private void refreshDietPlans() {
        //for each days diet in the list, refresh the diet
        for(int i=0;i<daysAgoDiets.size();i++) {
            int nDaysAgo = daysAgoDiets.keyAt(i);
            //get the meals from the previous two days
            DailyMeals dayBeforesMeals = getDaysMeals(nDaysAgo + 1);
            DailyMeals twoDaysBeforeMeals = getDaysMeals(nDaysAgo + 2);

            //adjust the diet counts based on the last few days meals
            double vegCountAdjusted = adjustTodaysDiet(dayBeforesMeals.getVegCount(), twoDaysBeforeMeals.getVegCount(), overallDiet.getDailyVeges());
            double proteinCountAdjusted = adjustTodaysDiet(dayBeforesMeals.getProteinCount(), twoDaysBeforeMeals.getProteinCount(), overallDiet.getDailyProtein());
            double dairyCountAdjusted = adjustTodaysDiet(dayBeforesMeals.getDairyCount(), twoDaysBeforeMeals.getDairyCount(), overallDiet.getDailyDairy());
            double grainCountAdjusted = adjustTodaysDiet(dayBeforesMeals.getGrainCount(), twoDaysBeforeMeals.getGrainCount(), overallDiet.getDailyGrain());
            double fruitCountAdjusted = adjustTodaysDiet(dayBeforesMeals.getFruitCount(), twoDaysBeforeMeals.getFruitCount(), overallDiet.getDailyFruit());

            //create new dietPlan
            DietPlan newDiet = new DietPlan(vegCountAdjusted, proteinCountAdjusted,
                    dairyCountAdjusted, grainCountAdjusted, fruitCountAdjusted,
                    overallDiet.getDailyWater(), overallDiet.getWeeklyCheats(), overallDiet.getUser());

            //replace the diet plan with the new one
            daysAgoDiets.put(nDaysAgo, newDiet);
        }

    }

    /**
     * looks at the amount of food eaten in the past two days of the particular food group, versus
     * the expected amount and comes up with a recommended amount to eat on this day
     * @param yesterdaysCount the count of the food group yesterday
     * @param dayBeforesCount te count of the food group the day before yesterday
     * @param expectedCount the expected food count
     * @return the adjusted amount of the food group
     */
    private double adjustTodaysDiet(double yesterdaysCount, double dayBeforesCount, double expectedCount) {
        //normalised values is the difference between the count and the expected count
        double yesterdayNormalised = yesterdaysCount - expectedCount;
        double dayBeforeNormalised = dayBeforesCount - expectedCount;
        //adjuster is how much the days food should be adjusted by
        double adjuster;
        //in general, this is just the negative of amount of food over or under eaten yesterday
        //for example, you ate two too many yesterday, you can have two fewer today
        //However, in the special case, where you ate too much/little in the day before,
        //so yesterday you were making up for it, things are different
        if((yesterdayNormalised > 0 && dayBeforeNormalised < 0)
                || (yesterdayNormalised < 0 && dayBeforeNormalised > 0)) {
            //if the amount you ate yesterday overcompensated for the error from the day before
            //then take the difference of the two
            if (Math.abs(yesterdayNormalised) > Math.abs(dayBeforeNormalised)) {
                adjuster = -(yesterdayNormalised + dayBeforeNormalised);
            } else {
                //in the case where you didn't make up for it enough, cut your losses and reset
                adjuster = 0;
            }
        } else {
            adjuster = -yesterdayNormalised;
        }
        return adjuster + expectedCount;
    }



    @Override
    public Task<Void> updateDietPlan(final DietPlan newDietPlan) {
        //updating the database
        Task<Void> updateDiet = db.collection(DietPlan.COLLECTION_NAME)
                .document(newDietPlan.getUser())
                .set(newDietPlan);
        //updating this object with the new data
        updateDiet.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                overallDiet = newDietPlan;
                updateListener();
            }
        });
        return updateDiet;
    }

    @Override
    public boolean isFoodCompletedToday() {
        return isFoodCompleted(0);
    }
    @Override
    public boolean isFoodCompleted(int nDaysAgo) {
        return isFoodCompleted(getDaysMeals(nDaysAgo), getDaysDietPlan(nDaysAgo));
    }
    private boolean isFoodCompleted(DailyMeals meal, DietPlan dietPlan) {
        return meal.getVegCount ()>= dietPlan.getDailyVeges()
                & meal.getProteinCount ()>= dietPlan.getDailyProtein()
                & meal.getDairyCount ()>= dietPlan.getDailyDairy()
                & meal.getGrainCount ()>= dietPlan.getDailyGrain()
                & meal.getFruitCount ()>= dietPlan.getDailyFruit();
    }

    @Override
    public boolean isWaterCompletedToday() {
        return isWaterCompleted(0);
    }
    @Override
    public boolean isWaterCompleted(int nDaysAgo) {
        return isWaterCompleted(getDaysMeals(nDaysAgo), getDaysDietPlan(nDaysAgo));
    }
    private boolean isWaterCompleted(DailyMeals meals, DietPlan dietPlan) {
        return meals.getWaterCount() >= dietPlan.getDailyWater();
    }

    @Override
    public boolean isOverCheatScoreToday() {
        return isOverCheatScore(0);
    }
    @Override
    public boolean isOverCheatScore(int nDaysAgo) {
        return isOverCheatScore(getDaysMeals(nDaysAgo), getDaysDietPlan(nDaysAgo));
    }
    private boolean isOverCheatScore(DailyMeals meals, DietPlan dietPlan) {
        return meals.getWeeklyCheats() > dietPlan.getWeeklyCheats();
    }

    @Override
    public void updateListener() {
        listener.refresh();
    }

    @Override
    public void updateDailyMeals(DailyMeals day) {
        refreshDietPlans();
        updateListener();
    }
}
