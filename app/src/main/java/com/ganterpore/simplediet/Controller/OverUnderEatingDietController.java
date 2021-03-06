package com.ganterpore.simplediet.Controller;

import android.text.format.DateUtils;
import android.util.SparseArray;

import com.ganterpore.simplediet.Model.DietPlan;
import com.ganterpore.simplediet.Model.Meal.FoodType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class OverUnderEatingDietController extends BasicDietController{
    private static final String TAG = "OverUnderEatingDietCo";
    public static final String UNDER_EATING_RECOMMENDATION_ID = "under_eating";
    public static final String OVER_EATING_RECOMMENDATION_ID = "over_eating";
    private SparseArray<DietPlan> daysAgoDiets = new SparseArray<>();

    public OverUnderEatingDietController() {
        super();
    }

    public OverUnderEatingDietController(DietControllerListener listener) {
        super(listener);
    }

    @Override
    public DietPlan getDaysDietPlan(int nDaysAgo) {
        //check if the dietplan has already been made
        DietPlan daysDiet = daysAgoDiets.get(nDaysAgo);
        if(daysDiet != null) {
            return daysDiet;
        } else {
            //if not already made, than make it
            daysDiet = getOverallDietPlan().copy();
            daysDiet.setDailyVeges(getDaysFoodTypePlan(nDaysAgo, FoodType.VEGETABLE));
            daysDiet.setDailyProtein(getDaysFoodTypePlan(nDaysAgo, FoodType.MEAT));
            daysDiet.setDailyDairy(getDaysFoodTypePlan(nDaysAgo, FoodType.DAIRY));
            daysDiet.setDailyGrain(getDaysFoodTypePlan(nDaysAgo, FoodType.GRAIN));
            daysDiet.setDailyFruit(getDaysFoodTypePlan(nDaysAgo, FoodType.FRUIT));
            daysAgoDiets.put(nDaysAgo, daysDiet);
        }
        return daysAgoDiets.get(nDaysAgo);
    }

    private double getDaysFoodTypePlan(int nDaysAgo, FoodType foodType) {
        double yesterdaysServeOfFood = getDaysMeals(nDaysAgo+1).getServesOf(foodType);
        double dayBeforesServesOfFood = getDaysMeals(nDaysAgo+2).getServesOf(foodType);
        double standardRecommendation = getOverallDietPlan().getServesOf(foodType);
        //if the amount the user ate over the past two days was more than the standard recommendation
        if((yesterdaysServeOfFood + dayBeforesServesOfFood) > (standardRecommendation*2)) {
            double yesterdaysExcess = getExcess(nDaysAgo+1, foodType);
            double dayBeforesUnderEating = getDearth(nDaysAgo+2, foodType);
            //then double check the user went over the actual recommendation for yesterday, and it wasn't just to do with the day befores undereating
            double normalisedExcess = dayBeforesUnderEating < 0 ? yesterdaysExcess + dayBeforesUnderEating : yesterdaysExcess;
            if(normalisedExcess > 0) {
                return standardRecommendation - normalisedExcess;
            }
        }
        //otherwise, recommend the standard recommendation
        return standardRecommendation;
    }

    private double getExcess(int nDaysAgo, FoodType foodType) {
        double excess;
        DietPlan plan = getDaysDietPlan(nDaysAgo);
        DailyMeals meals = getDaysMeals(nDaysAgo);
        excess = meals.getServesOf(foodType) - plan.getServesOf(foodType);
        //if excess is less than 0, return 0
        return excess > 0 ? excess : 0;
    }

    private double getDearth(int nDaysAgo, FoodType foodType) {
        double dearth;
        DietPlan plan = getDaysDietPlan(nDaysAgo);
        DailyMeals meals = getDaysMeals(nDaysAgo);
        dearth = meals.getServesOf(foodType) - plan.getServesOf(foodType);
        return dearth < 0 ? dearth : 0;
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
        DietPlan todaysPlan = getTodaysDietPlan();
        DietPlan overallDiet = getOverallDietPlan();
        String id = OVER_EATING_RECOMMENDATION_ID;
        long expiry = DateUtils.DAY_IN_MILLIS;
        String title = "Over eating";
        String message = "Yesterday you ate too much";
        ArrayList<String> overAteFoods = new ArrayList<>();
        if(todaysPlan.getDailyVeges() < overallDiet.getDailyVeges()) {
            overAteFoods.add("veges");
        }
        if(todaysPlan.getDailyProtein() < overallDiet.getDailyProtein()) {
            overAteFoods.add("proteins");
        }
        if(todaysPlan.getDailyDairy() < overallDiet.getDailyDairy()) {
            overAteFoods.add("dairy");
        }
        if(todaysPlan.getDailyGrain() < overallDiet.getDailyGrain()) {
            overAteFoods.add("grain");
        }
        if(todaysPlan.getDailyFruit() < overallDiet.getDailyFruit()) {
            overAteFoods.add("fruit");
        }
        if(!overAteFoods.isEmpty()) {
            for(int i=0;i<overAteFoods.size();i++) {
                if(i==0) {
                    //dont add a joiner at start
                    message += " ";
                }
                else if(i==overAteFoods.size()-1) {
                    //if the final one, the joiner is an and
                    message += " and ";
                } else {
                    //otherwise a comma
                    message += ", ";
                }
                message += overAteFoods.get(i);
            }
            message += ". Your recommended intake for today has been adjusted to compensate for this.";
            return new Recommendation(id, title, message, expiry);
        }
        return null;
    }

    private Recommendation getUnderEatingRecommendation() {
        String id = UNDER_EATING_RECOMMENDATION_ID;
        long expiry = DateUtils.DAY_IN_MILLIS;
        String title = "Under eating";
        String message = "Yesterday you didn't eat enough from each food category! You can still make up for it today if you eat";
        int count = 0;
        DietPlan yesterdaysDietPlan = getDaysDietPlan(1);
        DailyMeals yesterdaysMeals = getDaysMeals(1);
        ArrayList<String> underAteFood = new ArrayList<>();
        if(!isFoodCompleted(1, FoodType.VEGETABLE)) {
            underAteFood.add((yesterdaysDietPlan.getDailyVeges()
                    - yesterdaysMeals.getVegCount() - getExcess(0, FoodType.VEGETABLE))
                    + " more serves of veges");
            count++;
        }
        if(!isFoodCompleted(1, FoodType.MEAT)) {
            underAteFood.add((yesterdaysDietPlan.getDailyProtein()
                    - yesterdaysMeals.getProteinCount() - getExcess(0, FoodType.MEAT))
                    + " more serves of protein");
            count++;
        }
        if(!isFoodCompleted(1, FoodType.DAIRY)) {
            underAteFood.add((yesterdaysDietPlan.getDailyDairy()
                    - yesterdaysMeals.getDairyCount() - getExcess(0, FoodType.DAIRY))
                    + " more serves of dairy");
            count++;
        }
        if(!isFoodCompleted(1, FoodType.GRAIN)) {
            underAteFood.add((yesterdaysDietPlan.getDailyGrain()
                    - yesterdaysMeals.getGrainCount() - getExcess(0, FoodType.GRAIN))
                    + " more serves of grain");
            count++;
        }
        if(!isFoodCompleted(1, FoodType.FRUIT)) {
            underAteFood.add((yesterdaysDietPlan.getDailyFruit()
                    - yesterdaysMeals.getFruitCount() - getExcess(0, FoodType.FRUIT))
                    + " more serves of fruit");
            count++;
        }
        if(!underAteFood.isEmpty()) {
            for(int i=0;i<underAteFood.size();i++) {
                if(i==0) {
                    //dont add a joiner at start
                    message += " ";
                }
                else if(i==underAteFood.size()-1) {
                    //if the final one, the joiner is an and
                    message += " and ";
                } else {
                    //otherwise a comma
                    message += ", ";
                }
                message += underAteFood.get(i);
            }
            return new Recommendation(id, title, message, expiry);
        }
        return null;
    }

    @Override
    public void updateListener(DataType dataType, List<Integer> daysAgoUpdated) {
        //with this controller, all days after the first day changed are invalidated by a change
        //So set them to be updated
        if(dataType == DataType.MEAL && daysAgoUpdated !=null) {
            int maxDaysAgo = Collections.max(daysAgoUpdated);
            ArrayList<Integer> newNeedsUpdate = new ArrayList<>();
            //removing all days after the maxDaysAgo, so they are updated, then setting them to be updated
            for(int i=maxDaysAgo;i>=0;i--) {
                daysAgoDiets.remove(i);
                newNeedsUpdate.add(i);
            }
            super.updateListener(dataType, newNeedsUpdate);
        } else {
            super.updateListener(dataType, daysAgoUpdated);
        }
        if(dataType == DataType.DIET_PLAN) {
            daysAgoDiets.clear();
        }
    }

    /**
     * get a date representing the start time of a given day
     * @param date, the date with which you want the start time of the day from
     * @return the date at the start of the given day
     */
    private Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        calendar.set(year, month, day, 0, 0, 0);
        return calendar.getTime();
    }
}
