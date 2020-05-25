package com.ganterpore.simplediet.View.Activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.ganterpore.simplediet.Controller.BasicDietController;
import com.ganterpore.simplediet.Controller.DietController;
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

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements DietController.DietControllerListener, SnackbarReady {

    public static final String SHARED_PREFS_LOC = "com.ganterpore.simple_diet";
    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;

    private Fragment currentFragment;
    private DailyDisplayActivity dailyFragment;
    private HistoryActivity historyFragment;
    private SettingsActivity settingsFragment;
    private BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(new navigator());
        mAuth = FirebaseAuth.getInstance();
    }

    private class navigator implements BottomNavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            final FragmentManager fm = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment).getChildFragmentManager();
            //finding the currently displayed fragment
            for(Fragment fragment : fm.getFragments()) {
                if(!fragment.isHidden()) {
                    currentFragment = fragment;
                    break;
                }
            }
            Fragment newFragment = null;
            //finding or creating the selected fragment
            switch (menuItem.getItemId()) {
                case R.id.navigation_daily:
                    if(dailyFragment == null) {
                        dailyFragment = new DailyDisplayActivity();
                        fm.beginTransaction().add(R.id.nav_host_fragment, dailyFragment, "navigation_daily").commit();
                    }
                    newFragment = dailyFragment;
                    break;
                case R.id.navigation_history:
                    if(historyFragment == null) {
                        historyFragment = new HistoryActivity();
                        fm.beginTransaction().add(R.id.nav_host_fragment, historyFragment, "navigation_history").commit();
                    }
                    newFragment = historyFragment;
                    break;
                case R.id.navigation_options:
                    if(settingsFragment == null) {
                        settingsFragment = new SettingsActivity();
                        fm.beginTransaction().add(R.id.nav_host_fragment, settingsFragment, "navigation_options").commit();
                    }
                    newFragment = settingsFragment;
                    break;
            }
            //if we are not changing the fragment, don't do anything
            if(newFragment==currentFragment) {
                Log.d(TAG, "onNavigationItemSelected: new frag is current frag");
                return true;
            }
            if(newFragment != null) {
                Log.d(TAG, "onNavigationItemSelected: new frag being shown, hiding old");
                //otherwise display the selected fragment
                fm
                        .beginTransaction()
                        .show(newFragment)
                        .hide(currentFragment)
                        .commit();
                currentFragment = newFragment;
                currentFragment.onResume();
                return true;
            }
            return false;
        }

    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: starting");
        super.onStart();
        //setting up daily fragment
        final FragmentManager fm = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment).getChildFragmentManager();
        for(Fragment fragment : fm.getFragments()) {
            if(!fragment.isHidden()) {
                currentFragment = fragment;
                break;
            }
        }
        if(currentFragment instanceof DailyDisplayActivity && dailyFragment == null) {
            dailyFragment = (DailyDisplayActivity) currentFragment;
        }
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
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //if the dietController has been removed from memory and not replaced yet (say on a warm open)
        DietController dietController = BasicDietController.getInstance();
        if(dietController == null) {
            //Then get the appropriate dietcontroller back again
            boolean overUnderEatingFunctionality = getSharedPreferences(SHARED_PREFS_LOC, MODE_PRIVATE)
                    .getBoolean("over_under_eating", false);
            if(overUnderEatingFunctionality) {
                dietController = new OverUnderEatingDietController(this);
            } else {
                dietController = new BasicDietController(this);
            }
        }
    }

    @Override
    public void onBackPressed() {
        //if the current fragment is not the first one, then move to first page
        int firstPageID = navView.getMenu().getItem(0).getItemId();
        if(!(navView.getSelectedItemId() == firstPageID)) {
            navView.setSelectedItemId(firstPageID);
        } else {
            //otherwise act normally
            super.onBackPressed();
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
    public void refresh(DietController.DataType dataType, List<Integer> daysAgoUpdated) {
        //refresh child fragment if it can be
        if(dailyFragment != null) {
            dailyFragment.refresh(dataType, daysAgoUpdated);
        } if(historyFragment != null) {
            historyFragment.refresh(dataType, daysAgoUpdated);
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

    @Override
    public void dataLoadComplete() {    }

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
