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
    private double caffeineCount;
    private double alcoholStandards;
    private double hydrationScore;

    private double cheatScore;

    private boolean isDrink;

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
        this.isDrink = false;
        this.hydrationScore = 0;
    }

    public static Recipe drinkRecipe(String name, double waterCount, double dairyCount,
                 double caffieneCount, double alcoholStandards,
                 double hydrationFactor, double cheatScore, String user) {
        Recipe drink = new Recipe();
        drink.name = name;
        drink.waterCount = waterCount;
        drink.dairyCount = dairyCount;
        drink.caffeineCount = caffieneCount;
        drink.alcoholStandards = alcoholStandards;
        drink.hydrationScore = hydrationFactor;
        drink.cheatScore = cheatScore;
        drink.user = user;
        drink.isDrink = true;
        return drink;
    }

    public Recipe() {
        this.isDrink = false;
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
     * Calculates the total cheat points acquired from this meal
     * @return the value of the cheat points
     */
    public double calculateTotalCheats() {
        double serveCount = vegCount + proteinCount + dairyCount
                + grainCount + fruitCount + waterCount + excessServes;
        return serveCount * cheatScore;
    }

    /**
     * returns the text format of the number of serves of each food type
     * @return the string
     */
    public String serveCountText() {
        NumberFormat df = new DecimalFormat("##.##");
        String output = "";

        if(getWaterCount() > 0){
            output += "    Water: " + df.format(getWaterCount());
        }
        if(getVegCount() > 0){
            output += "    Veges: " + df.format(getVegCount());
        }
        if(getProteinCount() > 0){
            output += "    Protein: " + df.format(getProteinCount());
        }
        if(getDairyCount() > 0){
            output += "    Dairy: " + df.format(getDairyCount());
        }
        if(getGrainCount() > 0){
            output += "    Grain: " + df.format(getGrainCount());
        }
        if(getFruitCount() > 0){
            output += "    Fruit: " + df.format(getFruitCount());
        }
        if(getCaffeineCount() > 0){
            output += "    Caffeine: " + df.format(getCaffeineCount());
        }
        if(getAlcoholStandards() > 0){
            output += "    Alcohol: " + df.format(getAlcoholStandards());
        }
        if(getExcessServes() > 0){
            output += "    Excess: " + df.format(getExcessServes());
        }
        if(isDrink) {
            output  += "    Hydration: " + df.format(getHydrationScore());
        }
        output += "    Total Cheats: " + df.format(calculateTotalCheats());

        return output.trim();
    }

    /**
     * Converts the recipe object into a meal object
     * @return a meal instance of the given recipe
     */
    public Meal convertToMeal() {
        Meal meal = new Meal();
        meal.setName(name);
        meal.setVegCount(vegCount);
        meal.setProteinCount(proteinCount);
        meal.setDairyCount(dairyCount);
        meal.setGrainCount(grainCount);
        meal.setFruitCount(fruitCount);
        meal.setExcessServes(excessServes);
        meal.setWaterCount(waterCount);
        meal.setCaffeineCount(caffeineCount);
        meal.setAlcoholStandards(alcoholStandards);
        meal.setHydrationScore(hydrationScore);
        meal.setCheatScore(cheatScore);
        meal.setUser(user);
        meal.setDay(System.currentTimeMillis());
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

    public double getCaffeineCount() {
        return caffeineCount;
    }

    public void setCaffeineCount(double caffeineCount) {
        this.caffeineCount = caffeineCount;
    }

    public double getAlcoholStandards() {
        return alcoholStandards;
    }

    public void setAlcoholStandards(double alcoholStandards) {
        this.alcoholStandards = alcoholStandards;
    }

    public boolean isDrink() {
        return isDrink;
    }

    public void setDrink(boolean drink) {
        isDrink = drink;
    }

    public double getHydrationScore() {
        return hydrationScore;
    }

    public void setHydrationScore(double hydrationScore) {
        this.hydrationScore = hydrationScore;
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
