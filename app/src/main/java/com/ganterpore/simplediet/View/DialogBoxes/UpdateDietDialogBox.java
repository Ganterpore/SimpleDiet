package com.ganterpore.simplediet.View.DialogBoxes;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ganterpore.simplediet.Controller.DietController;
import com.ganterpore.simplediet.Model.DietPlan;
import com.ganterpore.simplediet.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class UpdateDietDialogBox {
    /**
     * opens a dialogue box recieving information on diet plans
     * then adds the meal to the database
     */
    public static void updateDiet(final Activity activity, final DietController dietController) {
        //inflate the dialog box view and get the text fields
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View updateDietLayout = layoutInflater.inflate(R.layout.dialog_box_diet_plan, null);
        final EditText vegCountET= updateDietLayout.findViewById(R.id.veg_count);

        //getting the text boxes from the view
        final EditText proteinCountET= updateDietLayout.findViewById(R.id.protein_count);
        final EditText dairyCountET = updateDietLayout.findViewById(R.id.dairy_count);
        final EditText grainCountET= updateDietLayout.findViewById(R.id.grain_count);
        final EditText fruitCountET = updateDietLayout.findViewById(R.id.fruit_count);
        final EditText waterCountET = updateDietLayout.findViewById(R.id.water_count);
        final EditText cheatScoreET = updateDietLayout.findViewById(R.id.cheat_score);

        //Build the dialog box
        AlertDialog.Builder addMealDialog = new AlertDialog.Builder(activity);
        addMealDialog.setTitle("Update Diet Plan");
        addMealDialog.setView(updateDietLayout);
        addMealDialog.setNegativeButton("Cancel", null);
        addMealDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //create a dietController object from the dialog box data
                DietPlan plan = new DietPlan(
                        Double.parseDouble(vegCountET.getText().toString()),
                        Double.parseDouble(proteinCountET.getText().toString()),
                        Double.parseDouble(dairyCountET.getText().toString()),
                        Double.parseDouble(grainCountET.getText().toString()),
                        Double.parseDouble(fruitCountET.getText().toString()),
                        Double.parseDouble(waterCountET.getText().toString()),
                        Double.parseDouble(cheatScoreET.getText().toString()),
                        FirebaseAuth.getInstance().getCurrentUser().getUid()
                );
                dietController.updateDietPlan(plan)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(activity, "Updated Diet Plan", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(activity, "Diet Plan Update Failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }).show();
    }
}
