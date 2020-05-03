package com.ganterpore.simplediet.View.Activities;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.ganterpore.simplediet.Controller.BasicDietController;
import com.ganterpore.simplediet.Controller.DailyMeals;
import com.ganterpore.simplediet.Controller.WeeklyIntake;
import com.ganterpore.simplediet.Model.DietPlan;
import com.ganterpore.simplediet.Model.Meal.FoodType;
import com.ganterpore.simplediet.R;
import com.ganterpore.simplediet.View.DialogBoxes.AddDrinkDialogBox;
import com.ganterpore.simplediet.View.DialogBoxes.AddMealDialogBox;
import com.ganterpore.simplediet.View.DialogBoxes.AddServeDialogBox;
import com.ganterpore.simplediet.View.DialogBoxes.RecipeListDialogBox;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import static android.content.Context.MODE_PRIVATE;
import static com.ganterpore.simplediet.View.Activities.MainActivity.SHARED_PREFS_LOC;

public class DailyDisplayActivity extends Fragment {
    private static final String TAG = "MainActivity";

    private SharedPreferences preferences;

    private MealHistoryDisplay mealView;

    private boolean isFABOpen = false;

    private View dailyDisplayView;

    private Activity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        dailyDisplayView = inflater.inflate(R.layout.activity_daily_display, container, false);
        activity = getActivity();
        if(activity==null) {
            System.exit(0);
        }

        preferences = activity.getSharedPreferences(SHARED_PREFS_LOC, MODE_PRIVATE);

        initialiseScrollEffect();

        mealView = new MealHistoryDisplay(activity,
                (RecyclerView) dailyDisplayView.findViewById(R.id.day_history_list),
                (ProgressBar) dailyDisplayView.findViewById(R.id.progress_bar));

        //creating click functionality
        View.OnClickListener addFoodOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFood(v);
            }
        };
        View.OnClickListener addSnackOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSnack(v);
            }
        };
        dailyDisplayView.findViewById(R.id.FABBackground).setOnClickListener(addFoodOnClick);
        dailyDisplayView.findViewById(R.id.recipeBookFAB).setOnClickListener(addFoodOnClick);
        dailyDisplayView.findViewById(R.id.addDrinkFAB).setOnClickListener(addFoodOnClick);
        dailyDisplayView.findViewById(R.id.addMealFAB).setOnClickListener(addFoodOnClick);
        dailyDisplayView.findViewById(R.id.addFoodFAB).setOnClickListener(addFoodOnClick);
        dailyDisplayView.findViewById(R.id.water_layout).setOnClickListener(addSnackOnClick);
        dailyDisplayView.findViewById(R.id.veg_layout).setOnClickListener(addSnackOnClick);
        dailyDisplayView.findViewById(R.id.protein_layout).setOnClickListener(addSnackOnClick);
        dailyDisplayView.findViewById(R.id.dairy_layout).setOnClickListener(addSnackOnClick);
        dailyDisplayView.findViewById(R.id.grain_layout).setOnClickListener(addSnackOnClick);
        dailyDisplayView.findViewById(R.id.fruit_layout).setOnClickListener(addSnackOnClick);
        dailyDisplayView.findViewById(R.id.cheat_layout).setOnClickListener(addSnackOnClick);

        return dailyDisplayView;
    }

    /**
     * Makes sure that items in the app bar scale appropriately when the main view is scrolled
     */
    private void initialiseScrollEffect() {
        final ConstraintLayout progressCircle = dailyDisplayView.findViewById(R.id.progress_sphere);
        final ConstraintLayout progressCheats = dailyDisplayView.findViewById(R.id.cheats_progress);
        final ConstraintLayout progressDrinks = dailyDisplayView.findViewById(R.id.drinks_progress);
        AppBarLayout appBarLayout = dailyDisplayView.findViewById(R.id.appBar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.BaseOnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                //making sure progress circle shrinks when scrolled upwards
                int scrollRange = appBarLayout.getTotalScrollRange();
                float offsetFactor = (float) (-verticalOffset) / (float) scrollRange;
                float scaleFactor = 1F - offsetFactor * .5F ;
                progressCircle.setScaleX(scaleFactor);
                progressCircle.setScaleY(scaleFactor);
                progressCheats.setScaleX(scaleFactor);
                progressCheats.setScaleY(scaleFactor);
                progressDrinks.setScaleX(scaleFactor);
                progressDrinks.setScaleY(scaleFactor);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        String mode = preferences.getString("mode", "normal");
        ProgressBar meatProgress = dailyDisplayView.findViewById(R.id.progress_meat);
        //if the mode has not been set, then just leave as is
        if(mode==null) {
            return;
        }
        //if in one mode, and we are seeing the wrong image, update it
        if ((mode.equals("vegan") || mode.equals("vegetarian"))) {
            meatProgress.setProgressDrawable(activity.getDrawable(R.drawable.progress_bar_meat_vegan));
        } else if(mode.equals("normal")) {
            meatProgress.setProgressDrawable(activity.getDrawable(R.drawable.progress_bar_meat));
        }
        //making sure to turn off any disabled view
        boolean track_cheats = preferences.getBoolean("track_cheats", true);
        boolean track_water = preferences.getBoolean("track_water", true);
        boolean track_alcohol = preferences.getBoolean("track_alcohol", true);
        boolean track_caffeine = preferences.getBoolean("track_caffeine", true);
        dailyDisplayView.findViewById(R.id.weekly_intake).setVisibility(!(track_alcohol || track_caffeine || track_cheats) ? View.GONE : View.VISIBLE);
        dailyDisplayView.findViewById(R.id.cheat_layout).setVisibility(track_cheats ? View.VISIBLE : View.GONE);
        dailyDisplayView.findViewById(R.id.cheats_progress).setVisibility(track_cheats ? View.VISIBLE : View.GONE);
        dailyDisplayView.findViewById(R.id.weekly_cheats_progress).setVisibility(track_cheats ? View.VISIBLE : View.GONE);
        dailyDisplayView.findViewById(R.id.weekly_cheat_count_header).setVisibility(track_cheats ? View.VISIBLE : View.GONE);
        dailyDisplayView.findViewById(R.id.weekly_cheat_count).setVisibility(track_cheats ? View.VISIBLE : View.GONE);
        dailyDisplayView.findViewById(R.id.drinks_progress).setVisibility(track_water ? View.VISIBLE : View.GONE);
        dailyDisplayView.findViewById(R.id.water_layout).setVisibility(track_water ? View.VISIBLE : View.GONE);
        dailyDisplayView.findViewById(R.id.weekly_alcohol_image).setVisibility(track_alcohol ? View.VISIBLE : View.GONE);
        dailyDisplayView.findViewById(R.id.alcohol_count_header).setVisibility(track_alcohol ? View.VISIBLE : View.GONE);
        dailyDisplayView.findViewById(R.id.weekly_alcohol_count).setVisibility(track_alcohol ? View.VISIBLE : View.GONE);
        dailyDisplayView.findViewById(R.id.alcohol_image).setVisibility(track_alcohol ? View.VISIBLE : View.GONE);
        dailyDisplayView.findViewById(R.id.alcohol_count).setVisibility(track_alcohol ? View.VISIBLE : View.GONE);
        dailyDisplayView.findViewById(R.id.weekly_caffeine_image).setVisibility(track_caffeine ? View.VISIBLE : View.GONE);
        dailyDisplayView.findViewById(R.id.caffeine_count_header).setVisibility(track_caffeine ? View.VISIBLE : View.GONE);
        dailyDisplayView.findViewById(R.id.weekly_caffeine_count).setVisibility(track_caffeine ? View.VISIBLE : View.GONE);
        dailyDisplayView.findViewById(R.id.caffeine_image).setVisibility(track_caffeine ? View.VISIBLE : View.GONE);
        dailyDisplayView.findViewById(R.id.caffeine_count).setVisibility(track_caffeine ? View.VISIBLE : View.GONE);
        refresh();
    }


    /**
     * called when a user taps the add food button
     * allows the user  to choose what type of food they want to add.
     * @param view, the view that called the function
     */
    private void addFood(final View view) {
        switch (view.getId()) {
            case R.id.addFoodFAB:
                openCloseFoodFAB();
                break;
            case R.id.addMealFAB:
                AddMealDialogBox.addMeal(activity);
                openCloseFoodFAB();
                break;
            case R.id.addDrinkFAB:
                AddDrinkDialogBox.addDrink(activity);
                openCloseFoodFAB();
                break;
            case R.id.recipeBookFAB:
                RecipeListDialogBox.openRecipeBook(activity);
                openCloseFoodFAB();
                break;
            case R.id.FABBackground:
                if(isFABOpen) {
                    openCloseFoodFAB();
                }
                break;
        }
    }

    /**
     * Used to expand or close the food floating action button opetions
     */
    private void openCloseFoodFAB() {
        //getting all the views and buttons
        View background = dailyDisplayView.findViewById(R.id.FABBackground);
        FloatingActionButton addFoodFAB = dailyDisplayView.findViewById(R.id.addFoodFAB);
        FloatingActionButton mealFAB = dailyDisplayView.findViewById(R.id.addMealFAB);
        FloatingActionButton drinkFAB = dailyDisplayView.findViewById(R.id.addDrinkFAB);
        FloatingActionButton recipeBookFAB = dailyDisplayView.findViewById(R.id.recipeBookFAB);
        TextView mealTV = dailyDisplayView.findViewById(R.id.addMealTV);
        TextView addDrinkTV = dailyDisplayView.findViewById(R.id.addDrinkTV);
        TextView recipeBookTV = dailyDisplayView.findViewById(R.id.recipeBookTV);

        //if the floating action button is open, close everything
        if(isFABOpen) {
            isFABOpen = false;
            addFoodFAB.animate().setDuration(400).rotation(45);
            mealFAB.hide();
            drinkFAB.hide();
            recipeBookFAB.hide();
            mealTV.setVisibility(View.GONE);
            addDrinkTV.setVisibility(View.GONE);
            recipeBookTV.setVisibility(View.GONE);
            background.setVisibility(View.GONE);
        } //otherwise open everything
        else {
            isFABOpen = true;
            addFoodFAB.animate().setDuration(400).rotation(180);
            mealFAB.show();
            recipeBookFAB.show();
            mealTV.setVisibility(View.VISIBLE);
            if(preferences.getBoolean("track_water", true)) {
                addDrinkTV.setVisibility(View.VISIBLE);
                drinkFAB.show();
            }
            recipeBookTV.setVisibility(View.VISIBLE);
            background.setVisibility(View.VISIBLE);
        }
    }

    /**
     * When a snack is pressed, get type from the view that clicked it, and open correct
     * window
     */
    private void addSnack(final View view) {
        FoodType type;
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.water_layout:
                type = FoodType.WATER;
                break;
            case R.id.veg_layout:
                type = FoodType.VEGETABLE;
                break;
            case R.id.protein_layout:
                type = FoodType.MEAT;
                break;
            case R.id.dairy_layout:
                type = FoodType.DAIRY;
                break;
            case R.id.grain_layout:
                type = FoodType.GRAIN;
                break;
            case R.id.fruit_layout:
                type = FoodType.FRUIT;
                break;
            case R.id.cheat_layout:
                type = FoodType.EXCESS;
                break;
            default:
                type = null;
                break;
        }
        intent.putExtra("foodType", type);
        AddServeDialogBox.addServe(activity, intent, null);
    }

    /**
     * updates the values of all the views on the screen to up to date values
     */
    public static class DisplayRefresher extends AsyncTask<Void, Void, Void> {

        DailyDisplayActivity parent;

        private double[] counts;
        private double[] plans;
        private String cheatRatio;
        private String cheatsToday;
        private double dailyCheats;
        private double totalCheats;

        DisplayRefresher(DailyDisplayActivity parent) {
            this.parent = parent;
        }

        @Override
        protected Void doInBackground(Void[] voids) {
            //getting variables to work with
            NumberFormat df = new DecimalFormat("##.##"); //format to show all decimal strings
            BasicDietController dietController = BasicDietController.getInstance();
            DietPlan todaysDietPlan = dietController.getTodaysDietPlan();
            DailyMeals today = dietController.getTodaysMeals();
            WeeklyIntake thisWeek = dietController.getThisWeeksIntake();

            //getting vaules to be sent to the refreshr to handle
            totalCheats = today.getTotalCheats();
            counts = new double[]{today.getVegCount(), today.getProteinCount(), today.getDairyCount(),
                    today.getGrainCount(), today.getFruitCount(), today.getHydrationScore(),
                    today.getCaffieneCount(), today.getAlcoholCount(), totalCheats,
                    thisWeek.getCaffieneCount(), thisWeek.getAlcoholCount(), thisWeek.getTotalCheats()};
            dailyCheats = todaysDietPlan.getDailyCheats();
            plans = new double[]{todaysDietPlan.getDailyVeges(), todaysDietPlan.getDailyProtein(), todaysDietPlan.getDailyDairy(),
                    todaysDietPlan.getDailyGrain(), todaysDietPlan.getDailyFruit(), todaysDietPlan.getDailyHydration(),
                    todaysDietPlan.getDailyCaffeine(), todaysDietPlan.getDailyAlcohol(), dailyCheats,
                    thisWeek.getWeeklyLimitCaffiene(), thisWeek.getWeeklyLimitAlcohol(), thisWeek.getWeeklyLimitCheats()};

            cheatRatio = String.format("%s/%s", df.format(totalCheats), df.format(dailyCheats));
            cheatsToday = String.format("%s today!", df.format(totalCheats));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            parent.refresh2(counts, plans, totalCheats, dailyCheats, cheatRatio, cheatsToday);
        }
    }

    void refresh() {
        new DisplayRefresher(this).execute();
    }

    private void refresh2(double[] counts, double[] plans, double totalCheats,  double dailyCheats,  String cheatRatio, String cheatsToday) {
        final int SCALE_FACTOR = 100; //how much to scale the progress bars by (to allow more granularity)
        NumberFormat df = new DecimalFormat("##.##"); //format to show all decimal strings
        View weeklyContainer = dailyDisplayView.findViewById(R.id.weekly_intake);

        //get the text views from the main activity
        TextView vegTV = dailyDisplayView.findViewById(R.id.veg_count);
        TextView proteinTV = dailyDisplayView.findViewById(R.id.protein_count);
        TextView dairyTV = dailyDisplayView.findViewById(R.id.dairy_count);
        TextView grainTV = dailyDisplayView.findViewById(R.id.grain_count);
        TextView fruitTV = dailyDisplayView.findViewById(R.id.fruit_count);
        TextView waterTV = dailyDisplayView.findViewById(R.id.water_count);
        TextView caffeineTV = dailyDisplayView.findViewById(R.id.caffeine_count);
        TextView alcoholTV = dailyDisplayView.findViewById(R.id.alcohol_count);
        TextView cheatTV = dailyDisplayView.findViewById(R.id.cheat_count);

        TextView weeklyCaffeineTV = dailyDisplayView.findViewById(R.id.weekly_caffeine_count);
        TextView weeklyAlcoholTV = dailyDisplayView.findViewById(R.id.weekly_alcohol_count);
        TextView weeklyCheatTV = dailyDisplayView.findViewById(R.id.weekly_cheat_count);

        TextView vegeLeftTV = dailyDisplayView.findViewById(R.id.veg_left);
        TextView proteinLeftTV = dailyDisplayView.findViewById(R.id.protein_left);
        TextView dairyLeftTV = dailyDisplayView.findViewById(R.id.dairy_left);
        TextView grainLeftTV = dailyDisplayView.findViewById(R.id.grain_left);
        TextView fruitLeftTV = dailyDisplayView.findViewById(R.id.fruit_left);
        TextView waterLeftTV = dailyDisplayView.findViewById(R.id.water_left);
        TextView cheatsTodayTV = dailyDisplayView.findViewById(R.id.cheats_today);

        //get the progress bars from the main activity
        ProgressBar vegPB = dailyDisplayView.findViewById(R.id.progress_vege);
        ProgressBar meatPB = dailyDisplayView.findViewById(R.id.progress_meat);
        ProgressBar dairyPB = dailyDisplayView.findViewById(R.id.progress_dairy);
        ProgressBar grainPB = dailyDisplayView.findViewById(R.id.progress_grain);
        ProgressBar fruitPB = dailyDisplayView.findViewById(R.id.progress_fruit);
        ProgressBar waterPB = dailyDisplayView.findViewById(R.id.progress_water);
        ProgressBar cheatsPB = dailyDisplayView.findViewById(R.id.toolbar_layout).findViewById(R.id.progress_cheats);
        ProgressBar weeklyCheatsPB = weeklyContainer.findViewById(R.id.progress_cheats);

        //creating arrays of the text views to update
        TextView[] textViewsCount = {vegTV, proteinTV, dairyTV, grainTV, fruitTV, waterTV, caffeineTV, alcoholTV, cheatTV,
                weeklyCaffeineTV, weeklyAlcoholTV, weeklyCheatTV};
        TextView[] textViewsLeft = {vegeLeftTV, proteinLeftTV, dairyLeftTV, grainLeftTV, fruitLeftTV, waterLeftTV, null, null,  null,
                null, null, null};
        ProgressBar[] progressBars = {vegPB, meatPB, dairyPB, grainPB, fruitPB, waterPB, null, null, null,
                null, null, weeklyCheatsPB};


        //updating text for all the main food groups
        for(int i=0;i<textViewsCount.length;i++) {
            //getting values from the arrays for this index
            TextView countTV = textViewsCount[i];
            TextView leftTV = textViewsLeft[i];
            ProgressBar progressBar = progressBars[i];
            double count = counts[i];
            double plan = plans[i];
            double servesLeft = plan - count;

            //if meal group has been completed, update the UI to reflect this
            if(servesLeft <= 0.2) {
                if(countTV != null) {
                    countTV.setText(String.format("%s/%s", df.format(count), df.format(plan)));
                } if(leftTV != null) {
                    leftTV.setText(R.string.completed_text);
                }
                //TODO update colors when food is completed
//                countTV.setTextColor(Color.GREEN);
//                leftTV.setTextColor(Color.GREEN);
//                countTV.setAlpha((float) 1);
//                leftTV.setAlpha((float) 1);
            } else {
                if(countTV != null) {
                    countTV.setText(String.format("%s/%s", df.format(count), df.format(plan)));
                } if (leftTV != null) {
                    leftTV.setText(String.format("%s left", df.format(servesLeft)));
                }
                //TODO update colors when food is completed
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    countTV.setTextColor(getResources().getColor(R.color.textColor, getTheme()));
//                    leftTV.setTextColor(getResources().getColor(R.color.textColor, getTheme()));
//                } else {
//                    countTV.setTextColor(getResources().getColor(R.color.textColor));
//                    leftTV.setTextColor(getResources().getColor(R.color.textColor));
//                }
//                countTV.setAlpha((float) 0.6);
//                leftTV.setAlpha((float) 0.6);
            }
            //animating the updating of the progress bar
            if(progressBar != null) {
                progressBar.setMax((int) (plan*SCALE_FACTOR));
                ObjectAnimator objectAnimator = ObjectAnimator.ofInt(progressBar, "progress", (int) (count*SCALE_FACTOR));
                objectAnimator.setDuration(500);
                objectAnimator.setInterpolator(new DecelerateInterpolator());
                objectAnimator.start();
            }
        }
        //updating other texts
        cheatTV.setText(cheatRatio);
        cheatsTodayTV.setText(cheatsToday);
        //animating any updates to the cheat progress bar
        cheatsPB.setMax((int) (dailyCheats * SCALE_FACTOR));
        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(cheatsPB, "progress",
                (int) (totalCheats *SCALE_FACTOR));
        objectAnimator.setDuration(500);
        objectAnimator.setInterpolator(new DecelerateInterpolator());
        objectAnimator.start();

        //refreshing the other views
        mealView.refresh();
    }
}
