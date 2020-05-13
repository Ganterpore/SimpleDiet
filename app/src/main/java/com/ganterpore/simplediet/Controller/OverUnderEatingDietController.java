package com.ganterpore.simplediet.Controller;

import android.text.format.DateUtils;
import android.util.SparseArray;

import com.ganterpore.simplediet.Model.DietPlan;
import com.ganterpore.simplediet.Model.Meal;
import com.ganterpore.simplediet.Model.Meal.FoodType;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;


public class OverUnderEatingDietController extends BasicDietController{
    private static final String TAG = "OverUnderEatingDietCo";
    private SparseArray<DietPlan> daysAgoDiets;

    public OverUnderEatingDietController() {
        super();
        //initialising variables
        daysAgoDiets = new SparseArray<>();

        //if a meal is added on a day, all diet plans for days after that day become wrong
        //these need to be removed so that if they are accessed again they will be correct
        Query dataQuery = FirebaseFirestore.getInstance().collection(DailyMeals.MEALS).whereEqualTo("user", FirebaseAuth.getInstance().getCurrentUser().getUid());
        //check to update the data when it changes. This will also run through on the first time
        //TODO remove this snapshot when refresh updated, and override update listeners to do this
        dataQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(queryDocumentSnapshots != null) {
                    //check the day of the data changes, and get the max days ago
                    int maxDaysAgo = 0;
                    List<DocumentChange> changedData = queryDocumentSnapshots.getDocumentChanges();
                    for(DocumentChange documentChange : changedData) {
                        //getting how many days ago
                        QueryDocumentSnapshot document = documentChange.getDocument();
                        long changedDay = document.toObject(Meal.class).getDay();
                        Date changedDayStart = getStartOfDay(new Date(changedDay));
                        long msDiff = System.currentTimeMillis() - changedDayStart.getTime();
                        int daysAgo = (int) TimeUnit.MILLISECONDS.toDays(msDiff);
                        //updating the max
                        if(daysAgo > maxDaysAgo) {
                            maxDaysAgo = daysAgo;
                        }
                    }
                    //removing all days after the maxDaysAgo, so they are updated
                    for(int i=maxDaysAgo-1;i>=0;i--) {
                        daysAgoDiets.remove(i);
                    }
                    updateListener();
                }
            }
        });
    }

    public OverUnderEatingDietController(DietControllerListener listener) {
        this();
        super.addListener(listener);
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
        String id = "over_eating";
        long expiry = DateUtils.DAY_IN_MILLIS;
        String title = "Over eating";
        String message = "Yesterday you ate too much ";
        int count = 0;
        if(todaysPlan.getDailyVeges() < overallDiet.getDailyVeges()) {
            message += "veges, ";
            count++;
        }
        if(todaysPlan.getDailyProtein() < overallDiet.getDailyProtein()) {
            message += "proteins, ";
            count++;
        }
        if(todaysPlan.getDailyDairy() < overallDiet.getDailyDairy()) {
            message += "dairy, ";
            count++;
        }
        if(todaysPlan.getDailyGrain() < overallDiet.getDailyGrain()) {
            message += "grain, ";
            count++;
        }
        if(todaysPlan.getDailyFruit() < overallDiet.getDailyFruit()) {
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
                    + " more serves of veges, ";
            count++;
        }
        if(!isFoodCompleted(1, FoodType.MEAT)) {
            message += (yesterdaysDietPlan.getDailyProtein()
                    - yesterdaysMeals.getProteinCount() - getExcess(0, FoodType.MEAT))
                    + " more serves of protein, ";
            count++;
        }
        if(!isFoodCompleted(1, FoodType.DAIRY)) {
            message += (yesterdaysDietPlan.getDailyDairy()
                    - yesterdaysMeals.getDairyCount() - getExcess(0, FoodType.DAIRY))
                    + " more serves of dairy, ";
            count++;
        }
        if(!isFoodCompleted(1, FoodType.GRAIN)) {
            message += (yesterdaysDietPlan.getDailyGrain()
                    - yesterdaysMeals.getGrainCount() - getExcess(0, FoodType.GRAIN))
                    + " more serves of grain, ";
            count++;
        }
        if(!isFoodCompleted(1, FoodType.FRUIT)) {
            message += (yesterdaysDietPlan.getDailyFruit()
                    - yesterdaysMeals.getFruitCount() - getExcess(0, FoodType.FRUIT))
                    + " more serves of fruit, ";
            count++;
        }
        if(count > 0) {
            message = message.substring(0, message.length() - 2);
            return new Recommendation(id, title, message, expiry);
        }
        return null;
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
