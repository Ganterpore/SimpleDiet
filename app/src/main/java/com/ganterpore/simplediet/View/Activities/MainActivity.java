package com.ganterpore.simplediet.View.Activities;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ganterpore.simplediet.Controller.BasicDietController;
import com.ganterpore.simplediet.Controller.DailyMeals;
import com.ganterpore.simplediet.Controller.DietController;
import com.ganterpore.simplediet.Controller.NotificationReciever;
import com.ganterpore.simplediet.Controller.OverUnderEatingDietController;
import com.ganterpore.simplediet.Model.DietPlan;
import com.ganterpore.simplediet.R;
import com.ganterpore.simplediet.View.DialogBoxes.AddMealDialogBox;
import com.ganterpore.simplediet.View.DialogBoxes.RecipeListDialogBox;
import com.ganterpore.simplediet.View.DialogBoxes.UpdateDietDialogBox;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class MainActivity extends AppCompatActivity implements DietController.DietControllerListener {
    private static final String TAG = "MainActivity";
    public static final String SHARED_PREFS_LOC = "com.ganterpore.simple_diet";
    private FirebaseAuth mAuth;

    private DietController dietController;

    private MealHistoryDisplay mealView;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);


        preferences = getSharedPreferences(SHARED_PREFS_LOC, MODE_PRIVATE);
        NotificationReciever.buildChannels(this);

        final ConstraintLayout progressCircle = findViewById(R.id.progress_sphere);
        AppBarLayout appBarLayout = findViewById(R.id.appBar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.BaseOnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int scrollRange = appBarLayout.getTotalScrollRange();
                float offsetFactor = (float) (-verticalOffset) / (float) scrollRange;
                float scaleFactor = 1F - offsetFactor * .5F ;
                progressCircle.setScaleX(scaleFactor);
                progressCircle.setScaleY(scaleFactor);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Log.d(TAG, "onStart: checking user");
        if(currentUser==null) {
            //if no user, then create an anonymous account
            new AlertDialog.Builder(this)
                    .setTitle("No account detected")
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
        Log.d(TAG, "signUpAnonymous: signing up");
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "onComplete: signed up");
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
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
        final String[] choices = {"add Meal", "open Recipe Book"};
        final Activity activity = this;
        new AlertDialog.Builder(this)
                .setTitle("new Meal or recipe?")
                .setItems(choices, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (choices[which]) {
                            case "add Meal":
                                AddMealDialogBox.addMeal(activity, view);
                                break;
                            case "open Recipe Book":
                                RecipeListDialogBox.openRecipeBook(activity);
                                break;
                    }
                }
        }).show();
    }
    /**
     * updates the values of all the views on the screen to up to date values
     */
    public void refresh() {
        DietPlan todaysDietPlan = dietController.getTodaysDietPlan();
        DailyMeals today = dietController.getTodaysMeals();

        //get the text views from the main activity
        TextView vegTV = findViewById(R.id.veg_count);
        TextView proteinTV = findViewById(R.id.protein_count);
        TextView dairyTV = findViewById(R.id.dairy_count);
        TextView grainTV = findViewById(R.id.grain_count);
        TextView fruitTV = findViewById(R.id.fruit_count);
        TextView waterTV = findViewById(R.id.water_count);
        TextView cheatTV = findViewById(R.id.cheat_count);

        TextView vegeLeftTV = findViewById(R.id.veg_left);
        TextView proteinLeftTV = findViewById(R.id.protein_left);
        TextView dairyLeftTV = findViewById(R.id.dairy_left);
        TextView grainLeftTV = findViewById(R.id.grain_left);
        TextView fruitLeftTV = findViewById(R.id.fruit_left);
        TextView waterLeftTV = findViewById(R.id.water_left);

        //get the progress bars from the main activity
        ProgressBar vegPB = findViewById(R.id.progress_vege);
        ProgressBar meatPB = findViewById(R.id.progress_meat);
        ProgressBar dairyPB = findViewById(R.id.progress_dairy);
        ProgressBar grainPB = findViewById(R.id.progress_grain);
        ProgressBar fruitPB = findViewById(R.id.progress_fruit);
        ProgressBar waterPB = findViewById(R.id.progress_water);

        //creating arrays of the text views to update
        TextView[] textViewsCount = {vegTV, proteinTV, dairyTV, grainTV, fruitTV, waterTV};
        TextView[] textViewsLeft = {vegeLeftTV, proteinLeftTV, dairyLeftTV, grainLeftTV, fruitLeftTV, waterLeftTV};
        ProgressBar[] progressBars = {vegPB, meatPB, dairyPB, grainPB, fruitPB, waterPB};
        double[] counts = {today.getVegCount(), today.getProteinCount(), today.getDairyCount(),
                            today.getGrainCount(), today.getFruitCount(), today.getWaterCount()};
        double[] plans = {todaysDietPlan.getDailyVeges(), todaysDietPlan.getDailyProtein(), todaysDietPlan.getDailyDairy(),
                            todaysDietPlan.getDailyGrain(), todaysDietPlan.getDailyFruit(), todaysDietPlan.getDailyWater()};

        NumberFormat df = new DecimalFormat("##.##");

        //updating text for all the main food groups
        for(int i=0;i<textViewsCount.length;i++) {
            final int SCALE_FACTOR = 100;
            TextView countTV = textViewsCount[i];
            TextView leftTV = textViewsLeft[i];
            ProgressBar progressBar = progressBars[i];
            double count = counts[i];
            double plan = plans[i];
            double servesLeft = plan - count;

            if(servesLeft <= 0.2) {
                countTV.setText(df.format(count) + "/" + df.format(plan));
                leftTV.setText("Done!");
                countTV.setTextColor(Color.GREEN);
                leftTV.setTextColor(Color.GREEN);
            } else {
                countTV.setText(df.format(count) + "/" + df.format(plan));
                leftTV.setText(df.format(servesLeft) + " left");
                countTV.setTextColor(Color.BLACK);
                leftTV.setTextColor(Color.BLACK);
            }
            if(progressBar != null) {
                progressBar.setMax((int) (plan*SCALE_FACTOR));
                ObjectAnimator objectAnimator = ObjectAnimator.ofInt(progressBar, "progress", (int) count*SCALE_FACTOR);
                objectAnimator.setDuration(500);
                objectAnimator.setInterpolator(new DecelerateInterpolator());
                objectAnimator.start();
            }
        }
        //updating text on other texts
        cheatTV.setText(df.format(today.getWeeklyCheats()) + "/" + df.format(todaysDietPlan.getWeeklyCheats()));

        mealView.refreshRecommendations();
    }

}
