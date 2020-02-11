package com.ganterpore.simplediet.View.DialogBoxes;

import android.app.Activity;
import android.app.AlertDialog;
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

public class AddDrinkDialogBox implements AddServeDialogBox.ServeListener {
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

    public AddDrinkDialogBox(final Activity activity) {
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

        //getting all the textviews
        //TODO more views for percent/mls etc
        milkCountTV = addDrinkLayout.findViewById(R.id.milk_count);
        waterCountTV = addDrinkLayout.findViewById(R.id.water_count);
        caffeineCountTV = addDrinkLayout.findViewById(R.id.caffiene_count);
        alcoholCountTV = addDrinkLayout.findViewById(R.id.alcohol_count);
        volumeTV = addDrinkLayout.findViewById(R.id.volume);
        hydrationFactor = addDrinkLayout.findViewById(R.id.hydration_factor);
        final EditText drinkNameET = addDrinkLayout.findViewById(R.id.drink_name);

        //TODO change example food when updated
        final RadioGroup cheatSelector = addDrinkLayout.findViewById(R.id.cheat_selector);
        final RadioGroup daySelector = addDrinkLayout.findViewById(R.id.day_selector);

        //building the alert dialog
        final AlertDialog.Builder addDrinkDialog = new AlertDialog.Builder(activity);
        addDrinkDialog.setView(addDrinkLayout);
        addDrinkDialog.setNeutralButton("Save Recipe", new DialogInterface.OnClickListener() {
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
                Recipe recipe = Recipe.drinkRecipe(drinkNameET.getText().toString(),
                        waterServes, milkServes, caffieneServes, alcoholServes, cheatScore,
                        FirebaseAuth.getInstance().getCurrentUser().getUid());
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
                Meal todaysDrink = Meal.Drink(waterServes, milkServes, caffieneServes, alcoholServes,
                        cheatScore, day, FirebaseAuth.getInstance().getCurrentUser().getUid());
                todaysDrink.setName(drinkNameET.getText().toString());

                todaysDrink.pushToDB()
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
        addDrinkDialog.show();
    }

    public static void addDrink(Activity activity) {
        new AddDrinkDialogBox(activity);
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
            case WATER:
                waterCountTV.setText(df.format(serve));
                waterServes = serve;
                break;
            case MILK:
                milkCountTV.setText(df.format(serve));
                milkServes = serve;
                break;
            case CAFFEINE:
                caffeineCountTV.setText(df.format(serve));
                caffieneServes = serve;
                break;
            case ALCOHOL:
                alcoholCountTV.setText(df.format(serve));
                alcoholServes = serve;
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
//        updateExampleFood();
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
            LinearLayout parent = (LinearLayout) view.getParent().getParent().getParent();
            TextView servesView;
            double serves;
            Intent intent = new Intent();
            //from the id of the button that called it, figure out what food to add serves to, and the current serves
            switch (view.getId()) {
                case R.id.water_image:
                    type = AddServeDialogBox.FoodType.WATER;
                    servesView = parent.findViewById(R.id.water_count);
                    break;
                case R.id.milk_image:
                    type = AddServeDialogBox.FoodType.MILK;
                    servesView = parent.findViewById(R.id.milk_count);
                    break;
                case R.id.caffeine_image:
                    type = AddServeDialogBox.FoodType.CAFFEINE;
                    servesView = parent.findViewById(R.id.caffiene_count);
                    break;
                case R.id.alcohol_image:
                    type = AddServeDialogBox.FoodType.ALCOHOL;
                    servesView = parent.findViewById(R.id.alcohol_count);
                    //for alcohol, we also need to get the volume of liquid (in serves)
                    String waterServeString = ((TextView) parent.findViewById(R.id.water_count))
                            .getText().toString();
                    double waterServe;
                    if(waterServeString.isEmpty()) {
                        waterServe = 0;
                    } else {
                        waterServe = Double.parseDouble(waterServeString);
                    }
                    String dairyServeString = ((TextView) parent.findViewById(R.id.milk_count))
                            .getText().toString();
                    double dairyServe;
                    if(dairyServeString.isEmpty()) {
                        dairyServe = 0;
                    } else {
                        dairyServe = Double.parseDouble(dairyServeString);
                    }
                    intent.putExtra("servesLiquid", waterServe+dairyServe);
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
