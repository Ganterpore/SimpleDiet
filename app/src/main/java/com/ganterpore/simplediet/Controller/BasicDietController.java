package com.ganterpore.simplediet.Controller;

import android.text.format.DateUtils;
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

    private DietControllerListener listener;
    private String user;
    private DailyMeals todaysMeals;
    private DietPlan todaysDiet;
    private FirebaseFirestore db;
    private SparseArray<DailyMeals> daysAgoMeals;

    public BasicDietController(DietControllerListener listener) {
        //initialising variabls
        this.db = FirebaseFirestore.getInstance();
        this.listener = listener;
        this.user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        todaysMeals = new DailyMeals(this, user);
        todaysDiet = new DietPlan(0,0,0,0,0,0,0,"");
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
                        todaysDiet = documentSnapshot.toObject(DietPlan.class);
                        updateListener();
                    }
                });
        //making sure any changes to the data are tracked
        dietPlanQuery.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot == null) {
                    todaysDiet = new DietPlan();
                } else {
                    todaysDiet = documentSnapshot.toObject(DietPlan.class);
                }
                updateListener();
            }
        });
    }

    @Override
    public DailyMeals getTodaysMeals() {
        return todaysMeals;
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
        return todaysDiet;
    }
    @Override
    public DietPlan getDaysDietPlan(int nDaysAgo) {
        return getTodaysDietPlan(); //For the BasicDietController, both todays and the overall diet plan are the same
    }

    @Override
    public DietPlan getOverallDietPlan() {
        return getTodaysDietPlan(); //For the BasicDietController, both todays and the overall diet plan are the same
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
                todaysDiet = newDietPlan;
                updateListener();
            }
        });
        return updateDiet;
    }

    @Override
    public boolean isFoodCompletedToday() {
        return isFoodCompleted(todaysMeals);
    }
    @Override
    public boolean isFoodCompleted(int nDaysAgo) {
        return isFoodCompleted(getDaysMeals(nDaysAgo));
    }
    private boolean isFoodCompleted(DailyMeals meal) {
        return meal.getVegCount ()>= todaysDiet.getDailyVeges()
                & meal.getProteinCount ()>= todaysDiet.getDailyProtein()
                & meal.getDairyCount ()>= todaysDiet.getDailyDairy()
                & meal.getGrainCount ()>= todaysDiet.getDailyGrain()
                & meal.getFruitCount ()>= todaysDiet.getDailyFruit();
    }

    @Override
    public boolean isWaterCompletedToday() {
        return isWaterCompleted(todaysMeals);
    }
    @Override
    public boolean isWaterCompleted(int nDaysAgo) {
        return isWaterCompleted(getDaysMeals(nDaysAgo));
    }
    private boolean isWaterCompleted(DailyMeals meals) {
        return meals.getWaterCount() >= todaysDiet.getDailyWater();
    }

    @Override
    public boolean isOverCheatScoreToday() {
        return isOverCheatScore(todaysMeals);
    }
    @Override
    public boolean isOverCheatScore(int nDaysAgo) {
        return isOverCheatScore(getDaysMeals(nDaysAgo));
    }
    private boolean isOverCheatScore(DailyMeals meals) {
        return meals.getWeeklyCheats() > todaysDiet.getWeeklyCheats();
    }

    @Override
    public List<Recommendation> getRecommendations() {
        ArrayList<Recommendation> recommendations = new ArrayList<>();
        Recommendation dietChangeRecommendation = getDietChangeRecommendation();
        Recommendation cheatScoreRecommendation = getCheatScoreRecommendation();
        if(dietChangeRecommendation != null) {
            recommendations.add(dietChangeRecommendation);
        }
        if(cheatScoreRecommendation != null) {
            recommendations.add(cheatScoreRecommendation);
        }
        return recommendations;
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
        //getting the foor from the last fortnight
        for(int i=0;i<14;i++) {
            DailyMeals daysMeals = getDaysMeals(i);
            fortnightlyVeges += daysMeals.getVegCount();
            fortnightlyProtein += daysMeals.getProteinCount();
            fortnightlyDairy += daysMeals.getDairyCount();
            fortnightlyGrain += daysMeals.getGrainCount();
            fortnightlyFruit += daysMeals.getFruitCount();
            fortnightlyWater += daysMeals.getWaterCount();
        }
        //creating the message tot he user
        String message = "Over the past two weeks you have been eating ";
        boolean overAte = false;
        String overAteMessage = "too much ";
        if(fortnightlyVeges > (14*todaysDiet.getDailyVeges() + 7)) {
            overAte = true;
            overAteMessage += "vegetables, ";
        }
        if(fortnightlyProtein > (14*todaysDiet.getDailyProtein() + 7)) {
            overAte = true;
            overAteMessage += "meats, ";
        }
        if(fortnightlyDairy > (14*todaysDiet.getDailyDairy() + 7)) {
            overAte = true;
            overAteMessage += "dairy, ";
        }
        if(fortnightlyGrain > (14*todaysDiet.getDailyGrain() + 7)) {
            overAte = true;
            overAteMessage += "grains, ";
        }
        if(fortnightlyFruit > (14*todaysDiet.getDailyFruit() + 7)) {
            overAte = true;
            overAteMessage += "fruits, ";
        }

        boolean underAte = false;
        String underAteMessage = "too little ";
        if(overAte) {
            message += overAteMessage;
            underAteMessage = "and too little ";
        }

        if(fortnightlyVeges < (14*todaysDiet.getDailyVeges() - 7)) {
            underAte = true;
            underAteMessage += "vegetables, ";
        }
        if(fortnightlyProtein < (14*todaysDiet.getDailyProtein() - 7)) {
            underAte = true;
            underAteMessage += "meats, ";
        }
        if(fortnightlyDairy < (14*todaysDiet.getDailyDairy() - 7)) {
            underAte = true;
            underAteMessage += "dairy, ";
        }
        if(fortnightlyGrain < (14*todaysDiet.getDailyGrain() - 7)) {
            underAte = true;
            underAteMessage += "grains, ";
        }
        if(fortnightlyFruit < (14*todaysDiet.getDailyFruit() - 7)) {
            underAte = true;
            underAteMessage += "fruit, ";
        }
        if(fortnightlyWater < (14*todaysDiet.getDailyWater() - 7)) {
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
            if(cheatsTomorrow < todaysDiet.getWeeklyCheats()) {
                //if not, then advise how few cheat points to have to get back on track
                message += "You will be back under your score tomorrow if you have less than ";
                message += (todaysDiet.getWeeklyCheats() - cheatsTomorrow);
                message += " cheat points tomorrow";
            } else {
                //if not, how many days until you are under again
                double currentCheats = todaysMeals.getWeeklyCheats();
                for(int i=6;i>=0;i--) {
                    currentCheats -= getDaysMeals(i).getTotalCheats();
                    if(currentCheats < todaysDiet.getWeeklyCheats()) {
                        message += "You can be back on track within ";
                        message += (7 - i);
                        message += "days if you minimise the bad food you eat";
                        break;
                    }
                }
            }
        } //if we are not over the cheat score, check if we are close
        else if((todaysMeals.getWeeklyCheats() + (todaysDiet.getWeeklyCheats()/7))
                >= todaysDiet.getWeeklyCheats()){
            title += "You are very close to going over your cheat score";
            message += "If you have more than ";
            message += todaysDiet.getWeeklyCheats() - todaysMeals.getWeeklyCheats();
            message += " cheat points today you will go over your maximum cheat score. ";
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
