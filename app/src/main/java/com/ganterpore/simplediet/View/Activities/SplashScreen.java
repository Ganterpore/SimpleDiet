package com.ganterpore.simplediet.View.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ganterpore.simplediet.Controller.BasicDietController;
import com.ganterpore.simplediet.Controller.DietController;
import com.ganterpore.simplediet.Controller.NotificationReciever;
import com.ganterpore.simplediet.Controller.OverUnderEatingDietController;
import com.ganterpore.simplediet.Model.Meal;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.ganterpore.simplediet.View.Activities.MainActivity.SHARED_PREFS_LOC;

public class SplashScreen extends AppCompatActivity implements DietController.DietControllerListener {
    public static final String TAG = "SplashScreen";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initialising services
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);
        NotificationReciever.buildChannels(this);
        //initialising diet controller
        boolean overUnderEatingFunctionality = getSharedPreferences(SHARED_PREFS_LOC, MODE_PRIVATE)
                                            .getBoolean("over_under_eating", false);
        if(overUnderEatingFunctionality) {
            new OverUnderEatingDietController(this);
        } else {
            new BasicDietController(this);
        }
    }


    @Override
    public void dataLoadComplete() {
        //once the data has been loaded in the data controller, start main activity
        startActivity(new Intent(SplashScreen.this, MainActivity.class));
        finish();
    }

    @Override
    public void refresh() { }

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
