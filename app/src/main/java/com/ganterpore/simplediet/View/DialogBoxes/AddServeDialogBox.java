package com.ganterpore.simplediet.View.DialogBoxes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ganterpore.simplediet.Model.Meal;
import com.ganterpore.simplediet.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;


public class AddServeDialogBox  {
    public enum FoodType {VEGETABLE, MEAT, DAIRY, GRAIN, FRUIT, EXCESS}
    public static final String TAG = "AddServeDialogBox";
    private static NumberFormat df = new DecimalFormat("##.##");

    public static void addServe(final Activity activity, final FoodType foodType, final ServeListener listener) {
        addServe(activity, foodType, 1, listener);
    }

    public static void addServe(final Activity activity, final FoodType foodType, double nServes, final ServeListener listener) {
        //inflate the dialog box view and get the text fields
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View addServeLayout = layoutInflater.inflate(R.layout.dialog_box_add_serves, null);

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
