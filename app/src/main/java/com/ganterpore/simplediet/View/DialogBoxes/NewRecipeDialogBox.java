package com.ganterpore.simplediet.View.DialogBoxes;

import android.app.Activity;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ganterpore.simplediet.Model.Recipe;
import com.ganterpore.simplediet.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;

public class NewRecipeDialogBox {
    /**
     * creates dialog box to allow the creation of a new recipe
     */
    public static void newRecipe(final Activity activity) {
        //inflate the dialog box view and get the text fields
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View newRecipeLayout = layoutInflater.inflate(R.layout.dialog_box_recipe, null);

        //getting all the text boxes from the view
        final EditText recipeName = newRecipeLayout.findViewById(R.id.recipe_name);
        final EditText vegCountET= newRecipeLayout.findViewById(R.id.veg_count);
        final EditText proteinCountET= newRecipeLayout.findViewById(R.id.protein_count);
        final EditText dairyCountET = newRecipeLayout.findViewById(R.id.dairy_count);
        final EditText grainCountET= newRecipeLayout.findViewById(R.id.grain_count);
        final EditText fruitCountET = newRecipeLayout.findViewById(R.id.fruit_count);
        final EditText waterCountET = newRecipeLayout.findViewById(R.id.water_count);
        final EditText excessCountET = newRecipeLayout.findViewById(R.id.excess_count);
        final EditText cheatScoreET = newRecipeLayout.findViewById(R.id.cheat_score);

        //Build the dialog box
        AlertDialog.Builder addMealDialog = new AlertDialog.Builder(activity);
        addMealDialog.setTitle("Add Meal");
        addMealDialog.setView(newRecipeLayout);
        addMealDialog.setNegativeButton("Cancel", null);
        addMealDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //create a recipe object from the dialog box data
                Recipe newRecipe = new Recipe(
                        recipeName.getText().toString(),
                        Double.parseDouble(vegCountET.getText().toString()),
                        Double.parseDouble(proteinCountET.getText().toString()),
                        Double.parseDouble(dairyCountET.getText().toString()),
                        Double.parseDouble(grainCountET.getText().toString()),
                        Double.parseDouble(fruitCountET.getText().toString()),
                        Double.parseDouble(waterCountET.getText().toString()),
                        Double.parseDouble(excessCountET.getText().toString()),
                        Double.parseDouble(cheatScoreET.getText().toString()),
                        FirebaseAuth.getInstance().getCurrentUser().getUid()
                );

                newRecipe.pushToDB()
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
            }
        }).show();
    }
}
