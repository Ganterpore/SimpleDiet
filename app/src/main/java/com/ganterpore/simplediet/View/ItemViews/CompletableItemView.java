package com.ganterpore.simplediet.View.ItemViews;

import android.content.Context;
import android.util.AttributeSet;

import com.ganterpore.simplediet.R;

/**
 * Represents a View object that can be in the state of either complete or incomplete.
 */
public class CompletableItemView extends androidx.appcompat.widget.AppCompatTextView {

    private static final int[] STATE_COMPLETED = {R.attr.state_completed};

    private boolean completed = false;

    public CompletableItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        refreshDrawableState();
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if(completed) {
            mergeDrawableStates(drawableState, STATE_COMPLETED);
        }
        return drawableState;
    }
}
