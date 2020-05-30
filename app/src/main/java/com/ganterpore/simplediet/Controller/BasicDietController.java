package com.ganterpore.simplediet.Controller;

import android.os.Build;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;

import com.ganterpore.simplediet.Model.DietPlan;
import com.ganterpore.simplediet.Model.Meal;
import com.ganterpore.simplediet.View.Activities.MainActivity;
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
    public static final String CHEAT_CHANGE_RECOMMENDATION_ID = "cheat_change";
    public static final String DIET_CHANGE_RECOMMENDATION_ID = "diet_change";
    public static final String CHEAT_SCORE_RECOMMENDATION_ID = "cheat";
    private static BasicDietController instance;
    private MealDataSorter data;
    private List<DietControllerListener> listeners;
    private String user;
    private DietPlan overallDiet;
    private FirebaseFirestore db;
    private SparseArray<DailyMeals> daysAgoMeals;
    private SparseBooleanArray mealNeedsUpdate;
    private SparseArray<WeeklyIntake> weeksAgoMeals;
    private SparseBooleanArray weekNeedsUpdate;

    private boolean dietPlanLoaded = false;
    private boolean mealDataLoaded = false;

    //TODO create factory for this method. Or remove interface.
    public static BasicDietController getInstance() throws NullPointerException {
        return instance;
    }
    public static BasicDietController getInstance(DietControllerListener listener) throws NullPointerException {
        if(instance != null) {
            if (!instance.listeners.contains(listener)) {
                instance.listeners.add(listener);
            }
        }
        return instance;
    }

    public BasicDietController() {
        //initialising variables
        this.db = FirebaseFirestore.getInstance();
        this.listeners = new ArrayList<>();
        this.user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        daysAgoMeals = new SparseArray<>();
        mealNeedsUpdate = new SparseBooleanArray();
        weeksAgoMeals = new SparseArray<>();
        weekNeedsUpdate = new SparseBooleanArray();

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

    public void addListeners(List<DietControllerListener> listeners) {
        this.listeners.addAll(listeners);
    }

    public void removeListener(DietControllerListener listener) {
        this.listeners.remove(listener);
    }

    private void getCurrentMealDataFromDB() {
        final Query dataQuery = db.collection(DailyMeals.MEALS).whereEqualTo("user", user);
        //check to update the data when it changes. This will also run through on the first time
        dataQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(queryDocumentSnapshots != null) {
                    //update data to the new changes
                    //TODO find a way to only update for things that have changed
                    data = new MealDataSorter(queryDocumentSnapshots.getDocuments());

                    //check if today is completed before the update
                    boolean todayCompleted = isFoodCompletedToday();
                    boolean yesterdayCompleted = isFoodCompleted(1);
                    boolean waterCompleted = isHydrationCompletedToday();
                    boolean cheatsOver = isOverCheatScoreToday();
                    boolean foodEnteredRecently = false;
                    ArrayList<Integer> daysNeedingUpdates = new ArrayList<>();

                    //check which data has changed, and set them to be updated when next accessed
                    List<DocumentChange> changedData = queryDocumentSnapshots.getDocumentChanges();
                    for (DocumentChange documentChange : changedData) {
                        //getting how many days ago
                        QueryDocumentSnapshot document = documentChange.getDocument();
                        long changedDay = document.toObject(Meal.class).getDay();
                        if (changedDay > System.currentTimeMillis() - DateUtils.MINUTE_IN_MILLIS) {
                            foodEnteredRecently = true;
                        }
                        Date changedDayStart = getStartOfDay(new Date(changedDay));
                        long msDiff = System.currentTimeMillis() - changedDayStart.getTime();
                        int daysAgo = (int) TimeUnit.MILLISECONDS.toDays(msDiff);
                        int weeksAgo = (int) daysAgo / 7;
                        mealNeedsUpdate.put(daysAgo, true);
                        daysNeedingUpdates.add(daysAgo);
                        weekNeedsUpdate.put(weeksAgo, true);
                    }

                    //checking if any achievable was not completed before, and it is completed now,
                    //and this change was recent
                    // then update the listener to know it was just completed
                    if (!todayCompleted) {
                        if (isFoodCompletedToday() && foodEnteredRecently) {
                            for (DietControllerListener listener : listeners) {
                                listener.todaysFoodCompleted();
                            }
                        }
                    }
                    if (!yesterdayCompleted) {
                        if (isFoodCompleted(1) && foodEnteredRecently) {
                            for (DietControllerListener listener : listeners) {
                                listener.yesterdaysFoodCompleted();
                            }
                        }
                    }
                    if (!waterCompleted) {
                        if (isHydrationCompletedToday() && foodEnteredRecently) {
                            for (DietControllerListener listener : listeners) {
                                listener.todaysHydrationCompleted();
                            }
                        }
                    }
                    if (!cheatsOver) {
                        if (isOverCheatScoreToday() && foodEnteredRecently) {
                            for (DietControllerListener listener : listeners) {
                                listener.todaysCheatsOver();
                            }
                        }
                    }
                    //now that the data is updated, make sure it flows through to the listener
                    updateListener(DataType.MEAL, daysNeedingUpdates);
                    //if the data has finished loading, update listeners
                    if (!mealDataLoaded && dietPlanLoaded) {
                        for (DietControllerListener listener : listeners) {
                            listener.dataLoadComplete();
                        }
                    }
                    mealDataLoaded = true;
                }
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
                updateListener(DataType.DIET_PLAN, null);
                //if the data has finished loading, update listeners
                if(mealDataLoaded && !dietPlanLoaded) {
                    for(DietControllerListener listener : listeners) {
                        listener.dataLoadComplete();
                    }
                }
                dietPlanLoaded = true;
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
        return getWeeksIntake(0);
    }

    @Override
    public WeeklyIntake getWeeksIntake(int weeksAgo) {
        WeeklyIntake weeksIntake = weeksAgoMeals.get(weeksAgo);
        //updating if null or data has changed
        if(weeksIntake == null || weekNeedsUpdate.get(weeksAgo, false)) {
            weeksIntake = new WeeklyIntake(data, overallDiet, weeksAgo);
            weeksAgoMeals.put(weeksAgo, weeksIntake);
            //now that it has been updated, it doesn't need one.
            weekNeedsUpdate.put(weeksAgo, false);
        }
        return weeksIntake;
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
                updateListener(DataType.DIET_PLAN, null);
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
        String id = CHEAT_CHANGE_RECOMMENDATION_ID;
        long expiry = DateUtils.WEEK_IN_MILLIS * 2;
        String title;
        String message;

        WeeklyIntake week1 = getWeeksIntake(0);
        WeeklyIntake week2 = getWeeksIntake(1);
        //get the number of cheats in the last fortnight
        double fortnightlyCheats = week1.getTotalCheats() + week2.getTotalCheats();
        //finding the max and min cheat points we should have
        double tooManyCheats = (week1.getWeeklyLimitCheats() + week2.getWeeklyLimitCheats() )*(1+SCALE_FACTOR);
        double tooFewCheats = (week1.getWeeklyLimitCheats() + week2.getWeeklyLimitCheats() )*(1-SCALE_FACTOR);
        //if over or under by either of these numbers, update the message
        if(fortnightlyCheats > tooManyCheats) {
            title = "Increase your cheat score";
            message = "Over the past two weeks you have been having too many cheat meals! " +
                    "Try to reduce the amount of cheat meals you have, or adjust your goals." +
                    "You can always change them again later!";
        } else if(fortnightlyCheats < tooFewCheats) {
            title = "Decrease your cheat score";
            message = "Well done! Over the past two weeks you have been well under your maximum cheat score! " +
                    "Consider giving yourself a challenge and reducing your allowed cheat meals.";
        } else {
            return null;
        }
        return new Recommendation(id, title, message, expiry);
    }

    private Recommendation getDietChangeRecommendation() {
        String id = DIET_CHANGE_RECOMMENDATION_ID;
        long expiry = DateUtils.WEEK_IN_MILLIS * 2;
        String title = "Recommendations for diet changes";

        WeeklyIntake week1 = getWeeksIntake(0);
        WeeklyIntake week2 = getWeeksIntake(1);
        //over or under ate by a large degree over the past fortnight
        double fortnightlyVeges = week1.getVegCount() + week2.getVegCount();
        double fortnightlyProtein = week1.getVegCount() + week2.getVegCount();
        double fortnightlyDairy = week1.getDairyCount()+week2.getDairyCount();
        double fortnightlyGrain = week1.getGrainCount()+week2.getGrainCount();
        double fortnightlyFruit = week1.getFruitCount()+week2.getFruitCount();
        double fortnightlyWater = week1.getHydrationScore()+week2.getHydrationScore();
        //creating the message to the user
        String message = "Over the past two weeks you have been eating ";
        boolean overAte = false;
//        String overAteMessage = "too much ";
        ArrayList<String> overAteFoods = new ArrayList<>();
        if(fortnightlyVeges > (14* overallDiet.getDailyVeges() + 7)) {
            overAte = true;
            overAteFoods.add("vegetables");
        }
        if(fortnightlyProtein > (14* overallDiet.getDailyProtein() + 7)) {
            overAte = true;
            overAteFoods.add("proteins");
        }
        if(fortnightlyDairy > (14* overallDiet.getDailyDairy() + 7)) {
            overAte = true;
            overAteFoods.add("dairy");
        }
        if(fortnightlyGrain > (14* overallDiet.getDailyGrain() + 7)) {
            overAte = true;
            overAteFoods.add("grains");
        }
        if(fortnightlyFruit > (14* overallDiet.getDailyFruit() + 7)) {
            overAte = true;
            overAteFoods.add("fruits");
        }
        if(!overAteFoods.isEmpty()) {
            message += "too much";
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
        }

        boolean underAte = false;
        String underAteMessage = "too little ";
        if(overAte) {
            underAteMessage = "and too little ";
        }
        ArrayList<String> underAteFood = new ArrayList<>();
        if(fortnightlyVeges < (14* overallDiet.getDailyVeges() - 7)) {
            underAte = true;
            underAteFood.add("vegetables");
        }
        if(fortnightlyProtein < (14* overallDiet.getDailyProtein() - 7)) {
            underAte = true;
            underAteFood.add("proteins");
        }
        if(fortnightlyDairy < (14* overallDiet.getDailyDairy() - 7)) {
            underAte = true;
            underAteFood.add("dairy");
        }
        if(fortnightlyGrain < (14* overallDiet.getDailyGrain() - 7)) {
            underAte = true;
            underAteFood.add("grains");
        }
        if(fortnightlyFruit < (14* overallDiet.getDailyFruit() - 7)) {
            underAte = true;
            underAteFood.add("fruit");
        }
        if(!underAteFood.isEmpty()) {
            message += "too little";
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
        }

        //finalising the recommendation, or returning null if no recommendation
        if(overAte || underAte) {
            message +=". ";
            message += "Try to adjust your diet to make up for this.";
            return new Recommendation(id, title, message, expiry);
        }
        return null;
    }

    private Recommendation getCheatScoreRecommendation() {
        String id = CHEAT_SCORE_RECOMMENDATION_ID;
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

    public void updateListener(DietController.DataType dataType, List<Integer> daysAgoUpdated) {
        for(DietControllerListener listener : listeners) {
            listener.refresh(dataType, daysAgoUpdated);
        }
    }

    //TODO this really needs to be moved to its own class or something
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

    public List<DietControllerListener> getListeners() {
        return listeners;
    }
}
