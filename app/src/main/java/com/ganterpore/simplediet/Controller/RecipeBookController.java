package com.ganterpore.simplediet.Controller;

import com.ganterpore.simplediet.Model.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class RecipeBookController {


    private static final String TAG = "RecipeBookController";

    public static Query getAllRecipes() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String user = FirebaseAuth.getInstance().getUid();

        return db.collection(Recipe.RECIPES).whereEqualTo("user", user)
                                            .orderBy("drink", Query.Direction.DESCENDING)
                                            .orderBy("name");

    }

    public static Query getMealRecipes() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String user = FirebaseAuth.getInstance().getUid();

        return db.collection(Recipe.RECIPES).whereEqualTo("user", user)
                                            .whereEqualTo("isDrink", false)
                                            .orderBy("name");
    }

    public static Query getDrinkRecipes() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String user = FirebaseAuth.getInstance().getUid();

        return db.collection(Recipe.RECIPES).whereEqualTo("user", user)
                                            .whereEqualTo("isDrink", true)
                                            .orderBy("name");
    }
}