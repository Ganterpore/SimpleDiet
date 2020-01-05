package com.ganterpore.simplediet.Model;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

public class DietPlan {
    public static final String COLLECTION_NAME = "DietPlans";

    private double dailyVeges;
    private double dailyProtein;
    private double dailyDairy;
    private double dailyGrain;
    private double dailyFruit;
    private double dailyWater;
    private double weeklyCheats;

    private String user;

    public DietPlan(double dailyVeges, double dailyProtein, double dailyDairy, double dailyGrain,
                    double dailyFruit, double dailyWater, double weeklyCheats, String user) {
        this.dailyVeges = dailyVeges;
        this.dailyProtein = dailyProtein;
        this.dailyDairy = dailyDairy;
        this.dailyGrain = dailyGrain;
        this.dailyFruit = dailyFruit;
        this.dailyWater = dailyWater;
        this.weeklyCheats = weeklyCheats;
        this.user = user;
    }

    public DietPlan() {
    }

    /**
     * pushes the current meal object to the database
     * @return a task for the database add
     */
    public Task<Void> pushToDB() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(COLLECTION_NAME).document(user).set(this);
    }

    public double getDailyVeges() {
        return dailyVeges;
    }

    public void setDailyVeges(double dailyVeges) {
        this.dailyVeges = dailyVeges;
    }

    public double getDailyProtein() {
        return dailyProtein;
    }

    public void setDailyProtein(double dailyProtein) {
        this.dailyProtein = dailyProtein;
    }

    public double getDailyDairy() {
        return dailyDairy;
    }

    public void setDailyDairy(double dailyDairy) {
        this.dailyDairy = dailyDairy;
    }

    public double getDailyGrain() {
        return dailyGrain;
    }

    public void setDailyGrain(double dailyGrain) {
        this.dailyGrain = dailyGrain;
    }

    public double getDailyFruit() {
        return dailyFruit;
    }

    public void setDailyFruit(double dailyFruit) {
        this.dailyFruit = dailyFruit;
    }

    public double getDailyWater() {
        return dailyWater;
    }

    public void setDailyWater(double dailyWater) {
        this.dailyWater = dailyWater;
    }

    public double getWeeklyCheats() {
        return weeklyCheats;
    }

    public void setWeeklyCheats(double weeklyCheats) {
        this.weeklyCheats = weeklyCheats;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
