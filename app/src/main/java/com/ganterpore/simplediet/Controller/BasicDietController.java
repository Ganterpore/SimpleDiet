package com.ganterpore.simplediet.Controller;

import android.os.Build;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;

import com.ganterpore.simplediet.Model.DietPlan;
import com.ganterpore.simplediet.Model.Meal;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

public class  BasicDietController implements DietController {
    private static final String TAG = "BasicDietController";
    private static BasicDietController instance;
    private List<DocumentSnapshot> data;
    private List<DietControllerListener> listeners;
    private String user;
    private DietPlan overallDiet;
    private FirebaseFirestore db;
    private SparseArray<DailyMeals> daysAgoMeals;
    private SparseBooleanArray mealNeedsUpdate;

    //TODO create factory for this method. Or remove interface.
    public static BasicDietController getInstance() {
        if(instance != null) {
            return instance;
        } else {
            return new BasicDietController();
        }
    }
    public static BasicDietController getInstance(DietControllerListener listener) {
        if(instance != null) {
            if(!instance.listeners.contains(listener)) {
                instance.listeners.add(listener);
            }
            return instance;
        } else {
            return new BasicDietController(listener);
        }
    }

    public BasicDietController() {
        //initialising variables
        this.db = FirebaseFirestore.getInstance();
        this.listeners = new ArrayList<>();
        this.user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        daysAgoMeals = new SparseArray<>();
        mealNeedsUpdate = new SparseBooleanArray();
        this.data = new ArrayList<>();

        //updating data
        getCurrentDietPlanFromDB();
        getCurrentMealDataFromDB();
        instance = this;
    }

    public BasicDietController(DietControllerListener listener) {
        this();
        this.listeners.add(listener);
    }

    public void addListener(DietControllerListener listener) {
        listeners.add(listener);
    }

    private void getCurrentMealDataFromDB() {
        Query dataQuery = db.collection(DailyMeals.MEALS).whereEqualTo("user", user);
        //check to update the data when it changes. This will also run through on the first time
        dataQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                //check if today is completed before the update
                boolean todayCompleted = isFoodCompletedToday();
                boolean yesterdayCompleted = isFoodCompleted(1);
                boolean waterCompleted = isHydrationCompletedToday();
                boolean cheatsOver = isOverCheatScoreToday();
                boolean foodEnteredRecently = false;
                if(queryDocumentSnapshots != null) {
                    //update data to the new changes
                    data = queryDocumentSnapshots.getDocuments();
                    //if running new enough android, sort the data by day
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        data.sort(new Comparator<DocumentSnapshot>() {
                            @Override
                            public int compare(DocumentSnapshot o1, DocumentSnapshot o2) {
                                double rawScore =  o1.getDouble("day") - o2.getDouble("day");
                                if(rawScore > 0) {return -1;}
                                else if(rawScore < 0) {return 1;}
                                else {return 0;}
                            }
                        });
                    }

                    //check which data has changed, and set them to be updated when next accessed
                    List<DocumentChange> changedData = queryDocumentSnapshots.getDocumentChanges();
                    for(DocumentChange documentChange : changedData) {
                        //getting how many days ago
                        QueryDocumentSnapshot document = documentChange.getDocument();
                        long changedDay = document.toObject(Meal.class).getDay();
                        if(changedDay > System.currentTimeMillis() - DateUtils.MINUTE_IN_MILLIS) {
                            foodEnteredRecently = true;
                        }
                        Date changedDayStart = getStartOfDay(new Date(changedDay));
                        long msDiff = System.currentTimeMillis() - changedDayStart.getTime();
                        int daysAgo = (int) TimeUnit.MILLISECONDS.toDays(msDiff);
                        //setting that day to needing an update
                        for(int i=0;i<8;i++) {
                            mealNeedsUpdate.put(daysAgo+i, true);
                        }
                    }
                }
                //checking if any achievable was not completed before, and it is completed now,
                //and this change was recent
                // then update the listener to know it was just completed
                if(!todayCompleted) {
                    if(isFoodCompletedToday() && foodEnteredRecently) {
                        for(DietControllerListener listener : listeners) {
                            listener.todaysFoodCompleted();
                        }
                    }
                }
                if(!yesterdayCompleted) {
                    if(isFoodCompleted(1) && foodEnteredRecently) {
                        for(DietControllerListener listener : listeners) {
                            listener.yesterdaysFoodCompleted();
                        }
                    }
                }
                if(!waterCompleted) {
                    if(isHydrationCompletedToday() && foodEnteredRecently) {
                        for(DietControllerListener listener : listeners) {
                            listener.todaysHydrationCompleted();
                        }
                    }
                }
                if(!cheatsOver) {
                    if(isOverCheatScoreToday() && foodEnteredRecently) {
                        for(DietControllerListener listener : listeners) {
                            listener.todaysCheatsOver();
                        }
                    }
                }
                //now that the data is updated, make sure it flows through to the listener
                updateListener();
            }
        });
    }

    /**
     * Updates the diet plan to the current one on the db for the user.
     */
    private void getCurrentDietPlanFromDB() {
        overallDiet = new DietPlan();
        //document reference to the diet plan
        DocumentReference dietPlanQuery = db.collection(DietPlan.COLLECTION_NAME).document(user);
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
        //updating if null or data has changed
        if(daysMeal == null || mealNeedsUpdate.get(nDaysAgo, false)) {
            daysMeal = new DailyMeals(nDaysAgo, data);
            daysAgoMeals.put(nDaysAgo, daysMeal);
            //now that it has been updated, it doesn't need one.
            mealNeedsUpdate.put(nDaysAgo, false);
        }
        return daysMeal;
    }

    @Override
    public DietPlan getTodaysDietPlan() {
        return getDaysDietPlan(0);
    }
    @Override
    public DietPlan getDaysDietPlan(int nDaysAgo) {
        return getOverallDietPlan(); //For the BasicDietController, all days diet plans and the overall diet plan are the same
    }

    @Override
    public WeeklyIntake getThisWeeksIntake() {
        return new WeeklyIntake(data, overallDiet);
    }

    @Override
    public WeeklyIntake getWeeksIntake(int weeksAgo) {
        return new WeeklyIntake(data, overallDiet, weeksAgo);
    }

    @Override
    public DietPlan getOverallDietPlan() {
        return overallDiet;
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
    public boolean isHydrationCompletedToday() {
        return isHydrationCompleted(0);
    }
    @Override
    public boolean isHydrationCompleted(int nDaysAgo) {
        return isHydrationCompleted(getDaysMeals(nDaysAgo), getDaysDietPlan(nDaysAgo));
    }
    private boolean isHydrationCompleted(DailyMeals meals, DietPlan dietPlan) {
        return meals.getHydrationScore() >= dietPlan.getDailyHydration();
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
        return meals.getTotalCheats() > dietPlan.getDailyCheats();
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
            fortnightlyCheats += getDaysMeals(i).getTotalCheats();
        }
        //finding te max and min cheat points we should have
        double tooManyCheats = getOverallDietPlan().getDailyCheats()*14*(1+SCALE_FACTOR);
        double tooFewCheats = getOverallDietPlan().getDailyCheats()*14*(1-SCALE_FACTOR);
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
            overAteMessage += "proteins, ";
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
            underAteMessage += "proteins, ";
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
        if(fortnightlyWater < (14* overallDiet.getDailyHydration() - 7)) {
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

        WeeklyIntake thisWeeksMeals = getThisWeeksIntake();
        //checks if we are over the cheat score for the week
        if(thisWeeksMeals.getTotalCheats() > thisWeeksMeals.getWeeklyLimitCheats()) {
            title += "You have had too many cheat meals this week!";
            //checks whether you will still be over the score tomorrow
            double cheatsTomorrow = thisWeeksMeals.getTotalCheats() - getDaysMeals(6).getTotalCheats();
            if(cheatsTomorrow < thisWeeksMeals.getWeeklyLimitCheats()) {
                //if not, then advise how few cheat points to have to get back on track
                message += "You will be back under your score tomorrow if you have less than ";
                message += (thisWeeksMeals.getWeeklyLimitCheats() - cheatsTomorrow);
                message += " cheat points today";
            } else {
                //if not, how many days until you are under again
                double currentCheats = thisWeeksMeals.getTotalCheats();
                for(int i=6;i>=0;i--) {
                    currentCheats -= getDaysMeals(i).getTotalCheats();
                    if(currentCheats < thisWeeksMeals.getTotalCheats()) {
                        message += "You can be back on track within ";
                        message += (7 - i);
                        message += " days if you minimise the bad food you eat";
                        break;
                    }
                }
            }
        } //if we are not over the cheat score, check if we are close
        else if((thisWeeksMeals.getTotalCheats() + (overallDiet.getDailyCheats()))
                >= overallDiet.getWeeklyCheats()){
            title += "You are very close to going over your cheat score";
            message += "If you have more than ";
            message += overallDiet.getWeeklyCheats() - thisWeeksMeals.getTotalCheats();
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
        for(DietControllerListener listener : listeners) {
            listener.refresh();
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
