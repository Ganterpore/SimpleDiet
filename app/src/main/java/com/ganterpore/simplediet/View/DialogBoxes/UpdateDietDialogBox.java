package com.ganterpore.simplediet.View.DialogBoxes;

import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.DialogPreference;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.ganterpore.simplediet.Controller.BasicDietController;
import com.ganterpore.simplediet.Controller.DietController;
import com.ganterpore.simplediet.Controller.RecipeBookController;
import com.ganterpore.simplediet.Model.DietPlan;
import com.ganterpore.simplediet.Model.Recipe;
import com.ganterpore.simplediet.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Query;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class UpdateDietDialogBox {
    private static NumberFormat df = new DecimalFormat("##.##");
    private static EditText vegCountET;
    private static EditText proteinCountET;
    private static EditText dairyCountET;
    private static EditText grainCountET;
    private static EditText fruitCountET;

    /**
     * opens a dialogue box recieving information on diet plans
     * then adds the meal to the database
     */
    public static void updateDiet(final Context context) {
        //inflate the dialog box view and get the text fields
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View updateDietLayout = layoutInflater.inflate(R.layout.dialog_box_diet_plan, null);
        DietPlan dietPlan = BasicDietController.getInstance().getOverallDietPlan();

        //getting the text boxes from the view
        vegCountET = updateDietLayout.findViewById(R.id.veg_count);
        proteinCountET = updateDietLayout.findViewById(R.id.protein_count);
        dairyCountET = updateDietLayout.findViewById(R.id.dairy_count);
        grainCountET = updateDietLayout.findViewById(R.id.grain_count);
        fruitCountET = updateDietLayout.findViewById(R.id.fruit_count);
        vegCountET.setText(df.format(dietPlan.getDailyVeges()));
        proteinCountET.setText(df.format(dietPlan.getDailyProtein()));
        dairyCountET.setText(df.format(dietPlan.getDailyDairy()));
        grainCountET.setText(df.format(dietPlan.getDailyGrain()));
        fruitCountET.setText(df.format(dietPlan.getDailyFruit()));
//        final EditText waterCountET = updateDietLayout.findViewById(R.id.water_count);
//        final EditText cheatScoreET = updateDietLayout.findViewById(R.id.cheat_score);

        //setting up the recycler view for the default diets
        RecyclerView defaultDiets = updateDietLayout.findViewById(R.id.default_diets);
        Query getDefaultDiets = DietPlan.defaultDiets();
        FirestoreRecyclerOptions<DietPlan> options = new FirestoreRecyclerOptions.Builder<DietPlan>()
                .setQuery(getDefaultDiets, DietPlan.class).build();
        FirestoreRecyclerAdapter<DietPlan, DietViewHolder> adapter;
        adapter = new FirestoreRecyclerAdapter<DietPlan, DietViewHolder>(options) {
            @NonNull
            @Override
            public DietViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View dietPlanListItem = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_diet_plan, parent, false);
                return new DietViewHolder(dietPlanListItem);
            }
            @Override
            protected void onBindViewHolder(@NonNull DietViewHolder dietViewHolder, int i, @NonNull DietPlan dietPlan) {
                dietViewHolder.build(dietPlan);
            }
        };
        adapter.notifyDataSetChanged();
        defaultDiets.setAdapter(adapter);
        adapter.startListening();

        //Build the dialog box
        AlertDialog.Builder addMealDialog = new AlertDialog.Builder(context);
        addMealDialog.setTitle("Update Daily Diet Plan");
        addMealDialog.setView(updateDietLayout);
        addMealDialog.setNegativeButton("Cancel", null);
        addMealDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //create a dietController object from the dialog box data
                DietPlan newPlan = BasicDietController.getInstance().getOverallDietPlan();
                newPlan.setDailyVeges(Double.parseDouble(vegCountET.getText().toString()));
                newPlan.setDailyProtein(Double.parseDouble(proteinCountET.getText().toString()));
                newPlan.setDailyDairy(Double.parseDouble(dairyCountET.getText().toString()));
                newPlan.setDailyGrain(Double.parseDouble(grainCountET.getText().toString()));
                newPlan.setDailyFruit(Double.parseDouble(fruitCountET.getText().toString()));
//                        plan.setDaily(Double.parseDouble(waterCountET.getText().toString()));
//                        plan.setDaily(Double.parseDouble(cheatScoreET.getText().toString()));
//                        FirebaseAuth.getInstance().getCurrentUser().getUid()
//                );
                newPlan.pushToDB()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void documentReference) {
                                    Toast.makeText(context, "Updated Diet Plan", Toast.LENGTH_SHORT).show();
                                }
                            })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Diet Plan Update Failed", Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        }).show();
    }

    public static class DietViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private View itemView;
        private DietPlan dietPlan;

        public DietViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            itemView.setOnClickListener(this);
        }

        public void build(DietPlan dietPlan) {
            this.dietPlan = dietPlan;
            TextView title = itemView.findViewById(R.id.title);
            TextView vegeCount = itemView.findViewById(R.id.veges_diet_count);
            TextView meatCount = itemView.findViewById(R.id.meat_diet_count);
            TextView dairyCount = itemView.findViewById(R.id.dairy_diet_count);
            TextView grainCount = itemView.findViewById(R.id.grain_diet_count);
            TextView fruitCount = itemView.findViewById(R.id.fruit_diet_count);

            title.setText(dietPlan.getDietName());
            vegeCount.setText("V: " + df.format(dietPlan.getDailyVeges()));
            meatCount.setText("M: " + df.format(dietPlan.getDailyProtein()));
            dairyCount.setText("D: " + df.format(dietPlan.getDailyDairy()));
            grainCount.setText("G: " + df.format(dietPlan.getDailyGrain()));
            fruitCount.setText("F: " + df.format(dietPlan.getDailyFruit()));
        }

        @Override
        public void onClick(View v) {
            //when clicked, update the current dietplan to match
            vegCountET.setText(df.format(dietPlan.getDailyVeges()));
            proteinCountET.setText(df.format(dietPlan.getDailyProtein()));
            dairyCountET.setText(df.format(dietPlan.getDailyDairy()));
            grainCountET.setText(df.format(dietPlan.getDailyGrain()));
            fruitCountET.setText(df.format(dietPlan.getDailyFruit()));
        }
    }

//    public class UpdateDietDialogPreference extends DialogPreference {
//        Context context;
//
//        public UpdateDietDialogPreference(Context context, AttributeSet attrs) {
//            super(context, attrs);
//            this.context = context;
//        }
//
//        @Override
//        protected void onClick() {
//            updateDiet(context);
//        }
//    }
}
