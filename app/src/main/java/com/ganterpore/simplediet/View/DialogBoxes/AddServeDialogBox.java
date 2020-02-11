package com.ganterpore.simplediet.View.DialogBoxes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ganterpore.simplediet.Model.Meal;
import com.ganterpore.simplediet.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;


public class AddServeDialogBox  {
    public enum FoodType {VEGETABLE, MEAT, DAIRY, GRAIN, FRUIT, EXCESS, MILK, WATER, CAFFEINE, ALCOHOL}
    public static final String TAG = "AddServeDialogBox";
    private static NumberFormat df = new DecimalFormat("##.##");


    public static void addServe(final Activity activity, final Intent intent, final ServeListener listener) {
        //getting values from intent
        double nServes = intent.getDoubleExtra("nServes", 1);
        final FoodType foodType = (FoodType) intent.getSerializableExtra("foodType");

        //inflate the dialog box view and get the text fields
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        //TODO different views for drinks
        View addServeLayout = layoutInflater.inflate(R.layout.dialog_box_add_serves, null);

        //if adding alcohol, adjust for different layout
        if(foodType == FoodType.ALCOHOL) {
            //getting values
            final double servesLiquid = intent.getDoubleExtra("servesLiquid", 1);
            addServeLayout = layoutInflater.inflate(R.layout.dialog_box_alcohol_serve, null);
            final SeekBar alcoholSeekbar = addServeLayout.findViewById(R.id.alcohol_seekbar);
            final EditText percentageValueET = addServeLayout.findViewById(R.id.alcohol_percent);
            final EditText numberOfStandards = addServeLayout.findViewById(R.id.number_of_serves);
            //updating the volume text
            TextView volume = addServeLayout.findViewById(R.id.current_volume);
            //TODO warning if no base added yet
            String volumeText = "Curent Volume: " + (int) servesLiquid * 250 + "mL";
            volume.setText(volumeText);

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
        }

        //getting fields
        final TextView oneServe = addServeLayout.findViewById(R.id.one_serve_explanation);
        final ImageView foodPicture = addServeLayout.findViewById(R.id.food_group_picture);
        final EditText numberOfServes = addServeLayout.findViewById(R.id.number_of_serves);
        numberOfServes.setText(df.format(nServes));

        //setting up the serve text to change when buttons pressed
        ServeChanger serveChanger = new ServeChanger(numberOfServes);
        ImageButton addOneButton = addServeLayout.findViewById(R.id.add_one_serve);
        ImageButton addOneQuarterButton = addServeLayout.findViewById(R.id.add_one_quarter_serve);
        ImageButton minusOneButton = addServeLayout.findViewById(R.id.minus_one_serve);
        ImageButton minusOneQuarterButton = addServeLayout.findViewById(R.id.minus_one_quarter_serve);
        addOneButton.setOnClickListener(serveChanger);
        addOneQuarterButton.setOnClickListener(serveChanger);
        minusOneButton.setOnClickListener(serveChanger);
        minusOneQuarterButton.setOnClickListener(serveChanger);

        AlertDialog.Builder addServeDialog = new AlertDialog.Builder(activity);

        //setting up the dialog box depending on the food used
        switch(foodType) {
            case MEAT:
                oneServe.setText(R.string.serve_proteins);
                foodPicture.setImageResource(R.drawable.meat_full);
                addServeDialog.setTitle("Add serve of meat");
                break;
            case DAIRY:
                oneServe.setText(R.string.serve_dairy);
                foodPicture.setImageResource(R.drawable.dairy_full);
                addServeDialog.setTitle("Add serve of dairy");
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

        addServeDialog.setView(addServeLayout);
        addServeDialog.setNegativeButton("Cancel", null);
        addServeDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                double serve = Double.parseDouble(numberOfServes.getText().toString());
                //if there is no listener, then get cheat count and push to db
                if(listener==null) {
                    Meal snack = new Meal();
                    switch (foodType) {
                        case VEGETABLE:
                            snack.setVegCount(serve);
                            break;
                        case MEAT:
                            snack.setProteinCount(serve);
                            break;
                        case DAIRY:
                        case MILK:
                            snack.setDairyCount(serve);
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
                            break;
                        case CAFFEINE:
                            snack.setCaffieneCount(serve);
                            break;
                        case ALCOHOL:
                            snack.setAlcoholStandards(serve);
                    }
                    snack.setUser(FirebaseAuth.getInstance().getUid());
                    snack.setDay(System.currentTimeMillis());

                    //setting the snack name up based on the time of day
                    Calendar c = Calendar.getInstance();
                    int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
                    if(timeOfDay >= 4 && timeOfDay < 11){
                        snack.setName("Morning snack");
                    }else if(timeOfDay >= 11 && timeOfDay < 14){
                        snack.setName("Midday snack");
                    }else if(timeOfDay >= 14 && timeOfDay < 18){
                        snack.setName("Afternoon snack");
                    }else if(timeOfDay >= 18 && timeOfDay < 22){
                        snack.setName("Evening snack");
                    }else if(timeOfDay >= 22 || timeOfDay < 4){
                        snack.setName("Midnight snack");
                    } else {
                        snack.setName("Snack");
                    }
                    AddCheatsDialogBox.addCheats(activity, snack);
                } else {
                    //otherwise inform listener of the serves added
                    listener.serveAdded(foodType, serve);
                }
            }
        });
        addServeDialog.show();
    }

    /**
     * used to update the serve count when a button is pressed
     */
    public static class ServeChanger implements View.OnClickListener {
        TextView servingCount; //the text to update

        public ServeChanger(TextView serveText) {
            this.servingCount = serveText;
        }

        @Override
        public void onClick(View view) {
            double currentServes = Double.parseDouble(servingCount.getText().toString());
            double newServes=1;
            //depending on the view that called the function, change the serve count
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
