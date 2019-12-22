package com.ganterpore.simplediet.View.DialogBoxes;

import android.app.Activity;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ganterpore.simplediet.Model.Meal;
import com.ganterpore.simplediet.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;

public class AddMealDialogBox {
    /**
     * opens a dialogue box recieving information on the meal to be added
     * then adds the meal to the database
     * @param view of the object that called the method
     */
    public static void addMeal(final Activity activity, View view) {
        //inflate the dialog box view and get the text fields
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View addMealLayout = layoutInflater.inflate(R.layout.dialog_box_meal, null);

        //getting all the text boxes from the view
        final EditText vegCountET= addMealLayout.findViewById(R.id.veg_count);
        final EditText proteinCountET= addMealLayout.findViewById(R.id.protein_count);
        final EditText dairyCountET = addMealLayout.findViewById(R.id.dairy_count);
        final EditText grainCountET= addMealLayout.findViewById(R.id.grain_count);
        final EditText fruitCountET = addMealLayout.findViewById(R.id.fruit_count);
        final EditText waterCountET = addMealLayout.findViewById(R.id.water_count);
        final EditText excessCountET = addMealLayout.findViewById(R.id.excess_count);
        final EditText cheatScoreET = addMealLayout.findViewById(R.id.cheat_score);

        //Build the dialog box
        AlertDialog.Builder addMealDialog = new AlertDialog.Builder(activity);
        addMealDialog.setTitle("Add Meal");
        addMealDialog.setView(addMealLayout);
        addMealDialog.setNegativeButton("Cancel", null);
        addMealDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //create a meal object from the dialog box data
                Meal todaysMeal = new Meal(
                        Double.parseDouble(vegCountET.getText().toString()),
                        Double.parseDouble(proteinCountET.getText().toString()),
                        Double.parseDouble(dairyCountET.getText().toString()),
                        Double.parseDouble(grainCountET.getText().toString()),
                        Double.parseDouble(fruitCountET.getText().toString()),
                        Double.parseDouble(waterCountET.getText().toString()),
                        Double.parseDouble(excessCountET.getText().toString()),
                        Double.parseDouble(cheatScoreET.getText().toString()),
                        System.currentTimeMillis(),
                        FirebaseAuth.getInstance().getCurrentUser().getUid()
                );

                todaysMeal.pushToDB()
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(activity, "Added Meal", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(activity, "Meal add fail", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }).show();
    }
}
