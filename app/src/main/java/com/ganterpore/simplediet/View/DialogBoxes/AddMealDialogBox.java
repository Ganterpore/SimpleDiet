package com.ganterpore.simplediet.View.DialogBoxes;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.ganterpore.simplediet.Model.Meal;
import com.ganterpore.simplediet.Model.Recipe;
import com.ganterpore.simplediet.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;

public class AddMealDialogBox implements AddServeDialogBox.ServeListener {
    public static final String TAG = "AddMealDialogBox";
    public static final int MEAL = 1;
    public static final int RECIPE = 2;

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

    public AddMealDialogBox(final Activity activity) {
        this(activity, MEAL);
    }

    public AddMealDialogBox(final Activity activity, int type) {
        this.activity = activity;
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

        //getting all the text views from the view
        mealNameTV = addMealLayout.findViewById(R.id.meal_name);
        vegCountTV = addMealLayout.findViewById(R.id.veg_count);
        proteinCountTV = addMealLayout.findViewById(R.id.protein_count);
        dairyCountTV = addMealLayout.findViewById(R.id.dairy_count);
        grainCountTV = addMealLayout.findViewById(R.id.grain_count);
        fruitCountTV = addMealLayout.findViewById(R.id.fruit_count);
        excessCountTV = addMealLayout.findViewById(R.id.excess_count);

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
        //setting up example food suggester
        cheatSelector = addMealLayout.findViewById(R.id.cheat_selector);
        exampleFood = addMealLayout.findViewById(R.id.example_food);
        updateExampleFood();
        //setting up cheat selector to update example food when changed
        cheatSelector.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                updateExampleFood();
            }
        });

        final RadioGroup daySelector = addMealLayout.findViewById(R.id.day_selector);
        //can't select the day if it is a recipe
        if(type==RECIPE) {
            daySelector.setVisibility(View.GONE);
        }

        //Build the dialog box
        AlertDialog.Builder addMealDialog = new AlertDialog.Builder(activity);
        addMealDialog.setView(addMealLayout);
        addMealDialog.setNeutralButton("Save Recipe", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
                        //create a meal object from the dialog box data
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

                        recipe.pushToDB()
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Toast.makeText(activity, "Added Recipe", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(activity, "Recipe add fail", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        RecipeListDialogBox.openRecipeBook(activity);
                    }
                });
        addMealDialog.setNegativeButton("Cancel", null);
        if(type==MEAL) {
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
                    //create a meal object from the dialog box data
                    Meal todaysMeal = new Meal(
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
            });
        }
        addMealDialog.show();
    }

    /**
     * opens a dialogue box recieving information on the meal to be added
     * then adds the meal to the database
     */
    public static void addMeal(final Activity activity) {
        AddMealDialogBox addMealListener = new AddMealDialogBox(activity);
    }
    public static void addMeal(final Activity activity, int type) {
        AddMealDialogBox addMealListener = new AddMealDialogBox(activity, type);
    }

    /**
     * used to update the field that describes an example food in the mix of selected food groups
     */
    public void updateExampleFood() {
        //finding the appropriate string resource prefix, based on the food groups being used
        String foodExamplePrefix = "";
        foodExamplePrefix += Double.parseDouble(vegCountTV.getText().toString())>0 ? "V" : "";
        foodExamplePrefix += Double.parseDouble(proteinCountTV.getText().toString())>0 ? "M" : "";
        foodExamplePrefix += Double.parseDouble(dairyCountTV.getText().toString())>0 ? "D" : "";
        foodExamplePrefix += Double.parseDouble(grainCountTV.getText().toString())>0 ? "G" : "";
        foodExamplePrefix += Double.parseDouble(fruitCountTV.getText().toString())>0 ? "F" : "";

        //getting the cheat score to find the relevant food example with the given cheat score
        final String finalFoodExamplePrefix = foodExamplePrefix;
        int cheatScore;
        int checkedId = cheatSelector.getCheckedRadioButtonId();
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
            serves = Double.parseDouble(servesView.getText().toString());
            intent.putExtra("foodType", type);
            if(serves > 0) {
                intent.putExtra("nServes", serves);
            }
            AddServeDialogBox.addServe(activity, intent, listener);
        }
    }
}
