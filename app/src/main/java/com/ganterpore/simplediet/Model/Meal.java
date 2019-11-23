package com.ganterpore.simplediet.Model;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Meal {
    private double vegCount;
    private double proteinCount;
    private double dairyCount;
    private double grainCount;
    private double fruitCount;
    private double waterCount;
    private double excessServes;

    private double cheatScore;
    private long day;

    private String user;
    private String name; //optional

    public Meal(double vegCount, double proteinCount, double dairyCount, double grainCount, double fruitCount,
                double waterCount, double excessServes, double cheatScore, long day, String user) {
        this.vegCount = vegCount;
        this.proteinCount = proteinCount;
        this.dairyCount = dairyCount;
        this.grainCount = grainCount;
        this.fruitCount = fruitCount;
        this.waterCount = waterCount;
        this.excessServes = excessServes;
        this.cheatScore = cheatScore;
        this.day = day;
        this.user = user;
    }

    /**
     * pushes the current meal object to the database
     * @return a task for the database add
     */
    public Task<DocumentReference> pushToDB() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("Meals").add(this);
    }

    public Meal() {
    }

    public Meal(DocumentSnapshot docMeal) {
        this.cheatScore = docMeal.getDouble("cheatScore");
        this.vegCount  = docMeal.getDouble("vegCount");
        this.proteinCount = docMeal.getDouble("proteinCount");
        this.dairyCount = docMeal.getDouble("dairyCount");
        this.grainCount = docMeal.getDouble("grainCount");
        this.fruitCount = docMeal.getDouble("fruitCount");
        this.waterCount = docMeal.getDouble("waterCount");
        this.excessServes = docMeal.getDouble("excessServes");
        this.day = docMeal.getLong("day");
        this.user = docMeal.getString("user");
        this.name = docMeal.getString("name");
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

    public long getDay() {
        return day;
    }

    public void setDay(long day) {
        this.day = day;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
