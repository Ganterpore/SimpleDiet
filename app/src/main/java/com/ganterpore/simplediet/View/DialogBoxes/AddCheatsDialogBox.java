package com.ganterpore.simplediet.View.DialogBoxes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.ganterpore.simplediet.Model.Meal;
import com.ganterpore.simplediet.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;

import static android.content.Context.MODE_PRIVATE;
import static com.ganterpore.simplediet.View.Activities.MainActivity.SHARED_PREFS_LOC;

public class AddCheatsDialogBox {
    public static final String TAG = "AddCheatsDialogBox";
    private static SharedPreferences preferences;

    public static void addCheats(final Activity activity, final Meal meal, boolean isDrink) {
        preferences = activity.getSharedPreferences(SHARED_PREFS_LOC, MODE_PRIVATE);
        String mode = preferences.getString("mode", "normal");
        //inflate the dialog box view and get the text fields
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View addCheatsLayout = layoutInflater.inflate(R.layout.dialog_box_add_cheats, null);

        final RadioGroup cheatSelector = addCheatsLayout.findViewById(R.id.cheat_selector);
        final TextView exampleFood = addCheatsLayout.findViewById(R.id.example_food);

        //finding the appropriate string resource prefix
        String foodExamplePrefix = "";
        if(isDrink) {
            foodExamplePrefix = "drink_";
            foodExamplePrefix += meal.getWaterCount() > 0 ? "W" : "";
            foodExamplePrefix += meal.getDairyCount() > 0 ? "M" : "";
            foodExamplePrefix += meal.getCaffieneCount() > 0 ? "C" : "";
            foodExamplePrefix += meal.getAlcoholStandards() > 0 ? "A" : "";
        } else {
            foodExamplePrefix += meal.getVegCount() > 0 ? "V" : "";
            foodExamplePrefix += meal.getProteinCount() > 0 ? "M" : "";
            foodExamplePrefix += meal.getDairyCount() > 0 ? "D" : "";
            foodExamplePrefix += meal.getGrainCount() > 0 ? "G" : "";
            foodExamplePrefix += meal.getFruitCount() > 0 ? "F" : "";
        }
        if(mode.equals("vegan")) {
            foodExamplePrefix = "VN_" + foodExamplePrefix;
        } else if(mode.equals("vegetarian")) {
            foodExamplePrefix = "VG_" + foodExamplePrefix;
        }

        //putting the appropriate string into the example text
        exampleFood.setText(
            activity.getResources()
                .getIdentifier(foodExamplePrefix+"_0", "string", activity.getPackageName())
        );
        //setting the example text to update as required
        final String finalFoodExamplePrefix = foodExamplePrefix;
        cheatSelector.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int cheatScore;
                switch (checkedId) {
                    case R.id.cheat_0:
                        cheatScore = 0;
                        break;
                    case R.id.cheat_1:
                        cheatScore = 1;
                        break;
                    case R.id.cheat_2:
                        cheatScore = 2;
                        break;
                    case R.id.cheat_3:
                        cheatScore = 3;
                        break;
                    default:
                        cheatScore = 0;
                }
                exampleFood.setText(
                        activity.getResources()
                                .getIdentifier(finalFoodExamplePrefix +"_"+cheatScore, "string", activity.getPackageName())
                );
            }
        });

        AlertDialog.Builder addCheatsDialog = new AlertDialog.Builder(activity);
        addCheatsDialog.setView(addCheatsLayout);
        addCheatsDialog.setNegativeButton("Cancel", null);
        addCheatsDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //adding meal to database when selected
                int cheatScore;
                switch (cheatSelector.getCheckedRadioButtonId()) {
                    case R.id.cheat_0:
                        cheatScore = 0;
                        break;
                    case R.id.cheat_1:
                        cheatScore = 1;
                        break;
                    case R.id.cheat_2:
                        cheatScore = 2;
                        break;
                    case R.id.cheat_3:
                        cheatScore = 3;
                        break;
                    default:
                        cheatScore = 0;
                }
                meal.setCheatScore(cheatScore);
                meal.pushToDB()
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(activity, "Added Snack", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(activity, "Snack add fail", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        addCheatsDialog.show();
    }
}
