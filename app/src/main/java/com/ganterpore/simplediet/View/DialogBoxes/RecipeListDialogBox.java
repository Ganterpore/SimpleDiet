package com.ganterpore.simplediet.View.DialogBoxes;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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
import com.ganterpore.simplediet.View.Activities.MealHistoryDisplay;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.primitives.Ints;
import com.google.firebase.firestore.Query;

public class RecipeListDialogBox {
    private static final String TAG = "RecipeListDialogBox";
    private View recipeBookLayout;
    private final AlertDialog recipeBookDialog;
    private Activity activity;


    /**
     * opens a dialog containing the users recipe book, where they can add one of their meals to eat,
     * or create a new recipe.
     */
    public static void openRecipeBook(final Activity activity) {
        new RecipeListDialogBox(activity);
    }
    public RecipeListDialogBox(final Activity activity) {
        this.activity = activity;
        //inflating the views
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        recipeBookLayout = layoutInflater.inflate(R.layout.dialog_box_recipe_book, null);

        //creating the Dialog box
        final AlertDialog.Builder recipeBookDialogBuilder = new AlertDialog.Builder(activity);
        recipeBookDialogBuilder.setTitle("Recipe Book");
        recipeBookDialogBuilder.setView(recipeBookLayout);
        recipeBookDialogBuilder.setNeutralButton("Create New Recipe", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AddMealDialogBox.addMeal(activity, new Intent().putExtra("type", AddMealDialogBox.NEW_RECIPE));
            }
        });
        recipeBookDialogBuilder.setNegativeButton("Close", null);
        recipeBookDialog = recipeBookDialogBuilder.show();

        //Creating the recyclerView of the list of recipes
        RecyclerView allRecipes = recipeBookLayout.findViewById(R.id.meal_recipe_list);
        Query getRecipes = RecipeBookController.getAllRecipes();

        FirestoreRecyclerOptions<Recipe> options = new FirestoreRecyclerOptions.Builder<Recipe>()
                .setQuery(getRecipes, Recipe.class).build();

        FirestoreRecyclerAdapter<Recipe, RecipeViewHolder> adapter;
        adapter = new FirestoreRecyclerAdapter<Recipe, RecipeViewHolder>(options) {
            final int MEAL = 1;
            final int MEAL_W_HEADER = 2;
            final int DRINK = 3;
            final int DRINK_W_HEADER = 4;
            @Override
            protected void onBindViewHolder(@NonNull RecipeViewHolder holder, int position, @NonNull Recipe recipe) {
                String documentID = getSnapshots().getSnapshot(position).getId();
                holder.build(recipe, documentID);
            }

            @NonNull
            @Override
            public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                View recipeListItem;
                if(viewType==MEAL_W_HEADER) {
                    recipeListItem = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.list_item_recipe_with_header, viewGroup, false);
                    TextView header = recipeListItem.findViewById(R.id.header);
                    header.setText("  Meals");
                } else if(viewType==DRINK_W_HEADER) {
                    recipeListItem = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.list_item_recipe_with_header, viewGroup, false);
                    TextView header = recipeListItem.findViewById(R.id.header);
                    header.setText("  Drinks");
                } else {
                    recipeListItem = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.list_item_recipe, viewGroup, false);
                }
                return new RecipeViewHolder(recipeListItem, recipeBookDialog, activity);
            }

            @Override
            public int getItemViewType(int position) {
                Recipe item = getItem(position);
                if(item.isDrink()) {
                    //assumes ordered by drinks first
                    if(position==0) {
                        return DRINK_W_HEADER;
                    }
                    return DRINK;
                } else {
                    //if the first meal, needs a header
                    if(position==0 || getItem(position-1).isDrink()) {
                        return MEAL_W_HEADER;
                    }
                    return MEAL;
                }
            }
        };
        RecipeSwipeController swipeController = new RecipeSwipeController(activity, allRecipes);
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(allRecipes);

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
        Activity context;
        AlertDialog dialog;
        String documentID;

        RecipeViewHolder(View itemView, AlertDialog dialog, Activity context) {
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
        void build(final Recipe recipe, String documentID) {
            this.recipe = recipe;
            this.documentID = documentID;
            TextView recipeName = itemView.findViewById(R.id.recipe_name);
            TextView servingCount = itemView.findViewById(R.id.serving_count);
            recipeName.setText(recipe.getName());
            servingCount.setText(recipe.serveCountText());
        }

        @Override
        public void onClick(View v) {
            //when the list item is clicked, create a dialog to add the meal
            Intent intent = new Intent()
                    .putExtra("type", AddMealDialogBox.RECIPE)
                    .putExtra("name", recipe.getName())
                    .putExtra("vegCount", recipe.getVegCount())
                    .putExtra("proteinCount", recipe.getProteinCount())
                    .putExtra("dairyCount", recipe.getDairyCount())
                    .putExtra("grainCount", recipe.getGrainCount())
                    .putExtra("fruitCount", recipe.getFruitCount())
                    .putExtra("excessServes", recipe.getExcessServes())
                    .putExtra("waterCount", recipe.getWaterCount())
                    .putExtra("caffeineCount", recipe.getCaffeineCount())
                    .putExtra("alcoholStandards", recipe.getAlcoholStandards())
                    .putExtra("cheatScore", recipe.getCheatScore())
                    .putExtra("id", documentID);
            if(recipe.isDrink()) {
                AddDrinkDialogBox.addDrink(context, intent);
            } else {
                AddMealDialogBox.addMeal(context, intent);
            }
            closeRecipeBook();
        }

        public void closeRecipeBook() {
            dialog.dismiss();
        }
    }

    class RecipeSwipeController extends ItemTouchHelper.Callback {

        private Activity activity;
        private RecyclerView recyclerView;
        private Drawable icon;
        private ColorDrawable background;

        public RecipeSwipeController(Activity activity, RecyclerView recyclerView) {
            this.activity = activity;
            this.recyclerView = recyclerView;
            icon = ContextCompat.getDrawable(activity,
                    android.R.drawable.ic_menu_delete);
            background = new ColorDrawable(Color.RED);
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            View itemView = viewHolder.itemView;
            int backgroundCornerOffset = 20;
            int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
            int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
            int iconBottom = iconTop + icon.getIntrinsicHeight();

            if (dX > 0) { // Swiping to the right
                int iconLeft = Ints.min(itemView.getLeft()+(int)dX - icon.getIntrinsicWidth(), itemView.getLeft() + iconMargin);
                int iconRight = Ints.min(itemView.getLeft()+(int)dX, itemView.getLeft() + iconMargin + icon.getIntrinsicWidth());
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                background.setBounds(itemView.getLeft(), itemView.getTop(),
                        itemView.getLeft() + ((int) dX) - backgroundCornerOffset,
                        itemView.getBottom());
                background.draw(c);
                icon.draw(c);
            } else if (dX < 0) { // Swiping to the left
                int iconLeft = Ints.max(itemView.getRight()+(int)dX, itemView.getRight() - iconMargin-icon.getIntrinsicWidth());
                int iconRight = Ints.max(itemView.getRight()+(int)dX +  icon.getIntrinsicWidth(), itemView.getRight() - iconMargin);
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                background.setBounds(itemView.getRight() + ((int) dX) + backgroundCornerOffset,
                        itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);
                icon.draw(c);
            } else { // view is unSwiped
                background.setBounds(0, 0, 0, 0);
            }
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
            new AlertDialog.Builder(activity)
                    .setTitle("Are you sure you want to remove this?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            RecipeBookController.deleteRecipe(((RecipeViewHolder) viewHolder).documentID)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            ((RecipeViewHolder) viewHolder).closeRecipeBook();
                                            RecipeListDialogBox recipeListDialogBox =  new RecipeListDialogBox(activity);
                                            recipeListDialogBox.undoDelete(((RecipeViewHolder) viewHolder).recipe);
                                        }
                                    });
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            recyclerView.getAdapter().notifyDataSetChanged();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    private void undoDelete(final Recipe savedRecipe) {
        Snackbar.make(recipeBookLayout,
                "Deleted Recipe", Snackbar.LENGTH_LONG)
                .setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        savedRecipe.pushToDB();
                        recipeBookDialog.dismiss();
                        RecipeListDialogBox.openRecipeBook(activity);
                    }
                })
                .show();
    }
}
