package com.ganterpore.simplediet.Controller;

import android.app.Activity;

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

public class BasicDietController implements DietController,
        DailyMeals.DailyMealsInterface { //, DietPlanWrapper.DietPlanInterface {

    DietControllerListener listener;
    String user;
    DailyMeals todaysMeals;
    DietPlan todaysDiet;
    FirebaseFirestore db;

    public BasicDietController(DietControllerListener listener) {
        this.db = FirebaseFirestore.getInstance();
        this.listener = listener;
        this.user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        todaysMeals = new DailyMeals(this, user);
        //creating a blank dietplan
        todaysDiet = new DietPlan(0,0,0,0,0,0,0,"");
        getCurrentDietPlanFromDB();
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
    public DietPlan getTodaysDietPlan() {
        return todaysDiet;
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
    public boolean isFoodCompleted() {
        return todaysMeals.getVegCount ()>= todaysDiet.getDailyVeges()
                & todaysMeals.getProteinCount ()>= todaysDiet.getDailyProtein()
                & todaysMeals.getDairyCount ()>= todaysDiet.getDailyDairy()
                & todaysMeals.getGrainCount ()>= todaysDiet.getDailyGrain()
                & todaysMeals.getFruitCount ()>= todaysDiet.getDailyFruit();
    }

    @Override
    public boolean isWaterCompleted() {
        return todaysMeals.getWaterCount() >= todaysDiet.getDailyWater();
    }

    @Override
    public boolean isOverCheatScore() {
        return todaysMeals.getWeeklyCheats() > todaysDiet.getWeeklyCheats();
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
