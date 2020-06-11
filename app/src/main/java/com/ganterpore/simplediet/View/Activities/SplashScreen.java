package com.ganterpore.simplediet.View.Activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ganterpore.simplediet.Controller.BasicDietController;
import com.ganterpore.simplediet.Controller.DietController;
import com.ganterpore.simplediet.Controller.NotificationReciever;
import com.ganterpore.simplediet.Controller.OverUnderEatingDietController;
import com.ganterpore.simplediet.Model.Meal;
import com.ganterpore.simplediet.View.DialogBoxes.UpdateCheatsDialogBox;
import com.ganterpore.simplediet.View.DialogBoxes.UpdateDietDialogBox;
import com.ganterpore.simplediet.View.DialogBoxes.UpdateDrinkDietPlanDialogBox;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.ganterpore.simplediet.View.Activities.MainActivity.SHARED_PREFS_LOC;

public class SplashScreen extends AppCompatActivity implements DietController.DietControllerListener {
    public static final String TAG = "SplashScreen";
    BasicDietController dietController;
    private FirebaseAuth mAuth;
    private boolean newUser = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initialising services
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);
        NotificationReciever.buildChannels(this);

        // Check if user is signed in
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser==null) {
            newUser = true;
            //if no user, then create an anonymous account
            signUpAnonymous();
        } else {
            buildDietController();
        }
    }

    private void buildDietController() {
        //initialising diet controller
        boolean overUnderEatingFunctionality = getSharedPreferences(SHARED_PREFS_LOC, MODE_PRIVATE)
                                            .getBoolean("over_under_eating", false);
        if(overUnderEatingFunctionality) {
            dietController = new OverUnderEatingDietController(this);
        } else {
            dietController = new BasicDietController(this);
        }
    }


    @Override
    public void dataLoadComplete() {
        dietController.removeListener(this);
        //once the data has been loaded in the data controller, start main activity
        Intent intent = new Intent(SplashScreen.this, MainActivity.class);
        intent.putExtra("new_user", newUser);
        startActivity(intent);
        finish();
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
                            Toast.makeText(SplashScreen.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void signUpAnonymous() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            onStart();
                            buildDietController();
                        } else {
                            // If sign in fails, display a message to the user.
                            task.getException().printStackTrace();
                            Toast.makeText(SplashScreen.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void refresh(DietController.DataType dataType, List<Integer> daysAgoUpdated) { }

    @Override
    public void todaysFoodCompleted() { }

    @Override
    public void todaysHydrationCompleted() { }

    @Override
    public void todaysCheatsOver() {

    }

    @Override
    public void yesterdaysFoodCompleted() {

    }
}
