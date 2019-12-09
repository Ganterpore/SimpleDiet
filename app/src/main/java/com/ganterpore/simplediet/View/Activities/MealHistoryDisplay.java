package com.ganterpore.simplediet.View.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ganterpore.simplediet.Controller.DailyMeals;
import com.ganterpore.simplediet.Controller.DietController;
import com.ganterpore.simplediet.Model.Meal;
import com.ganterpore.simplediet.R;
import com.ganterpore.simplediet.View.ItemViews.CompletableItemView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MealHistoryDisplay  {
    private static final String TAG = "MealHistoryDisplay";
    private Activity activity;
    private DietController dietController;
    private RecyclerView history;

    public MealHistoryDisplay(Activity activity, DietController dietController) {
        this.activity = activity;
        this.dietController = dietController;
        RecyclerView history = activity.findViewById(R.id.day_history_list);
        history.setAdapter(new DayHistoryAdapter(activity, 7));
        this.history = history;
    }

   public void refresh() {
        history.getAdapter().notifyDataSetChanged();
   }

    /**
     * adapter for the day history list.
     */
    public class DayHistoryAdapter extends RecyclerView.Adapter<DayHistoryViewHolder>
            implements DailyMeals.DailyMealsInterface{

        public final static int RECOMMENDATION_VIEW = 1;
        public final static int MEAL_HISTORY_VIEW = 2;

        int nDays;
        Activity activity;
        List<DietController.Recommendation> recommendations;

        public DayHistoryAdapter(Activity activity, int nDays) {
            this.activity = activity;
            this.nDays = nDays;
            this.recommendations = dietController.getRecommendations();
        }

        @NonNull
        @Override
        public DayHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view;
            if(viewType==RECOMMENDATION_VIEW) {
                view = LayoutInflater.from(activity).inflate(R.layout.list_item_recommendation, viewGroup, false);
            } else {
                view = LayoutInflater.from(activity).inflate(R.layout.list_item_day_history, viewGroup, false);
            }
            return new DayHistoryViewHolder(activity, view, viewType, recommendations);
        }

        @Override
        public void onBindViewHolder(@NonNull DayHistoryViewHolder dayHistoryViewHolder, int position) {
            int daysAgo = position - recommendations.size();
            if(daysAgo<0) {
                dayHistoryViewHolder.buildRecommendation(position);
            } else {
                dayHistoryViewHolder.buildMeal(daysAgo);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if(position < recommendations.size()) {
                return RECOMMENDATION_VIEW;
            }
            return MEAL_HISTORY_VIEW;
        }

        @Override
        public int getItemCount() {
            return recommendations.size() + nDays;
        }

        @Override
        public void updateDailyMeals(DailyMeals day) {
            this.notifyDataSetChanged();
        }
    }

    /**
     * View Holder for the information about a day.
     */
    public class DayHistoryViewHolder extends RecyclerView.ViewHolder {
        public static final String EXPIRY_TAG = "_expiry";
        View itemView;
        DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        Activity activity;
        int viewType;
        List<DietController.Recommendation> recommendations;

        public DayHistoryViewHolder(Activity activity, @NonNull View itemView,
                                    int viewType, List<DietController.Recommendation> recommendations) {
            super(itemView);
            this.itemView = itemView;
            this.activity = activity;
            this.viewType = viewType;
            this.recommendations = recommendations;
        }

        private void buildRecommendation(int position) {
            final DietController.Recommendation recommendation = recommendations.get(position);

            //check if the recommendation is hidden, and its hide hasn't expired. If it should be hidden, hide it.
            SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
            long hideNotificationExpiry = preferences.getLong(recommendation.getId() + EXPIRY_TAG, 0);
            if(hideNotificationExpiry > System.currentTimeMillis()) {
                itemView.setVisibility(View.GONE);
                return;
            }

            //if it shouldn't be hidden, then update the values of text fields
            TextView title = itemView.findViewById(R.id.title);
            TextView message = itemView.findViewById(R.id.message);

            title.setText(recommendation.getTitle());
            message.setText(recommendation.getMessage());

            //x button functionality
            Button exitButton = itemView.findViewById(R.id.x_button);
            exitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideRecommendation(recommendation);
                }
            });
        }

        /**
         * used to hide a recommendation, and make sure it doesn't reappear until the expiry date
         */
        private void hideRecommendation(DietController.Recommendation recommendation) {
            SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
            Date expiry = new Date(System.currentTimeMillis() + recommendation.getExpiry());
            preferences.edit().putLong(recommendation.getId() + EXPIRY_TAG, getStartOfDay(expiry).getTime()).apply();
            itemView.setVisibility(View.GONE);
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

        public void buildMeal(int nDaysAgo) {
            DailyMeals day = dietController.getDaysMeals(nDaysAgo);
            //getting and updating values for the views
            TextView dateTV = itemView.findViewById(R.id.date);
            CompletableItemView completedFoodTV = itemView.findViewById(R.id.completed_food);
            CompletableItemView completedWaterTV = itemView.findViewById(R.id.completed_water);
            CompletableItemView didntCheatTV = itemView.findViewById(R.id.didnt_cheat);

            dateTV.setText(dateFormat.format(day.getDate()));
            completedFoodTV.setCompleted(dietController.isFoodCompleted(nDaysAgo));
            completedWaterTV.setCompleted(dietController.isWaterCompleted(nDaysAgo));
            didntCheatTV.setCompleted(dietController.isOverCheatScore(nDaysAgo));

            //creating a list of the meals eaten that day
            final RecyclerView mealsList = itemView.findViewById(R.id.meals_list);
            mealsList.setAdapter(new MealsAdapter(activity, day.getMeals()));

            //creating functionality for the button that shows meals eaten that day
            final ImageView dropdownButton = itemView.findViewById(R.id.dropdown_button);
            dropdownButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: Clicked!");
                    if(mealsList.getVisibility() == View.GONE) {
                        mealsList.setVisibility(View.VISIBLE);
                        dropdownButton.setImageDrawable(activity.getResources().getDrawable(android.R.drawable.arrow_up_float));
                    } else {
                        mealsList.setVisibility(View.GONE);
                        dropdownButton.setImageDrawable(activity.getResources().getDrawable(android.R.drawable.arrow_down_float));
                    }
                }
            });
        }
    }

    /**
     * adapter for the meals list item, showing meals eaten on a day.
     */
    public class MealsAdapter extends RecyclerView.Adapter<MealsViewHolder> {

        private Activity activity;
        private List<Meal> meals;
        private final int HAS_MEAL = 1;
        private final int NO_MEAL = 2;

        public MealsAdapter(Activity activity, List<Meal> meals) {
            this.activity = activity;
            this.meals = meals;
        }

        @Override
        public int getItemViewType(int position) {
            Meal meal = meals.get(position);
            if(meal.getName() != null) {
                return HAS_MEAL;
            } else {
                return NO_MEAL;
            }
        }

        @NonNull
        @Override
        public MealsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view;
            //differentiate the view based on whether the meal has a name or not.
            if(viewType == HAS_MEAL) {
                view = LayoutInflater.from(activity).inflate(R.layout.list_item_meal, viewGroup, false);
            } else {
                view = LayoutInflater.from(activity).inflate(R.layout.list_item_meal_no_name, viewGroup, false);
            }
            return new MealsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MealsViewHolder mealsViewHolder, int i) {
            mealsViewHolder.build(meals.get(i));
        }

        @Override
        public int getItemCount() {
            if(meals == null) {
                return 0;
            } else {
                return meals.size();
            }
        }
    }

    /**
     * View holder for the meal list item
     */
    public class MealsViewHolder extends RecyclerView.ViewHolder {

        public MealsViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void build(Meal meal) {
            if(meal.getName() != null) {
                TextView mealNameTV = itemView.findViewById(R.id.meal_name);
                mealNameTV.setText(meal.getName());
            }
            TextView servingCountTV = itemView.findViewById(R.id.serving_count);
            servingCountTV.setText(meal.serveCountText());
        }
    }


}
