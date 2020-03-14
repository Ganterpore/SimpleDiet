package com.ganterpore.simplediet.Controller;

import com.ganterpore.simplediet.Model.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import static com.ganterpore.simplediet.Model.Recipe.RECIPES;

public class RecipeBookController {


    private static final String TAG = "RecipeBookController";

    public static Query getAllRecipes() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String user = FirebaseAuth.getInstance().getUid();

        return db.collection(RECIPES).whereEqualTo("user", user)
                                            .orderBy("drink", Query.Direction.DESCENDING)
                                            .orderBy("name");

    }

    public static void deleteRecipe(String id) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String user = FirebaseAuth.getInstance().getUid();
        db.collection(RECIPES).document(id).delete();
    }

    public static Query getMealRecipes() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String user = FirebaseAuth.getInstance().getUid();

        return db.collection(RECIPES).whereEqualTo("user", user)
                                            .whereEqualTo("isDrink", false)
                                            .orderBy("name");
    }

    public static Query getDrinkRecipes() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String user = FirebaseAuth.getInstance().getUid();

        return db.collection(RECIPES).whereEqualTo("user", user)
                                            .whereEqualTo("isDrink", true)
                                            .orderBy("name");
    }
}