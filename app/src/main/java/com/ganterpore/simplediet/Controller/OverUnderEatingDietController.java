package com.ganterpore.simplediet.Controller;

import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseArray;

import com.ganterpore.simplediet.Model.Meal.FoodType;
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

    private double getExcess(int nDaysAgo, FoodType foodType) {
        double excess;
        DietPlan plan = getDaysDietPlan(nDaysAgo);
        DailyMeals meals = getDaysMeals(nDaysAgo);
        switch (foodType) {
            case VEGETABLE:
                excess = meals.getVegCount() - plan.getDailyVeges();
                break;
            case MEAT:
                excess = meals.getProteinCount() - plan.getDailyProtein();
                break;
            case DAIRY:
                excess = meals.getDairyCount() - plan.getDailyDairy();
                break;
            case GRAIN:
                excess = meals.getGrainCount() - plan.getDailyGrain();
                break;
            case FRUIT:
                excess = meals.getFruitCount() - plan.getDailyFruit();
                break;
            default:
                excess = 0.0;
        }
        //if excess is less than 0, return 0
        return excess > 0 ? excess : 0;
    }

    public boolean isFoodCompleted(int nDaysAgo, FoodType foodType) {
        DailyMeals daysMeals = getDaysMeals(nDaysAgo);
        DietPlan daysDietPlan = getDaysDietPlan(nDaysAgo);

        double plan=0;
        double count=0;
        double nextDaysExcess=0;

        switch (foodType) {
            case VEGETABLE:
                count = daysMeals.getVegCount();
                plan = daysDietPlan.getDailyVeges();
                nextDaysExcess = getExcess(nDaysAgo-1, foodType);
                break;
            case MEAT:
                count = daysMeals.getProteinCount();
                plan = daysDietPlan.getDailyProtein();
                nextDaysExcess = getExcess(nDaysAgo-1, foodType);
                break;
            case DAIRY:
                count = daysMeals.getDairyCount();
                plan = daysDietPlan.getDailyDairy();
                nextDaysExcess = getExcess(nDaysAgo-1, foodType);
                break;
            case GRAIN:
                count = daysMeals.getGrainCount();
                plan = daysDietPlan.getDailyGrain();
                nextDaysExcess = getExcess(nDaysAgo-1, foodType);
                break;
            case FRUIT:
                count = daysMeals.getFruitCount();
                plan = daysDietPlan.getDailyFruit();
                nextDaysExcess = getExcess(nDaysAgo-1, foodType);
                break;
        }

        //food completion under this controller is true if the sum of the food eaten today
        //and tomorrow's is above the diet plan. This means you can make up for the undereating yesterday today
        return (count + nextDaysExcess) >= plan;
    }

    @Override
    public boolean isFoodCompleted(int nDaysAgo) {
        return isFoodCompleted(nDaysAgo, FoodType.VEGETABLE)
                && isFoodCompleted(nDaysAgo, FoodType.MEAT)
                && isFoodCompleted(nDaysAgo, FoodType.DAIRY)
                && isFoodCompleted(nDaysAgo, FoodType.GRAIN)
                && isFoodCompleted(nDaysAgo, FoodType.FRUIT);
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
        String message = "Yesterday you didn't eat enough from each food category! You can still make up for it today if you eat ";
        int count = 0;
        DietPlan yesterdaysDietPlan = getDaysDietPlan(1);
        DailyMeals yesterdaysMeals = getDaysMeals(1);
        if(!isFoodCompleted(1, FoodType.VEGETABLE)) {
            message += (yesterdaysDietPlan.getDailyVeges()
                    - yesterdaysMeals.getVegCount() - getExcess(0, FoodType.VEGETABLE))
                    + " extra serves of veges, ";
            count++;
        }
        if(!isFoodCompleted(1, FoodType.MEAT)) {
            message += (yesterdaysDietPlan.getDailyProtein()
                    - yesterdaysMeals.getProteinCount() - getExcess(0, FoodType.MEAT))
                    + " extra serves of protein, ";
            count++;
        }
        if(!isFoodCompleted(1, FoodType.DAIRY)) {
            message += (yesterdaysDietPlan.getDailyDairy()
                    - yesterdaysMeals.getDairyCount() - getExcess(0, FoodType.DAIRY))
                    + " extra serves of dairy, ";
            count++;
        }
        if(!isFoodCompleted(1, FoodType.GRAIN)) {
            message += (yesterdaysDietPlan.getDailyGrain()
                    - yesterdaysMeals.getGrainCount() - getExcess(0, FoodType.GRAIN))
                    + " extra serves of grain, ";
            count++;
        }
        if(!isFoodCompleted(1, FoodType.FRUIT)) {
            message += (yesterdaysDietPlan.getDailyFruit()
                    - yesterdaysMeals.getFruitCount() - getExcess(0, FoodType.FRUIT))
                    + " extra serves of fruit, ";
            count++;
        }
        if(count > 0) {
            message = message.substring(0, message.length() - 2);
            message += " on top of your normal diet.";
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
