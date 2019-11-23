package com.ganterpore.simplediet.View;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ganterpore.simplediet.R;

public class DayHistoryItemView extends android.support.v7.widget.AppCompatTextView {

    private static final int[] STATE_COMPLETED = {R.attr.state_completed};

    private boolean completed = false;

    public DayHistoryItemView(Context context, AttributeSet attrs) {
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
