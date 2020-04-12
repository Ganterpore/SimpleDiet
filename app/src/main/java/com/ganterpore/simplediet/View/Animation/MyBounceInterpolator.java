package com.ganterpore.simplediet.View.Animation;

import android.view.animation.Interpolator;

public class MyBounceInterpolator implements Interpolator {
    private double mAmplitude = 0.2;
    private double mFrequency = 20;

    public MyBounceInterpolator() {
    }

    public MyBounceInterpolator(double amplitude, double frequency) {
        mAmplitude = amplitude;
        mFrequency = frequency;
    }

    public float getInterpolation(float time) {
        return (float) (-1 * Math.pow(Math.E, -time/ mAmplitude) *
                Math.cos(mFrequency * time) + 1);
    }
}
