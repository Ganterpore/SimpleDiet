package com.ganterpore.simplediet.View.DialogBoxes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ganterpore.simplediet.Model.Meal;
import com.ganterpore.simplediet.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;

import com.ganterpore.simplediet.Model.Meal.FoodType;

import static android.content.Context.MODE_PRIVATE;
import static com.ganterpore.simplediet.View.Activities.MainActivity.SHARED_PREFS_LOC;
import static com.ganterpore.simplediet.Model.Meal.FoodType;


public class AddServeDialogBox  {
    private static SharedPreferences preferences;

    static final double DRINK_STANDARD_SERVE = 250;
    public static final String TAG = "AddServeDialogBox";
    private static NumberFormat df = new DecimalFormat("##.##");

    public static void addServe(final Activity activity, final Intent intent, final ServeListener listener) {
        preferences = activity.getSharedPreferences(SHARED_PREFS_LOC, MODE_PRIVATE);
        String mode = preferences.getString("mode", "normal");
        //getting values from intent
        final FoodType foodType = (FoodType) intent.getSerializableExtra("foodType");
        final boolean isDrink = foodType == FoodType.MILK || foodType == FoodType.WATER;
        double nServes = intent.getDoubleExtra("nServes", 1);
        if(isDrink) {
            //then convert to millilitres
            nServes = nServes * DRINK_STANDARD_SERVE;
        }

        //inflate the dialog box view and get the text fields
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View addServeLayout = null;
        //alcohol has a completely different layout, so update view if so
        if(foodType == FoodType.ALCOHOL) {
            final double servesLiquid = intent.getDoubleExtra("servesLiquid", 1);
            if(servesLiquid <= 0) {
                Toast.makeText(activity, "You need to add serves of a base liquid (water or milk) first. Most alcohols (beer, spirits, wine) are water based.", Toast.LENGTH_LONG).show();
            } else {
                addServeLayout = updateViewForAlcohol(activity, servesLiquid);
            }
            if (addServeLayout == null) return;
        } else { //otherwise get the drink or meal layout
            if(isDrink) {
                addServeLayout = layoutInflater.inflate(R.layout.dialog_box_add_serves_drink, null);
            } else {
                addServeLayout = layoutInflater.inflate(R.layout.dialog_box_add_serves, null);
            }
        }

        //getting fields from servelayout
        final TextView oneServe = addServeLayout.findViewById(R.id.one_serve_explanation);
        final ImageView foodPicture = addServeLayout.findViewById(R.id.food_group_picture);
        final EditText numberOfServes = addServeLayout.findViewById(R.id.number_of_serves);
        numberOfServes.setText(df.format(nServes));

        //setting up the serve text to change when buttons pressed
        ServeChanger serveChanger = new ServeChanger(numberOfServes, isDrink);
        ImageButton addOneButton = addServeLayout.findViewById(R.id.add_one_serve);
        ImageButton addOneQuarterButton = addServeLayout.findViewById(R.id.add_one_quarter_serve);
        ImageButton minusOneButton = addServeLayout.findViewById(R.id.minus_one_serve);
        ImageButton minusOneQuarterButton = addServeLayout.findViewById(R.id.minus_one_quarter_serve);
        addOneButton.setOnClickListener(serveChanger);
        addOneQuarterButton.setOnClickListener(serveChanger);
        minusOneButton.setOnClickListener(serveChanger);
        minusOneQuarterButton.setOnClickListener(serveChanger);

        //setting up the dialog box
        AlertDialog.Builder addServeDialog = new AlertDialog.Builder(activity);
        updateFieldsFromFoodType(mode, foodType, oneServe, foodPicture, addServeDialog);
        addServeDialog.setView(addServeLayout);
        addServeDialog.setNegativeButton("Cancel", null);
        addServeDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                double serve = Double.parseDouble(numberOfServes.getText().toString());
                //if its a drink, convert from millilitres to standard serves
                if(isDrink) {
                    serve = serve/DRINK_STANDARD_SERVE;
                }
                //if there is no listener, then update meal and get cheats from user
                if(listener==null) {
                    Meal snack = new Meal();
                    updateMealServe(snack, foodType, serve);
                    snack.setUser(FirebaseAuth.getInstance().getUid());
                    snack.setDay(System.currentTimeMillis());
                    updateMealName(snack, isDrink);
                    SharedPreferences preferences = activity.getSharedPreferences(SHARED_PREFS_LOC, MODE_PRIVATE);
                    //if we are tracking cheats, get them first
                    if(preferences.getBoolean("track_cheats", true)) {
                        AddCheatsDialogBox.addCheats(activity, snack, isDrink);
                    } else {
                        //otherwise, set to 0 and add
                        snack.setCheatScore(0);
                        snack.pushToDB();
                    }

                } else {
                    //otherwise inform listener of the serves added
                    listener.serveAdded(foodType, serve);
                }
            }
        });
        addServeDialog.show();
    }

    /**
     * Sets the name of the meal, based on the time of the day and whetyher it is a drink or not
     * @param snack, the meal that is being updated
     * @param isDrink, whether snack is a drink
     */
    private static void updateMealName(Meal snack, boolean isDrink) {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        if(!isDrink) {
            if (timeOfDay >= 4 && timeOfDay < 11) {
                snack.setName("Morning snack");
            } else if (timeOfDay >= 11 && timeOfDay < 14) {
                snack.setName("Midday snack");
            } else if (timeOfDay >= 14 && timeOfDay < 18) {
                snack.setName("Afternoon snack");
            } else if (timeOfDay >= 18 && timeOfDay < 22) {
                snack.setName("Evening snack");
            } else if (timeOfDay >= 22 || timeOfDay < 4) {
                snack.setName("Midnight snack");
            } else {
                snack.setName("Snack");
            }
        } else {
            if (timeOfDay >= 4 && timeOfDay < 11) {
                snack.setName("Morning drink");
            } else if (timeOfDay >= 11 && timeOfDay < 14) {
                snack.setName("Midday drink");
            } else if (timeOfDay >= 14 && timeOfDay < 18) {
                snack.setName("Afternoon drink");
            } else if (timeOfDay >= 18 && timeOfDay < 22) {
                snack.setName("Evening drink");
            } else if (timeOfDay >= 22 || timeOfDay < 4) {
                snack.setName("Midnight drink");
            } else {
                snack.setName("Drink");
            }
        }
    }

    /**
     * Updates the number of serves in the snack, based on the count and foodtype
     * @param snack, the meal to change the values of
     * @param foodType, the foodtype being modified
     * @param serve, the number of serves to add
     */
    private static void updateMealServe(Meal snack, FoodType foodType, double serve) {
        switch (foodType) {
            case VEGETABLE:
                snack.setVegCount(serve);
                break;
            case MEAT:
                snack.setProteinCount(serve);
                break;
            case DAIRY:
                snack.setDairyCount(serve);
                break;
            case MILK:
                snack.setDairyCount(serve);
                snack.setHydrationScore(serve);
                break;
            case GRAIN:
                snack.setGrainCount(serve);
                break;
            case FRUIT:
                snack.setFruitCount(serve);
                break;
            case EXCESS:
                snack.setExcessServes(serve);
                break;
            case WATER:
                snack.setWaterCount(serve);
                snack.setHydrationScore(serve);
                break;
            case CAFFEINE:
                snack.setCaffeineCount(serve);
                break;
            case ALCOHOL:
                snack.setAlcoholStandards(serve);
        }
    }

    /**
     * Using the foodtype, update values for various views
     * @param mode, the mode (vegan, vegetarian or normal) set by the user
     * @param foodType, the food group of the food being added
     * @param oneServe, the text describing what one serve of said food is. This will be changed by the function
     * @param foodPicture, the picture showing the food type. This will be changed by the function.
     * @param addServeDialog, the title of the dialog box. This will be changed by the function.
     */
    private static void updateFieldsFromFoodType(String mode, FoodType foodType, TextView oneServe, ImageView foodPicture, AlertDialog.Builder addServeDialog) {
        switch(foodType) {
            case MEAT:
                if (mode != null) {
                    switch (mode) {
                        case "normal":
                            addServeDialog.setTitle("Add serve of meat");
                            oneServe.setText(R.string.serve_proteins);
                            foodPicture.setImageResource(R.drawable.meat_full);
                            break;
                        case "vegetarian":
                            addServeDialog.setTitle("Add serve of Protein");
                            oneServe.setText(R.string.serve_proteins_vegetarian);
                            foodPicture.setImageResource(R.drawable.vegan_meat_full);
                            break;
                        case "vegan":
                            addServeDialog.setTitle("Add serve of Protein");
                            oneServe.setText(R.string.serve_proteins_vegan);
                            foodPicture.setImageResource(R.drawable.vegan_meat_full);
                            break;
                    }
                }
                break;
            case DAIRY:
                oneServe.setText(R.string.serve_dairy);
                foodPicture.setImageResource(R.drawable.dairy_full);
                addServeDialog.setTitle("Add serve of dairy");
                if (mode != null && mode.equals("vegan")) {
                    oneServe.setText(R.string.serve_dairy_vegan);
                }
                break;
            case FRUIT:
                oneServe.setText(R.string.serve_fruits);
                foodPicture.setImageResource(R.drawable.fruit_full);
                addServeDialog.setTitle("Add serve of fruit");
                break;
            case GRAIN:
                oneServe.setText(R.string.serve_grains);
                foodPicture.setImageResource(R.drawable.grain_full);
                addServeDialog.setTitle("Add serve of grain");
                break;
            case VEGETABLE:
                oneServe.setText(R.string.serve_vegetables);
                foodPicture.setImageResource(R.drawable.vegetables_full);
                addServeDialog.setTitle("Add serve of vegetables");
                break;
            case EXCESS:
                oneServe.setText(R.string.serve_excess);
                foodPicture.setImageResource(R.drawable.excess);
                addServeDialog.setTitle("Add excess serves");
                break;
            case MILK:
                oneServe.setText(R.string.serve_milk);
                foodPicture.setImageResource(R.drawable.dairy_full);
                addServeDialog.setTitle("Add serves of milk or other dairy");
                break;
            case WATER:
                oneServe.setText(R.string.serve_water);
                foodPicture.setImageResource(R.drawable.symbol_water_completed);
                addServeDialog.setTitle("Add serves of water based liquid");
                break;
            case CAFFEINE:
                oneServe.setText(R.string.serve_caffeine);
                foodPicture.setImageResource(R.drawable.caffiene);
                foodPicture.setRotation(0);
                addServeDialog.setTitle("Add serves of caffeine");
                break;
            case ALCOHOL:
                addServeDialog.setTitle("Add standard serves of alcohol");
                break;
        }
    }

    /**
     * Converts the view from a normal add serve view type, to one for adding alcohol
     * @param activity, the activity the view is being created above
     * @param servesLiquid, the number of serves of liquid added so far
     * @return the new view
     */
    private static View updateViewForAlcohol(Activity activity, final double servesLiquid) {
        //inflating view and getting sub views
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View addServeLayout = layoutInflater.inflate(R.layout.dialog_box_alcohol_serve, null);
        final SeekBar alcoholSeekbar = addServeLayout.findViewById(R.id.alcohol_seekbar);
        final EditText percentageValueET = addServeLayout.findViewById(R.id.alcohol_percent);
        final EditText numberOfStandards = addServeLayout.findViewById(R.id.number_of_serves);
        final ImageButton minusPercentButton = addServeLayout.findViewById(R.id.minus_one_percent);
        final ImageButton addPercentButton = addServeLayout.findViewById(R.id.add_one_percent);

        //updating the volume text
        TextView volume = addServeLayout.findViewById(R.id.current_volume);
        String volumeText = "Curent Volume: " + (int) servesLiquid * DRINK_STANDARD_SERVE + "mL";
        volume.setText(volumeText);


        minusPercentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPercent = (int) Double.parseDouble(percentageValueET.getText().toString());
                percentageValueET.setText(df.format(currentPercent-1));
            }
        });
        addPercentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPercent = (int) Double.parseDouble(percentageValueET.getText().toString());
                percentageValueET.setText(df.format(currentPercent+1));
            }
        });
        //when the seekbar is updated, we want to update the percentage text
        alcoholSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //get the current percentage in the edittext
                double oldPercentage;
                if(percentageValueET.getText().toString().isEmpty()) {
                    oldPercentage = 0;
                } else {
                    oldPercentage = Double.parseDouble(percentageValueET.getText().toString());
                }
                //if the editText is different to the current percentage, update it
                //this is necessary so that they aren't fighting eachother for values
                if((int) oldPercentage != progress) {
                    percentageValueET.setText(df.format(progress));
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        //whenever the alcohol percent text changes, update the seekbar and number of standards
        percentageValueET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable editablePercentage) {
                double percentage;
                if(editablePercentage.toString().isEmpty()) {
                    percentage = 0;
                } else {
                    percentage = Double.parseDouble(editablePercentage.toString());
                }
                alcoholSeekbar.setProgress((int) percentage);

                //getting number of standards
                double standards = Meal.getStandardsFromPercent(servesLiquid, 0, percentage);
                String numberOfstandardsString = numberOfStandards.getText().toString();
                //getting old number of standards
                double oldStandards;
                if(numberOfstandardsString.isEmpty()) {
                    oldStandards = 0;
                } else {
                    oldStandards = Double.parseDouble(numberOfstandardsString);
                }
                //if significant distance in old and new values, update the editText
                if(standards > oldStandards + 0.01 || standards < oldStandards - 0.01) {
                    numberOfStandards.setText(df.format(standards));
                }
            }
        });
        //when the number of standards updates, update the percentage
        numberOfStandards.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable editableStandards) {
                //getting the new standards count
                double standards;
                if(editableStandards.toString().isEmpty()) {
                    standards = 0;
                } else {
                    standards = Double.parseDouble(editableStandards.toString());
                }

                //getting the new percent
                double percent = Meal.getPercentFromStandards(servesLiquid,0,standards);
                //getting the old percent
                double oldPercentage;
                if(percentageValueET.getText().toString().isEmpty()) {
                    oldPercentage = 0;
                } else {
                    oldPercentage = Double.parseDouble(percentageValueET.getText().toString());
                }
                //if significant distance in old and new values, update the editText
                if(percent > oldPercentage + 0.5 || percent < oldPercentage - 0.5) {
                    if(percent > 100) {
                        percent = 100;
                    }
                    percentageValueET.setText(df.format(percent));
                }

            }
        });
        return addServeLayout;
    }

    /**
     * used to update the serve count when a button is pressed
     */
    public static class ServeChanger implements View.OnClickListener {
        TextView servingCount; //the text to update
        boolean isDrink;

        ServeChanger(TextView servingCount, boolean isDrink) {
            this.servingCount = servingCount;
            this.isDrink = isDrink;
        }

        @Override
        public void onClick(View view) {
            double currentServes = Double.parseDouble(servingCount.getText().toString());
            double newServes=1;
            //depending on the view that called the function, change the serve count
            if(!isDrink) {
                switch (view.getId()) {
                    case R.id.add_one_serve:
                        newServes = currentServes + 1;
                        break;
                    case R.id.add_one_quarter_serve:
                        newServes = currentServes + 0.25;
                        break;
                    case R.id.minus_one_quarter_serve:
                        newServes = currentServes - 0.25;
                        break;
                    case R.id.minus_one_serve:
                        newServes = currentServes - 1;
                        break;
                }
            } else {
                switch (view.getId()) {
                    case R.id.add_one_serve:
                        newServes = currentServes + DRINK_STANDARD_SERVE;
                        break;
                    case R.id.add_one_quarter_serve:
                        newServes = currentServes + 30;
                        break;
                    case R.id.minus_one_quarter_serve:
                        newServes = currentServes - 30;
                        break;
                    case R.id.minus_one_serve:
                        newServes = currentServes - DRINK_STANDARD_SERVE;
                        break;
                }
            }
            //serves cannot go below 0
            if(newServes < 0) {
                newServes = 0;
            }
            servingCount.setText(df.format(newServes));
        }
    }

    /**
     * interface used to watch serves received. Allows other views to update using this.
     */
    public interface ServeListener {
        void serveAdded(FoodType type, double serve);
    }
}
