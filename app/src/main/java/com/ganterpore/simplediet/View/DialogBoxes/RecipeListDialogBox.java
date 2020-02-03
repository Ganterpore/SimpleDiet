package com.ganterpore.simplediet.View.DialogBoxes;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.ganterpore.simplediet.Controller.RecipeBookController;
import com.ganterpore.simplediet.Model.Meal;
import com.ganterpore.simplediet.Model.Recipe;
import com.ganterpore.simplediet.R;
import com.google.firebase.firestore.Query;

public class RecipeListDialogBox {
    /**
     * opens a dialog containing the users recipe book, where they can add one of their meals to eat,
     * or create a new recipe.
     */
    public static void openRecipeBook(final Activity activity) {
        //inflating the views
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View recipeBookLayout = layoutInflater.inflate(R.layout.dialog_box_recipe_book, null);
        final Context context = activity;

        //creating the Dialog box
        final AlertDialog.Builder recipeBookDialogBuilder = new AlertDialog.Builder(activity);
        recipeBookDialogBuilder.setTitle("Recipe Book");
        recipeBookDialogBuilder.setView(recipeBookLayout);
        recipeBookDialogBuilder.setNeutralButton("Create New Recipe", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AddMealDialogBox.addMeal(activity, AddMealDialogBox.RECIPE);
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
}
