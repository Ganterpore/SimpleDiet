package com.ganterpore.simplediet;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ganterpore.simplediet.Model.Meal;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Log.d("A", "onStart: checking user");
        if(currentUser==null) {
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



    public void signUpEmail(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
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

    public void addMeal(View view) {
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

        AlertDialog.Builder addMealDialog = new AlertDialog.Builder(this);
        addMealDialog.setTitle("Add Meal");
        addMealDialog.setView(addMealLayout);
        addMealDialog.setNegativeButton("Cancel", null);
        addMealDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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
                db.collection("Meals").add(todaysMeal)
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
}
