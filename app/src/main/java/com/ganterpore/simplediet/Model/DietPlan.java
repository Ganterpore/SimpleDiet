package com.ganterpore.simplediet.Model;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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
    private double dailyHydration;
    private double dailyCheats;
    private double weeklyCheats;
    private int order;
    private String dietName;

    private double dailyCaffeine;
    private double dailyAlcohol;
    private double weeklyCaffeine;
    private double weeklyAlcohol;

    private String user;

    public DietPlan(double dailyVeges, double dailyProtein, double dailyDairy, double dailyGrain,
                    double dailyFruit, double dailyHydration, double dailyCheats, double weeklyCheats,
                    int order, String dietName, double dailyCaffeine, double dailyAlcohol,
                    double weeklyCaffeine, double weeklyAlcohol, String user) {
        this.dailyVeges = dailyVeges;
        this.dailyProtein = dailyProtein;
        this.dailyDairy = dailyDairy;
        this.dailyGrain = dailyGrain;
        this.dailyFruit = dailyFruit;
        this.dailyHydration = dailyHydration;
        this.dailyCheats = dailyCheats;
        this.weeklyCheats = weeklyCheats;
        this.order = order;
        this.dietName = dietName;
        this.dailyCaffeine = dailyCaffeine;
        this.dailyAlcohol = dailyAlcohol;
        this.weeklyCaffeine = weeklyCaffeine;
        this.weeklyAlcohol = weeklyAlcohol;
        this.user = user;
    }

    /**
     * Creates an empty DietPLan with default values
     */
    public DietPlan() {
        this.dailyVeges = 6;
        this.dailyProtein = 3;
        this.dailyDairy = 2.5;
        this.dailyGrain = 6;
        this.dailyFruit = 2;
        this.dailyHydration = 10;
        this.dailyCheats = 0;
        this.dailyCaffeine = 4;
        this.dailyAlcohol = 4;
        this.user = FirebaseAuth.getInstance().getUid();
    }

    public DietPlan copy() {
        return new DietPlan(dailyVeges, dailyProtein, dailyDairy, dailyGrain, dailyFruit, dailyHydration,
                dailyCheats, weeklyCheats, order, dietName, dailyCaffeine, dailyAlcohol, weeklyCaffeine,
                weeklyAlcohol, user);
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

    public double totalServes() {
        return dailyVeges + dailyProtein + dailyDairy + dailyGrain + dailyFruit;
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

    public double getDailyHydration() {
        return dailyHydration;
    }

    public void setDailyHydration(double dailyHydration) {
        this.dailyHydration = dailyHydration;
    }

    public double getDailyCheats() {
        return dailyCheats;
    }

    public void setDailyCheats(double dailyCheats) {
        this.dailyCheats = dailyCheats;
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

    public double getWeeklyCaffeine() {
        return weeklyCaffeine;
    }

    public void setWeeklyCaffeine(double weeklyCaffeine) {
        this.weeklyCaffeine = weeklyCaffeine;
    }

    public double getWeeklyAlcohol() {
        return weeklyAlcohol;
    }

    public void setWeeklyAlcohol(double weeklyAlcohol) {
        this.weeklyAlcohol = weeklyAlcohol;
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
