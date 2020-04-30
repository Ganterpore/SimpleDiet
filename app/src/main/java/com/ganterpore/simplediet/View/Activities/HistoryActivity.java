package com.ganterpore.simplediet.View.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.ganterpore.simplediet.Controller.BasicDietController;
import com.ganterpore.simplediet.Controller.DailyMeals;
import com.ganterpore.simplediet.Controller.DietController;
import com.ganterpore.simplediet.Controller.WeeklyIntake;
import com.ganterpore.simplediet.Model.DietPlan;
import com.ganterpore.simplediet.Model.Meal;
import com.ganterpore.simplediet.R;
import com.ganterpore.simplediet.View.ItemViews.CompletableItemView;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;
import static com.ganterpore.simplediet.View.Activities.MainActivity.SHARED_PREFS_LOC;

public class HistoryActivity extends Fragment {
    public static final String TAG = "HistoryActivity";
//    private DietController dietController;
    private boolean trackWater;
    private boolean trackAlcohol;
    private boolean trackCaffeine;
    private boolean trackCheats;

    private View historyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        historyView = inflater.inflate(R.layout.activity_history, container, false);
        return historyView;
    }

    @Override
    public void onStart() {
        super.onStart();
        //getting elements required for the view
        SharedPreferences preferences = Objects.requireNonNull(getActivity()).getSharedPreferences(SHARED_PREFS_LOC, MODE_PRIVATE);
        trackWater = preferences.getBoolean("track_water", true);
        trackAlcohol = preferences.getBoolean("track_alcohol", true);
        trackCaffeine = preferences.getBoolean("track_caffeine", true);
        trackCheats = preferences.getBoolean("track_cheats", true);

        //setting up the fragment
        hideIrrelevantViews();
        new ViewBuilder(this).execute();
    }

    /**
     * Hides views from the fragment if the user has turned off that view
     */
    private void hideIrrelevantViews() {
        if (!trackWater) {
            historyView.findViewById(R.id.monthly_water_container).setVisibility(View.GONE);
        }
        if (!trackCheats) {
            historyView.findViewById(R.id.monthly_cheat_container).setVisibility(View.GONE);
        }
        if (!trackCaffeine) {
            historyView.findViewById(R.id.monthly_caffeine_container).setVisibility(View.GONE);
        }
        if (!trackAlcohol) {
            historyView.findViewById(R.id.monthly_alcohol_container).setVisibility(View.GONE);
        }
    }

    /**
     * This class is in charge of building the view of a HistoryActivity fragment it is given.
     */
    private static class ViewBuilder extends AsyncTask<Void, Void, Void>  {
        DietController dietController;
        HistoryActivity activity;

        ViewBuilder(HistoryActivity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            activity.showProgress(true);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            //running heavier methods in the background
            DietController dietController = BasicDietController.getInstance();
            setMonthlyView(activity.historyView, dietController);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //calling UI methods in the foreground
            activity.buildWeeksHistory(dietController);
            activity.showProgress(false);
        }

    }

    /**
     * Builds the weeks history recyclerview
     * @param dietController, the controller with which to get information from
     */
    private void buildWeeksHistory(DietController dietController) {
        RecyclerView weeksHistory = historyView.findViewById(R.id.weeks_history);
        WeekHistoryAdapter adapter = new WeekHistoryAdapter(getActivity(), 8);
        weeksHistory.setAdapter(adapter);
    }


    /**
     * Sets up all the values of the monthly view
     */
    private static void setMonthlyView(View historyView, DietController dietController) {
        final int SCALE_FACTOR = 100; //how much to scale the progress bars by (to allow more granularity)
        NumberFormat df = new DecimalFormat("##.##"); //format to show all decimal strings
        View monthlyContainer = historyView.findViewById(R.id.monthly_container);

        WeeklyIntake week1 = dietController.getWeeksIntake(0);
        WeeklyIntake week2 = dietController.getWeeksIntake(1);
        WeeklyIntake week3 = dietController.getWeeksIntake(2);
        WeeklyIntake week4 = dietController.getWeeksIntake(3);

        //get the text views from the main activity
        TextView monthlyVegTV = historyView.findViewById(R.id.monthly_veges_intake);
        TextView monthlyProteinTV = historyView.findViewById(R.id.monthly_protein_intake);
        TextView monthlyDairyTV = historyView.findViewById(R.id.monthly_dairy_intake);
        TextView monthlyGrainTV = historyView.findViewById(R.id.monthly_grain_intake);
        TextView monthlyFruitTV = historyView.findViewById(R.id.monthly_fruit_intake);
        TextView monthlyWaterTV = historyView.findViewById(R.id.monthly_water_intake);
        TextView monthlyCaffeineTV = historyView.findViewById(R.id.monthly_caffeine_count);
        TextView monthlyAlcoholTV = historyView.findViewById(R.id.monthly_alcohol_count);
        TextView monthlyCheatTV = historyView.findViewById(R.id.monthly_cheat_count);

        //get the progress bars from the main activity
        ProgressBar monthlyCheatsPB = monthlyContainer.findViewById(R.id.progress_cheats);

        //creating arrays of the text views to update
        TextView[] textViewsCount = {monthlyVegTV, monthlyProteinTV, monthlyDairyTV, monthlyGrainTV, monthlyFruitTV, monthlyWaterTV,
                monthlyCaffeineTV, monthlyAlcoholTV, monthlyCheatTV};
        double[] counts = {week1.getVegCount()+week2.getVegCount()+week3.getVegCount()+week4.getVegCount(),
                        week1.getProteinCount()+week2.getProteinCount()+week3.getProteinCount()+week4.getProteinCount(),
                        week1.getDairyCount()+week2.getDairyCount()+week3.getDairyCount()+week4.getDairyCount(),
                        week1.getGrainCount()+week2.getGrainCount()+week3.getGrainCount()+week4.getGrainCount(),
                        week1.getFruitCount()+week2.getFruitCount()+week3.getFruitCount()+week4.getFruitCount(),
                        week1.getWaterCount()+week2.getWaterCount()+week3.getWaterCount()+week4.getWaterCount(),
                        week1.getCaffieneCount()+week2.getCaffieneCount()+week3.getCaffieneCount()+week4.getCaffieneCount(),
                        week1.getAlcoholCount()+week2.getAlcoholCount()+week3.getAlcoholCount()+week4.getAlcoholCount(),
                        week1.getTotalCheats()+week2.getTotalCheats()+week3.getTotalCheats()+week4.getTotalCheats()};
        double[] plans = {week1.getWeeklyLimitVeg()+week2.getWeeklyLimitVeg()+week3.getWeeklyLimitVeg()+week4.getWeeklyLimitVeg(),
                        week1.getWeeklyLimitProtein()+week2.getWeeklyLimitProtein()+week3.getWeeklyLimitProtein()+week4.getWeeklyLimitProtein(),
                        week1.getWeeklyLimitDairy()+week2.getWeeklyLimitDairy()+week3.getWeeklyLimitDairy()+week4.getWeeklyLimitDairy(),
                        week1.getWeeklyLimitGrain()+week2.getWeeklyLimitGrain()+week3.getWeeklyLimitGrain()+week4.getWeeklyLimitGrain(),
                        week1.getWeeklyLimitFruit()+week2.getWeeklyLimitFruit()+week3.getWeeklyLimitFruit()+week4.getWeeklyLimitFruit(),
                        week1.getWeeklyLimitHydration()+week2.getWeeklyLimitHydration()+week3.getWeeklyLimitHydration()+week4.getWeeklyLimitHydration(),
                        week1.getWeeklyLimitCaffiene()+week2.getWeeklyLimitCaffiene()+week3.getWeeklyLimitCaffiene()+week4.getWeeklyLimitCaffiene(),
                        week1.getWeeklyLimitAlcohol()+week2.getWeeklyLimitAlcohol()+week3.getWeeklyLimitAlcohol()+week4.getWeeklyLimitAlcohol(),
                        week1.getWeeklyLimitCheats()+week2.getWeeklyLimitCheats()+week3.getWeeklyLimitCheats()+week4.getWeeklyLimitCheats()};

        //updating text for all the main food groups
        for(int i=0;i<textViewsCount.length;i++) {
            //getting values from the arrays for this index
            TextView countTV = textViewsCount[i];
            double count = counts[i];
            double plan = plans[i];

            countTV.setText(String.format("%s/%s", df.format(count), df.format(plan)));
        }

        monthlyCheatsPB.setMax((int) (week1.getWeeklyLimitCheats() * 4 * SCALE_FACTOR));
        monthlyCheatsPB.setProgress((int) (week1.getTotalCheats()+week2.getTotalCheats()+week3.getTotalCheats()+week4.getTotalCheats())*SCALE_FACTOR);
    }

    /**
     * Adapter for the weeks information
     */
    public class WeekHistoryAdapter extends RecyclerView.Adapter<WeekHistoryViewHolder> {
        int nWeeks;
        Activity activity;
        SparseBooleanArray nDaysAgoVisible;

        WeekHistoryAdapter(Activity activity, int nWeeks) {
            this.activity = activity;
            this.nWeeks = nWeeks;
            this.nDaysAgoVisible = new SparseBooleanArray();
        }

        @NonNull
        @Override
        public WeekHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(activity).inflate(R.layout.list_item_weekly_data, viewGroup, false);
            return new WeekHistoryViewHolder(activity, view, this);
        }

        @Override
        public void onBindViewHolder(@NonNull WeekHistoryViewHolder weekHistoryViewHolder, int position) {
            new WeekBuilder(weekHistoryViewHolder, position);
        }

        @Override
        public int getItemCount() {
            return nWeeks;
        }
    }

    /**
     * Class in charge of building the weekshistory view for a given WeekHistoryViewHolder
     */
    public static class WeekBuilder {

        WeekBuilder(WeekHistoryViewHolder parent, int nWeeksAgo) {
            parent.nWeeksAgo = nWeeksAgo;

            //delegate heavy methods to two background tasks
            new WeekImageBuilder(parent).execute();
            new WeekStringBuilder(parent).execute();
        }

        /**
         * builds the images for the view, and sends them off to the viewholder to build once ready
         */
        private static class WeekImageBuilder extends AsyncTask<Void, Void, Void> {
            private HashMap<String, Drawable> imageMap = new HashMap<>();
            private HashMap<String, Boolean> completedMap = new HashMap<>();
            WeekHistoryViewHolder parent;
            DietController dietController;
            private double cheatMax;
            private double cheatProgress;

            WeekImageBuilder(WeekHistoryViewHolder parent) {
                this.parent = parent;
            }

            @Override
            protected Void doInBackground(Void... voids) {
                dietController = BasicDietController.getInstance();
                WeeklyIntake week = dietController.getWeeksIntake(parent.nWeeksAgo);

                //getting drawables from the device
                Drawable completableWater = parent.activity.getDrawable(R.drawable.symbol_water_completion_selctor);
                Drawable completableFood = parent.activity.getDrawable(R.drawable.symbol_food_completion_selector);
                Drawable checked = parent.activity.getDrawable(R.drawable.symbol_check_selector);
                Drawable arrowDown = parent.activity.getDrawable(android.R.drawable.arrow_down_float);

                //putting the drawables and completion information into hashmaps
                imageMap.put("water", completableWater);
                completedMap.put("water", week.isHydrationCompleted());
                imageMap.put("food", completableFood);
                completedMap.put("food", week.isFoodCompleted());
                imageMap.put("cheat", checked);
                completedMap.put("cheat", week.isOverCheatScore());
                imageMap.put("arrow_down", arrowDown);

                //getting cheat information
                cheatMax = week.getWeeklyLimitCheats();
                cheatProgress = week.getTotalCheats();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                parent.buildDrawables(imageMap, completedMap, cheatMax, cheatProgress);
            }
        }

        private static class WeekStringBuilder extends AsyncTask<Void, Void, HashMap<Meal.FoodType, String>> {
            private WeeklyIntake week;
            WeekHistoryViewHolder parent;
            DietController dietController;

            WeekStringBuilder(WeekHistoryViewHolder parent) {
                this.parent = parent;
            }

            @Override
            protected HashMap<Meal.FoodType, String> doInBackground(Void... voids) {
                NumberFormat df = new DecimalFormat("##.##");
                dietController = BasicDietController.getInstance();
                week = dietController.getWeeksIntake(parent.nWeeksAgo);

                //creating text for different views from dietcontroller information
                String vegCountText = df.format(week.getVegCount()) + "/" + df.format(week.getWeeklyLimitVeg());
                String proteinCountText = df.format(week.getProteinCount()) + "/" + df.format(week.getWeeklyLimitProtein());
                String dairyCountText = df.format(week.getDairyCount()) + "/" + df.format(week.getWeeklyLimitDairy());
                String grainCountText = df.format(week.getGrainCount()) + "/" + df.format(week.getWeeklyLimitGrain());
                String fruitCountText = df.format(week.getFruitCount()) + "/" + df.format(week.getWeeklyLimitFruit());
                String waterCountText = df.format(week.getHydrationScore()) + "/" + df.format(week.getWeeklyLimitHydration());
                String cheatCountText = df.format(week.getTotalCheats()) + "/" + df.format(week.getWeeklyLimitCheats());
                String caffeineCountText = df.format(week.getCaffieneCount()) + "/" + df.format(week.getWeeklyLimitCaffiene());
                String alcoholCountText = df.format(week.getAlcoholCount()) + "/" + df.format(week.getWeeklyLimitAlcohol());

                //putting text into a map
                HashMap<Meal.FoodType, String> stringMap = new HashMap<>();
                stringMap.put(Meal.FoodType.VEGETABLE, vegCountText);
                stringMap.put(Meal.FoodType.MEAT, proteinCountText);
                stringMap.put(Meal.FoodType.DAIRY, dairyCountText);
                stringMap.put(Meal.FoodType.GRAIN, grainCountText);
                stringMap.put(Meal.FoodType.FRUIT, fruitCountText);
                stringMap.put(Meal.FoodType.WATER, waterCountText);
                stringMap.put(Meal.FoodType.EXCESS, cheatCountText);
                stringMap.put(Meal.FoodType.CAFFEINE, caffeineCountText);
                stringMap.put(Meal.FoodType.ALCOHOL, alcoholCountText);
                return stringMap;
            }

            @Override
            protected void onPostExecute(HashMap<Meal.FoodType, String> stringMap) {
                parent.buildText(week, stringMap);
            }
        }
    }


    /**
     * View Holder for the information about a week.
     */
    private class WeekHistoryViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        DateFormat dateFormat = new SimpleDateFormat("MMM dd");
        Activity activity;
        WeekHistoryAdapter adapter;
        int nWeeksAgo;

        WeekHistoryViewHolder(Activity activity, @NonNull View itemView, WeekHistoryAdapter adapter) {
            super(itemView);
            this.itemView = itemView;
            this.activity = activity;
            this.adapter = adapter;
        }

        /**
         * Builds the drawables for the viewHolder
         * @param imageMap, map of strings to the drawables that need to be updated
         * @param completedMap, whether each item is completed or not
         * @param cheatMax, the max value that the cheat score can reach
         * @param cheatProgress, the current cheat score
         */
        void buildDrawables(HashMap<String, Drawable> imageMap, HashMap<String, Boolean> completedMap,
                            double cheatMax, double cheatProgress) {
            final int SCALE_FACTOR = 100;
            final View expandableView = itemView.findViewById(R.id.expanded_layout);
            final ImageView dropdownButton = itemView.findViewById(R.id.dropdown_button);

            //getting views
            CompletableItemView completedFoodView = itemView.findViewById(R.id.completed_food);
            CompletableItemView completedWaterView = itemView.findViewById(R.id.completed_water);
            CompletableItemView didntCheatView = itemView.findViewById(R.id.didnt_cheat);
            ProgressBar cheatsProgressBar = itemView.findViewById(R.id.progress_cheats);

            //setting images and completion status
            completedFoodView.setBackground(imageMap.get("food"));
            completedFoodView.setCompleted(completedMap.get("food"));
            if(trackWater) {
                completedWaterView.setBackground(imageMap.get("water"));
                completedWaterView.setCompleted(completedMap.get("water"));
            } else {
                completedWaterView.setVisibility(View.GONE);
            } if (trackCheats) {
                didntCheatView.setBackground(imageMap.get("cheat"));
                didntCheatView.setCompleted(!completedMap.get("cheat"));
            } else {
                itemView.findViewById(R.id.cheat_progress_container).setVisibility(View.GONE);
            }
            dropdownButton.setBackground(imageMap.get("arrow_down"));

            //setting up the cheat progress bar
            cheatsProgressBar.setMax((int) cheatMax*SCALE_FACTOR);
            cheatsProgressBar.setProgress((int) cheatProgress*SCALE_FACTOR);

            //making sure view is shown as it should be, either expanded or not
            if (adapter.nDaysAgoVisible.get(nWeeksAgo, false)) {
                expandableView.setVisibility(View.VISIBLE);
                dropdownButton.setRotation(180);
            } else {
                expandableView.setVisibility(View.GONE);
                dropdownButton.setRotation(0);
            }

            //creating functionality for the button that expands the card
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (expandableView.getVisibility() == View.GONE) {
                        expandableView.setVisibility(View.VISIBLE);
                        dropdownButton.animate().setDuration(200).rotation(180);
                        adapter.nDaysAgoVisible.put(nWeeksAgo, true);
//                        adapter.notifyItemChanged(nWeeksAgo);
                    } else {
                        expandableView.setVisibility(View.GONE);
                        dropdownButton.animate().setDuration(200).rotation(0);
                        adapter.nDaysAgoVisible.put(nWeeksAgo, false);
//                        adapter.notifyItemChanged(nWeeksAgo);
                    }
                }
            });

            //setting up the dayHistory RecyclerView
            DaysHistoryAdapter dayHistoryAdapter = new DaysHistoryAdapter(activity, 7, nWeeksAgo);
            RecyclerView daysHistory = itemView.findViewById(R.id.days_list);
            daysHistory.setAdapter(dayHistoryAdapter);
        }

        void buildText(WeeklyIntake week, HashMap<Meal.FoodType, String> stringMap) {
            //getting view objects
            TextView dateTV = itemView.findViewById(R.id.date);

            //building the expanding view and adding it to the layout
            LayoutInflater inflater = LayoutInflater.from(activity);
            ConstraintLayout foodGroupContainer = (ConstraintLayout) inflater.inflate(R.layout.container_food_group_counts_expanded, null);
            foodGroupContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            LinearLayout expandableView = itemView.findViewById(R.id.expanded_layout);
            expandableView.addView(foodGroupContainer, 0);

            //getting the text views
            TextView vegCount = itemView.findViewById(R.id.weekly_veges_intake);
            TextView proteinCount = itemView.findViewById(R.id.weekly_protein_intake);
            TextView dairyCount = itemView.findViewById(R.id.weekly_dairy_intake);
            TextView grainCount = itemView.findViewById(R.id.weekly_grain_intake);
            TextView fruitCount = itemView.findViewById(R.id.weekly_fruit_intake);
            TextView waterCount = itemView.findViewById(R.id.weekly_water_intake);
            TextView cheatCount = itemView.findViewById(R.id.weekly_cheat_count);
            TextView caffeineCount = itemView.findViewById(R.id.weekly_caffeine_count);
            TextView alcoholCount = itemView.findViewById(R.id.weekly_alcohol_count);

            //setting text view objects
            dateTV.setText(String.format("%s - %s", dateFormat.format(week.getStartDate()), dateFormat.format(week.getEndDate().getTime() - DateUtils.DAY_IN_MILLIS)));
            vegCount.setText(stringMap.get(Meal.FoodType.VEGETABLE));
            proteinCount.setText(stringMap.get(Meal.FoodType.MEAT));
            dairyCount.setText(stringMap.get(Meal.FoodType.DAIRY));
            grainCount.setText(stringMap.get(Meal.FoodType.GRAIN));
            fruitCount.setText(stringMap.get(Meal.FoodType.FRUIT));
            waterCount.setText(stringMap.get(Meal.FoodType.WATER));
            cheatCount.setText(stringMap.get(Meal.FoodType.EXCESS));
            caffeineCount.setText(stringMap.get(Meal.FoodType.CAFFEINE));
            alcoholCount.setText(stringMap.get(Meal.FoodType.ALCOHOL));

            //removing unnecessary views
            if(!trackCheats) {
                itemView.findViewById(R.id.cheat_count_header).setVisibility(View.GONE);
                itemView.findViewById(R.id.weekly_cheat_count).setVisibility(View.GONE);
            } if(!trackWater) {
                itemView.findViewById(R.id.water_count_header).setVisibility(View.GONE);
                itemView.findViewById(R.id.weekly_water_intake).setVisibility(View.GONE);
            } if(!trackAlcohol) {
                itemView.findViewById(R.id.alcohol_count_header).setVisibility(View.GONE);
                itemView.findViewById(R.id.weekly_alcohol_count).setVisibility(View.GONE);
            } if(!trackCaffeine) {
                itemView.findViewById(R.id.caffeine_count_header).setVisibility(View.GONE);
                itemView.findViewById(R.id.weekly_caffeine_count).setVisibility(View.GONE);
            }
        }
    }

    /**
     * Adapter for the days information
     */
    public class DaysHistoryAdapter extends RecyclerView.Adapter<DaysHistoryViewHolder> {
        int nDays;
        Activity activity;
        SparseBooleanArray nDaysAgoVisible;
        int weeksAgo;

        DaysHistoryAdapter(Activity activity, int nDays, int weeksAgo) {
            this.activity = activity;
            this.nDays = nDays;
            this.nDaysAgoVisible = new SparseBooleanArray();
            this.weeksAgo = weeksAgo;
        }

        @NonNull
        @Override
        public DaysHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view;
            view = LayoutInflater.from(activity).inflate(R.layout.list_item_day_summary, viewGroup, false);
            return new HistoryActivity.DaysHistoryViewHolder(activity, view, this);
        }

        @Override
        public void onBindViewHolder(@NonNull DaysHistoryViewHolder daysHistoryViewHolder, int position) {
            daysHistoryViewHolder.build(position, weeksAgo);
        }

        @Override
        public int getItemCount() {
            return nDays;
        }
    }


    /**
     * View Holder for the information about a day.
     */
    private class DaysHistoryViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        Activity activity;
        DaysHistoryAdapter adapter;

        DaysHistoryViewHolder(Activity activity, @NonNull View itemView, DaysHistoryAdapter adapter) {
            super(itemView);
            this.itemView = itemView;
            this.activity = activity;
            this.adapter = adapter;
        }

        public void build(final int dayNo, int nWeeksAgo) {
            DietController dietController = BasicDietController.getInstance();
            DailyMeals day = dietController.getDaysMeals(dayNo + 7*nWeeksAgo);
            DietPlan daysPlan = dietController.getDaysDietPlan(dayNo + 7*nWeeksAgo);

            //updating date information
            TextView dateTV = itemView.findViewById(R.id.date);
            dateTV.setText(dateFormat.format(day.getDate()));

            //updating text views
            NumberFormat df = new DecimalFormat("##.##");
            TextView vegCount = itemView.findViewById(R.id.veg_count);
            TextView proteinCount = itemView.findViewById(R.id.protein_count);
            TextView dairyCount = itemView.findViewById(R.id.dairy_count);
            TextView grainCount = itemView.findViewById(R.id.grain_count);
            TextView fruitCount = itemView.findViewById(R.id.fruit_count);
            TextView waterCount = itemView.findViewById(R.id.water_count);
            TextView cheatCount = itemView.findViewById(R.id.cheat_count);

            vegCount.setText((df.format(day.getVegCount()) + "/" + df.format(daysPlan.getDailyVeges())));
            proteinCount.setText((df.format(day.getProteinCount()) + "/" + df.format(daysPlan.getDailyProtein())));
            dairyCount.setText((df.format(day.getDairyCount()) + "/" + df.format(daysPlan.getDailyDairy())));
            grainCount.setText((df.format(day.getGrainCount()) + "/" + df.format(daysPlan.getDailyGrain())));
            fruitCount.setText((df.format(day.getFruitCount()) + "/" + df.format(daysPlan.getDailyFruit())));
            waterCount.setText((df.format(day.getHydrationScore()) + "/" + df.format(daysPlan.getDailyHydration())));
            cheatCount.setText((df.format(day.getTotalCheats()) + "/" + df.format(daysPlan.getDailyCheats())));

            if(!trackCheats) {
                itemView.findViewById(R.id.cheat_container).setVisibility(View.GONE);
            } if(!trackWater) {
                itemView.findViewById(R.id.water_container).setVisibility(View.GONE);
            }
        }

    }

    /**
     * Shows the progress UI and hides the Chat Information
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        //getting progressbar and animation
        final View progressView = historyView.findViewById(R.id.progress);
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        //making visibility match the show variable
        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        progressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}
