package com.ganterpore.simplediet.View.Activities;

import com.google.firebase.firestore.DocumentReference;

public interface SnackbarReady {
    /**
     * Used to give the user the option to undo a delete when it occurs
     * @param savedObject the recipe that was deleted
     */
    void undoDelete(final Object savedObject);

    /**
     * Used to give the user the option to undo an add when it occurs
     * @param documentReference, the document that was added
     */
    void undoAdd(final DocumentReference documentReference);
}
