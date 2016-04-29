package com.koroshiya.listeners;

import android.view.ScaleGestureDetector;

import com.koroshiya.controls.JImageView;
import com.koroshiya.controls.JScrollView;

public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

    private final JScrollView jsv;

    public ScaleListener(JScrollView jsv) {
        this.jsv = jsv;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {

        JImageView jiv = jsv.getJImageView();
        jiv.zoomRatio(detector.getScaleFactor());
        jiv.invalidate();
        return true;

    }

}
