package com.ganterpore.simplediet.Model;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

public class Meal {
    private static final String TAG = "Meal";
    public static final String COLLECTION_PATH = "Meals";

    private String id;
    //food fields
    private double vegCount;
    private double proteinCount;
    private double dairyCount;
    private double grainCount;
    private double fruitCount;

    //drink fields
    private double waterCount;
    private double caffeineCount;
    private double alcoholStandards;
    private double hydrationScore;

    private double excessServes;

    private double cheatScore;
    private long day;

    private String user;
    private String name; //optional

    private DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyy");

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
     * Creates a meal with the drink parameters filled in
     */
    public static Meal Drink(double waterCount, double dairyCount, double caffieneCount, double alcoholStandards,
                             double hydrationScore, double cheatScore, long day, String user) {
        Meal drink = new Meal();
        drink.waterCount = waterCount;
        drink.dairyCount = dairyCount;
        drink.caffeineCount = caffieneCount;
        drink.alcoholStandards = alcoholStandards;
        drink.hydrationScore = hydrationScore;
        drink.cheatScore = cheatScore;
        drink.day = day;
        drink.user = user;
        return drink;
    }

    /**
     * converts an alcohol percent to a number of standards
     * @param waterCount, number of serves of water (one serve is 250mL)
     * @param dairyCount, number of serves of milk
     * @param alcoholPercent, the percentage abv of alcohol, in percent (4.7 etc (not 0.047))
     * @return number of standards of alcohol
     */
    public static double getStandardsFromPercent(double waterCount, double dairyCount, double alcoholPercent) {
        double serves = waterCount + dairyCount;
        double volume = serves * .25; //(litres)
        double standards = volume * alcoholPercent * 0.789;
        return standards;
    }

    /**
     * converts an alcohol percent to a number of standards
     * @param waterCount, number of serves of water (one serve is 250mL)
     * @param dairyCount, number of serves of milk
     * @param alcoholStandards, the number of standard drinks of alcohol
     * @return alcohol percent in drink
     */
    public static double getPercentFromStandards(double waterCount, double dairyCount, double alcoholStandards) {
        double serves = waterCount + dairyCount;
        double volume = serves * .25; //(litres)
        double percent = alcoholStandards / (volume * 0.789);
        return percent;
    }

    /**
     * pushes the current meal object to the database
     * @return a task for the database add
     */
    public Task<DocumentReference> pushToDB() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(COLLECTION_PATH).add(this);
    }

    public Task<Void> deleteMeal() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(COLLECTION_PATH).document(id).delete();
    }

    public Meal() {
    }

    public Meal(DocumentSnapshot docMeal) {
        id = docMeal.getId();
        if(docMeal.contains("cheatScore")) {
            this.cheatScore = docMeal.getDouble("cheatScore");
        } else {
            this.cheatScore = 0.0;
        }
        if(docMeal.contains("vegCount")) {
            this.vegCount  = docMeal.getDouble("vegCount");
        } else {
            this.vegCount = 0.0;
        }
        if(docMeal.contains("proteinCount")) {
            this.proteinCount = docMeal.getDouble("proteinCount");
        } else {
            this.proteinCount = 0.0;
        }
        if(docMeal.contains("dairyCount")) {
            this.dairyCount = docMeal.getDouble("dairyCount");
        } else {
            this.dairyCount = 0.0;
        }
        if(docMeal.contains("grainCount")) {
            this.grainCount = docMeal.getDouble("grainCount");
        } else {
            this.grainCount = 0.0;
        }
        if(docMeal.contains("fruitCount")) {
            this.fruitCount = docMeal.getDouble("fruitCount");
        } else {
            this.fruitCount = 0.0;
        }
        if(docMeal.contains("excessServes")) {
            this.excessServes = docMeal.getDouble("excessServes");
        } else {
            this.excessServes = 0.0;
        }
        if(docMeal.contains("day")) {
            this.day = docMeal.getLong("day");
        } else {
            this.day = 0;
        }
        this.user = docMeal.getString("user");
        this.name = docMeal.getString("name");

        if(docMeal.contains("waterCount")) {
            this.waterCount = docMeal.getDouble("waterCount");
        } else {
            this.waterCount = 0.0;
        }
        if(docMeal.contains("caffeineCount")) {
            this.caffeineCount = docMeal.getDouble("caffeineCount");
        } else {
            this.caffeineCount = 0.0;
        }
        if(docMeal.contains("alcoholStandards")) {
            this.alcoholStandards = docMeal.getDouble("alcoholStandards");
        } else {
            this.alcoholStandards = 0.0;
        }
        if(docMeal.contains("hydrationScore")) {
            this.hydrationScore = docMeal.getDouble("hydrationScore");
        } else {
            this.hydrationScore = 0.0;
        }
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
            output += "    W:" + df.format(getWaterCount());
        }
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
        if(getCaffeineCount() > 0){
            output += "    C:" + df.format(getCaffeineCount());
        }
        if(getAlcoholStandards() > 0){
            output += "    A:" + df.format(getAlcoholStandards());
        }
        if(getExcessServes() > 0){
            output += "    Ex:" + df.format(getExcessServes());
        }
        if(hydrationScore > 0.1 || hydrationScore < -0.1) {
            output  += "    Hydration:" + df.format(getHydrationScore());
        }
        output += "    Total Cheats:" + df.format(calculateTotalCheats());

        return output.trim();
    }

    public String dateAsString() {
        return dateFormat.format(day);
    }

    public String getId() {
        return id;
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

    public double getHydrationScore() {
        return hydrationScore;
    }

    public void setHydrationScore(double hydrationScore) {
        this.hydrationScore = hydrationScore;
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
