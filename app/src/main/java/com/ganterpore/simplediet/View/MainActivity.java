package com.ganterpore.simplediet.View;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ganterpore.simplediet.Controller.DailyMeals;
import com.ganterpore.simplediet.Controller.DietPlanWrapper;
import com.ganterpore.simplediet.Controller.WeeklyMeals;
import com.ganterpore.simplediet.Model.DietPlan;
import com.ganterpore.simplediet.Model.Meal;
import com.ganterpore.simplediet.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class MainActivity extends AppCompatActivity
        implements DailyMeals.DailyMealsInterface, WeeklyMeals.WeeklyMealsInterface, DietPlanWrapper.DietPlanInterface {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private DailyMeals today;
    private WeeklyMeals thisWeek;
    private DietPlanWrapper diet;

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

        //instantiating a day and week to track
        today = new DailyMeals(this);
        thisWeek = new WeeklyMeals(this);
        diet = new DietPlanWrapper(this, mAuth.getCurrentUser().getUid());

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Log.d("A", "onStart: checking user");
        if(currentUser==null) {
            //if no user, then create an anonymous account
            Log.d("A", "onStart: no user");
            new AlertDialog.Builder(this)
                    .setTitle("No account detected")
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.update_plan:
                updateDiet();
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
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * opens a dialogue box recieving information on the meal to be added
     * then adds the meal to the database
     * @param view of the object that called the method
     */
    public void addMeal(View view) {
        //inflate the dialog box view and get the text fields
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View addMealLayout = layoutInflater.inflate(R.layout.dialog_box_meal, null);
        final EditText vegCountET= addMealLayout.findViewById(R.id.veg_count);
        final EditText proteinCountET= addMealLayout.findViewById(R.id.protein_count);
        final EditText dairyCountET = addMealLayout.findViewById(R.id.dairy_count);
        final EditText grainCountET= addMealLayout.findViewById(R.id.grain_count);
        final EditText fruitCountET = addMealLayout.findViewById(R.id.fruit_count);
        final EditText waterCountET = addMealLayout.findViewById(R.id.water_count);
        final EditText excessCountET = addMealLayout.findViewById(R.id.excess_count);
        final EditText cheatScoreET = addMealLayout.findViewById(R.id.cheat_score);

        //Build the dialog box
        AlertDialog.Builder addMealDialog = new AlertDialog.Builder(this);
        addMealDialog.setTitle("Add Meal");
        addMealDialog.setView(addMealLayout);
        addMealDialog.setNegativeButton("Cancel", null);
        addMealDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //create a meal object from the dialog box data
                Meal todaysMeal = new Meal(
                        Integer.parseInt(vegCountET.getText().toString()),
                        Integer.parseInt(proteinCountET.getText().toString()),
                        Integer.parseInt(dairyCountET.getText().toString()),
                        Integer.parseInt(grainCountET.getText().toString()),
                        Integer.parseInt(fruitCountET.getText().toString()),
                        Integer.parseInt(waterCountET.getText().toString()),
                        Integer.parseInt(excessCountET.getText().toString()),
                        Integer.parseInt(cheatScoreET.getText().toString()),
                        System.currentTimeMillis(),
                        mAuth.getCurrentUser().getUid()
                );

                todaysMeal.pushToDB()
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(MainActivity.this, "Added Meal", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, "Meal add fail", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }).show();
    }

    /**
     * opens a dialogue box recieving information on diet plans
     * then adds the meal to the database
     */
    public void updateDiet() {
        //inflate the dialog box view and get the text fields
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View updateDietLayout = layoutInflater.inflate(R.layout.dialog_box_diet_plan, null);
        final EditText vegCountET= updateDietLayout.findViewById(R.id.veg_count);
        final EditText proteinCountET= updateDietLayout.findViewById(R.id.protein_count);
        final EditText dairyCountET = updateDietLayout.findViewById(R.id.dairy_count);
        final EditText grainCountET= updateDietLayout.findViewById(R.id.grain_count);
        final EditText fruitCountET = updateDietLayout.findViewById(R.id.fruit_count);
        final EditText waterCountET = updateDietLayout.findViewById(R.id.water_count);
        final EditText cheatScoreET = updateDietLayout.findViewById(R.id.cheat_score);

        //Build the dialog box
        AlertDialog.Builder addMealDialog = new AlertDialog.Builder(this);
        addMealDialog.setTitle("Update Diet Plan");
        addMealDialog.setView(updateDietLayout);
        addMealDialog.setNegativeButton("Cancel", null);
        addMealDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //create a meal object from the dialog box data
                DietPlan plan = new DietPlan(
                        Integer.parseInt(vegCountET.getText().toString()),
                        Integer.parseInt(proteinCountET.getText().toString()),
                        Integer.parseInt(dairyCountET.getText().toString()),
                        Integer.parseInt(grainCountET.getText().toString()),
                        Integer.parseInt(fruitCountET.getText().toString()),
                        Integer.parseInt(waterCountET.getText().toString()),
                        Integer.parseInt(cheatScoreET.getText().toString()),
                        mAuth.getCurrentUser().getUid()
                );
                diet.updateDietPlan(plan)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MainActivity.this, "Updated Diet Plan", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, "Diet Plan Update Failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }).show();
    }

    @Override
    public void updateDailyMeals(DailyMeals day) {
        updateDisplayValues();
    }


    @Override
    public void updateWeeklyMeals(WeeklyMeals week) {
        updateDisplayValues();
    }

    @Override
    public void updateDietPlan(DietPlan diet) {
        updateDisplayValues();
    }

    private void updateDisplayValues() {
        DietPlan dietPlan = diet.getDietPlan();

        TextView vegTV = findViewById(R.id.veg_count);
        TextView proteinTV = findViewById(R.id.protein_count);
        TextView dairyTV = findViewById(R.id.dairy_count);
        TextView grainTV = findViewById(R.id.grain_count);
        TextView fruitTV = findViewById(R.id.fruit_count);
        TextView waterTV = findViewById(R.id.water_count);
        TextView excessTV = findViewById(R.id.excess_serves_count);
        TextView cheatTV = findViewById(R.id.cheat_count);


        vegTV.setText(today.getVegCount() + "/" + dietPlan.getDailyVeges());
        proteinTV.setText(today.getProteinCount() + "/" + dietPlan.getDailyProtein());
        dairyTV.setText(today.getDairyCount() + "/" + dietPlan.getDailyDairy());
        grainTV.setText(today.getGrainCount() + "/" + dietPlan.getDailyGrain());
        fruitTV.setText(today.getFruitCount() + "/" + dietPlan.getDailyFruit());
        waterTV.setText(today.getWaterCount() + "/" + dietPlan.getDailyWater());
        excessTV.setText(today.getExcessServes() + "");
        cheatTV.setText(thisWeek.getWeeklyCheats() + "/" + dietPlan.getWeeklyCheats());

    }
}
