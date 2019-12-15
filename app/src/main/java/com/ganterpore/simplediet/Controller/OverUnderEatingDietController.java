package com.ganterpore.simplediet.Controller;

import android.text.format.DateUtils;
import android.util.SparseArray;

import com.ganterpore.simplediet.Model.DietPlan;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class OverUnderEatingDietController extends BasicDietController{
    private static final String TAG = "OverUnderEatingDietCo";
    private DietControllerListener listener;
    private FirebaseFirestore db;
    private SparseArray<DietPlan> daysAgoDiets;

    public OverUnderEatingDietController(DietControllerListener listener) {
        super(listener);
        //initialising variables
        this.db = FirebaseFirestore.getInstance();
        this.listener = listener;
        daysAgoDiets = new SparseArray<>();
        //adding the diet plan to be tracked (todays), then refresh diet plans so it updates.
        daysAgoDiets.append(0, new DietPlan());
        refreshDietPlans();
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

            //if (essentially) no meals were eaten yesterday, assume that the user forgot to input
            //data, rather than didn't eat, and therefore don't adjust diet to absurd numbers.
            if(dayBeforesMeals.getTotalServes() < 0.5) {
                daysAgoDiets.put(nDaysAgo, getOverallDietPlan());
                continue;
            }

            //adjust the diet counts based on the last few days meals
            double vegCountAdjusted = adjustTodaysDiet(dayBeforesMeals.getVegCount(), twoDaysBeforeMeals.getVegCount(), getOverallDietPlan().getDailyVeges());
            double proteinCountAdjusted = adjustTodaysDiet(dayBeforesMeals.getProteinCount(), twoDaysBeforeMeals.getProteinCount(), getOverallDietPlan().getDailyProtein());
            double dairyCountAdjusted = adjustTodaysDiet(dayBeforesMeals.getDairyCount(), twoDaysBeforeMeals.getDairyCount(), getOverallDietPlan().getDailyDairy());
            double grainCountAdjusted = adjustTodaysDiet(dayBeforesMeals.getGrainCount(), twoDaysBeforeMeals.getGrainCount(), getOverallDietPlan().getDailyGrain());
            double fruitCountAdjusted = adjustTodaysDiet(dayBeforesMeals.getFruitCount(), twoDaysBeforeMeals.getFruitCount(), getOverallDietPlan().getDailyFruit());

            //create new dietPlan
            DietPlan newDiet = new DietPlan(vegCountAdjusted, proteinCountAdjusted,
                    dairyCountAdjusted, grainCountAdjusted, fruitCountAdjusted,
                    getOverallDietPlan().getDailyWater(), getOverallDietPlan().getWeeklyCheats(), getOverallDietPlan().getUser());

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
    private double adjustTodaysDiet(double yesterdaysCount, double dayBeforesCount, double expectedCount) {
        //normalised values is the difference between the count and the expected count
        double yesterdayNormalised = yesterdaysCount - expectedCount;
        double dayBeforeNormalised = dayBeforesCount - expectedCount;
        //adjuster is how much the days food should be adjusted by
        double adjuster;
        //in general, this is just the negative of amount of food over or under eaten yesterday
        //for example, you ate two too many yesterday, you can have two fewer today
        //However, in the special case, where you ate too much/little in the day before,
        //so yesterday you were making up for it, things are different
        if((yesterdayNormalised > 0 && dayBeforeNormalised < 0)
                || (yesterdayNormalised < 0 && dayBeforeNormalised > 0)) {
            //if the amount you ate yesterday overcompensated for the error from the day before
            //then take the difference of the two
            if (Math.abs(yesterdayNormalised) > Math.abs(dayBeforeNormalised)) {
                adjuster = -(yesterdayNormalised + dayBeforeNormalised);
            } else {
                //in the case where you didn't make up for it enough, cut your losses and reset
                adjuster = 0;
            }
        } else {
            adjuster = -yesterdayNormalised;
        }
        return adjuster + expectedCount;
    }

    @Override
    public List<Recommendation> getRecommendations() {
        //get the recommendations from the superior DietController
        ArrayList<Recommendation> recommendations = new ArrayList<>();
        recommendations.addAll(super.getRecommendations());
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
        DietPlan todaysDietPlan = getTodaysDietPlan();
        int count = 0;
        if(todaysDietPlan.getDailyVeges() > getOverallDietPlan().getDailyVeges()) {
            message += "veges, ";
            count++;
        }
        if(todaysDietPlan.getDailyProtein() > getOverallDietPlan().getDailyProtein()) {
            message += "proteins, ";
            count++;
        }
        if(todaysDietPlan.getDailyDairy() > getOverallDietPlan().getDailyDairy()) {
            message += "dairy, ";
            count++;
        }
        if(todaysDietPlan.getDailyGrain() > getOverallDietPlan().getDailyGrain()) {
            message += "grain, ";
            count++;
        }
        if(todaysDietPlan.getDailyFruit() > getOverallDietPlan().getDailyFruit()) {
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
    @Override
    public void updateListener() {
        listener.refresh();
    }

    @Override
    public void updateDailyMeals(DailyMeals day) {
        refreshDietPlans();
        updateListener();
    }
}
