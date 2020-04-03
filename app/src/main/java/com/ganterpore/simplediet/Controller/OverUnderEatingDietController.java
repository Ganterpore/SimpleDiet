package com.ganterpore.simplediet.Controller;

import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseArray;

import com.ganterpore.simplediet.Model.DietPlan;

import java.util.ArrayList;
import java.util.List;

public class OverUnderEatingDietController extends BasicDietController{
    private static final String TAG = "OverUnderEatingDietCo";
    private SparseArray<DietPlan> daysAgoDiets;

    public OverUnderEatingDietController(DietControllerListener listener) {
        super(listener);
        //initialising variables
        daysAgoDiets = new SparseArray<>();
    }

    @Override
    public DietPlan getDaysDietPlan(int nDaysAgo) {
        DietPlan daysDiet = daysAgoDiets.get(nDaysAgo);
        if(daysDiet == null) {
            daysAgoDiets.append(nDaysAgo, new DietPlan());
            refreshDietPlans();
        }
        return daysAgoDiets.get(nDaysAgo);
    }

    @Override
    public boolean isFoodCompleted(int nDaysAgo) {
        DietPlan daysDietPlan = getDaysDietPlan(nDaysAgo);
        DietPlan nextDaysDietPlan = getDaysDietPlan(nDaysAgo - 1);
        DailyMeals daysMeals = getDaysMeals(nDaysAgo);
        DailyMeals nextDaysMeals = getDaysMeals(nDaysAgo - 1);

        //getting the excess food eaten on the next day
        double nextDaysExcessVeges = nextDaysMeals.getVegCount() - nextDaysDietPlan.getDailyVeges();
        double nextDaysExcessProtein = nextDaysMeals.getProteinCount() - nextDaysDietPlan.getDailyProtein();
        double nextDaysExcessDairy = nextDaysMeals.getDairyCount() - nextDaysDietPlan.getDailyDairy();
        double nextDaysExcessGrain = nextDaysMeals.getGrainCount() - nextDaysDietPlan.getDailyGrain();
        double nextDaysExcessFruit = nextDaysMeals.getFruitCount() - nextDaysDietPlan.getDailyFruit();

        //only keeping data if the excess is positive
        nextDaysExcessVeges = nextDaysExcessVeges>0 ? nextDaysExcessVeges : 0;
        nextDaysExcessProtein = nextDaysExcessProtein>0 ? nextDaysExcessProtein : 0;
        nextDaysExcessDairy = nextDaysExcessDairy>0 ? nextDaysExcessDairy : 0;
        nextDaysExcessGrain = nextDaysExcessGrain>0 ? nextDaysExcessGrain : 0;
        nextDaysExcessFruit = nextDaysExcessFruit>0 ? nextDaysExcessFruit : 0;

        //checking if food is completed based on today's meals, and tomorrows excess.
        return (daysMeals.getVegCount() + nextDaysExcessVeges)>= daysDietPlan.getDailyVeges()
                & (daysMeals.getProteinCount() + nextDaysExcessProtein)>= daysDietPlan.getDailyProtein()
                & (daysMeals.getDairyCount() + nextDaysExcessDairy)>= daysDietPlan.getDailyDairy()
                & (daysMeals.getGrainCount() + nextDaysExcessGrain)>= daysDietPlan.getDailyGrain()
                & (daysMeals.getFruitCount() + nextDaysExcessFruit)>= daysDietPlan.getDailyFruit();
    }

    /**
     * updates all the DietPlans that have been looked at so far by the Controller, and updates their values.
     * should be called when an update to the overall DietPlan or any DailyMeals has occurred.
     */
    private void refreshDietPlans() {
        //for each days diet in the list, refresh the diet
        for(int i=0;i<daysAgoDiets.size();i++) {
            int nDaysAgo = daysAgoDiets.keyAt(i);
            //get the meals from the previous two days
            DailyMeals dayBeforesMeals = getDaysMeals(nDaysAgo + 1);
            DailyMeals twoDaysBeforeMeals = getDaysMeals(nDaysAgo + 2);

            double vegCountAdjusted;
            double proteinCountAdjusted;
            double dairyCountAdjusted;
            double grainCountAdjusted;
            double fruitCountAdjusted;
            if(twoDaysBeforeMeals.getTotalServes() < 0.5) {
                //if two days before is an invalid entry, then just look at yesterday
                vegCountAdjusted = adjustTodaysDiet(dayBeforesMeals.getVegCount(), getOverallDietPlan().getDailyVeges());
                proteinCountAdjusted = adjustTodaysDiet(dayBeforesMeals.getProteinCount(), getOverallDietPlan().getDailyProtein());
                dairyCountAdjusted = adjustTodaysDiet(dayBeforesMeals.getDairyCount(), getOverallDietPlan().getDailyDairy());
                grainCountAdjusted = adjustTodaysDiet(dayBeforesMeals.getGrainCount(), getOverallDietPlan().getDailyGrain());
                fruitCountAdjusted = adjustTodaysDiet(dayBeforesMeals.getFruitCount(), getOverallDietPlan().getDailyFruit());
            } else {
                //adjust the diet counts based on the last few days meals
                vegCountAdjusted = adjustTodaysDiet(dayBeforesMeals.getVegCount(), twoDaysBeforeMeals.getVegCount(), getOverallDietPlan().getDailyVeges());
                proteinCountAdjusted = adjustTodaysDiet(dayBeforesMeals.getProteinCount(), twoDaysBeforeMeals.getProteinCount(), getOverallDietPlan().getDailyProtein());
                dairyCountAdjusted = adjustTodaysDiet(dayBeforesMeals.getDairyCount(), twoDaysBeforeMeals.getDairyCount(), getOverallDietPlan().getDailyDairy());
                grainCountAdjusted = adjustTodaysDiet(dayBeforesMeals.getGrainCount(), twoDaysBeforeMeals.getGrainCount(), getOverallDietPlan().getDailyGrain());
                fruitCountAdjusted = adjustTodaysDiet(dayBeforesMeals.getFruitCount(), twoDaysBeforeMeals.getFruitCount(), getOverallDietPlan().getDailyFruit());
            }

            //create new dietPlan
            DietPlan newDiet = new DietPlan(vegCountAdjusted, proteinCountAdjusted,
                    dairyCountAdjusted, grainCountAdjusted, fruitCountAdjusted,
                    getOverallDietPlan().getDailyHydration(), getOverallDietPlan().getDailyCheats(),
                    getOverallDietPlan().getDailyCaffeine(), getOverallDietPlan().getDailyAlcohol(),
                    getOverallDietPlan().getUser());

            //replace the diet plan with the new one
            daysAgoDiets.put(nDaysAgo, newDiet);
        }

    }

    /**
     * looks at the amount of food eaten in the past two days of the particular food group, versus
     * the expected amount and comes up with a recommended amount to eat on this day
     * @param yesterdaysCount the count of the food group yesterday
     * @param dayBeforesCount te count of the food group the day before yesterday
     * @param expectedCount the expected food count
     * @return the adjusted amount of the food group
     */
    private double adjustTodaysDiet(double yesterdaysCount, Double dayBeforesCount, double expectedCount) {
        //normalised values is the difference between the count and the expected count
        double yesterdayNormalised = yesterdaysCount - expectedCount;
        double dayBeforeNormalised = dayBeforesCount - expectedCount;
        double adjuster = 0;

        if(yesterdayNormalised > 0 && dayBeforeNormalised >= 0) {
            //if I ate too much yesterday, I can eat less today. However if I also ate too much the day before don't stack it
            adjuster = -yesterdayNormalised;
        } else if(yesterdayNormalised > 0 && dayBeforeNormalised < 0) {
            //if I ate too much yesterday, but it was due to eating too little the day before get the difference
            //if this is still positive, then adjust the diet to eat less
            double difference = yesterdayNormalised + dayBeforeNormalised;
            if(difference > 0) {
                adjuster = -difference;
            } else {
                //otherwise don't adjust the diet
                adjuster = 0;
            }
        }else if(yesterdayNormalised <= 0 && dayBeforeNormalised > 0) {
            //if I ate too little yesterday, but too much the day before, get the difference
            //if I overall ate too much over the two days, adjust the diet to eat less
            double difference = yesterdayNormalised + dayBeforeNormalised;
            if(difference > 0) {
                adjuster = -difference;
            } else {
                //otherwise don't adjust the diet
                adjuster = 0;
            }
        } else if(yesterdayNormalised < 0 && dayBeforeNormalised <= 0) {
            //If I ate too little over both the days, don't adjust the diet
            adjuster = 0;
        } else if(yesterdayNormalised==0) {
            //if I ate the right amount, don't adjust the diet
            adjuster = 0;
        }
        return expectedCount + adjuster;
    }

    /**
     * Same function, but only looks at one previous days data
     */
    private double adjustTodaysDiet(double yesterdaysCount, double expectedCount) {
        double yesterdayNormalised = yesterdaysCount - expectedCount; //eg. two too many yesterday, x = 2
        if(yesterdayNormalised > 0) {
            return expectedCount - yesterdayNormalised; //ate too much yesterday, eat less today
        }
        return expectedCount; //ate too little yesterday? just eat the normal amount today
        //if I overate yesterday, then expected count reduces and vice versa
//        return expectedCount + (expectedCount - yesterdaysCount);
    }

    @Override
    public List<Recommendation> getRecommendations() {
        //get the recommendations from the superior DietController
        ArrayList<Recommendation> recommendations = new ArrayList<>(super.getRecommendations());
        //getting over/under eating recommendations
        Recommendation overEatingRecommendation = getOverEatingRecommendation();
        Recommendation underEatingRecommendation = getUnderEatingRecommendation();
        //adding them to the list if they are not null
        if(overEatingRecommendation != null) {
            recommendations.add(overEatingRecommendation);
        }
        if(underEatingRecommendation != null) {
            recommendations.add(underEatingRecommendation);
        }
        //returning the list of recommendations
        return recommendations;
    }

    private Recommendation getOverEatingRecommendation() {
        String id = "over_eating";
        long expiry = DateUtils.DAY_IN_MILLIS;
        String title = "Over eating";
        String message = "Yesterday you ate too much ";
        DietPlan todaysDietPlan = getTodaysDietPlan();
        int count = 0;
        if(todaysDietPlan.getDailyVeges() < getOverallDietPlan().getDailyVeges()) {
            message += "veges, ";
            count++;
        }
        if(todaysDietPlan.getDailyProtein() < getOverallDietPlan().getDailyProtein()) {
            message += "proteins, ";
            count++;
        }
        if(todaysDietPlan.getDailyDairy() < getOverallDietPlan().getDailyDairy()) {
            message += "dairy, ";
            count++;
        }
        if(todaysDietPlan.getDailyGrain() < getOverallDietPlan().getDailyGrain()) {
            message += "grain, ";
            count++;
        }
        if(todaysDietPlan.getDailyFruit() < getOverallDietPlan().getDailyFruit()) {
            message += "fruit, ";
            count++;
        }
        if(count > 0) {
            message = message.substring(0, message.length() - 2);
            message += ". Your recommended intake for today has been adjusted to compensate for this.";
            return new Recommendation(id, title, message, expiry);
        }
        return null;
    }

    private Recommendation getUnderEatingRecommendation() {
        String id = "under_eating";
        long expiry = DateUtils.DAY_IN_MILLIS;
        String title = "Under eating";
        String message = "Yesterday you ate too little ";
        int count = 0;
        DietPlan yesterdaysDietPlan = getDaysDietPlan(1);
        DailyMeals yesterdaysMeals = getDaysMeals(1);
        if(yesterdaysDietPlan.getDailyVeges() > yesterdaysMeals.getVegCount()) {
            message += "veges by " + (yesterdaysDietPlan.getDailyVeges() - yesterdaysMeals.getVegCount()) + " serves, ";
            count++;
        }
        if(yesterdaysDietPlan.getDailyProtein() > yesterdaysMeals.getProteinCount()) {
            message += "proteins " + (yesterdaysDietPlan.getDailyProtein() - yesterdaysMeals.getProteinCount()) + " serves , ";
            count++;
        }
        if(yesterdaysDietPlan.getDailyDairy() > yesterdaysMeals.getDairyCount()) {
            message += "dairy " + (yesterdaysDietPlan.getDailyDairy() - yesterdaysMeals.getDairyCount()) + " serves , ";
            count++;
        }
        if(yesterdaysDietPlan.getDailyGrain() > yesterdaysMeals.getGrainCount()) {
            message += "grain " + (yesterdaysDietPlan.getDailyGrain() - yesterdaysMeals.getGrainCount()) + " serves , ";
            count++;
        }
        if(yesterdaysDietPlan.getDailyFruit() > yesterdaysMeals.getFruitCount()) {
            message += "fruit " + (yesterdaysDietPlan.getDailyFruit() - yesterdaysMeals.getFruitCount()) + " serves , ";
            count++;
        }
        if(count > 0) {
            message = message.substring(0, message.length() - 2);
            message += ". If you eat extra today you can still hit your goals for yesterday!";
            return new Recommendation(id, title, message, expiry);
        }
        return null;
    }

    @Override
    public void updateListener() {
        //before informing the listener, make sure own data is accurate
        refreshDietPlans();
        super.updateListener();
    }
}
