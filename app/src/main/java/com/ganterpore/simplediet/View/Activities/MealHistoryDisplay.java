package com.ganterpore.simplediet.View.Activities;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ganterpore.simplediet.Controller.DailyMeals;
import com.ganterpore.simplediet.Controller.DietController;
import com.ganterpore.simplediet.Model.Meal;
import com.ganterpore.simplediet.R;
import com.ganterpore.simplediet.View.ItemViews.CompletableItemView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class MealHistoryDisplay  {
    public static final String TAG = "MealHistoryDisplay";
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

        int nDays;
        Activity activity;

        public DayHistoryAdapter(Activity activity, int nDays) {
            this.activity = activity;
            this.nDays = nDays;
        }

        @NonNull
        @Override
        public DayHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(activity).inflate(R.layout.list_item_day_history, viewGroup, false);
            return new DayHistoryViewHolder(activity, view);
        }

        @Override
        public void onBindViewHolder(@NonNull DayHistoryViewHolder dayHistoryViewHolder, int i) {
            dayHistoryViewHolder.build(i);
        }

        @Override
        public int getItemCount() {
            return nDays;
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
        View itemView;
        DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        Activity activity;

        public DayHistoryViewHolder(Activity activity, @NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.activity = activity;
        }

        public void build(int nDaysAgo) {
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
