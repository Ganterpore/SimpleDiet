package com.ganterpore.simplediet.View.DialogBoxes;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

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
import static com.ganterpore.simplediet.View.Activities.MainActivity.SHARED_PREFS_LOC;

public class AddMealDialogBox implements AddServeDialogBox.ServeListener {
    public static final String TAG = "AddMealDialogBox";
    public static final int MEAL = 1;
    public static final int NEW_RECIPE = 2;
    public static final int RECIPE = 3;

    private final TextView vegCountTV;
    private final TextView proteinCountTV;
    private final TextView dairyCountTV;
    private final TextView grainCountTV;
    private final TextView fruitCountTV;
    private final TextView excessCountTV;

    private final TextView exampleFood;
    private final EditText mealNameTV;
    private RadioGroup cheatSelector;
    private Activity activity;

    /**
     * opens a dialogue box recieving information on the meal to be added
     * then adds the meal to the database
     */
    public static void addMeal(final Activity activity) {
        AddMealDialogBox addMealListener = new AddMealDialogBox(activity);
    }
    public static void addMeal(final Activity activity, Intent intent) {
        AddMealDialogBox addMealListener = new AddMealDialogBox(activity, intent);
    }

    public AddMealDialogBox(final Activity activity) {
        this(activity, new Intent());
    }

    public AddMealDialogBox(final Activity activity, final Intent intent) {
        final int type = intent.getIntExtra("type", MEAL);
        this.activity = activity;
        final SnackbarReady snackbarView = (SnackbarReady) activity;
        //inflate the dialog box view and get the text fields
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View addMealLayout = layoutInflater.inflate(R.layout.dialog_box_meal, null);

        //setting up the buttons for the view
        ImageView vegButton = addMealLayout.findViewById(R.id.veg_button);
        ImageView proteinButton = addMealLayout.findViewById(R.id.protein_button);
        ImageView dairyButton = addMealLayout.findViewById(R.id.dairy_button);
        ImageView grainButton = addMealLayout.findViewById(R.id.grain_button);
        ImageView fruitButton = addMealLayout.findViewById(R.id.fruit_button);
        ImageView excessButton = addMealLayout.findViewById(R.id.excess_button);
        AddServesOnClick onClick = new AddServesOnClick(activity, this);
        vegButton.setOnClickListener(onClick);
        proteinButton.setOnClickListener(onClick);
        dairyButton.setOnClickListener(onClick);
        grainButton.setOnClickListener(onClick);
        fruitButton.setOnClickListener(onClick);
        excessButton.setOnClickListener(onClick);

        //updating the view if in vegan or vegetarian mode
        SharedPreferences preferences = activity.getSharedPreferences(SHARED_PREFS_LOC, MODE_PRIVATE);
        String mode = preferences.getString("mode", "normal");
        if (mode != null && (mode.equals("vegan") || mode.equals("vegetarian"))) {
            proteinButton.setImageResource(R.drawable.vegan_meat_full);
        }

        //getting all the text views from the view
        mealNameTV = addMealLayout.findViewById(R.id.meal_name);
        vegCountTV = addMealLayout.findViewById(R.id.veg_count);
        proteinCountTV = addMealLayout.findViewById(R.id.protein_count);
        dairyCountTV = addMealLayout.findViewById(R.id.dairy_count);
        grainCountTV = addMealLayout.findViewById(R.id.grain_count);
        fruitCountTV = addMealLayout.findViewById(R.id.fruit_count);
        excessCountTV = addMealLayout.findViewById(R.id.excess_count);
        cheatSelector = addMealLayout.findViewById(R.id.cheat_selector);
        exampleFood = addMealLayout.findViewById(R.id.example_food);
        final RadioGroup daySelector = addMealLayout.findViewById(R.id.day_selector);

        //setting the default meal name up based on the time of day
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        if(timeOfDay >= 4 && timeOfDay < 11){
            mealNameTV.setText("Breakfast Meal");
        }else if(timeOfDay >= 11 && timeOfDay < 16){
            mealNameTV.setText("Lunch");
        }else if(timeOfDay >= 16 && timeOfDay < 22){
            mealNameTV.setText("Dinner");
        }else if(timeOfDay >= 22 || timeOfDay < 4){
            mealNameTV.setText("Midnight Meal");
        }

        //if the type is a recipe, then update the values to the recipes values
        if(type==RECIPE) {
            updateValuesToMatchRecipe(intent);
        }

        //setting up cheat selector to update example food when changed
        updateExampleFood();
        cheatSelector.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                updateExampleFood();
            }
        });

        //can't select the day if it is a recipe
        if(type == NEW_RECIPE) {
            daySelector.setVisibility(View.GONE);
        }

        //Build the dialog box
        AlertDialog.Builder addMealDialog = new AlertDialog.Builder(activity);
        addMealDialog.setView(addMealLayout);
        String updateRecipeText = type==RECIPE ? "Update Recipe" : "Save Recipe";
        final int checkedRadioButtonId = cheatSelector.getCheckedRadioButtonId();
        addMealDialog.setNeutralButton(updateRecipeText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int cheatScore = getCheatScoreFromID(checkedRadioButtonId);
                        //create a recipe object from the dialog box data
                        Recipe recipe = new Recipe(
                                mealNameTV.getText().toString(),
                                Double.parseDouble(vegCountTV.getText().toString()),
                                Double.parseDouble(proteinCountTV.getText().toString()),
                                Double.parseDouble(dairyCountTV.getText().toString()),
                                Double.parseDouble(grainCountTV.getText().toString()),
                                Double.parseDouble(fruitCountTV.getText().toString()),
                                0,
                                Double.parseDouble(excessCountTV.getText().toString()),
                                cheatScore,
                                FirebaseAuth.getInstance().getCurrentUser().getUid()
                        );
                        //if we are updating a recipe, delete the old one
                        if(type==RECIPE) {
                            RecipeBookController.deleteRecipe(intent.getStringExtra("id"));
                        }
                        recipe.pushToDB().addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                //create a new recipe item, and push the undo snackbar to it
                                new RecipeListDialogBox(activity).undoAdd(documentReference);
                            }
                        });

                    }
                });
        addMealDialog.setNegativeButton("Cancel", null);
        //can only add meal from recipe or meal, new recipes cannot be eaten
        if(type==MEAL || type==RECIPE) {
            addMealDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    long day;
                    if (daySelector.getCheckedRadioButtonId() == R.id.todays_meal) {
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
                    //create a meal object from the dialog box data
                    final Meal todaysMeal = new Meal(
                            Double.parseDouble(vegCountTV.getText().toString()),
                            Double.parseDouble(proteinCountTV.getText().toString()),
                            Double.parseDouble(dairyCountTV.getText().toString()),
                            Double.parseDouble(grainCountTV.getText().toString()),
                            Double.parseDouble(fruitCountTV.getText().toString()),
                            0,
                            Double.parseDouble(excessCountTV.getText().toString()),
                            cheatScore,
                            day,
                            FirebaseAuth.getInstance().getCurrentUser().getUid()
                    );
                    todaysMeal.setName(mealNameTV.getText().toString());

                    todaysMeal.pushToDB()
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(final DocumentReference documentReference) {
                                    snackbarView.undoAdd(documentReference);
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
        }
        addMealDialog.show();
    }

    /**
     * Converts the cheat_id from the cheat selector to a cheat score
     * @param cheat_id, a cheat selector radio id
     * @return the cheat score it is equivalent to
     */
    private int getCheatScoreFromID(int cheat_id) {
        int cheatScore;
        switch (cheat_id) {
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
        mealNameTV.setText(intent.getStringExtra("name"));
        vegCountTV.setText(String.valueOf(intent.getDoubleExtra("vegCount", 0)));
        proteinCountTV.setText(String.valueOf(intent.getDoubleExtra("proteinCount", 0)));
        dairyCountTV.setText(String.valueOf(intent.getDoubleExtra("dairyCount", 0)));
        grainCountTV.setText(String.valueOf(intent.getDoubleExtra("grainCount", 0)));
        fruitCountTV.setText(String.valueOf(intent.getDoubleExtra("fruitCount", 0)));
        excessCountTV.setText(String.valueOf(intent.getDoubleExtra("excessServes", 0)));
    }

    /**
     * used to update the field that describes an example food in the mix of selected food groups
     */
    private void updateExampleFood() {
        //finding the appropriate string resource prefix, based on the food groups being used
        String foodExamplePrefix = "";
        foodExamplePrefix += Double.parseDouble(vegCountTV.getText().toString())>0 ? "V" : "";
        foodExamplePrefix += Double.parseDouble(proteinCountTV.getText().toString())>0 ? "M" : "";
        foodExamplePrefix += Double.parseDouble(dairyCountTV.getText().toString())>0 ? "D" : "";
        foodExamplePrefix += Double.parseDouble(grainCountTV.getText().toString())>0 ? "G" : "";
        foodExamplePrefix += Double.parseDouble(fruitCountTV.getText().toString())>0 ? "F" : "";

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
        //Getting the relevant string resource based on the information, and setting the example text to ity
        exampleFood.setText(
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
    public void serveAdded(AddServeDialogBox.FoodType type, double serve) {
        //update the serve text
        NumberFormat df = new DecimalFormat("##.##");
        switch (type) {
            case VEGETABLE:
                vegCountTV.setText(df.format(serve));
                break;
            case MEAT:
                proteinCountTV.setText(df.format(serve));
                break;
            case DAIRY:
                dairyCountTV.setText(df.format(serve));
                break;
            case GRAIN:
                grainCountTV.setText(df.format(serve));
                break;
            case FRUIT:
                fruitCountTV.setText(df.format(serve));
                break;
            case EXCESS:
                excessCountTV.setText(df.format(serve));
                break;
        }
        updateExampleFood();
    }

    /**
     * OnClickListener, used to create a dialog to set a number of serves up for the given food type
     */
    static class AddServesOnClick implements View.OnClickListener {
        Activity activity;
        AddServeDialogBox.ServeListener listener;

        AddServesOnClick(Activity activity, AddServeDialogBox.ServeListener listener) {
            this.activity = activity;
            this.listener = listener;
        }

        @Override
        public void onClick(View view) {
            AddServeDialogBox.FoodType type;
            LinearLayout parent = (LinearLayout) view.getParent();
            TextView servesView;
            double serves;
            Intent intent = new Intent();
            //from the id of the button that called it, figure out what food to add serves to, and the current serves
            switch (view.getId()) {
                case R.id.veg_button:
                    type = AddServeDialogBox.FoodType.VEGETABLE;
                    servesView = parent.findViewById(R.id.veg_count);
                    break;
                case R.id.protein_button:
                    type = AddServeDialogBox.FoodType.MEAT;
                    servesView = parent.findViewById(R.id.protein_count);
                    break;
                case R.id.dairy_button:
                    type = AddServeDialogBox.FoodType.DAIRY;
                    servesView = parent.findViewById(R.id.dairy_count);
                    break;
                case R.id.grain_button:
                    type = AddServeDialogBox.FoodType.GRAIN;
                    servesView = parent.findViewById(R.id.grain_count);
                    break;
                case R.id.fruit_button:
                    type = AddServeDialogBox.FoodType.FRUIT;
                    servesView = parent.findViewById(R.id.fruit_count);
                    break;
                case R.id.excess_button:
                    type = AddServeDialogBox.FoodType.EXCESS;
                    servesView = parent.findViewById(R.id.excess_count);
                    break;
                default:
                    type = null;
                    servesView = null;
                    break;
            }
            //if the number of serves is already set (>0), then set that to the default, otherwise use the default default
            serves = Double.parseDouble(servesView != null ? servesView.getText().toString() : "0.0");
            intent.putExtra("foodType", type);
            if(serves > 0) {
                intent.putExtra("nServes", serves);
            }
            AddServeDialogBox.addServe(activity, intent, listener);
        }
    }
}
