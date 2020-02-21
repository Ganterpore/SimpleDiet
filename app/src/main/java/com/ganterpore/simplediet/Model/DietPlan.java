package com.ganterpore.simplediet.Model;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class DietPlan {
    public static final String COLLECTION_NAME = "DietPlans";
    public static final String DEFAULT_DIETS_COLLECTION_NAME = "DefaultDiets";

    private double dailyVeges;
    private double dailyProtein;
    private double dailyDairy;
    private double dailyGrain;
    private double dailyFruit;
    private double dailyWater;
    private double weeklyCheats;
    private int order;
    private String dietName;

    private double dailyCaffeine;
    private double dailyAlcohol;

    private String user;

    public DietPlan(double dailyVeges, double dailyProtein, double dailyDairy, double dailyGrain,
                    double dailyFruit, double dailyWater, double weeklyCheats,
                    double dailyCaffeine, double dailyAlcohol, String user) {
        this.dailyVeges = dailyVeges;
        this.dailyProtein = dailyProtein;
        this.dailyDairy = dailyDairy;
        this.dailyGrain = dailyGrain;
        this.dailyFruit = dailyFruit;
        this.dailyWater = dailyWater;
        this.weeklyCheats = weeklyCheats;
        this.dailyCaffeine = dailyCaffeine;
        this.dailyAlcohol = dailyAlcohol;
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

    public static Query defaultDiets() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        return db.collection(DEFAULT_DIETS_COLLECTION_NAME)
                .orderBy("order");
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

    public double getDailyAlcohol() {
        return dailyAlcohol;
    }

    public void setDailyAlcohol(double dailyAlcohol) {
        this.dailyAlcohol = dailyAlcohol;
    }

    public double getDailyCaffeine() {
        return dailyCaffeine;
    }

    public void setDailyCaffeine(double dailyCaffeine) {
        this.dailyCaffeine = dailyCaffeine;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDietName() {
        return dietName;
    }

    public void setDietName(String dietName) {
        this.dietName = dietName;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
