package com.ganterpore.simplediet.View.Activities;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import com.ganterpore.simplediet.Controller.DietController;
import com.ganterpore.simplediet.Controller.OverUnderEatingDietController;
import com.ganterpore.simplediet.Controller.WeeklyIntake;
import com.ganterpore.simplediet.Model.DietPlan;
import com.ganterpore.simplediet.Model.Meal.FoodType;
import com.ganterpore.simplediet.R;
import com.ganterpore.simplediet.View.DialogBoxes.AddDrinkDialogBox;
import com.ganterpore.simplediet.View.DialogBoxes.AddMealDialogBox;
import com.ganterpore.simplediet.View.DialogBoxes.AddServeDialogBox;
import com.ganterpore.simplediet.View.DialogBoxes.RecipeListDialogBox;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.SimpleShowcaseEventListener;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.ganterpore.simplediet.View.Activities.MainActivity.SHARED_PREFS_LOC;
import static com.ganterpore.simplediet.View.Activities.MealHistoryDisplay.EXPIRY_TAG;

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
                (RecyclerView) dailyDisplayView.findViewById(R.id.day_history_list));

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
        dailyDisplayView.findViewById(R.id.cheats_container).setOnClickListener(addSnackOnClick);

        return dailyDisplayView;
    }

    /**
     * Makes sure that items in the app bar scale appropriately when the main view is scrolled
     */
    private void initialiseScrollEffect() {
        final ConstraintLayout progressCircle = dailyDisplayView.findViewById(R.id.progress_sphere);
        final ConstraintLayout progressCheats = dailyDisplayView.findViewById(R.id.cheats_progress);
        final ConstraintLayout progressDrinks = dailyDisplayView.findViewById(R.id.drinks_progress);
        final View[] viewsToHide = {
                dailyDisplayView.findViewById(R.id.header_information),
                dailyDisplayView.findViewById(R.id.water_count_header),
                dailyDisplayView.findViewById(R.id.water_count),
                dailyDisplayView.findViewById(R.id.water_left),
                dailyDisplayView.findViewById(R.id.cheat_count_header),
                dailyDisplayView.findViewById(R.id.cheat_count)
        };
        AppBarLayout appBarLayout = dailyDisplayView.findViewById(R.id.appBar);

        //setting the min height to half that of the starting height of the progress circle
        CollapsingToolbarLayout collapsingToolbarLayout = dailyDisplayView.findViewById(R.id.toolbar_layout);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int deviceWidth = displayMetrics.widthPixels;
        int progressCircleWidth = deviceWidth*3/7;
        final float paddingDIP = 8f;
        int paddingPX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paddingDIP*2, getResources().getDisplayMetrics());
        int minHeight = progressCircleWidth *6/10 + paddingPX;
        collapsingToolbarLayout.setMinimumHeight(minHeight);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.BaseOnOffsetChangedListener() {
            boolean viewsHidden = false;
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                //making sure progress circle shrinks when scrolled upwards
                //will become half the size at full scroll
                int scrollRange = appBarLayout.getTotalScrollRange();
                float percentScrolled = (float) (-verticalOffset) / (float) scrollRange;
                float scaleFactor = 1F - percentScrolled * .4F ;
                progressCircle.setScaleX(scaleFactor);
                progressCircle.setScaleY(scaleFactor);
                progressCheats.setScaleX(scaleFactor);
                progressCheats.setScaleY(scaleFactor);
                progressDrinks.setScaleX(scaleFactor);
                progressDrinks.setScaleY(scaleFactor);

                int paddingPX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paddingDIP, getResources().getDisplayMetrics());
                progressCircle.setTranslationY(paddingPX*percentScrolled);

                //hid the viesToHide when the bar is fully scrolled up
                if(verticalOffset + scrollRange == 0) { //fully scrolled
                    for (View view : viewsToHide) {
                        view.setVisibility(View.INVISIBLE);
                    }
                    viewsHidden = true;
                } else if(viewsHidden) {
                    for (View view : viewsToHide) {
                        view.setVisibility(View.VISIBLE);
                    }
                    viewsHidden = false;
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
        if(preferences == null || activity == null) {
            activity = getActivity();
            if(activity == null) {
                System.exit(0);
            }
            preferences = activity.getSharedPreferences(SHARED_PREFS_LOC, MODE_PRIVATE);
        }
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
        dailyDisplayView.findViewById(R.id.cheats_container).setVisibility(track_cheats ? View.VISIBLE : View.INVISIBLE);
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
        refresh(null, null);

        //if a new user, give them a tutorial
        if(getActivity() != null && getActivity().getIntent().getBooleanExtra("new_user", false)) {
            newUserTutorial();
        }
    }

    /**
     * Starts a tutorial instructing the user how to use the app
     */
    private void newUserTutorial() {
        //hiding recommendations for new user
        SharedPreferences preferences = activity.getPreferences(MODE_PRIVATE);
        Date fortnightExpiry = new Date(System.currentTimeMillis() + DateUtils.WEEK_IN_MILLIS*2);
        Date dayExpiry = new Date(System.currentTimeMillis() + DateUtils.DAY_IN_MILLIS);
        preferences.edit().putLong(BasicDietController.CHEAT_CHANGE_RECOMMENDATION_ID + EXPIRY_TAG, getStartOfDay(fortnightExpiry).getTime()).apply();
        preferences.edit().putLong(BasicDietController.DIET_CHANGE_RECOMMENDATION_ID + EXPIRY_TAG, getStartOfDay(fortnightExpiry).getTime()).apply();
        preferences.edit().putLong(BasicDietController.CHEAT_SCORE_RECOMMENDATION_ID + EXPIRY_TAG, getStartOfDay(dayExpiry).getTime()).apply();
        preferences.edit().putLong(OverUnderEatingDietController.OVER_EATING_RECOMMENDATION_ID + EXPIRY_TAG, getStartOfDay(dayExpiry).getTime()).apply();
        preferences.edit().putLong(OverUnderEatingDietController.UNDER_EATING_RECOMMENDATION_ID + EXPIRY_TAG, getStartOfDay(dayExpiry).getTime()).apply();

        //getting targets for tutorial
        ViewTarget addFoodTarget = new ViewTarget(R.id.addFoodFAB, getActivity());
        ViewTarget goalsTarget = new ViewTarget(R.id.progress_sphere, getActivity());
        ViewTarget waterTarget = new ViewTarget(R.id.progress_water, getActivity());
        ViewTarget cheatsTarget = new ViewTarget(R.id.cheats_progress, getActivity());
        ViewTarget daysTarget = new ViewTarget(R.id.weekly_intake, getActivity());

        //building each tutorial page
        final ShowcaseView.Builder foodShowcase = new ShowcaseView.Builder(getActivity())
                .setTarget(addFoodTarget)
                .blockAllTouches()
                .hideOnTouchOutside()
                .setStyle(R.style.CustomShowcaseTheme)
                .setContentTitle("Adding Food")
                .setContentText("To add food tap the add button");

        final ShowcaseView.Builder goalsShowcase = new ShowcaseView.Builder(getActivity())
                .setTarget(goalsTarget)
                .blockAllTouches()
                .setStyle(R.style.CustomShowcaseTheme)
                .setContentTitle("Achieve Goals")
                .setContentText("Try to achieve your goals throughout the day by eating all the food in each category");

        final ShowcaseView.Builder drinksShowcase = new ShowcaseView.Builder(getActivity())
                .setTarget(waterTarget)
                .blockAllTouches()
                .setStyle(R.style.CustomShowcaseTheme)
                .setContentTitle("Achieve Goals")
                .setContentText("Try to drink enough water, but don't have too much alcohol or caffeine!");

        final ShowcaseView.Builder cheatsShowcase = new ShowcaseView.Builder(getActivity())
                .setTarget(cheatsTarget)
                .blockAllTouches()
                .setStyle(R.style.CustomShowcaseTheme)
                .setContentTitle("Be Careful!")
                .setContentText("Make sure not to eat too much unhealthy food and drink while achieving your goals, otherwise you will go over your cheat limits!");

        final ShowcaseView.Builder daysShowcase = new ShowcaseView.Builder(getActivity())
                .setTarget(daysTarget)
                .blockAllTouches()
                .setStyle(R.style.CustomShowcaseTheme)
                .setContentTitle("History")
                .setContentText("You can find your history here.");

        //ordering the tutorial
        foodShowcase.setShowcaseEventListener(new OnShowcaseEventListener() {
            @Override
            public void onShowcaseViewHide(ShowcaseView showcaseView) {
                goalsShowcase.build();
            }
            @Override
            public void onShowcaseViewDidHide(ShowcaseView showcaseView) { }
            @Override
            public void onShowcaseViewShow(ShowcaseView showcaseView) { }
            @Override
            public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) { }
        });
        goalsShowcase.setShowcaseEventListener(new OnShowcaseEventListener() {
            @Override
            public void onShowcaseViewHide(ShowcaseView showcaseView) {
                drinksShowcase.build();
            }
            @Override
            public void onShowcaseViewDidHide(ShowcaseView showcaseView) { }
            @Override
            public void onShowcaseViewShow(ShowcaseView showcaseView) { }
            @Override
            public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) { }
        });
        drinksShowcase.setShowcaseEventListener(new OnShowcaseEventListener() {
            @Override
            public void onShowcaseViewHide(ShowcaseView showcaseView) {
                cheatsShowcase.build();
            }
            @Override
            public void onShowcaseViewDidHide(ShowcaseView showcaseView) { }
            @Override
            public void onShowcaseViewShow(ShowcaseView showcaseView) { }
            @Override
            public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) { }
        });
        cheatsShowcase.setShowcaseEventListener(new OnShowcaseEventListener() {
            @Override
            public void onShowcaseViewHide(ShowcaseView showcaseView) {
                daysShowcase.build();
            }
            @Override
            public void onShowcaseViewDidHide(ShowcaseView showcaseView) { }
            @Override
            public void onShowcaseViewShow(ShowcaseView showcaseView) { }
            @Override
            public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) { }
        });

        //building
        foodShowcase.build();

//        foodShowcase.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        goalsShowcase.build();
//                    }
//                });

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
            case R.id.cheats_container:
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

            //getting vaules to be sent to the refresher to handle
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
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            parent.refresh2(counts, plans, totalCheats, dailyCheats);
        }
    }

    void refresh(DietController.DataType dataType, List<Integer> daysAgoUpdated) {
        //if null, all should be updated. If today or a day in the past week is changed, update data
        if(daysAgoUpdated == null || daysAgoUpdated.contains(0) || daysAgoUpdated.contains(1)
                || daysAgoUpdated.contains(2) || daysAgoUpdated.contains(3) || daysAgoUpdated.contains(4)
                || daysAgoUpdated.contains(5) ||daysAgoUpdated.contains(6)) {
            new DisplayRefresher(this).execute();
        }
        mealView.refresh(dataType, daysAgoUpdated);
    }

    private void refresh2(double[] counts, double[] plans, double totalCheats,  double dailyCheats) {
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
//        TextView cheatsTodayTV = dailyDisplayView.findViewById(R.id.cheats_today);

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
            if(servesLeft <= 0) {
                if(countTV != null) {
                    countTV.setText(String.format("%s/%s", df.format(count), df.format(plan)));
                } if(leftTV != null) {
                    leftTV.setText(R.string.completed_text);
                }
                //if new enough version to set text appearance, then update text colour
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (countTV != null) {
                        countTV.setTextColor(getResources().getColor(R.color.completedGood, null));
                    }
                    if (leftTV != null) {
                        leftTV.setTextColor(getResources().getColor(R.color.completedGood, null));
                    }
                }
            } else {
                //change text colour back to normal
                if(countTV != null) {
                    countTV.setText(String.format("%s/%s", df.format(count), df.format(plan)));
                } if (leftTV != null) {
                    leftTV.setText(String.format("%s left", df.format(servesLeft)));
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (countTV != null) {
                        countTV.setTextAppearance(R.style.TextAppearance_AppCompat_Small);
                    }
                    if (leftTV != null) {
                        leftTV.setTextAppearance(R.style.TextAppearance_AppCompat_Small);
                    }
                }
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
        String cheatRatio = String.format("%s/%s", df.format(totalCheats), df.format(dailyCheats));
        String cheatsToday = String.format("%s today!", df.format(totalCheats));
        cheatTV.setText(cheatRatio);
//        cheatsTodayTV.setText(cheatsToday);
        if(totalCheats > dailyCheats && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cheatTV.setTextColor(getResources().getColor(R.color.completedBad, null));
//            cheatsTodayTV.setTextColor(getResources().getColor(R.color.completedBad, null));
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cheatTV.setTextAppearance(R.style.TextAppearance_AppCompat_Small);
//            cheatsTodayTV.setTextAppearance(R.style.TextAppearance_AppCompat_Small);
        }
        //animating any updates to the cheat progress bar
        cheatsPB.setMax((int) (dailyCheats * SCALE_FACTOR));
        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(cheatsPB, "progress",
                (int) (totalCheats *SCALE_FACTOR));
        objectAnimator.setDuration(500);
        objectAnimator.setInterpolator(new DecelerateInterpolator());
        objectAnimator.start();
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
