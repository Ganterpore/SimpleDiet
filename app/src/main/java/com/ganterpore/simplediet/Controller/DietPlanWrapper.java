package com.ganterpore.simplediet.Controller;

import android.support.annotation.NonNull;

import com.ganterpore.simplediet.Model.DietPlan;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DietPlanWrapper {
    private List<DietPlanInterface> listeners;
    private DietPlan dietPlan;

    /**
     * Get the dietPlan plan from the database with the given userID
     * @param listener, the listener to be informed when data is collected
     * @param user, the userid to search for
     */
    public DietPlanWrapper(DietPlanInterface listener, String user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        listeners = new ArrayList<>();
        addListener(listener);
        //temporary diet plan wjilst loading
        dietPlan = new DietPlan(0,0,0,0,0,0,0,"");
        db.collection(DietPlan.COLLECTION_NAME).document(user).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        dietPlan = documentSnapshot.toObject(DietPlan.class);
                        updateListeners();
                    }
                });
    }

    /**
     * Updates the dietPlan plan in the database and object to the one that is given
     * @param newDietPlan, the new dietPlan plan to ceate
     * @return the task of the database update job
     */
    public Task<Void> updateDietPlan(final DietPlan newDietPlan) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Task<Void> updateDiet = db.collection(DietPlan.COLLECTION_NAME)
                .document(newDietPlan.getUser())
                .set(newDietPlan);
        updateDiet.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                dietPlan = newDietPlan;
                updateListeners();
            }
        });
        return updateDiet;
    }

    public DietPlan getDietPlan() {
        return dietPlan;
    }

    public interface DietPlanInterface {
        void updateDietPlan(DietPlan diet);
    }

    private void addListener(DietPlanInterface listener) {
        listeners.add(listener);
    }

    private void updateListeners() {
        for(DietPlanInterface listener : listeners) {
            listener.updateDietPlan(dietPlan);
        }
    }

}
