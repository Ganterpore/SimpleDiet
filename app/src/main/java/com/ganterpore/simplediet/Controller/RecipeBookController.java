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

        return db.collection(Recipe.RECIPES).whereEqualTo("user", user).orderBy("name");
    }
}