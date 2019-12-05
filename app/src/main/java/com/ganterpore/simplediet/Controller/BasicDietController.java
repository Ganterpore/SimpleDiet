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

public class BasicDietController implements DietController, DailyMeals.DailyMealsInterface {

    private DietControllerListener listener;
    private String user;
    private DailyMeals todaysMeals;
    private DietPlan todaysDiet;
    private FirebaseFirestore db;
    private SparseArray<DailyMeals> daysAgoMeals;

    public BasicDietController(DietControllerListener listener) {
        //initialising variabls
        this.db = FirebaseFirestore.getInstance();
        this.listener = listener;
        this.user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        todaysMeals = new DailyMeals(this, user);
        todaysDiet = new DietPlan(0,0,0,0,0,0,0,"");
        getCurrentDietPlanFromDB();
        daysAgoMeals = new SparseArray<>();
        daysAgoMeals.append(0, todaysMeals);
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
                        todaysDiet = documentSnapshot.toObject(DietPlan.class);
                        updateListener();
                    }
                });
        //making sure any changes to the data are tracked
        dietPlanQuery.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                todaysDiet = documentSnapshot.toObject(DietPlan.class);
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
    public DietPlan getTodaysDietPlan() {
        return todaysDiet;
    }
    @Override
    public DietPlan getDaysDietPlan(int nDaysAgo) {
        return getTodaysDietPlan(); //For the BasicDietController, both todays and the overall diet plan are the same
    }

    @Override
    public DietPlan getOverallDietPlan() {
        return getTodaysDietPlan(); //For the BasicDietController, both todays and the overall diet plan are the same
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
                todaysDiet = newDietPlan;
                updateListener();
            }
        });
        return updateDiet;
    }

    @Override
    public boolean isFoodCompletedToday() {
        return isFoodCompleted(todaysMeals);
    }
    @Override
    public boolean isFoodCompleted(int nDaysAgo) {
        return isFoodCompleted(getDaysMeals(nDaysAgo));
    }
    private boolean isFoodCompleted(DailyMeals meal) {
        return meal.getVegCount ()>= todaysDiet.getDailyVeges()
                & meal.getProteinCount ()>= todaysDiet.getDailyProtein()
                & meal.getDairyCount ()>= todaysDiet.getDailyDairy()
                & meal.getGrainCount ()>= todaysDiet.getDailyGrain()
                & meal.getFruitCount ()>= todaysDiet.getDailyFruit();
    }

    @Override
    public boolean isWaterCompletedToday() {
        return isWaterCompleted(todaysMeals);
    }
    @Override
    public boolean isWaterCompleted(int nDaysAgo) {
        return isWaterCompleted(getDaysMeals(nDaysAgo));
    }
    private boolean isWaterCompleted(DailyMeals meals) {
        return meals.getWaterCount() >= todaysDiet.getDailyWater();
    }

    @Override
    public boolean isOverCheatScoreToday() {
        return isOverCheatScore(todaysMeals);
    }
    @Override
    public boolean isOverCheatScore(int nDaysAgo) {
        return isOverCheatScore(getDaysMeals(nDaysAgo));
    }
    private boolean isOverCheatScore(DailyMeals meals) {
        return meals.getWeeklyCheats() > todaysDiet.getWeeklyCheats();
    }

    @Override
    public void updateListener() {
        listener.refresh();
    }

    @Override
    public void updateDailyMeals(DailyMeals day) {
        updateListener();
    }
}
