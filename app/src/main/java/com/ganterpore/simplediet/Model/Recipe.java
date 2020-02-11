package com.ganterpore.simplediet.Model;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Recipe {
    public static final String RECIPES = "Recipes";
    private String name;

    private double vegCount;
    private double proteinCount;
    private double dairyCount;
    private double grainCount;
    private double fruitCount;
    private double excessServes;

    private double waterCount;
    private double caffieneCount;
    private double alcoholStandards;

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

    public static Recipe drinkRecipe(String name, double waterCount, double dairyCount,
                 double caffieneCount, double alcoholStandards, double cheatScore, String user) {
        Recipe drink = new Recipe();
        drink.name = name;
        drink.waterCount = waterCount;
        drink.dairyCount = dairyCount;
        drink.caffieneCount = caffieneCount;
        drink.alcoholStandards = alcoholStandards;
        drink.cheatScore = cheatScore;
        drink.user = user;
        return drink;
    }

    public Recipe() {
    }

    /**
     * pushes the current recipe object to the database
     * @return a task for the database add
     */
    public Task<DocumentReference> pushToDB() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(RECIPES).add(this);
    }

    /**
     * returns the text format of the number of serves of each food type
     * @return the string
     */
    public String serveCountText() {
        NumberFormat df = new DecimalFormat("##.##");
        String output = "";

        if(getVegCount() > 0){
            output += "    V:" + df.format(getVegCount());
        }
        if(getProteinCount() > 0){
            output += "    P:" + df.format(getProteinCount());
        }
        if(getDairyCount() > 0){
            output += "    D:" + df.format(getDairyCount());
        }
        if(getGrainCount() > 0){
            output += "    G:" + df.format(getGrainCount());
        }
        if(getFruitCount() > 0){
            output += "    F:" + df.format(getFruitCount());
        }
        if(getExcessServes() > 0){
            output += "    Ex:" + df.format(getExcessServes());
        }
        output += "    Cheats:" + df.format(getCheatScore());

        return output.trim();
    }

    /**
     * Converts the recipe object into a meal object
     * @return a meal instance of the given recipe
     */
    public Meal convertToMeal() {
        Meal meal = new Meal(vegCount, proteinCount, dairyCount, grainCount, fruitCount, waterCount,
                excessServes, cheatScore, System.currentTimeMillis(), user);
        meal.setName(name);
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
