package com.ganterpore.simplediet.View.DialogBoxes;

import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.ganterpore.simplediet.Controller.BasicDietController;
import com.ganterpore.simplediet.Model.DietPlan;
import com.ganterpore.simplediet.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class UpdateCheatsDialogBox {
    private static NumberFormat df = new DecimalFormat("##.##");

    /**
     * opens a dialogue box recieving information on diet plans
     * then adds the meal to the database
     */
    public static void updateDiet(final Context context) {
        //inflate the dialog box view and get the text fields
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View updateDietLayout = layoutInflater.inflate(R.layout.dialog_box_cheat_plan, null);
        final DietPlan dietPlan = BasicDietController.getInstance().getOverallDietPlan();

        //getting the text boxes from the view
        final EditText dailyCheats = updateDietLayout.findViewById(R.id.daily_cheats);
        final EditText weeklyCheats = updateDietLayout.findViewById(R.id.weekly_cheats);
        final TextView cheatDescription = updateDietLayout.findViewById(R.id.cheat_description);
        //Making sure the description updates when cheats change
        dailyCheats.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                //after the text has changed, update the other fields to match
                if(s.toString().isEmpty()) {
                    dietPlan.setDailyCheats(0);
                    dietPlan.setWeeklyCheats(0);
                } else {
                    dietPlan.setDailyCheats(Double.parseDouble(s.toString()));
                    dietPlan.setWeeklyCheats(7 * dietPlan.getDailyCheats());
                }
                String newCheatDescription = context.getResources().getString(R.string.cheat_description_template);
                newCheatDescription = newCheatDescription.replace(" X ", " "+df.format(dietPlan.totalServes())+" ");
                newCheatDescription = newCheatDescription.replace(" Y ", " "+df.format(dietPlan.getDailyCheats()/(dietPlan.totalServes()))+" ");
                cheatDescription.setText(newCheatDescription);
                weeklyCheats.setText(df.format(dietPlan.getWeeklyCheats()));
            }
        });
        weeklyCheats.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().isEmpty()) {
                    dietPlan.setWeeklyCheats(0);
                } else {
                    dietPlan.setWeeklyCheats(Double.parseDouble(s.toString()));
                }
            }
        });
        dailyCheats.setText(df.format(dietPlan.getDailyCheats()));
        weeklyCheats.setText(df.format(dietPlan.getWeeklyCheats()));

        //Build the dialog box
        AlertDialog.Builder addMealDialog = new AlertDialog.Builder(context);
        addMealDialog.setTitle("Update Daily Cheat Plan");
        addMealDialog.setView(updateDietLayout);
        addMealDialog.setNegativeButton("Cancel", null);
        addMealDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //create a dietController object from the dialog box data
                dietPlan.pushToDB()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void documentReference) {
                                Toast.makeText(context, "Updated Diet Plan", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Diet Plan Update Failed", Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        }).show();
    }
}
