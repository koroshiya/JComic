package com.koroshiya.listeners;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.koroshiya.controls.JImageView;
import com.koroshiya.controls.JScrollView;
import com.koroshiya.fragments.ReadFragment;

public class GestureListener extends GestureDetector.SimpleOnGestureListener implements View.OnTouchListener {

    private final JScrollView jsv;
    private final ScaleGestureDetector sgd;
    private final GestureDetector gd;
    private final ReadFragment readFragment;

    private float startx, starty;
    private float currentx = 0, currenty = 0;
    private int pageStartx;
    private boolean lastDown = true;

    public GestureListener(JScrollView jsv, ReadFragment readFragment){
        this.jsv = jsv;
        Context c = jsv.getContext();
        this.sgd = new ScaleGestureDetector(c, new ScaleListener(jsv));
        this.gd = new GestureDetector(c, this);
        this.readFragment = readFragment;
    }

    @Override
    public boolean onDown(MotionEvent e){
        if (e.getPointerCount() > 1){
            lastDown = false;
            sgd.onTouchEvent(e);
        }else{
            lastDown = true;
            startx = currentx = e.getX();
            starty = currenty = e.getY();
            pageStartx = jsv.getScrollX();
        }
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int action = event.getAction();
        if (event.getPointerCount() > 1){
            Log.i("GL", "Two fingers");
            lastDown = false;
            sgd.onTouchEvent(event);
        }else {

            if (lastDown){
                if (action == MotionEvent.ACTION_MOVE) {
                    Log.i("GL", "Moving curX: "+currentx+", getX: "+event.getX());
                    Log.i("GL", "Moving curY: "+currenty+", getY: "+event.getY());
                    int x = (int)(currentx - event.getX());
                    int y = (int)(currenty - event.getY());

                    jsv.scrollBy(x, y);

                    currentx -= x;
                    currenty -= y;

                } else if (action == MotionEvent.ACTION_UP) {
                    Log.i("GL", "Up");
                    up(event.getX(), event.getY());
                }
            }

            if (!gd.onTouchEvent(event)) {
                jsv.onTouchEvent(event);
            }
        }

        return true;

    }

    public void up(double x2, double y2) {

        JImageView jiv = jsv.getJImageView();
        if (pageStartx == jsv.getRight() || pageStartx == jiv.getWidth() - jsv.getRight() || jsv.getRight() >= jiv.getWidth()) {
            Log.i("GL", "Right");
            if ((Math.abs(startx - x2) > Math.abs(starty - y2))) {
                Log.i("GL", "Moving");
                if (startx > x2) {
                    readFragment.next(jsv);
                } else if (pageStartx == jsv.getLeft()) { //In case getLeft and getRight are the same (ie. no horizontal scroll)
                    readFragment.previous(jsv);
                }
            }
        } else if (pageStartx == jsv.getLeft()) {
            Log.i("GL", "Left");
            if ((Math.abs(startx - x2) > Math.abs(starty - y2)) && startx < x2) {
                readFragment.previous(jsv);
            }
        } else {
            Log.i("GL", "Scroll");
            currentx -= x2;
            currenty -= y2;
            jsv.scrollBy((int)currentx, (int)currenty);

            currentx = jsv.getScrollX();
            currenty = jsv.getScrollY();
        }

    }

}
