package com.ganterpore.simplediet.View.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ganterpore.simplediet.Controller.DailyMeals;
import com.ganterpore.simplediet.Controller.DietController;
import com.ganterpore.simplediet.Model.DietPlan;
import com.ganterpore.simplediet.Model.Meal;
import com.ganterpore.simplediet.R;
import com.ganterpore.simplediet.View.ItemViews.CompletableItemView;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.ganterpore.simplediet.View.Activities.MainActivity.SHARED_PREFS_LOC;


public class MealHistoryDisplay  {
    public static final String EXPIRY_TAG = "_expiry";
    private static final String TAG = "MealHistoryDisplay";
    private Activity activity;
    private DietController dietController;
    private List<DietController.Recommendation> recommendations;
    private RecyclerView history;

    public MealHistoryDisplay(Activity activity, DietController dietController) {
        this.activity = activity;
        this.dietController = dietController;
        RecyclerView history = activity.findViewById(R.id.day_history_list);
        recommendations = new ArrayList<>();
        history.setAdapter(new DayHistoryAdapter(activity, 7));
        this.history = history;

        SwipeController swipeController = new SwipeController(activity, history);
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(history);
    }

    public void setDietController(DietController dietController) {
        this.dietController = dietController;
    }

    /**
     * called to update the list of recommendations for the user, to all recommendations that are
     * not hidden
     */
    public void refreshRecommendations() {
        //getting all recommendations, and clearing the list of visible recommendations.
        List<DietController.Recommendation> allRecomendations = dietController.getRecommendations();
        if(recommendations != null) {
            recommendations.clear();
        } else {
            recommendations = new ArrayList<>();
        }

        //check if the recommendation should be hidden. If not, then add it to the viewable recommendations.
        SharedPreferences preferences = activity.getPreferences(MODE_PRIVATE);
        for(DietController.Recommendation recommendation : allRecomendations) {
            //getting the expiry date of the notification hide feature
            long hideNotificationExpiry = preferences.getLong(recommendation.getId() + EXPIRY_TAG, 0);
            if(hideNotificationExpiry <= System.currentTimeMillis()) {
                recommendations.add(recommendation);
            }
        }
        //informing the history adapter of the change
        if(history!=null && history.getAdapter()!= null) {
            history.getAdapter().notifyDataSetChanged();
        }
    }

    class SwipeController extends ItemTouchHelper.Callback {

        Activity activity;
        RecyclerView recyclerView;
        public SwipeController(Activity activity, RecyclerView recyclerView) {
            this.activity = activity;
            this.recyclerView = recyclerView;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int type = viewHolder.getItemViewType();
            if(type == DayHistoryAdapter.RECOMMENDATION_VIEW) {
                return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            } else {
                return makeMovementFlags(0, 0);
            }
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }



        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
            new AlertDialog.Builder(activity)
                    .setTitle("Are you sure you want to remove this?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((DayHistoryViewHolder) viewHolder).hideRecommendation();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            recyclerView.getAdapter().notifyItemChanged(viewHolder.getAdapterPosition());
                        }
                    })
                    .show();
        }
    }

    /**
     * adapter for the day history list.
     */
    public class DayHistoryAdapter extends RecyclerView.Adapter<DayHistoryViewHolder> {

        public final static int RECOMMENDATION_VIEW = 1;
        public final static int MEAL_HISTORY_VIEW = 2;

        int nDays;
        Activity activity;
        public SparseBooleanArray nDaysAgoVisible;

        public DayHistoryAdapter(Activity activity, int nDays) {
            this.activity = activity;
            this.nDays = nDays;
            this.nDaysAgoVisible = new SparseBooleanArray();
        }

        @NonNull
        @Override
        public DayHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view;
            //openning either a recommendation or day history item
            if(viewType==RECOMMENDATION_VIEW) {
                view = LayoutInflater.from(activity).inflate(R.layout.list_item_recommendation, viewGroup, false);
            } else {
                view = LayoutInflater.from(activity).inflate(R.layout.list_item_day_history, viewGroup, false);
            }
            return new DayHistoryViewHolder(activity, view, viewType, this);
        }

        @Override
        public void onBindViewHolder(@NonNull DayHistoryViewHolder dayHistoryViewHolder, int position) {
            dayHistoryViewHolder.build(position);
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
    }

    /**
     * View Holder for the information about a day.
     */
    public class DayHistoryViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        Activity activity;
        int viewType;
        DayHistoryAdapter adapter;
        int position;

        public DayHistoryViewHolder(Activity activity, @NonNull View itemView,
                                    int viewType, DayHistoryAdapter adapter) {
            super(itemView);
            this.itemView = itemView;
            this.activity = activity;
            this.viewType = viewType;
            this.adapter = adapter;
        }

        public void build(int position) {
            this.position = position;
            int daysAgo = position - recommendations.size();
            if(daysAgo<0) {
                buildRecommendation(position);
            } else {
                buildMeal(daysAgo);
            }
        }

        private void buildRecommendation(final int position) {
            final DietController.Recommendation recommendation = recommendations.get(position);
            //updating text fields
            TextView title = itemView.findViewById(R.id.title);
            TextView message = itemView.findViewById(R.id.message);
            title.setText(recommendation.getTitle());
            message.setText(recommendation.getMessage());

            //x button functionality
            ImageButton exitButton = itemView.findViewById(R.id.x_button);
            exitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideRecommendation();
                }
            });
        }

        /**
         * used to hide a recommendation, and make sure it doesn't reappear until the expiry date
         */
        private void hideRecommendation() {
            final DietController.Recommendation recommendation = recommendations.get(position);
            //updating the expiry of the recommendation id
            SharedPreferences preferences = activity.getPreferences(MODE_PRIVATE);
            Date expiry = new Date(System.currentTimeMillis() + recommendation.getExpiry());
            preferences.edit().putLong(recommendation.getId() + EXPIRY_TAG, getStartOfDay(expiry).getTime()).apply();

//            adapter.notifyItemRemoved(position);
            //updating the list of recommendations. The recommendation will be hidden because of the new expiry.
            refreshRecommendations();
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

        private void buildMeal(final int nDaysAgo) {
            final int SCALE_FACTOR = 100;

            DailyMeals day = dietController.getDaysMeals(nDaysAgo);
            DietPlan daysPlan = dietController.getDaysDietPlan(nDaysAgo);
            //getting and updating values for the views
            TextView dateTV = itemView.findViewById(R.id.date);
            CompletableItemView completedFoodTV = itemView.findViewById(R.id.completed_food);
            CompletableItemView completedWaterTV = itemView.findViewById(R.id.completed_water);
            CompletableItemView didntCheatTV = itemView.findViewById(R.id.didnt_cheat);
            ProgressBar cheatsProgress = itemView.findViewById(R.id.progress_cheats);

            dateTV.setText(dateFormat.format(day.getDate()));
            completedFoodTV.setCompleted(dietController.isFoodCompleted(nDaysAgo));
            completedWaterTV.setCompleted(dietController.isHydrationCompleted(nDaysAgo));
            didntCheatTV.setCompleted(!dietController.isOverCheatScore(nDaysAgo));
            cheatsProgress.setMax((int) daysPlan.getWeeklyCheats() * SCALE_FACTOR);
            cheatsProgress.setProgress((int) day.getWeeklyCheats() * SCALE_FACTOR);

            //creating a list of the meals eaten that day
            RecyclerView mealsList = itemView.findViewById(R.id.meals_list);
            mealsList.setAdapter(new MealsAdapter(activity, day.getMeals()));

            final ConstraintLayout expandableView = itemView.findViewById(R.id.expanded_layout);
            final ImageView dropdownButton = itemView.findViewById(R.id.dropdown_button);

            //making sure view is shown as it should be
            if(adapter.nDaysAgoVisible.get(nDaysAgo, false)) {
                expandableView.setVisibility(View.VISIBLE);
                dropdownButton.setRotation(180);
            } else {
                expandableView.setVisibility(View.GONE);
                dropdownButton.setRotation(0);
            }

            //creating functionality for the button that shows meals eaten that day
            dropdownButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(expandableView.getVisibility() == View.GONE) {
                        expandableView.setVisibility(View.VISIBLE);
                        dropdownButton.animate().setDuration(200).rotation(180);
                        adapter.nDaysAgoVisible.append(nDaysAgo, true);
                        adapter.notifyItemChanged(position);
                    } else {
                        expandableView.setVisibility(View.GONE);
                        dropdownButton.animate().setDuration(200).rotation(0);
                        adapter.nDaysAgoVisible.append(nDaysAgo, false);
                        adapter.notifyItemChanged(position);
                    }
                }
            });

            //updating text views
            NumberFormat df = new DecimalFormat("##.##");
            TextView vegCount = itemView.findViewById(R.id.veg_count);
            TextView proteinCount = itemView.findViewById(R.id.protein_count);
            TextView dairyCount = itemView.findViewById(R.id.dairy_count);
            TextView grainCount = itemView.findViewById(R.id.grain_count);
            TextView fruitCount = itemView.findViewById(R.id.fruit_count);
            TextView waterCount = itemView.findViewById(R.id.water_count);
            TextView cheatCount = itemView.findViewById(R.id.cheat_count);
            TextView proteinCountHeader = itemView.findViewById(R.id.protein_count_header);

            vegCount.setText((df.format(day.getVegCount()) + "/" + df.format(daysPlan.getDailyVeges())));
            proteinCount.setText((df.format(day.getProteinCount()) + "/" + df.format(daysPlan.getDailyProtein())));
            dairyCount.setText((df.format(day.getDairyCount()) + "/" + df.format(daysPlan.getDailyDairy())));
            grainCount.setText((df.format(day.getGrainCount()) + "/" + df.format(daysPlan.getDailyGrain())));
            fruitCount.setText((df.format(day.getFruitCount()) + "/" + df.format(daysPlan.getDailyFruit())));
            waterCount.setText((df.format(day.getHydrationScore()) + "/" + df.format(daysPlan.getDailyHydration())));
            cheatCount.setText((df.format(day.getTotalCheats())));

            SharedPreferences preferences = activity.getSharedPreferences(SHARED_PREFS_LOC, MODE_PRIVATE);
            String mode = preferences.getString("mode", "normal");
            if(mode.equals("vegan") || mode.equals("vegetarian")) {
                proteinCountHeader.setText("P");
            }
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
