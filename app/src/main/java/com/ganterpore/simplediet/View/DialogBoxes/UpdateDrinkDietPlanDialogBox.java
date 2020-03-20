package com.ganterpore.simplediet.View.DialogBoxes;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
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

public class UpdateDrinkDietPlanDialogBox {
    private static NumberFormat df = new DecimalFormat("##.##");
    private EditText dailyHydration;
    private EditText dailyCaffeine;
    private EditText weeklyCaffeine;
    private EditText dailyAlcohol;
    private EditText weeklyAlcohol;

    /**
     * opens a dialogue box recieving information on diet plans
     * then adds the meal to the database
     */
    public static void updateDiet(final Context context) {
        new UpdateDrinkDietPlanDialogBox(context);
    }
    public UpdateDrinkDietPlanDialogBox(final Context context) {
        //inflate the dialog box view and get the text fields
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View updateDietLayout = layoutInflater.inflate(R.layout.dialog_box_drink_diet_plan, null);
        DietPlan dietPlan = BasicDietController.getInstance().getOverallDietPlan();

        //getting the text boxes from the view
        dailyHydration = updateDietLayout.findViewById(R.id.water_count);
        dailyCaffeine = updateDietLayout.findViewById(R.id.daily_caffeine_count);
        weeklyCaffeine = updateDietLayout.findViewById(R.id.weekly_caffeine_count);
        dailyAlcohol = updateDietLayout.findViewById(R.id.daily_alcohol_count);
        weeklyAlcohol = updateDietLayout.findViewById(R.id.weekly_alcohol_count);
        dailyHydration.setText(df.format(dietPlan.getDailyHydration()));
        dailyCaffeine.setText(df.format(dietPlan.getDailyCaffeine()));
        weeklyCaffeine.setText(df.format(dietPlan.getWeeklyCaffeine()));
        dailyAlcohol.setText(df.format(dietPlan.getDailyAlcohol()));
        weeklyAlcohol.setText(df.format(dietPlan.getWeeklyAlcohol()));

        //Build the dialog box
        AlertDialog.Builder addMealDialog = new AlertDialog.Builder(context);
        addMealDialog.setTitle("Update Daily/Weekly Drink Plan");
        addMealDialog.setView(updateDietLayout);
        addMealDialog.setNegativeButton("Cancel", null);
        addMealDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //create a dietController object from the dialog box data
                DietPlan newPlan = BasicDietController.getInstance().getOverallDietPlan();
                newPlan.setDailyHydration(Double.parseDouble(dailyHydration.getText().toString()));
                newPlan.setDailyCaffeine(Double.parseDouble(dailyCaffeine.getText().toString()));
                newPlan.setWeeklyCaffeine(Double.parseDouble(weeklyCaffeine.getText().toString()));
                newPlan.setDailyAlcohol(Double.parseDouble(dailyAlcohol.getText().toString()));
                newPlan.setWeeklyAlcohol(Double.parseDouble(weeklyAlcohol.getText().toString()));
                BasicDietController.getInstance().updateDietPlan(newPlan);
//                newPlan.pushToDB()
//                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void documentReference) {
//                                Toast.makeText(context, "Updated Diet Plan", Toast.LENGTH_SHORT).show();
//                            }
//                        })
//                        .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Toast.makeText(context, "Diet Plan Update Failed", Toast.LENGTH_SHORT).show();
//                            }
//                        });

            }
        }).show();
    }
}
