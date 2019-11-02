package com.ganterpore.simplediet.Model;

public class Meal {
    private int vegCount;
    private int proteinCount;
    private int dairyCount;
    private int grainCount;
    private int fruitCount;
    private int waterCount;
    private int excessServes;

    private int cheatScore;
    private long day;

    private String user;

    public Meal(int vegCount, int proteinCount, int dairyCount, int grainCount, int fruitCount, int waterCount, int excessServes, int cheatScore, long day, String user) {
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

    public Meal() {
    }

    public int getVegCount() {
        return vegCount;
    }

    public void setVegCount(int vegCount) {
        this.vegCount = vegCount;
    }

    public int getProteinCount() {
        return proteinCount;
    }

    public void setProteinCount(int proteinCount) {
        this.proteinCount = proteinCount;
    }

    public int getDairyCount() {
        return dairyCount;
    }

    public void setDairyCount(int dairyCount) {
        this.dairyCount = dairyCount;
    }

    public int getGrainCount() {
        return grainCount;
    }

    public void setGrainCount(int grainCount) {
        this.grainCount = grainCount;
    }

    public int getFruitCount() {
        return fruitCount;
    }

    public void setFruitCount(int fruitCount) {
        this.fruitCount = fruitCount;
    }

    public int getWaterCount() {
        return waterCount;
    }

    public void setWaterCount(int waterCount) {
        this.waterCount = waterCount;
    }

    public int getExcessServes() {
        return excessServes;
    }

    public void setExcessServes(int excessServes) {
        this.excessServes = excessServes;
    }

    public int getCheatScore() {
        return cheatScore;
    }

    public void setCheatScore(int cheatScore) {
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
}
