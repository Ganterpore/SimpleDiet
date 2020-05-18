package com.ganterpore.simplediet.Controller;

import com.ganterpore.simplediet.Model.DietPlan;
import com.google.android.gms.tasks.Task;

import java.util.Date;
import java.util.List;

public interface DietController {
    enum DataType {
        MEAL,
        DIET_PLAN;
    }
    /**
     * gets the meals that have been eaten today
     * @return object containing todays meals
     */
    DailyMeals getTodaysMeals();
    DailyMeals getDaysMeals(int nDaysAgo);

    /**
     * Gets the diet plan expected for today. aka, how many of each food group to be eaten today
     */
    DietPlan getTodaysDietPlan();
    DietPlan getDaysDietPlan(int nDaysAgo);


    WeeklyIntake getThisWeeksIntake();
    WeeklyIntake getWeeksIntake(int weeksAgo);

    /**
     * Gets the generic diet plan the user has set up
     */
    DietPlan getOverallDietPlan();
    /**
     * Updates the dietPlan plan in the database and object to the one that is given
     * @param newDietPlan, the new dietPlan plan to ceate
     * @return the task of the database update job
     */
    Task<Void> updateDietPlan(DietPlan newDietPlan);

    /**
     * @return if the user has eaten all food groups
     */
    boolean isFoodCompletedToday();
    boolean isFoodCompleted(int nDaysAgo);

    /**
     * @return if the user has had all their water
     */
    boolean isHydrationCompletedToday();
    boolean isHydrationCompleted(int nDaysAgo);

    /**
     * @return if the user has gone over their cheat limit
     */
    boolean isOverCheatScoreToday();
    boolean isOverCheatScore(int nDaysAgo);

    /**
     * @return a list of recommendations for the user
     */
    List<Recommendation> getRecommendations();

    /**
     * Interface for listeners to a diet controller
     */
    interface DietControllerListener {
        void dataLoadComplete();

        /**
         * lets listener know that the data has been updated and the information needs to be refreshed
         */
        void refresh(DataType dataType, List<Integer> daysAgoUpdated);

        /**
         * Lets listener know that todays food was completed recently
         */
        void todaysFoodCompleted();
        /**
         * Lets listener know that todays water was completed recently
         */
        void todaysHydrationCompleted();
        /**
         * Lets listener know that we went over todays cheats recently
         */
        void todaysCheatsOver();

        /**
         * Lets listener know that yesterdays food was completed recently
         */
        void yesterdaysFoodCompleted();
    }

    /**
     * Recommendation objects contain all the information needed to create a recommendation notification;
     * These are used to display information to the user, and recommend changes to their diet.
     */
    class Recommendation {
        private String id;
        private String title;
        private String message;
        private long expiry;

        public Recommendation(String id, String title, String message, long expiry) {
            this.id = id;
            this.title = title;
            this.message = message;
            this.expiry = expiry;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getMessage() {
            return message;
        }

        public long getExpiry() {
            return expiry;
        }
    }
}
