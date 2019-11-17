package com.ganterpore.simplediet.Model;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Recipe {
    public static final String RECIPES = "Recipes";
    private String name;

    private double vegCount;
    private double proteinCount;
    private double dairyCount;
    private double grainCount;
    private double fruitCount;
    private double waterCount;
    private double excessServes;

    private double cheatScore;

    private String user;

    public Recipe(String name, double vegCount, double proteinCount, double dairyCount, double grainCount,
                  double fruitCount, double waterCount, double excessServes, double cheatScore, String user) {
        this.name = name;
        this.vegCount = vegCount;
        this.proteinCount = proteinCount;
        this.dairyCount = dairyCount;
        this.grainCount = grainCount;
        this.fruitCount = fruitCount;
        this.waterCount = waterCount;
        this.excessServes = excessServes;
        this.cheatScore = cheatScore;
        this.user = user;
    }

    public Recipe() {
    }

    public Task<DocumentReference> pushToDB() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(RECIPES).add(this);
    }

    public String serveCountText() {
        return "V:" + getVegCount()
                + "    P:" + getProteinCount()
                + "    D:" + getDairyCount()
                + "    G:" + getGrainCount()
                + "    F:" + getFruitCount()
                + "    Ex:" + getExcessServes()
                + "    Cheats:" + getCheatScore();
    }

    public Meal convertToMeal() {
        Meal meal = new Meal(vegCount, proteinCount, dairyCount, grainCount, fruitCount, waterCount,
                excessServes, cheatScore, System.currentTimeMillis(), user);
        return meal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getVegCount() {
        return vegCount;
    }

    public void setVegCount(double vegCount) {
        this.vegCount = vegCount;
    }

    public double getProteinCount() {
        return proteinCount;
    }

    public void setProteinCount(double proteinCount) {
        this.proteinCount = proteinCount;
    }

    public double getDairyCount() {
        return dairyCount;
    }

    public void setDairyCount(double dairyCount) {
        this.dairyCount = dairyCount;
    }

    public double getGrainCount() {
        return grainCount;
    }

    public void setGrainCount(double grainCount) {
        this.grainCount = grainCount;
    }

    public double getFruitCount() {
        return fruitCount;
    }

    public void setFruitCount(double fruitCount) {
        this.fruitCount = fruitCount;
    }

    public double getWaterCount() {
        return waterCount;
    }

    public void setWaterCount(double waterCount) {
        this.waterCount = waterCount;
    }

    public double getExcessServes() {
        return excessServes;
    }

    public void setExcessServes(double excessServes) {
        this.excessServes = excessServes;
    }

    public double getCheatScore() {
        return cheatScore;
    }

    public void setCheatScore(double cheatScore) {
        this.cheatScore = cheatScore;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
