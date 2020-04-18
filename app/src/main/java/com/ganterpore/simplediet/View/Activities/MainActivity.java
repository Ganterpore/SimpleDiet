package com.ganterpore.simplediet.View.Activities;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ganterpore.simplediet.Model.Meal.FoodType;
import com.ganterpore.simplediet.Model.DietPlan;
import com.ganterpore.simplediet.Controller.BasicDietController;
import com.ganterpore.simplediet.Controller.DailyMeals;
import com.ganterpore.simplediet.Controller.DietController;
import com.ganterpore.simplediet.Controller.NotificationReciever;
import com.ganterpore.simplediet.Controller.OverUnderEatingDietController;
import com.ganterpore.simplediet.Controller.WeeklyIntake;
import com.ganterpore.simplediet.Model.DietPlan;
import com.ganterpore.simplediet.Model.Meal;
import com.ganterpore.simplediet.R;
import com.ganterpore.simplediet.View.Animation.MyBounceInterpolator;
import com.ganterpore.simplediet.View.DialogBoxes.AddDrinkDialogBox;
import com.ganterpore.simplediet.View.DialogBoxes.AddMealDialogBox;
import com.ganterpore.simplediet.View.DialogBoxes.AddServeDialogBox;
import com.ganterpore.simplediet.View.DialogBoxes.RecipeListDialogBox;
import com.ganterpore.simplediet.View.DialogBoxes.UpdateCheatsDialogBox;
import com.ganterpore.simplediet.View.DialogBoxes.UpdateDietDialogBox;
import com.ganterpore.simplediet.View.DialogBoxes.UpdateDrinkDietPlanDialogBox;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class MainActivity extends AppCompatActivity implements DietController.DietControllerListener,
                                                                SnackbarReady{
    private static final String TAG = "MainActivity";
    public static final String SHARED_PREFS_LOC = "com.ganterpore.simple_diet";
    private FirebaseAuth mAuth;
    private SharedPreferences preferences;

    private DietController dietController;
    private MealHistoryDisplay mealView;

    private boolean isFABOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = getSharedPreferences(SHARED_PREFS_LOC, MODE_PRIVATE);
        //initialising services
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);
        NotificationReciever.buildChannels(this);

        //setting up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        initialiseScrollEffect();

        View weeklyContainer = findViewById(R.id.weekly_intake);
        final Activity activity = this;
        weeklyContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, HistoryActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Makes sure that items in the app bar scale appropriately when the main view is scrolled
     */
    private void initialiseScrollEffect() {
        final ConstraintLayout progressCircle = findViewById(R.id.progress_sphere);
        final ConstraintLayout progressCheats = findViewById(R.id.cheats_progress);
        final ConstraintLayout progressDrinks = findViewById(R.id.drinks_progress);
        AppBarLayout appBarLayout = findViewById(R.id.appBar);
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
    protected void onResume() {
        super.onResume();
        String mode = preferences.getString("mode", "normal");
        ProgressBar meatProgress = findViewById(R.id.progress_meat);
        //if the mode has not been set, then just leave as is
        if(mode==null) {
            return;
        }
        //if in one mode, and we are seeing the wrong image, update it
        if ((mode.equals("vegan") || mode.equals("vegetarian"))) {
            meatProgress.setProgressDrawable(getDrawable(R.drawable.progress_bar_meat_vegan));
            ((ImageView) findViewById(R.id.weekly_protein_image)).setImageResource(R.drawable.vegan_meat_full_thumbnail);
        } else if(mode.equals("normal")) {
            meatProgress.setProgressDrawable(getDrawable(R.drawable.progress_bar_meat));
            ((ImageView) findViewById(R.id.weekly_protein_image)).setImageResource(R.drawable.meat_full_thumbnail);
        }
        if(!preferences.getBoolean("track_cheats", true)) {
            findViewById(R.id.cheat_layout).setVisibility(View.GONE);
            findViewById(R.id.cheats_progress).setVisibility(View.GONE);
            findViewById(R.id.weekly_cheat_container).setVisibility(View.GONE);
        } else {
            findViewById(R.id.cheat_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.cheats_progress).setVisibility(View.VISIBLE);
            findViewById(R.id.weekly_cheat_container).setVisibility(View.VISIBLE);
        }
        if(preferences.getBoolean("track_water", true)) {
            findViewById(R.id.drinks_progress).setVisibility(View.VISIBLE);
            findViewById(R.id.weekly_water_container).setVisibility(View.VISIBLE);
            findViewById(R.id.water_layout).setVisibility(View.VISIBLE);
            if(!preferences.getBoolean("track_alcohol", true)) {
                findViewById(R.id.weekly_alcohol_container).setVisibility(View.GONE);
                findViewById(R.id.alcohol_image).setVisibility(View.GONE);
                findViewById(R.id.alcohol_count).setVisibility(View.GONE);
            } else {
                findViewById(R.id.weekly_alcohol_container).setVisibility(View.VISIBLE);
                findViewById(R.id.alcohol_image).setVisibility(View.VISIBLE);
                findViewById(R.id.alcohol_count).setVisibility(View.VISIBLE);
            }
            if(!preferences.getBoolean("track_caffeine", true)) {
                findViewById(R.id.weekly_caffeine_container).setVisibility(View.GONE);
                findViewById(R.id.caffeine_image).setVisibility(View.GONE);
                findViewById(R.id.caffeine_count).setVisibility(View.GONE);
            } else {
                findViewById(R.id.weekly_caffeine_container).setVisibility(View.VISIBLE);
                findViewById(R.id.caffeine_image).setVisibility(View.VISIBLE);
                findViewById(R.id.caffeine_count).setVisibility(View.VISIBLE);
            }
        } else {
            findViewById(R.id.drinks_progress).setVisibility(View.GONE);
            findViewById(R.id.weekly_water_container).setVisibility(View.GONE);
            findViewById(R.id.water_layout).setVisibility(View.GONE);

            findViewById(R.id.weekly_alcohol_container).setVisibility(View.GONE);
            findViewById(R.id.alcohol_image).setVisibility(View.GONE);
            findViewById(R.id.alcohol_count).setVisibility(View.GONE);
            findViewById(R.id.weekly_caffeine_container).setVisibility(View.GONE);
            findViewById(R.id.caffeine_image).setVisibility(View.GONE);
            findViewById(R.id.caffeine_count).setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        //TODO improve new user sign in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser==null) {
            //if no user, then create an anonymous account
            new AlertDialog.Builder(this)
                    .setTitle(  "No account detected")
                    .setMessage("Create new anonymous account?")
                    .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            signUpAnonymous();
                        }
                    }).show();
        } else {
            //if the dietController is not instantiated or is a member of the wrong class, update it.
            boolean overUnderEatingFunctionality = preferences.getBoolean("over_under_eating", false);
            if (overUnderEatingFunctionality) {
                if(dietController == null || !(dietController instanceof OverUnderEatingDietController)) {
                    dietController = new OverUnderEatingDietController(this);
                    mealView = new MealHistoryDisplay(this, dietController);
                    refresh();
                }
            } else {
                if(dietController == null || (dietController instanceof OverUnderEatingDietController)) {
                    dietController = new BasicDietController(this);
                    mealView = new MealHistoryDisplay(this, dietController);
                    refresh();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
        }
        return false;
    }

    public void signUpEmail(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void signUpAnonymous() {
        final Activity activity = this;
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "onComplete: signed up");
                        if (task.isSuccessful()) {
                            onStart();
                            // Sign in success, update UI with the signed-in user's information
                            UpdateCheatsDialogBox.updateDiet(activity);
                            UpdateDrinkDietPlanDialogBox.updateDiet(activity);
                            UpdateDietDialogBox.updateDiet(activity);
                        } else {
                            // If sign in fails, display a message to the user.
                            task.getException().printStackTrace();
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * called when a user taps the add food button
     * allows the user  to choose what type of food they want to add.
     * @param view, the view that called the function
     */
    public void addFood(final View view) {
        switch (view.getId()) {
            case R.id.addFoodFAB:
                openCloseFoodFAB();
                break;
            case R.id.addMealFAB:
                AddMealDialogBox.addMeal(this);
                openCloseFoodFAB();
                break;
            case R.id.addDrinkFAB:
                AddDrinkDialogBox.addDrink(this);
                openCloseFoodFAB();
                break;
            case R.id.recipeBookFAB:
                RecipeListDialogBox.openRecipeBook(this);
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
        View background = findViewById(R.id.FABBackground);
        FloatingActionButton addFoodFAB = findViewById(R.id.addFoodFAB);
        FloatingActionButton mealFAB = findViewById(R.id.addMealFAB);
        FloatingActionButton drinkFAB = findViewById(R.id.addDrinkFAB);
        FloatingActionButton recipeBookFAB = findViewById(R.id.recipeBookFAB);
        TextView mealTV = findViewById(R.id.addMealTV);
        TextView addDrinkTV = findViewById(R.id.addDrinkTV);
        TextView recipeBookTV = findViewById(R.id.recipeBookTV);

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
    public void addSnack(final View view) {
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
        AddServeDialogBox.addServe(this, intent, null);
    }

    /**
     * updates the values of all the views on the screen to up to date values
     */
    public void refresh() {
        final int SCALE_FACTOR = 100; //how much to scale the progress bars by (to allow more granularity)
        NumberFormat df = new DecimalFormat("##.##"); //format to show all decimal strings
        DietPlan todaysDietPlan = dietController.getTodaysDietPlan();
        DailyMeals today = dietController.getTodaysMeals();
        WeeklyIntake thisWeek = dietController.getThisWeeksIntake();
        View weeklyContainer = findViewById(R.id.weekly_intake);

        //get the text views from the main activity
        TextView vegTV = findViewById(R.id.veg_count);
        TextView proteinTV = findViewById(R.id.protein_count);
        TextView dairyTV = findViewById(R.id.dairy_count);
        TextView grainTV = findViewById(R.id.grain_count);
        TextView fruitTV = findViewById(R.id.fruit_count);
        TextView waterTV = findViewById(R.id.water_count);
        TextView caffeineTV = findViewById(R.id.caffeine_count);
        TextView alcoholTV = findViewById(R.id.alcohol_count);
        TextView cheatTV = findViewById(R.id.cheat_count);

        TextView weeklyVegTV = findViewById(R.id.weekly_veges_intake);
        TextView weeklyProteinTV = findViewById(R.id.weekly_protein_intake);
        TextView weeklyDairyTV = findViewById(R.id.weekly_dairy_intake);
        TextView weeklyGrainTV = findViewById(R.id.weekly_grain_intake);
        TextView weeklyFruitTV = findViewById(R.id.weekly_fruit_intake);
        TextView weeklyWaterTV = findViewById(R.id.weekly_water_intake);
        TextView weeklyCaffeineTV = findViewById(R.id.weekly_caffeine_count);
        TextView weeklyAlcoholTV = findViewById(R.id.weekly_alcohol_count);
        TextView weeklyCheatTV = findViewById(R.id.weekly_cheat_count);

        TextView vegeLeftTV = findViewById(R.id.veg_left);
        TextView proteinLeftTV = findViewById(R.id.protein_left);
        TextView dairyLeftTV = findViewById(R.id.dairy_left);
        TextView grainLeftTV = findViewById(R.id.grain_left);
        TextView fruitLeftTV = findViewById(R.id.fruit_left);
        TextView waterLeftTV = findViewById(R.id.water_left);
        TextView cheatsTodayTV = findViewById(R.id.cheats_today);

        //get the progress bars from the main activity
        ProgressBar vegPB = findViewById(R.id.progress_vege);
        ProgressBar meatPB = findViewById(R.id.progress_meat);
        ProgressBar dairyPB = findViewById(R.id.progress_dairy);
        ProgressBar grainPB = findViewById(R.id.progress_grain);
        ProgressBar fruitPB = findViewById(R.id.progress_fruit);
        ProgressBar waterPB = findViewById(R.id.progress_water);
        ProgressBar cheatsPB = findViewById(R.id.toolbar_layout).findViewById(R.id.progress_cheats);
        ProgressBar weeklyCheatsPB = weeklyContainer.findViewById(R.id.progress_cheats);

        //creating arrays of the text views to update
        TextView[] textViewsCount = {vegTV, proteinTV, dairyTV, grainTV, fruitTV, waterTV, caffeineTV, alcoholTV, cheatTV,
                weeklyVegTV, weeklyProteinTV, weeklyDairyTV, weeklyGrainTV, weeklyFruitTV, weeklyWaterTV,
                weeklyCaffeineTV, weeklyAlcoholTV, weeklyCheatTV};
        TextView[] textViewsLeft = {vegeLeftTV, proteinLeftTV, dairyLeftTV, grainLeftTV, fruitLeftTV, waterLeftTV, null, null,  null,
                null, null, null, null, null, null, null, null, null};
        ProgressBar[] progressBars = {vegPB, meatPB, dairyPB, grainPB, fruitPB, waterPB, null, null, null,
                null, null, null, null, null, null, null, null, weeklyCheatsPB};
        double[] counts = {today.getVegCount(), today.getProteinCount(), today.getDairyCount(),
                            today.getGrainCount(), today.getFruitCount(), today.getHydrationScore(),
                            today.getCaffieneCount(), today.getAlcoholCount(), today.getTotalCheats(),
                            thisWeek.getVegCount(), thisWeek.getProteinCount(), thisWeek.getDairyCount(),
                            thisWeek.getGrainCount(), thisWeek.getFruitCount(), thisWeek.getHydrationScore(),
                            thisWeek.getCaffieneCount(), thisWeek.getAlcoholCount(), thisWeek.getTotalCheats()};
        double[] plans = {todaysDietPlan.getDailyVeges(), todaysDietPlan.getDailyProtein(), todaysDietPlan.getDailyDairy(),
                            todaysDietPlan.getDailyGrain(), todaysDietPlan.getDailyFruit(), todaysDietPlan.getDailyHydration(),
                            todaysDietPlan.getDailyCaffeine(), todaysDietPlan.getDailyAlcohol(), todaysDietPlan.getDailyCheats(),
                            thisWeek.getWeeklyLimitVeg(), thisWeek.getWeeklyLimitProtein(), thisWeek.getWeeklyLimitDairy(),
                            thisWeek.getWeeklyLimitGrain(), thisWeek.getWeeklyLimitFruit(), thisWeek.getWeeklyLimitHydration(),
                            thisWeek.getWeeklyLimitCaffiene(), thisWeek.getWeeklyLimitAlcohol(), thisWeek.getWeeklyLimitCheats()};

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
                    countTV.setText(df.format(count) + "/" + df.format(plan));
                } if(leftTV != null) {
                    leftTV.setText("Done!");
                }
                //TODO update colors when food is completed
//                countTV.setTextColor(Color.GREEN);
//                leftTV.setTextColor(Color.GREEN);
//                countTV.setAlpha((float) 1);
//                leftTV.setAlpha((float) 1);
            } else {
                if(countTV != null) {
                    countTV.setText(df.format(count) + "/" + df.format(plan));
                } if (leftTV != null) {
                    leftTV.setText(df.format(servesLeft) + " left");
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
        cheatTV.setText(String.format("%s/%s", df.format(today.getTotalCheats()), df.format(todaysDietPlan.getDailyCheats())));
        cheatsTodayTV.setText(String.format("%s today!", df.format(today.getTotalCheats())));
        //animating any updates to the cheat progress bar
        cheatsPB.setMax((int) (todaysDietPlan.getDailyCheats() * SCALE_FACTOR));
        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(cheatsPB, "progress",
                (int) today.getTotalCheats()*SCALE_FACTOR);
        objectAnimator.setDuration(500);
        objectAnimator.setInterpolator(new DecelerateInterpolator());
        objectAnimator.start();

        //refreshing the other views
        mealView.refreshRecommendations();
    }

    /**
     * Shows an achievement animation popup to the user
     * @param activity, the activity this is being called from
     * @param achievementText, the text to display to the user
     */
    private static void achievementAnimation(final Activity activity, int imageID, String achievementText) {
        final View popup = LayoutInflater.from(activity).inflate(R.layout.popup_food_completion_achievement, null);
        activity.addContentView(popup, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ImageView popupImage = popup.findViewById(R.id.popup_image);
        popupImage.setImageResource(imageID);
        TextView popupText = popup.findViewById(R.id.popup_text);
        if(popupText!=null) {
            popupText.setText(achievementText);
        }

        Animation popupAnimation = AnimationUtils.loadAnimation(activity, R.anim.popup_anim);
        Animation popupAnimationDelayed = AnimationUtils.loadAnimation(activity, R.anim.popup_anim);
        popupAnimationDelayed.setStartOffset(200);
        //make popup appear and dissapear before/after animation
        popupAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                popup.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                popup.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        popup.startAnimation(popupAnimation);
        popupImage.startAnimation(popupAnimationDelayed);
    }

    /** Methods to create achievements when goals are met */

    @Override
    public void todaysFoodCompleted() {
        achievementAnimation(this, R.drawable.symbol_food_completed_thumbnail, getString(R.string.achievement_food_complete_text));
    }
    @Override
    public void yesterdaysFoodCompleted() {
        achievementAnimation(this, R.drawable.symbol_food_completed_thumbnail, getString(R.string.achievement_yesterday_food_complete_text));
    }

    @Override
    public void todaysHydrationCompleted() {
        achievementAnimation(this, R.drawable.symbol_water_completed, getString(R.string.achievement_hydration_complete_text));
    }

    @Override
    public void todaysCheatsOver() {
        achievementAnimation(this, R.drawable.excess_upright, getString(R.string.achievement_over_cheats_text));
    }
    /////////Functions for the SnackbarReady interface

    @Override
    public void undoDelete(Object savedObject) {
        if(savedObject instanceof Meal) {
            final Meal savedMeal = (Meal) savedObject;
            Snackbar.make(findViewById(R.id.coordinator_overlay),
                    "Deleted Meal", Snackbar.LENGTH_LONG)
                    .setAction("Undo", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            savedMeal.pushToDB();
                        }
                    })
                    .show();
        }
    }

    @Override
    public void undoAdd(final DocumentReference documentReference) {
        Snackbar.make(findViewById(R.id.coordinator_overlay),
                "Added Meal", Snackbar.LENGTH_LONG)
                .setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        documentReference.delete();
                    }
                })
                .show();
    }
}
