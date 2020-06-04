package com.ganterpore.simplediet.View.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.ganterpore.simplediet.Controller.BasicDietController;
import com.ganterpore.simplediet.Controller.DailyMeals;
import com.ganterpore.simplediet.Controller.DietController;
import com.ganterpore.simplediet.Model.DietPlan;
import com.ganterpore.simplediet.Model.Meal;
import com.ganterpore.simplediet.R;
import com.ganterpore.simplediet.View.ItemViews.CompletableItemView;
import com.google.common.primitives.Ints;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.ganterpore.simplediet.View.Activities.MainActivity.SHARED_PREFS_LOC;


class MealHistoryDisplay  {
    public static final String EXPIRY_TAG = "_expiry";
    public static final String TAG = "MealHistoryDisplay";
    private Activity activity;
    private List<DietController.Recommendation> recommendations;
    private RecyclerView history;

    MealHistoryDisplay(Activity activity, RecyclerView history) {
        this.activity = activity;
        this.history = history;
        recommendations = new ArrayList<>();
        //when a new display is create, make sure it loads in
        this.history.setAdapter(new DayHistoryAdapter(activity, 7));

        //setting up ability to swipe recommendations
        RecommendationSwipeController swipeController = new RecommendationSwipeController(activity, history);
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(history);
    }

    static class RecommendationCollector extends AsyncTask<Void, Void, Void> {
        MealHistoryDisplay parent;
        List<DietController.Recommendation> recommendations;

        RecommendationCollector(MealHistoryDisplay parent) {
            this.parent = parent;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            recommendations = BasicDietController.getInstance().getRecommendations();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            parent.refreshRecommendations(recommendations);
        }
    }

    void refresh(DietController.DataType dataType, List<Integer> daysAgoUpdated) {
        //collecting any changed recommendations
        new RecommendationCollector(this).execute();
        //informing the history adapter of the refresh
        if(history!=null && history.getAdapter()!= null) {
            if(daysAgoUpdated == null) {
                //if null, all days should update
                history.getAdapter().notifyDataSetChanged();
            } else {
                //otherwise only update the days that have changed
                for(int day : daysAgoUpdated) {
                    DayHistoryAdapter adapter = (DayHistoryAdapter) history.getAdapter();
                    adapter.notifyItemChanged(adapter.getDaysPositionFromDaysAgo(day));
                }
            }
        }
    }

    /**
     * Updates the list of recommendations
     */
    private void refreshRecommendations(List<DietController.Recommendation> allRecommendations) {
        int oldRecommendationsSize = recommendations.size();
        if(recommendations != null) {
            recommendations.clear();
        } else {
            recommendations = new ArrayList<>();
        }
        //check if the recommendation should be hidden. If not, then add it to the viewable recommendations.
        SharedPreferences preferences = activity.getPreferences(MODE_PRIVATE);
        for(DietController.Recommendation recommendation : allRecommendations) {
            if(recommendation.getId().equals("cheat_change") && !preferences.getBoolean("track_cheats", true)) {
                //if it is a cheat recommendation, and we are not tracking cheats, skip
                continue;
            }
            //if the recommendation is not hidden, show the reco
            long hideNotificationExpiry = preferences.getLong(recommendation.getId() + EXPIRY_TAG, 0);
            if(hideNotificationExpiry <= System.currentTimeMillis()) {
                recommendations.add(recommendation);
            }
        }
        //informing the history adapter of the changes to recommendations
        if(history!=null && history.getAdapter()!= null) {
            DayHistoryAdapter adapter = (DayHistoryAdapter) history.getAdapter();
            //if the number of recommendations have changed, update all items in adapter to move
            if(oldRecommendationsSize != recommendations.size()) {
                adapter.notifyDataSetChanged();
            } else {
                //otherwise only update the look of the recommendations
                for (int i = 0; i < adapter.getNumberOfRecommendations(); i++) {
                    adapter.notifyItemChanged(i);
                }
            }
        }
    }

    /**
     * Makes recommendations able to be swiped away by the user
     */
    class RecommendationSwipeController extends ItemTouchHelper.Callback {
        Activity activity;
        RecyclerView recyclerView;
        RecommendationSwipeController(Activity activity, RecyclerView recyclerView) {
            this.activity = activity;
            this.recyclerView = recyclerView;
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int type = viewHolder.getItemViewType();
            if(type == DayHistoryAdapter.RECOMMENDATION_VIEW) {
                return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            } else {
                return makeMovementFlags(0, 0);
            }
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
            ((DayHistoryViewHolder) viewHolder).hideRecommendation();
        }
    }

    /**
     * adapter for the day history list.
     */
    public class DayHistoryAdapter extends RecyclerView.Adapter<DayHistoryViewHolder> {

        final static int RECOMMENDATION_VIEW = 1;
        final static int MEAL_HISTORY_VIEW = 2;

        int nDays;
        Activity activity;
        SparseBooleanArray nDaysAgoVisible;

        DayHistoryAdapter(Activity activity, int nDays) {
            this.activity = activity;
            this.nDays = nDays;
            this.nDaysAgoVisible = new SparseBooleanArray();
        }

        @NonNull
        @Override
        public DayHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view;
            //opening either a recommendation or day history item
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

        public int getDaysPositionFromDaysAgo(int daysAgo) {
            return recommendations.size() + daysAgo;
        }

        public int getNumberOfRecommendations() {
            return recommendations.size();
        }
    }

    private static class MealBuilder extends AsyncTask<Void, Void, Void> {
        DayHistoryViewHolder parent;
        int nDaysAgo;
        private HashMap<Meal.FoodType, String> stringMap;
        private HashMap<String, Boolean> completionMap;
        private double dailyCheats;
        private double totalCheats;
        private List<Meal> meals;

        MealBuilder(DayHistoryViewHolder parent, int nDaysAgo) {
            this.parent = parent;
            this.nDaysAgo = nDaysAgo;
        }

        @Override
        protected Void doInBackground(Void[] voids) {
            NumberFormat df = new DecimalFormat("##.##");
            //getting controller information
            DietController dietController = BasicDietController.getInstance();
            DailyMeals day = dietController.getDaysMeals(nDaysAgo);
            DietPlan daysPlan = dietController.getDaysDietPlan(nDaysAgo);
            meals = day.getMeals();

            //getting completion status
            boolean foodCompleted = dietController.isFoodCompleted(nDaysAgo);
            boolean hydrationCompleted = dietController.isHydrationCompleted(nDaysAgo);
            boolean overCheatScore = dietController.isOverCheatScore(nDaysAgo);
            completionMap = new HashMap<>();
            completionMap.put("food", foodCompleted);
            completionMap.put("water", hydrationCompleted);
            completionMap.put("cheats", overCheatScore);

            //getting and building strings
            String vegText = df.format(day.getVegCount()) + "/" + df.format(daysPlan.getDailyVeges());
            String proteinText = df.format(day.getProteinCount()) + "/" + df.format(daysPlan.getDailyProtein());
            String dairyText = df.format(day.getDairyCount()) + "/" + df.format(daysPlan.getDailyDairy());
            String grainText = df.format(day.getGrainCount()) + "/" + df.format(daysPlan.getDailyGrain());
            String fruitText = df.format(day.getFruitCount()) + "/" + df.format(daysPlan.getDailyFruit());
            String hydrationText = df.format(day.getHydrationScore()) + "/" + df.format(daysPlan.getDailyHydration());

            stringMap = new HashMap<>();
            stringMap.put(Meal.FoodType.VEGETABLE, vegText);
            stringMap.put(Meal.FoodType.MEAT, proteinText);
            stringMap.put(Meal.FoodType.DAIRY, dairyText);
            stringMap.put(Meal.FoodType.GRAIN, grainText);
            stringMap.put(Meal.FoodType.FRUIT, fruitText);
            stringMap.put(Meal.FoodType.WATER, hydrationText);

            //getting other information
            dailyCheats = daysPlan.getDailyCheats();
            totalCheats = day.getTotalCheats();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            parent.buildMeals(nDaysAgo, meals, stringMap, completionMap, dailyCheats, totalCheats);
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

        DayHistoryViewHolder(Activity activity, @NonNull View itemView,
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
                TextView dateTV = itemView.findViewById(R.id.date);
                dateTV.setText(dateFormat.format(new Date(System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS*daysAgo)));
                new MealBuilder(this, daysAgo).execute();
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
            //confirm with the user if they actually want to remove notification
//            new AlertDialog.Builder(activity)
//                    .setTitle("Are you sure you want to remove this?")
//                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
                            //if so, proceed with removal
                            final DietController.Recommendation recommendation = recommendations.get(position);
                            //updating the expiry of the recommendation id
                            SharedPreferences preferences = activity.getPreferences(MODE_PRIVATE);
                            Date expiry = new Date(System.currentTimeMillis() + recommendation.getExpiry());
                            preferences.edit().putLong(recommendation.getId() + EXPIRY_TAG, getStartOfDay(expiry).getTime()).apply();

                            //updating the list of recommendations. The recommendation will be hidden because of the new expiry.
                            refresh(null, new ArrayList<Integer>());
//                        }
//                    })
//                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            //if not, move back if swiped away
//                            adapter.notifyItemChanged(getAdapterPosition());
//                        }
//                    })
//                    .setCancelable(false)
//                    .show();
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

        private void buildMeals(final int nDaysAgo, List<Meal> meals, HashMap<Meal.FoodType, String> stringMap, HashMap<String, Boolean> completionMap, double dailyCheats, double totalCheats) {
            final int SCALE_FACTOR = 100;
            NumberFormat df = new DecimalFormat("##.##");

            //getting and updating values for the views
            CompletableItemView completedFoodView = itemView.findViewById(R.id.completed_food);
            CompletableItemView completedWaterView = itemView.findViewById(R.id.completed_water);
            CompletableItemView didntCheatView = itemView.findViewById(R.id.didnt_cheat);
            ProgressBar cheatsProgress = itemView.findViewById(R.id.progress_cheats);

            completedFoodView.setCompleted(completionMap.get("food"));
            completedWaterView.setCompleted(completionMap.get("water"));
            didntCheatView.setCompleted(!completionMap.get("cheats"));
            cheatsProgress.setMax((int) (dailyCheats * SCALE_FACTOR));
            cheatsProgress.setProgress((int) totalCheats * SCALE_FACTOR);

            //building mealsList recyclerview
            RecyclerView mealsList = itemView.findViewById(R.id.meals_list);
            mealsList.setAdapter(new MealsAdapter(activity, meals));

            final LinearLayout expandableView = itemView.findViewById(R.id.expanded_layout);
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
            itemView.setOnClickListener(new View.OnClickListener() {
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

            if(itemView.findViewById(R.id.header_information)==null) {
                LayoutInflater inflater = LayoutInflater.from(activity);
                LinearLayout foodCounts = (LinearLayout) inflater.inflate(R.layout.container_food_groups_counts_mini, null);
                foodCounts.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                expandableView.addView(foodCounts, 0);
            }

            //updating text views
            TextView vegCount = itemView.findViewById(R.id.veg_count);
            TextView proteinCount = itemView.findViewById(R.id.protein_count);
            TextView dairyCount = itemView.findViewById(R.id.dairy_count);
            TextView grainCount = itemView.findViewById(R.id.grain_count);
            TextView fruitCount = itemView.findViewById(R.id.fruit_count);
            TextView waterCount = itemView.findViewById(R.id.water_count);
            TextView cheatCount = itemView.findViewById(R.id.cheat_count);
            TextView proteinCountHeader = itemView.findViewById(R.id.protein_count_header);

            vegCount.setText(stringMap.get(Meal.FoodType.VEGETABLE));
            proteinCount.setText(stringMap.get(Meal.FoodType.MEAT));
            dairyCount.setText(stringMap.get(Meal.FoodType.DAIRY));
            grainCount.setText(stringMap.get(Meal.FoodType.GRAIN));
            fruitCount.setText(stringMap.get(Meal.FoodType.FRUIT));
            waterCount.setText(stringMap.get(Meal.FoodType.WATER));
            cheatCount.setText((df.format(totalCheats)));

            //removing/changing disabled views
            SharedPreferences preferences = activity.getSharedPreferences(SHARED_PREFS_LOC, MODE_PRIVATE);
            String mode = preferences.getString("mode", "normal");
            if (mode != null && (mode.equals("vegan") || mode.equals("vegetarian"))) {
                proteinCountHeader.setText("P");
            }
            boolean trackWater = preferences.getBoolean("track_water", true);
            boolean trackCheats = preferences.getBoolean("track_cheats", true);
            if(!trackWater) {
                itemView.findViewById(R.id.water_container).setVisibility(View.GONE);
                itemView.findViewById(R.id.completed_water).setVisibility(View.GONE);
            }
            if(!trackCheats) {
                itemView.findViewById(R.id.cheat_container).setVisibility(View.GONE);
                itemView.findViewById(R.id.cheat_progress_container).setVisibility(View.GONE);
            }

            //setting up swiping ability
            MealSwipeController swipeController = new MealSwipeController(activity, mealsList);
            ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
            itemTouchhelper.attachToRecyclerView(mealsList);
        }
    }

    /**
     * adapter for the meals list item, showing meals eaten on a day.
     */
    public class MealsAdapter extends RecyclerView.Adapter<MealsViewHolder> {

        private Activity activity;
        List<Meal> meals;
        private final int HAS_MEAL = 1;
        private final int NO_MEAL = 2;

        MealsAdapter(Activity activity, List<Meal> meals) {
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
        private Meal meal;

        MealsViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void build(Meal meal) {
            this.meal = meal;
            if(meal.getName() != null) {
                TextView mealNameTV = itemView.findViewById(R.id.meal_name);
                mealNameTV.setText(meal.getName());
            }
            TextView servingCountTV = itemView.findViewById(R.id.serving_count);
            servingCountTV.setText(meal.serveCountText());
        }

        void deleteMeal() {
            meal.deleteMeal();
        }
    }

    class MealSwipeController extends ItemTouchHelper.Callback {

        private Activity activity;
        private RecyclerView recyclerView;
        private Drawable icon;
        private final ColorDrawable background;

        MealSwipeController(Activity activity, RecyclerView recyclerView) {
            this.activity = activity;
            this.recyclerView = recyclerView;
            icon = ContextCompat.getDrawable(activity,
                    android.R.drawable.ic_menu_delete);
            background = new ColorDrawable(Color.RED);
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            View itemView = viewHolder.itemView;
            int backgroundCornerOffset = 20;
            int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
            int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
            int iconBottom = iconTop + icon.getIntrinsicHeight();

            if (dX > 0) { // Swiping to the right
                int iconLeft = Ints.min(itemView.getLeft()+(int)dX - icon.getIntrinsicWidth(), itemView.getLeft() + iconMargin);
                int iconRight = Ints.min(itemView.getLeft()+(int)dX, itemView.getLeft() + iconMargin + icon.getIntrinsicWidth());
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                background.setBounds(itemView.getLeft(), itemView.getTop(),
                        itemView.getLeft() + ((int) dX) - backgroundCornerOffset,
                        itemView.getBottom());
                background.draw(c);
                icon.draw(c);
            } else if (dX < 0) { // Swiping to the left
                int iconLeft = Ints.max(itemView.getRight()+(int)dX, itemView.getRight() - iconMargin-icon.getIntrinsicWidth());
                int iconRight = Ints.max(itemView.getRight()+(int)dX +  icon.getIntrinsicWidth(), itemView.getRight() - iconMargin);
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                background.setBounds(itemView.getRight() + ((int) dX) + backgroundCornerOffset,
                        itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);
                icon.draw(c);
            } else { // view is unSwiped
                background.setBounds(0, 0, 0, 0);
            }
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
            new AlertDialog.Builder(activity)
                    .setTitle("Are you sure you want to remove this?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final Meal savedMeal = ((MealsViewHolder) viewHolder).meal;
                            ((MealsViewHolder) viewHolder).deleteMeal();
                            ((MealsAdapter) recyclerView.getAdapter()).meals.remove(viewHolder.getAdapterPosition());
                            recyclerView.getAdapter().notifyItemRemoved(viewHolder.getAdapterPosition());
                            ((SnackbarReady) activity).undoDelete(savedMeal);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            recyclerView.getAdapter().notifyItemChanged(viewHolder.getAdapterPosition());
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }
}
