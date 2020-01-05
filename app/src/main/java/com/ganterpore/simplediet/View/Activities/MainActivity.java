package com.ganterpore.simplediet.View.Activities;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
    private FirebaseFirestore db;

    private DietController dietController;

    private DailyMeals today;
    private MealHistoryDisplay mealView;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        preferences = getSharedPreferences(SHARED_PREFS_LOC, MODE_PRIVATE);
        NotificationReciever.buildChannels(this);
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
                }
            } else {
                if(dietController == null || !(dietController instanceof BasicDietController)) {
                    dietController = new BasicDietController(this);
                }
            }

            //instantiating a day to track
            today = dietController.getTodaysMeals();

            //Creating the history view
            mealView = new MealHistoryDisplay(this, dietController);
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

        //get the text views from the main activity
        TextView vegTV = findViewById(R.id.veg_count);
        TextView proteinTV = findViewById(R.id.protein_count);
        TextView dairyTV = findViewById(R.id.dairy_count);
        TextView grainTV = findViewById(R.id.grain_count);
        TextView fruitTV = findViewById(R.id.fruit_count);
        TextView waterTV = findViewById(R.id.water_count);
        TextView excessTV = findViewById(R.id.excess_serves_count);
        TextView cheatTV = findViewById(R.id.cheat_count);

        //creating arrays of the text views to update
        TextView[] textViews = {vegTV, proteinTV, dairyTV, grainTV, fruitTV, waterTV};
        double[] counts = {today.getVegCount(), today.getProteinCount(), today.getDairyCount(),
                            today.getGrainCount(), today.getFruitCount(), today.getWaterCount()};
        double[] plans = {todaysDietPlan.getDailyVeges(), todaysDietPlan.getDailyProtein(), todaysDietPlan.getDailyDairy(),
                            todaysDietPlan.getDailyGrain(), todaysDietPlan.getDailyFruit(), todaysDietPlan.getDailyWater()};

        NumberFormat df = new DecimalFormat("##.##");

        //updating text for all the main food groups
        for(int i=0;i<textViews.length;i++) {
            TextView textView = textViews[i];
            double count = counts[i];
            double plan = plans[i];
            double servesLeft = plan - count;

            if(servesLeft <= 0.2) {
                textView.setText(df.format(count) + "/" + df.format(plan) + " - Completed!");
                textView.setTextColor(Color.GREEN);
            } else {
                textView.setText(df.format(count) + "/" + df.format(plan) + " - " + df.format(servesLeft) + " serves to go");
                textView.setTextColor(Color.BLACK);
            }
        }
        //updating text on other texts
        excessTV.setText(df.format(today.getExcessServes()) + "");
        cheatTV.setText(df.format(today.getWeeklyCheats()) + "/" + df.format(todaysDietPlan.getWeeklyCheats()));

        mealView.setDietController(dietController);
        mealView.refreshRecommendations();
    }
}
