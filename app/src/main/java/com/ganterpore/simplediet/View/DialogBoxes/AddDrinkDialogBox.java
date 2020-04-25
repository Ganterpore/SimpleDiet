package com.ganterpore.simplediet.View.DialogBoxes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import static com.ganterpore.simplediet.Model.Meal.FoodType;
import com.ganterpore.simplediet.Controller.RecipeBookController;
import com.ganterpore.simplediet.Model.Meal;
import com.ganterpore.simplediet.Model.Recipe;
import com.ganterpore.simplediet.R;
import com.ganterpore.simplediet.View.Activities.SnackbarReady;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;
import static com.ganterpore.simplediet.View.Activities.DailyDisplayActivity.SHARED_PREFS_LOC;
import static com.ganterpore.simplediet.View.DialogBoxes.AddServeDialogBox.DRINK_STANDARD_SERVE;

public class AddDrinkDialogBox implements AddServeDialogBox.ServeListener {
    public static final int DRINK = 1;
    public static final int NEW_RECIPE = 2;
    public static final int RECIPE = 3;

    private Activity activity;
    private final TextView milkCountTV;
    private final TextView waterCountTV;
    private final TextView caffeineCountTV;
    private final TextView alcoholCountTV;
    private double milkServes;
    private double waterServes;
    private double caffieneServes;
    private double alcoholServes;
    private final TextView volumeTV;
    private TextView hydrationFactor;
    private TextView exampleDrink;
    private EditText drinkNameET;
    private final RadioGroup cheatSelector;

    private NumberFormat df = new DecimalFormat("##.##");

    public static void addDrink(Activity activity) {
        new AddDrinkDialogBox(activity);
    }
    public static void addDrink(Activity activity, Intent intent) {
        new AddDrinkDialogBox(activity, intent);
    }

    public AddDrinkDialogBox(final Activity activity) {
        this(activity, new Intent());
    }

    public AddDrinkDialogBox(final Activity activity, final Intent intent) {
        this.activity = activity;
        final int type = intent.getIntExtra("type", DRINK);
        //inflate the dialog box view and get the text fields
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View addDrinkLayout = layoutInflater.inflate(R.layout.dialog_box_drink, null);

        //setting up all the buttons
        ImageView milkButton = addDrinkLayout.findViewById(R.id.milk_image);
        ImageView waterButton = addDrinkLayout.findViewById(R.id.water_image);
        final ImageView caffeineButton = addDrinkLayout.findViewById(R.id.caffeine_image);
        ImageView alcoholButton = addDrinkLayout.findViewById(R.id.alcohol_image);
        AddServesOnClick onClick = new AddServesOnClick(activity, this);
        milkButton.setOnClickListener(onClick);
        waterButton.setOnClickListener(onClick);
        caffeineButton.setOnClickListener(onClick);
        alcoholButton.setOnClickListener(onClick);

        //getting all the views
        milkCountTV = addDrinkLayout.findViewById(R.id.milk_count);
        waterCountTV = addDrinkLayout.findViewById(R.id.water_count);
        caffeineCountTV = addDrinkLayout.findViewById(R.id.caffeine_count);
        alcoholCountTV = addDrinkLayout.findViewById(R.id.alcohol_count);
        volumeTV = addDrinkLayout.findViewById(R.id.volume);
        hydrationFactor = addDrinkLayout.findViewById(R.id.hydration_factor);
        exampleDrink = addDrinkLayout.findViewById(R.id.example_food);
        drinkNameET = addDrinkLayout.findViewById(R.id.drink_name);
        cheatSelector = addDrinkLayout.findViewById(R.id.cheat_selector);
        final RadioGroup daySelector = addDrinkLayout.findViewById(R.id.day_selector);

        //If it is a recipe, then update the values of the dialog box to match the recipe
        if(type==RECIPE) {
            updateValuesToMatchRecipe(intent);
        }
        updateExampleDrink();

        final SharedPreferences preferences = activity.getSharedPreferences(SHARED_PREFS_LOC, MODE_PRIVATE);
        if(!preferences.getBoolean("track_caffeine", true)
                && !preferences.getBoolean("track_alcohol", true)) {
            addDrinkLayout.findViewById(R.id.drink_additions).setVisibility(View.GONE);
            addDrinkLayout.findViewById(R.id.additions_text).setVisibility(View.GONE);
            addDrinkLayout.findViewById(R.id.hydration_factor).setVisibility(View.GONE);
            addDrinkLayout.findViewById(R.id.hydration_factor_text).setVisibility(View.GONE);
        } else {
            if (!preferences.getBoolean("track_caffeine", true)) {
                addDrinkLayout.findViewById(R.id.caffeine_container).setVisibility(View.GONE);
                addDrinkLayout.findViewById(R.id.caffeine_count).setVisibility(View.GONE);
                addDrinkLayout.findViewById(R.id.middle_space).setVisibility(View.GONE);
            }
            if (!preferences.getBoolean("track_alcohol", true)) {
                addDrinkLayout.findViewById(R.id.alcohol_container).setVisibility(View.GONE);
                addDrinkLayout.findViewById(R.id.alcohol_count).setVisibility(View.GONE);
                addDrinkLayout.findViewById(R.id.middle_space).setVisibility(View.GONE);
            }
        }
        if(!preferences.getBoolean("track_cheats", true)) {
            addDrinkLayout.findViewById(R.id.cheat_layout).setVisibility(View.GONE);
        }

        //when the cheat selector is changed, update the example text to match
        cheatSelector.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //if there is alcohol, and the cheats are 0, warn the user of this
                if(alcoholServes > 0 && checkedId==R.id.cheat_0 && preferences.getBoolean("track_cheats", true)) {
                    Toast.makeText(activity, "Alcohol is inherently unhealthy. We don't recommend making it a zero cheat score.", Toast.LENGTH_LONG)
                            .show();
                }
                updateExampleDrink();
            }
        });


        //building the alert dialog
        final AlertDialog.Builder addDrinkDialog = new AlertDialog.Builder(activity);
        addDrinkDialog.setView(addDrinkLayout);
        String updateRecipeText = type==RECIPE ? "Update Recipe" : "Save Recipe";
        addDrinkDialog.setNeutralButton(updateRecipeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Get the values from the dialog box, and push them to the database
                int cheatScore = getCheatScoreFromID(cheatSelector.getCheckedRadioButtonId());
                double hydrationFactor = waterServes + milkServes - caffieneServes - alcoholServes;
                //create a recipe object from the dialog box data
                Recipe recipe = Recipe.drinkRecipe(drinkNameET.getText().toString(),
                        waterServes, milkServes, caffieneServes, alcoholServes, hydrationFactor,
                        cheatScore, FirebaseAuth.getInstance().getCurrentUser().getUid());
                //if we are updating a recipe, then delete the old recipe first
                if(type==RECIPE) {
                    RecipeBookController.deleteRecipe(intent.getStringExtra("id"));
                }
                recipe.pushToDB()
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(final DocumentReference documentReference) {
                                //create a new recipe item, and push the undo snackbar to it
                                new RecipeListDialogBox(activity).undoAdd(documentReference);
                            }
                        });
            }
        });
        addDrinkDialog.setNegativeButton("Cancel", null);
        addDrinkDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                long day;
                if (daySelector.getCheckedRadioButtonId() == R.id.todays_drink) {
                    //if the user has selected the meal to be todays meal, set it to the current time
                    day = System.currentTimeMillis();
                } else {
                    //otherwise, if it is for yesterday, get the start of today, and set it to an hour before then
                    Calendar calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int date = calendar.get(Calendar.DATE);
                    calendar.set(year, month, date, 0, 0, 0);
                    day = calendar.getTimeInMillis() - DateUtils.HOUR_IN_MILLIS;
                }
                int cheatScore = getCheatScoreFromID(cheatSelector.getCheckedRadioButtonId());
                double hydrationFactor = waterServes + milkServes - caffieneServes - alcoholServes;
                //create a meal object from the dialog box data
                Meal todaysDrink = Meal.Drink(waterServes, milkServes, caffieneServes, alcoholServes,
                        hydrationFactor, cheatScore, day, FirebaseAuth.getInstance().getCurrentUser().getUid());
                todaysDrink.setName(drinkNameET.getText().toString());

                todaysDrink.pushToDB()
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(final DocumentReference documentReference) {
                                ((SnackbarReady) activity).undoAdd(documentReference);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(activity, "Meal add fail", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        addDrinkDialog.show();
    }

    /**
     * Converts the cheat_id from the cheat selector to a cheat score
     * @param cheat_id, a cheat selector radio id
     * @return the cheat score it is equivalent to
     */
    private int getCheatScoreFromID(int cheat_id) {
        int cheatScore;
        switch (cheat_id) {
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
     * Updates the values of the dialog box so that they match the recipe they are passed from
     * Recipe details should be contained on the intent
     * @param intent, the intent containing all the information
     */
    private void updateValuesToMatchRecipe(Intent intent) {
        //selecting the right cheat selector radio button
        int cheatScore = (int) intent.getDoubleExtra("cheatScore", 0);
        switch (cheatScore) {
            case 3:
                cheatSelector.check(R.id.cheat_3);
                break;
            case 2:
                cheatSelector.check(R.id.cheat_2);
                break;
            case 1:
                cheatSelector.check(R.id.cheat_1);
                break;
            case 0:
            default:
                cheatSelector.check(R.id.cheat_0);
                break;
        }
        //updating the text values
        drinkNameET.setText(intent.getStringExtra("name"));
        milkServes = intent.getDoubleExtra("dairyCount", 0);
        milkCountTV.setText(String.valueOf(milkServes));
        waterServes = intent.getDoubleExtra("waterCount", 0);
        waterCountTV.setText(String.valueOf(waterServes));
        caffieneServes = intent.getDoubleExtra("caffeineCount", 0);
        caffeineCountTV.setText(String.valueOf(caffieneServes));
        alcoholServes = intent.getDoubleExtra("alcoholStandards", 0);
        alcoholCountTV.setText(String.valueOf(alcoholServes));
        volumeTV.setText(String.valueOf(250*(milkServes+waterServes)));
        String hydrationText = df.format(waterServes) + " water + " + df.format(milkServes) + " milk - "
                + df.format(caffieneServes) + " caffeine - " + df.format(alcoholServes) + " alcohol = "
                + df.format(waterServes + milkServes - caffieneServes - alcoholServes);
        hydrationFactor.setText(hydrationText);
    }

    /**
     * used to update the field that describes an example food in the mix of selected food groups
     */
    private void updateExampleDrink() {
        //finding the appropriate string resource prefix, based on the food groups being used
        String foodExamplePrefix = "drink_";
        foodExamplePrefix += waterServes>0 ? "W" : "";
        foodExamplePrefix += milkServes>0 ? "M" : "";
        foodExamplePrefix += caffieneServes>0 ? "C" : "";
        foodExamplePrefix += alcoholServes>0 ? "A" : "";

        //adding the vegan/vegetarian prefix if necessary
        SharedPreferences preferences = activity.getSharedPreferences(SHARED_PREFS_LOC, MODE_PRIVATE);
        String mode = preferences.getString("mode", "normal");
        if(mode.equals("vegan")) {
            foodExamplePrefix = "VN_" + foodExamplePrefix;
        } else if(mode.equals("vegetarian")) {
            foodExamplePrefix = "VG_" + foodExamplePrefix;
        }

        //getting the cheat score to find the relevant food example with the given cheat score
        final String finalFoodExamplePrefix = foodExamplePrefix;
        int cheatScore = getCheatScoreFromID(cheatSelector.getCheckedRadioButtonId());
        //Getting the relevant string resource based on the information, and setting the example text to it
        exampleDrink.setText(
                activity.getResources()
                        .getIdentifier(finalFoodExamplePrefix +"_"+cheatScore, "string", activity.getPackageName())
        );
    }

    /**
     * when a serve has been added this function is called, so that the view updates with the serve that has been added
     * @param type, the type of serve added
     * @param serve, the size of the added serve
     */
    @Override
    public void serveAdded(FoodType type, double serve) {
        //update the serve text
        switch (type) {
            case WATER:
                waterCountTV.setText(String.format("%s/%smL", df.format(serve), df.format(serve * DRINK_STANDARD_SERVE)));
                waterServes = serve;
                break;
            case MILK:
                milkCountTV.setText(String.format("%s/%smL", df.format(serve), df.format(serve * DRINK_STANDARD_SERVE)));
                milkServes = serve;
                break;
            case CAFFEINE:
                caffeineCountTV.setText(df.format(serve));
                caffieneServes = serve;
                break;
            case ALCOHOL:
                alcoholCountTV.setText(String.format("%s/%s%%", df.format(serve), df.format(Meal.getPercentFromStandards(waterServes, milkServes, serve))));
                alcoholServes = serve;
                //if alcoholic, and the cheat score is 0, move to one as alcohol is unhealthy
                if(alcoholServes > 0 && cheatSelector.getCheckedRadioButtonId()==R.id.cheat_0) {
                    cheatSelector.check(R.id.cheat_1);
                }
                break;
        }
        //update other texts related to serves
        double serves = waterServes + milkServes;
        double volume = serves*250;
        volumeTV.setText(String.valueOf((int) volume));
        String hydrationText = df.format(waterServes) + " water + " + df.format(milkServes) + " milk - "
                + df.format(caffieneServes) + " caffeine - " + df.format(alcoholServes) + " alcohol = "
                + df.format(waterServes + milkServes - caffieneServes - alcoholServes);
        hydrationFactor.setText(hydrationText);
        updateExampleDrink();
    }

    /**
     * OnClickListener, used to create a dialog to set a number of serves up for the given food type
     */
    class AddServesOnClick implements View.OnClickListener {
        Activity activity;
        AddDrinkDialogBox dialogBox;

        AddServesOnClick(Activity activity, AddDrinkDialogBox parent) {
            this.activity = activity;
            this.dialogBox = parent;
        }

        @Override
        public void onClick(View view) {
            FoodType type;
            double serves;
            Intent intent = new Intent();
            //from the id of the button that called it, figure out what food to add serves to, and the current serves
            switch (view.getId()) {
                case R.id.water_image:
                    type = FoodType.WATER;
                    serves = dialogBox.waterServes;
                    break;
                case R.id.milk_image:
                    type = FoodType.MILK;
                    serves = dialogBox.milkServes;
                    break;
                case R.id.caffeine_image:
                    type = FoodType.CAFFEINE;
                    serves = dialogBox.caffieneServes;
                    break;
                case R.id.alcohol_image:
                    type = FoodType.ALCOHOL;
                    serves = dialogBox.alcoholServes;
                    //for alcohol, we also need to get the volume of liquid (in serves)
                    double waterServe = dialogBox.waterServes;
                    double dairyServe = dialogBox.milkServes;
                    intent.putExtra("servesLiquid", waterServe+dairyServe);
                    break;
                default:
                    type = null;
                    serves = 0;
                    break;
            }
            //if the number of serves is already set (>0), then set that to the default, otherwise use the default default
            intent.putExtra("foodType", type);
            if(serves > 0) {
                intent.putExtra("nServes", serves);
            }
            AddServeDialogBox.addServe(activity, intent, dialogBox);
        }
    }
}
