package com.ganterpore.simplediet.View.DialogBoxes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.ganterpore.simplediet.Model.Meal;
import com.ganterpore.simplediet.R;
import com.ganterpore.simplediet.View.Activities.SnackbarReady;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;

import static android.content.Context.MODE_PRIVATE;
import static com.ganterpore.simplediet.View.Activities.MainActivity.SHARED_PREFS_LOC;

public class AddCheatsDialogBox {
    public static final String TAG = "AddCheatsDialogBox";
    private static SharedPreferences preferences;

    public static void addCheats(final Activity activity, final Meal meal, boolean isDrink) {
        //TODO remove if cheats turned off
        //getting preferences
        preferences = activity.getSharedPreferences(SHARED_PREFS_LOC, MODE_PRIVATE);
        String mode = preferences.getString("mode", "normal");
        //inflating the dialog box view and getting views
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View addCheatsLayout = layoutInflater.inflate(R.layout.dialog_box_add_cheats, null);
        final RadioGroup cheatSelector = addCheatsLayout.findViewById(R.id.cheat_selector);
        final TextView exampleFood = addCheatsLayout.findViewById(R.id.example_food);

        //putting the appropriate string into the example text
        final String foodExamplePrefix = getFoodExamplePrefix(meal, isDrink, mode);
        exampleFood.setText(
            activity.getResources()
                .getIdentifier(foodExamplePrefix+"_0", "string", activity.getPackageName())
        );
        //setting the example text to update when the cheats are updated
        cheatSelector.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                exampleFood.setText(activity.getResources()
                            .getIdentifier(foodExamplePrefix +"_" +getCheatScoreFromID(checkedId),
                                        "string", activity.getPackageName())
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
                meal.setCheatScore(getCheatScoreFromID(cheatSelector.getCheckedRadioButtonId()));
                meal.pushToDB()
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(final DocumentReference documentReference) {
                                ((SnackbarReady) activity).undoAdd(documentReference);
                            }
                        });
            }
        });
        addCheatsDialog.show();
    }

    /**
     * Converts an ID into a cheat score
     * @param checkedId, the ID of the radioselector choixe
     * @return the cheat score related to that ID
     */
    private static int getCheatScoreFromID(int checkedId) {
        int cheatScore;
        switch (checkedId) {
            case R.id.cheat_3:
                cheatScore = 3;
                break;
            case R.id.cheat_2:
                cheatScore = 2;
                break;
            case R.id.cheat_1:
                cheatScore = 1;
                break;
            case R.id.cheat_0:
            default:
                cheatScore = 0;
        }
        return cheatScore;
    }

    /**
     * Converts a set of parameters into a prefix for getting the correct food example string resource
     * @param meal, the meal the string will be for
     * @param isDrink, whether it is a drink or not
     * @param mode, the mode the user is in from settings
     * @return the prefix for getting string resources
     */
    private static String getFoodExamplePrefix(Meal meal, boolean isDrink, String mode) {
        String foodExamplePrefix = "";
        if(isDrink) {
            foodExamplePrefix = "drink_";
            foodExamplePrefix += meal.getWaterCount() > 0 ? "W" : "";
            foodExamplePrefix += meal.getDairyCount() > 0 ? "M" : "";
            foodExamplePrefix += meal.getCaffeineCount() > 0 ? "C" : "";
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
        return foodExamplePrefix;
    }
}
