package com.ganterpore.simplediet.View.DialogBoxes;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;


public class UpdateDietDialogPreference extends DialogPreference {
    private Context context;

    public UpdateDietDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    protected void onClick() {
        switch (getKey()) {
            case "update_diet":
                UpdateDietDialogBox.updateDiet(context);
                break;
            case "update_hydration":
                UpdateDrinkDietPlanDialogBox.updateDiet(context);
                break;
            case "update_cheat_points":
                UpdateCheatsDialogBox.updateDiet(context);
                break;
        }

    }
}