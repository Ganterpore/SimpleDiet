package com.ganterpore.simplediet.View;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.ganterpore.simplediet.Controller.DailyMeals;
import com.ganterpore.simplediet.Controller.DietPlanWrapper;
import com.ganterpore.simplediet.Controller.RecipeBookController;
import com.ganterpore.simplediet.Controller.WeeklyMeals;
import com.ganterpore.simplediet.Model.DietPlan;
import com.ganterpore.simplediet.Model.Meal;
import com.ganterpore.simplediet.Model.Recipe;
import com.ganterpore.simplediet.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements DailyMeals.DailyMealsInterface, WeeklyMeals.WeeklyMealsInterface, DietPlanWrapper.DietPlanInterface {
    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private DailyMeals today;
    private WeeklyMeals thisWeek;
    private DietPlanWrapper diet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        //instantiating a day and week to track
        today = new DailyMeals(this, FirebaseAuth.getInstance().getCurrentUser().getUid());
        thisWeek = new WeeklyMeals(this, FirebaseAuth.getInstance().getCurrentUser().getUid());
        diet = new DietPlanWrapper(this, mAuth.getCurrentUser().getUid());

        RecyclerView history = findViewById(R.id.day_history_list);
        history.setAdapter(new DayHistoryAdapter(this, 7));
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Log.d(TAG, "onStart: checking user");
        if(currentUser==null) {
            //if no user, then create an anonymous account
            Log.d(TAG, "onStart: no user");
            new AlertDialog.Builder(this)
                    .setTitle("No account detected")
                    .setMessage("Create new anonymous account?")
                    .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            signUpAnonymous();
                        }
                    }).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.update_plan:
                updateDiet();
                return true;
        }
        return false;
    }

    public void signUpEmail(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    public void signUpAnonymous() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * called when a user taps the add food button
     * allows the user  to choose what type of food they want to add.
     * @param view, the view that called the function
     */
    public void addFood(final View view) {
        final String[] choices = {"add Meal", "open Recipe Book"};
        new AlertDialog.Builder(this)
                .setTitle("new Meal or recipe?")
                .setItems(choices, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (choices[which]) {
                            case "add Meal":
                                addMeal(view);
                                break;
                            case "open Recipe Book":
                                openRecipeBook();
                                break;
                    }
                }
        }).show();
    }

    /**
     * opens a dialog containing the users recipe book, where they can add one of their meals to eat,
     * or create a new recipe.
     */
    private void openRecipeBook() {
        //inflating the views
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View recipeBookLayout = layoutInflater.inflate(R.layout.dialog_box_recipe_book, null);
        final Context context = this;

        //creating the Dialog box
        final AlertDialog.Builder recipeBookDialogBuilder = new AlertDialog.Builder(this);
        recipeBookDialogBuilder.setTitle("Recipe Book");
        recipeBookDialogBuilder.setView(recipeBookLayout);
        recipeBookDialogBuilder.setNeutralButton("Create New Recipe", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newRecipe();
            }
        });
        final AlertDialog recipeBookDialog = recipeBookDialogBuilder.show();

        //Creating the recyclerView of the list of recipes
        RecyclerView allRecipes = recipeBookLayout.findViewById(R.id.recipe_list);
        Query getRecipes = RecipeBookController.getAllRecipes();

        FirestoreRecyclerOptions<Recipe> options = new FirestoreRecyclerOptions.Builder<Recipe>()
                .setQuery(getRecipes, Recipe.class).build();

        FirestoreRecyclerAdapter<Recipe, RecipeViewHolder> adapter;
        adapter = new FirestoreRecyclerAdapter<Recipe, RecipeViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RecipeViewHolder holder, int position, @NonNull Recipe recipe) {
                holder.build(recipe);
            }

            @NonNull
            @Override
            public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View recipeListItem = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.list_item_recipe, viewGroup, false);
                return new RecipeViewHolder(recipeListItem, recipeBookDialog, context);
            }
        };
        adapter.notifyDataSetChanged();
        allRecipes.setAdapter(adapter);
        adapter.startListening();
    }

    /**
     * creates dialog box to allow the creation of a new recipe
     */
    public void newRecipe() {
        //inflate the dialog box view and get the text fields
        LayoutInflater layoutInflater = LayoutInflater.from(this);
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
        AlertDialog.Builder addMealDialog = new AlertDialog.Builder(this);
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
                        mAuth.getCurrentUser().getUid()
                );

                newRecipe.pushToDB()
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(MainActivity.this, "Added Recipe", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, "Recipe add fail", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }).show();
    }

    /**
     * opens a dialogue box recieving information on the meal to be added
     * then adds the meal to the database
     * @param view of the object that called the method
     */
    public void addMeal(View view) {
        //inflate the dialog box view and get the text fields
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View addMealLayout = layoutInflater.inflate(R.layout.dialog_box_meal, null);

        //getting all the text boxes from the view
        final EditText vegCountET= addMealLayout.findViewById(R.id.veg_count);
        final EditText proteinCountET= addMealLayout.findViewById(R.id.protein_count);
        final EditText dairyCountET = addMealLayout.findViewById(R.id.dairy_count);
        final EditText grainCountET= addMealLayout.findViewById(R.id.grain_count);
        final EditText fruitCountET = addMealLayout.findViewById(R.id.fruit_count);
        final EditText waterCountET = addMealLayout.findViewById(R.id.water_count);
        final EditText excessCountET = addMealLayout.findViewById(R.id.excess_count);
        final EditText cheatScoreET = addMealLayout.findViewById(R.id.cheat_score);

        //Build the dialog box
        AlertDialog.Builder addMealDialog = new AlertDialog.Builder(this);
        addMealDialog.setTitle("Add Meal");
        addMealDialog.setView(addMealLayout);
        addMealDialog.setNegativeButton("Cancel", null);
        addMealDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //create a meal object from the dialog box data
                Meal todaysMeal = new Meal(
                        Double.parseDouble(vegCountET.getText().toString()),
                        Double.parseDouble(proteinCountET.getText().toString()),
                        Double.parseDouble(dairyCountET.getText().toString()),
                        Double.parseDouble(grainCountET.getText().toString()),
                        Double.parseDouble(fruitCountET.getText().toString()),
                        Double.parseDouble(waterCountET.getText().toString()),
                        Double.parseDouble(excessCountET.getText().toString()),
                        Double.parseDouble(cheatScoreET.getText().toString()),
                        System.currentTimeMillis(),
                        mAuth.getCurrentUser().getUid()
                );

                todaysMeal.pushToDB()
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(MainActivity.this, "Added Meal", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, "Meal add fail", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }).show();
    }

    /**
     * opens a dialogue box recieving information on diet plans
     * then adds the meal to the database
     */
    public void updateDiet() {
        //inflate the dialog box view and get the text fields
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View updateDietLayout = layoutInflater.inflate(R.layout.dialog_box_diet_plan, null);
        final EditText vegCountET= updateDietLayout.findViewById(R.id.veg_count);

        //getting the text boxes from the view
        final EditText proteinCountET= updateDietLayout.findViewById(R.id.protein_count);
        final EditText dairyCountET = updateDietLayout.findViewById(R.id.dairy_count);
        final EditText grainCountET= updateDietLayout.findViewById(R.id.grain_count);
        final EditText fruitCountET = updateDietLayout.findViewById(R.id.fruit_count);
        final EditText waterCountET = updateDietLayout.findViewById(R.id.water_count);
        final EditText cheatScoreET = updateDietLayout.findViewById(R.id.cheat_score);

        //Build the dialog box
        AlertDialog.Builder addMealDialog = new AlertDialog.Builder(this);
        addMealDialog.setTitle("Update Diet Plan");
        addMealDialog.setView(updateDietLayout);
        addMealDialog.setNegativeButton("Cancel", null);
        addMealDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //create a diet object from the dialog box data
                DietPlan plan = new DietPlan(
                        Double.parseDouble(vegCountET.getText().toString()),
                        Double.parseDouble(proteinCountET.getText().toString()),
                        Double.parseDouble(dairyCountET.getText().toString()),
                        Double.parseDouble(grainCountET.getText().toString()),
                        Double.parseDouble(fruitCountET.getText().toString()),
                        Double.parseDouble(waterCountET.getText().toString()),
                        Double.parseDouble(cheatScoreET.getText().toString()),
                        mAuth.getCurrentUser().getUid()
                );
                diet.updateDietPlan(plan)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MainActivity.this, "Updated Diet Plan", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, "Diet Plan Update Failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }).show();
    }

    /*** methods called by various interfaces. All used to update the display when information is
    updated ***/

    @Override
    public void updateDailyMeals(DailyMeals day) {
        updateDisplayValues();
    }


    @Override
    public void updateWeeklyMeals(WeeklyMeals week) {
        updateDisplayValues();
    }

    @Override
    public void updateDietPlan(DietPlan diet) {
        updateDisplayValues();
    }

    private void updateDisplayValues() {
        DietPlan dietPlan = diet.getDietPlan();

        //get the text views from the main activity
        TextView vegTV = findViewById(R.id.veg_count);
        TextView proteinTV = findViewById(R.id.protein_count);
        TextView dairyTV = findViewById(R.id.dairy_count);
        TextView grainTV = findViewById(R.id.grain_count);
        TextView fruitTV = findViewById(R.id.fruit_count);
        TextView waterTV = findViewById(R.id.water_count);
        TextView excessTV = findViewById(R.id.excess_serves_count);
        TextView cheatTV = findViewById(R.id.cheat_count);

        //creating arrays of the text views to update
        TextView[] textViews = {vegTV, proteinTV, dairyTV, grainTV, fruitTV, waterTV};
        double[] counts = {today.getVegCount(), today.getProteinCount(), today.getDairyCount(),
                            today.getGrainCount(), today.getFruitCount(), today.getWaterCount()};
        double[] plans = {dietPlan.getDailyVeges(), dietPlan.getDailyProtein(), dietPlan.getDailyDairy(),
                            dietPlan.getDailyGrain(), dietPlan.getDailyFruit(), dietPlan.getDailyWater()};

        //updating text for all the main food groups
        for(int i=0;i<textViews.length;i++) {
            TextView textView = textViews[i];
            double count = counts[i];
            double plan = plans[i];
            double servesLeft = plan - count;

            if(servesLeft <= 0.2) {
                textView.setText(count + "/" + plan + " - Completed!");
                textView.setTextColor(Color.GREEN);
            } else {
                textView.setText(count + "/" + plan + " - " + servesLeft + " serves to go");
                textView.setTextColor(Color.BLACK);
            }


        }

        //updating text on other texts
        excessTV.setText(today.getExcessServes() + "");
        cheatTV.setText(thisWeek.getWeeklyCheats() + "/" + dietPlan.getWeeklyCheats());
    }

    /**
     * ViewHolder for the recipe list items in the recipe book recyclerView
     */
    public static class RecipeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View itemView;
        Recipe recipe;
        Context context;
        AlertDialog dialog;

        RecipeViewHolder(View itemView, AlertDialog dialog, Context context) {
            super(itemView);
            this.itemView = itemView;
            this.setIsRecyclable(false);
            this.context = context;
            this.dialog = dialog;
            itemView.setOnClickListener(this);
        }

        /**
         * Builds the view based on the recipe given
         * @param recipe, the recipe to build around
         */
        void build(final Recipe recipe) {
            this.recipe = recipe;
            TextView recipeName = itemView.findViewById(R.id.recipe_name);
            TextView servingCount = itemView.findViewById(R.id.serving_count);
            recipeName.setText(recipe.getName());
            servingCount.setText(recipe.serveCountText());
        }

        @Override
        public void onClick(View v) {
            //when the list item is clicked, create a dialog to add the meal
            AlertDialog.Builder confirmMeal = new AlertDialog.Builder(context);
            confirmMeal.setTitle("Would you like to add " + recipe.getName() + "?");
            confirmMeal.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Meal recipeMeal = recipe.convertToMeal();
                    recipeMeal.pushToDB();
                    //close the recipe book dialog if a meal is added
                    closeRecipeBook();
                }
            });
            confirmMeal.setNegativeButton("Cancel", null);
            confirmMeal.show();
        }

        public void closeRecipeBook() {
            dialog.dismiss();
        }
    }

    public class DayHistoryViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        Activity activity;

        public DayHistoryViewHolder(Activity activity, @NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.activity = activity;
        }

        public void build(DailyMeals day) {
            TextView dateTV = itemView.findViewById(R.id.date);
            DayHistoryItemView completedFoodTV = itemView.findViewById(R.id.completed_food);
            DayHistoryItemView completedWaterTV = itemView.findViewById(R.id.completed_water);
            DayHistoryItemView didntCheatTV = itemView.findViewById(R.id.didnt_cheat);

            dateTV.setText(dateFormat.format(day.getDate()));
            completedFoodTV.setCompleted(day.isFoodCompleted());
            completedWaterTV.setCompleted(day.isWaterCompleted());
            didntCheatTV.setCompleted(day.isOverCheatScore());

            final RecyclerView mealsList = itemView.findViewById(R.id.meals_list);
            mealsList.setAdapter(new MealsAdapter(activity, day.getMeals()));

            final ImageView dropdownButton = itemView.findViewById(R.id.dropdown_button);
            dropdownButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: Clicked!");
                    if(mealsList.getVisibility() == View.GONE) {
                        mealsList.setVisibility(View.VISIBLE);
                        dropdownButton.setImageDrawable(getResources().getDrawable(android.R.drawable.arrow_up_float));
                    } else {
                        mealsList.setVisibility(View.GONE);
                        dropdownButton.setImageDrawable(getResources().getDrawable(android.R.drawable.arrow_down_float));
                    }
                }
            });
        }
    }

    public class MealsAdapter extends RecyclerView.Adapter<MealsViewHolder> {

        private Activity activity;
        private List<Meal> meals;

        public MealsAdapter(Activity activity, List<Meal> meals) {
            this.activity = activity;
            this.meals = meals;
        }

        @NonNull
        @Override
        public MealsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view;
            if(meals.get(i).getName() != null) {
                view = LayoutInflater.from(activity).inflate(R.layout.list_item_meal, viewGroup, false);
            } else {
                view = LayoutInflater.from(activity).inflate(R.layout.list_item_meal_no_name, viewGroup, false);
            }
            return new MealsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MealsViewHolder mealsViewHolder, int i) {
            mealsViewHolder.build(meals.get(i));
        }

        @Override
        public int getItemCount() {
            if(meals == null) {
                return 0;
            } else {
                return meals.size();
            }
        }
    }

    public class MealsViewHolder extends RecyclerView.ViewHolder {

        public MealsViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void build(Meal meal) {
            if(meal.getName() != null) {
                TextView mealNameTV = itemView.findViewById(R.id.meal_name);
                mealNameTV.setText(meal.getName());
            }
            TextView servingCountTV = itemView.findViewById(R.id.serving_count);
            servingCountTV.setText(meal.serveCountText());
        }
    }

    public class DayHistoryAdapter extends RecyclerView.Adapter<DayHistoryViewHolder>
                                            implements DailyMeals.DailyMealsInterface{

        List<DailyMeals> days;
        Activity activity;

        public DayHistoryAdapter(Activity activity, int nDays) {
            days = new ArrayList<>();
            this.activity = activity;

            for(int i=0;i<nDays;i++) {
                days.add(new DailyMeals(this, FirebaseAuth.getInstance().getCurrentUser().getUid(), i));
            }

        }

        @NonNull
        @Override
        public DayHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(activity).inflate(R.layout.list_item_day_history, viewGroup, false);
            return new DayHistoryViewHolder(activity, view);
        }

        @Override
        public void onBindViewHolder(@NonNull DayHistoryViewHolder dayHistoryViewHolder, int i) {
            dayHistoryViewHolder.build(days.get(i));
        }

        @Override
        public int getItemCount() {
            return days.size();
        }

        @Override
        public void updateDailyMeals(DailyMeals day) {
            this.notifyDataSetChanged();
        }
    }
}
