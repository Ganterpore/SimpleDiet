package com.ganterpore.simplediet.View.DialogBoxes;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.preference.DialogPreference;


public class UpdateDietDialogPreference extends DialogPreference {
    Context context;

    public UpdateDietDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    protected void onClick() {
        switch (getKey()) {
            case "update_diet":
                UpdateDietDialogBox.updateDiet(context);
                break;
            case "update_hydration":
                UpdateDrinkDietPlanDialogBox.updateDiet(context);
                break;
            case "update_cheat_points":
                UpdateCheatsDialogBox.updateDiet(context);
                break;
        }

    }
}

    //TODO may move to single class in future
//    /**
//     * opens a dialogue box recieving information on diet plans
//     * then adds the meal to the database
//     */
//    public static void updateDiet(final Context context) {
//        //inflate the dialog box view and get the text fields
//        LayoutInflater layoutInflater = LayoutInflater.from(context);
//        View updateDietLayout = layoutInflater.inflate(R.layout.dialog_box_diet_plan, null);
//        final EditText vegCountET= updateDietLayout.findViewById(R.id.veg_count);
//
//        //getting the text boxes from the view
//        final EditText proteinCountET= updateDietLayout.findViewById(R.id.protein_count);
//        final EditText dairyCountET = updateDietLayout.findViewById(R.id.dairy_count);
//        final EditText grainCountET= updateDietLayout.findViewById(R.id.grain_count);
//        final EditText fruitCountET = updateDietLayout.findViewById(R.id.fruit_count);
//        final EditText waterCountET = updateDietLayout.findViewById(R.id.water_count);
//        final EditText cheatScoreET = updateDietLayout.findViewById(R.id.cheat_score);
//
//        //Build the dialog box
//        AlertDialog.Builder addMealDialog = new AlertDialog.Builder(context);
//        addMealDialog.setTitle("Update Diet Plan");
//        addMealDialog.setView(updateDietLayout);
//        addMealDialog.setNegativeButton("Cancel", null);
//        addMealDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                //create a dietController object from the dialog box data
//                DietPlan plan = new DietPlan(
//                        Double.parseDouble(vegCountET.getText().toString()),
//                        Double.parseDouble(proteinCountET.getText().toString()),
//                        Double.parseDouble(dairyCountET.getText().toString()),
//                        Double.parseDouble(grainCountET.getText().toString()),
//                        Double.parseDouble(fruitCountET.getText().toString()),
//                        Double.parseDouble(waterCountET.getText().toString()),
//                        Double.parseDouble(cheatScoreET.getText().toString()),
//                        FirebaseAuth.getInstance().getCurrentUser().getUid()
//                );
//                plan.pushToDB()
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
//
//            }
//        }).show();
//    }
