package com.ganterpore.simplediet.Controller;

import com.ganterpore.simplediet.Model.DietPlan;
import com.google.android.gms.tasks.Task;

public interface DietController {
    /**
     * gets the meals that have been eaten today
     * @return object containing todays meals
     */
    DailyMeals getTodaysMeals();

    /**
     * Gets the diet plan expected for today. aka, how many of each food group to be eaten today
     */
    DietPlan getTodaysDietPlan();

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
    boolean isFoodCompleted();

    /**
     * @return if the user has had all their water
     */
    boolean isWaterCompleted();

    /**
     * @return if the user has gone over their cheat limit
     */
    boolean isOverCheatScore();

    /**
     * Update the listener to the diet controller of a change
     */
    void updateListener();

    /**
     * Interface for listeners to a diet controller
     */
    interface DietControllerListener {
        void refresh();
    }
}
