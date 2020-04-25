package com.ganterpore.simplediet.View.Activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ganterpore.simplediet.Controller.BasicDietController;
import com.ganterpore.simplediet.Controller.DietController;
import com.ganterpore.simplediet.Controller.NotificationReciever;
import com.ganterpore.simplediet.Controller.OverUnderEatingDietController;
import com.ganterpore.simplediet.Model.Meal;
import com.ganterpore.simplediet.R;
import com.ganterpore.simplediet.View.DialogBoxes.UpdateCheatsDialogBox;
import com.ganterpore.simplediet.View.DialogBoxes.UpdateDietDialogBox;
import com.ganterpore.simplediet.View.DialogBoxes.UpdateDrinkDietPlanDialogBox;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity implements DietController.DietControllerListener,
                                                            SnackbarReady {

    public static final String SHARED_PREFS_LOC = "com.ganterpore.simple_diet";
    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private SharedPreferences preferences;
    private DietController dietController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);

        preferences = getSharedPreferences(SHARED_PREFS_LOC, MODE_PRIVATE);
        //initialising services
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);
        NotificationReciever.buildChannels(this);
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
//                    mealView = new MealHistoryDisplay(this, dietController);
                    refresh();
                }
            } else {
                if(dietController == null || (dietController instanceof OverUnderEatingDietController)) {
                    dietController = new BasicDietController(this);
//                    mealView = new MealHistoryDisplay(this, dietController);
                    refresh();
                }
            }
        }
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

    @Override
    public void refresh() {
        //refresh child fragment if it can be
        Fragment navHostFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        Fragment currentFragment = navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
        if(currentFragment instanceof DailyDisplayActivity) {
            ((DailyDisplayActivity) currentFragment).refresh();
        }
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
            Snackbar.make(findViewById(R.id.nav_host_fragment),
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
        Snackbar.make(findViewById(R.id.nav_host_fragment),
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
