package com.ganterpore.simplediet.Model;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class DietPlan {
    public static final String COLLECTION_NAME = "DietPlans";

    private int dailyVeges;
    private int dailyProtein;
    private int dailyDairy;
    private int dailyGrain;
    private int dailyFruit;
    private int dailyWater;
    private int weeklyCheats;

    private String user;

    public DietPlan(int dailyVeges, int dailyProtein, int dailyDairy, int dailyGrain,
                    int dailyFruit, int dailyWater, int weeklyCheats, String user) {
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

    public int getDailyVeges() {
        return dailyVeges;
    }

    public void setDailyVeges(int dailyVeges) {
        this.dailyVeges = dailyVeges;
    }

    public int getDailyProtein() {
        return dailyProtein;
    }

    public void setDailyProtein(int dailyProtein) {
        this.dailyProtein = dailyProtein;
    }

    public int getDailyDairy() {
        return dailyDairy;
    }

    public void setDailyDairy(int dailyDairy) {
        this.dailyDairy = dailyDairy;
    }

    public int getDailyGrain() {
        return dailyGrain;
    }

    public void setDailyGrain(int dailyGrain) {
        this.dailyGrain = dailyGrain;
    }

    public int getDailyFruit() {
        return dailyFruit;
    }

    public void setDailyFruit(int dailyFruit) {
        this.dailyFruit = dailyFruit;
    }

    public int getDailyWater() {
        return dailyWater;
    }

    public void setDailyWater(int dailyWater) {
        this.dailyWater = dailyWater;
    }

    public int getWeeklyCheats() {
        return weeklyCheats;
    }

    public void setWeeklyCheats(int weeklyCheats) {
        this.weeklyCheats = weeklyCheats;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
