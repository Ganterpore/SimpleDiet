package com.ganterpore.simplediet.Controller;

import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseArray;

import com.ganterpore.simplediet.Model.DietPlan;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class BasicDietController implements DietController, DailyMeals.DailyMealsInterface {

    private static final String TAG = "BasicDietController";
    private DietControllerListener listener;
    private String user;
    private DailyMeals todaysMeals;
    private DietPlan overallDiet;
    private FirebaseFirestore db;
    private SparseArray<DailyMeals> daysAgoMeals;

    public BasicDietController(DietControllerListener listener) {
        //initialising variables
        this.db = FirebaseFirestore.getInstance();
        this.listener = listener;
        this.user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        todaysMeals = new DailyMeals(this, user);
        overallDiet = new DietPlan(0,0,0,0,0,0,0,"");
        getCurrentDietPlanFromDB();
        daysAgoMeals = new SparseArray<>();
        daysAgoMeals.append(0, todaysMeals);
    }

    /**
     * Updates the diet plan to the current one on the db for the user.
     */
    private void getCurrentDietPlanFromDB() {
        //document reference to the diet plan
        DocumentReference dietPlanQuery = db.collection(DietPlan.COLLECTION_NAME).document(user);
        //getting the data and updating
        dietPlanQuery.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        overallDiet = documentSnapshot.toObject(DietPlan.class);
                        updateListener();
                    }
                });
        //making sure any changes to the data are tracked
        dietPlanQuery.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot == null) {
                    overallDiet = new DietPlan();
                } else {
                    overallDiet = documentSnapshot.toObject(DietPlan.class);
                }
                updateListener();
            }
        });
    }

    @Override
    public DailyMeals getTodaysMeals() {
        return getDaysMeals(0);
    }
    @Override
    public DailyMeals getDaysMeals(int nDaysAgo) {
        DailyMeals daysMeal = daysAgoMeals.get(nDaysAgo);
        if(daysMeal == null) {
            daysMeal = new DailyMeals(this, user, nDaysAgo);
            daysAgoMeals.append(nDaysAgo, daysMeal);
        }
        return daysMeal;
    }

    @Override
    public DietPlan getTodaysDietPlan() {
        return getDaysDietPlan(0);
    }
    @Override
    public DietPlan getDaysDietPlan(int nDaysAgo) {
        return getOverallDietPlan(); //For the BasicDietController, both todays and the overall diet plan are the same
    }

    @Override
    public DietPlan getOverallDietPlan() {
        return overallDiet; //For the BasicDietController, both todays and the overall diet plan are the same
    }

    @Override
    public Task<Void> updateDietPlan(final DietPlan newDietPlan) {
        //updating the database
        Task<Void> updateDiet = db.collection(DietPlan.COLLECTION_NAME)
                .document(newDietPlan.getUser())
                .set(newDietPlan);
        //updating this object with the new data
        updateDiet.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                overallDiet = newDietPlan;
                updateListener();
            }
        });
        return updateDiet;
    }

    @Override
    public boolean isFoodCompletedToday() {
        return isFoodCompleted(0);
    }
    @Override
    public boolean isFoodCompleted(int nDaysAgo) {
        return isFoodCompleted(getDaysMeals(nDaysAgo), getDaysDietPlan(nDaysAgo));
    }
    private boolean isFoodCompleted(DailyMeals meal, DietPlan dietPlan) {
        return meal.getVegCount ()>= dietPlan.getDailyVeges()
                & meal.getProteinCount ()>= dietPlan.getDailyProtein()
                & meal.getDairyCount ()>= dietPlan.getDailyDairy()
                & meal.getGrainCount ()>= dietPlan.getDailyGrain()
                & meal.getFruitCount ()>= dietPlan.getDailyFruit();
    }

    @Override
    public boolean isWaterCompletedToday() {
        return isWaterCompleted(0);
    }
    @Override
    public boolean isWaterCompleted(int nDaysAgo) {
        return isWaterCompleted(getDaysMeals(nDaysAgo), getDaysDietPlan(nDaysAgo));
    }
    private boolean isWaterCompleted(DailyMeals meals, DietPlan dietPlan) {
        return meals.getWaterCount() >= dietPlan.getDailyWater();
    }

    @Override
    public boolean isOverCheatScoreToday() {
        return isOverCheatScore(0);
    }
    @Override
    public boolean isOverCheatScore(int nDaysAgo) {
        return isOverCheatScore(getDaysMeals(nDaysAgo), getDaysDietPlan(nDaysAgo));
    }
    private boolean isOverCheatScore(DailyMeals meals, DietPlan dietPlan) {
        return meals.getWeeklyCheats() > dietPlan.getWeeklyCheats();
    }
    @Override
    public List<Recommendation> getRecommendations() {
        ArrayList<Recommendation> recommendations = new ArrayList<>();
        Recommendation cheatChangeRecommendation = getCheatChangeRecommendation();
        Recommendation dietChangeRecommendation = getDietChangeRecommendation();
        Recommendation cheatScoreRecommendation = getCheatScoreRecommendation();
        if(cheatChangeRecommendation != null) {
            recommendations.add(cheatChangeRecommendation);
        }
        if(dietChangeRecommendation != null) {
            recommendations.add(dietChangeRecommendation);
        }
        if(cheatScoreRecommendation != null) {
            recommendations.add(cheatScoreRecommendation);
        }
        return recommendations;
    }

    private Recommendation getCheatChangeRecommendation() {
        //if we are over/under the target cheat points by more than this factor, suggest a change
        final double SCALE_FACTOR = 0.2;
        String id = "cheat_change";
        long expiry = DateUtils.WEEK_IN_MILLIS * 2;
        String title;
        String message;

        //get the number of cheats in the last fortnight
        double fortnightlyCheats = 0;
        for(int i=0;i<14;i++) {
            fortnightlyCheats += getDaysMeals(i).getWeeklyCheats();
        }
        //finding te max and min cheat points we should have
        double tooManyCheats = getOverallDietPlan().getWeeklyCheats()*14*(1+SCALE_FACTOR);
        double tooFewCheats = getOverallDietPlan().getWeeklyCheats()*14*(1-SCALE_FACTOR);
        //if over or under by either of these numbers, update the message
        if(fortnightlyCheats > tooManyCheats) {
            title = "Increase your cheat score";
            message = "Over the past two weeks you have been having too many cheat meals. " +
                    "Consider giving yourself a break and increasing your maximum. You can always" +
                    " reduce later at a more achievable rate.";
        } else if(fortnightlyCheats < tooFewCheats) {
            title = "Decrease your cheat score";
            message = "Well done! Over the past two weeks you have been well under your maximum cheat score. " +
                    "Consider giving yourself a challenge and reducing your allowed cheat meals.";
        } else {
            return null;
        }
        return new Recommendation(id, title, message, expiry);
    }

    private Recommendation getDietChangeRecommendation() {
        String id = "diet_change";
        long expiry = DateUtils.WEEK_IN_MILLIS * 2;
        String title = "Recommendations for diet changes";
        //over or under ate by a large degree over the past fortnight
        double fortnightlyVeges = 0;
        double fortnightlyProtein = 0;
        double fortnightlyDairy = 0;
        double fortnightlyGrain = 0;
        double fortnightlyFruit = 0;
        double fortnightlyWater = 0;
        //getting the food from the last fortnight
        for(int i=0;i<14;i++) {
            DailyMeals daysMeals = getDaysMeals(i);
            fortnightlyVeges += daysMeals.getVegCount();
            fortnightlyProtein += daysMeals.getProteinCount();
            fortnightlyDairy += daysMeals.getDairyCount();
            fortnightlyGrain += daysMeals.getGrainCount();
            fortnightlyFruit += daysMeals.getFruitCount();
            fortnightlyWater += daysMeals.getWaterCount();
        }
        //creating the message to the user
        String message = "Over the past two weeks you have been eating ";
        boolean overAte = false;
        String overAteMessage = "too much ";
        if(fortnightlyVeges > (14* overallDiet.getDailyVeges() + 7)) {
            overAte = true;
            overAteMessage += "vegetables, ";
        }
        if(fortnightlyProtein > (14* overallDiet.getDailyProtein() + 7)) {
            overAte = true;
            overAteMessage += "meats, ";
        }
        if(fortnightlyDairy > (14* overallDiet.getDailyDairy() + 7)) {
            overAte = true;
            overAteMessage += "dairy, ";
        }
        if(fortnightlyGrain > (14* overallDiet.getDailyGrain() + 7)) {
            overAte = true;
            overAteMessage += "grains, ";
        }
        if(fortnightlyFruit > (14* overallDiet.getDailyFruit() + 7)) {
            overAte = true;
            overAteMessage += "fruits, ";
        }

        boolean underAte = false;
        String underAteMessage = "too little ";
        if(overAte) {
            message += overAteMessage;
            underAteMessage = "and too little ";
        }

        if(fortnightlyVeges < (14* overallDiet.getDailyVeges() - 7)) {
            underAte = true;
            underAteMessage += "vegetables, ";
        }
        if(fortnightlyProtein < (14* overallDiet.getDailyProtein() - 7)) {
            underAte = true;
            underAteMessage += "meats, ";
        }
        if(fortnightlyDairy < (14* overallDiet.getDailyDairy() - 7)) {
            underAte = true;
            underAteMessage += "dairy, ";
        }
        if(fortnightlyGrain < (14* overallDiet.getDailyGrain() - 7)) {
            underAte = true;
            underAteMessage += "grains, ";
        }
        if(fortnightlyFruit < (14* overallDiet.getDailyFruit() - 7)) {
            underAte = true;
            underAteMessage += "fruit, ";
        }
        if(fortnightlyWater < (14* overallDiet.getDailyWater() - 7)) {
            underAte = true;
            underAteMessage += "water, ";
        }

        if(underAte) {
            message += underAteMessage;
        }

        //finalising the recommendation, or returning null if no recommendation
        if(overAte || underAte) {
            message = message.substring(0, message.length()-2) + ". ";
            message += "Try to adjust your diet to make up for this. " +
                    "Alternatively update your diet plan to reflect your actual planned diet";
            return new Recommendation(id, title, message, expiry);
        }
        return null;
    }

    private Recommendation getCheatScoreRecommendation() {
        String id = "cheat";
        long expiry = DateUtils.DAY_IN_MILLIS;
        String title = "";
        String message = "";

        DailyMeals todaysMeals = getTodaysMeals();
        //checks if we are over the cheat score for the week
        if(isOverCheatScoreToday()) {
            title += "You have had too many cheat meals!";
            //checks whether you will still be over the score tomorrow
            double cheatsTomorrow = todaysMeals.getWeeklyCheats() - getDaysMeals(6).getTotalCheats();
            if(cheatsTomorrow < overallDiet.getWeeklyCheats()) {
                //if not, then advise how few cheat points to have to get back on track
                message += "You will be back under your score tomorrow if you have less than ";
                message += (overallDiet.getWeeklyCheats() - cheatsTomorrow);
                message += " cheat points today";
            } else {
                //if not, how many days until you are under again
                double currentCheats = todaysMeals.getWeeklyCheats();
                for(int i=6;i>=0;i--) {
                    currentCheats -= getDaysMeals(i).getTotalCheats();
                    if(currentCheats < overallDiet.getWeeklyCheats()) {
                        message += "You can be back on track within ";
                        message += (7 - i);
                        message += " days if you minimise the bad food you eat";
                        break;
                    }
                }
            }
        } //if we are not over the cheat score, check if we are close
        else if((todaysMeals.getWeeklyCheats() + (overallDiet.getWeeklyCheats()/7))
                >= overallDiet.getWeeklyCheats()){
            title += "You are very close to going over your cheat score";
            message += "If you have more than ";
            message += overallDiet.getWeeklyCheats() - todaysMeals.getWeeklyCheats();
            message += " cheat points today you will go over your maximum cheat score.";
            message += "Try to eat healthily today";

        } else {
            //if we are not over, or close to going over, send no recomendation.
            return null;
        }
        return new Recommendation(id, title, message, expiry);
    }

    @Override
    public void updateListener() {
        listener.refresh();
    }

    @Override
    public void updateDailyMeals(DailyMeals day) {
        updateListener();
    }
}
