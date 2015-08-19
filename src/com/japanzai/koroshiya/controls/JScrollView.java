package com.japanzai.koroshiya.controls;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.reader.MainActivity;
import com.japanzai.koroshiya.reader.Reader;
import com.japanzai.koroshiya.reader.ToastThread;
import com.japanzai.koroshiya.settings.SettingsManager;

/**
 * Purpose: Turns a ScrollView into a scrollable ImageView.
 * */
public class JScrollView extends TwoDScrollView {

	private final JImageView view;
	private final GestureDetector gestureDetector;
	private final GestureListener gestureListener;
	private float currentx, currenty;
	private float startx, starty;
	private int pageStartx;
	
	private boolean lastDown = true;

	public JScrollView(Context context) {
		super(context);
		view = new JImageView(context);
		gestureListener = new GestureListener();
		gestureDetector = new GestureDetector(context, gestureListener);
		//gestureDetector.setOnLongClickListener(gestureListener);
		this.addView(view);
		initialize();
		
	}
	
	
	public GestureDetector getGestureDetector(){
		return this.gestureDetector;
	}

	public void move(float x, float y) {
		int totalx = (int) (currentx - x);
		int totaly = (int) (currenty - y);
		this.scrollBy(totalx, totaly);
		down(x, y);
	}

	public void down(float x, float y) {
		currentx = x;
		currenty = y;
	}
	
	private Reader getReader(){
		return (Reader) view.getContext();
	}

	public void up(double x2, double y2) {

		if (!getReader().isAlertShowing()) {
			if (pageStartx == getRight() || pageStartx == view.getWidth() - getRight() || getRight() >= view.getWidth()) {
				if ((Math.abs(startx - x2) > Math.abs(starty - y2))) {
					if (startx > x2) {
						getReader().getCache().next();
					} else if (pageStartx == getLeft()) { //In case getLeft and getRight are the same (ie. no horizontal scroll)
						getReader().getCache().previous();
					}
				}
			} else if (pageStartx == getLeft()) {
				if ((Math.abs(startx - x2) > Math.abs(starty - y2)) && startx < x2) {
					getReader().getCache().previous();
				}
			} else {
				scroll(x2, y2);
			}
		}
		
	}

	public void scroll(double x2, double y2) {

		int totalx = (int) (currentx - x2);
		int totaly = (int) (currenty - y2);
		this.scrollBy(totalx, totaly);
		
	}

	/**
	 * Sets up JScrollView for use
	 * */
	private void initialize() {
		setOnTouchListener(gestureListener);
	}

    @SuppressLint("NewApi")
	private class GestureListener extends GestureDetector.SimpleOnGestureListener implements OnTouchListener{

		private final ScaleGestureDetector sgd = sgdSupported() ? new ScaleGestureDetector(getContext(), new ScaleListener()) : null;

		private boolean sgdSupported(){
			return android.os.Build.VERSION.SDK_INT >= 8;
		}
		
		private void cycleZoom(MainActivity parent, SettingsManager settings){
			double defaultZoom = settings.getCurrentZoomRatio();
			double curZoom = view.getZoom();
			double newIndex;
			
			String[] zooms = getResources().getStringArray(R.array.general_setting_default_zoom);
			String message;
			if (curZoom == SettingsManager.AUTO_SIZE){
				newIndex = SettingsManager.getZoomRatio(5);
				message = zooms[5];
			}else if (curZoom == SettingsManager.FULL_SIZE){
				if (defaultZoom == SettingsManager.FULL_SIZE){
					newIndex = SettingsManager.getZoomRatio(0);
					message = zooms[0];
				}else{
					newIndex = defaultZoom;
					message = zooms[parent.getSettings().getZoom()];
				}
			}else{
				newIndex = SettingsManager.getZoomRatio(0);
				message = zooms[0];
			}
			
			parent.runOnUiThread(new ToastThread(message + " " + parent.getString(R.string.string_zoom), parent));
			view.zoom(newIndex);
		}
		
		private void zoom(MainActivity parent, boolean zoomIn){
			int curZoom = SettingsManager.getZoomIndex(view.getZoom());
			String[] zooms = getResources().getStringArray(R.array.general_setting_default_zoom);
			if (zoomIn){
				curZoom = curZoom < zooms.length - 1 ? curZoom + 1 : 0;
			}else{
				curZoom = curZoom > 0 ? curZoom - 1 : zooms.length - 1;
			}
			String message = zooms[curZoom];
			parent.runOnUiThread(new ToastThread(message + " " + parent.getString(R.string.string_zoom), parent));
			view.zoom(SettingsManager.getZoomRatio(curZoom));
		}
		
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			
			MainActivity parent = MainActivity.getMainActivity();
			SettingsManager settings = parent.getSettings();
			int doubleTapIndex = settings.getDoubleTapIndex();
			
			if (doubleTapIndex == 0){
				cycleZoom(parent, settings);
			}else if (doubleTapIndex == 1){
				zoom(parent, true);
			}else if (doubleTapIndex == 2){
				zoom(parent, false);
			}
			
			return false;
		}

        @SuppressLint("NewApi")
		@Override
		public boolean onDown(MotionEvent e){
			if (e.getPointerCount() > 1){
				lastDown = false;
				if (sgdSupported()) sgd.onTouchEvent(e);
			}else{
				lastDown = true;
				down(e.getX(), e.getY());
				startx = e.getX();
				starty = e.getY();
				pageStartx = getScrollX();
			}
			return true;
		}
		
		@SuppressLint("NewApi")
        @Override
		public boolean onTouch(View v, MotionEvent event) {

			int action = event.getAction();
			if (event.getPointerCount() > 1){
				lastDown = false;
                if (sgdSupported()) sgd.onTouchEvent(event);
			}else {
				
				if (lastDown){ 
					if (action == MotionEvent.ACTION_MOVE) {
						move(event.getX(), event.getY());
					} else if (action == MotionEvent.ACTION_UP) {
						up(event.getX(), event.getY());
					}
				}
				
				if (!getGestureDetector().onTouchEvent(event)) {
					onTouchEvent(event);
				}
			}
				
			return true;
	
		}
		
		@Override
	     public void onLongPress(MotionEvent e) {          
			if (getReader().getSettings().isContextMenuEnabled()){
				getReader().showContextMenu();
			}
	    }
		
	}
	
	@TargetApi(Build.VERSION_CODES.FROYO)
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
		public boolean onScale(ScaleGestureDetector detector) {
			
			view.zoomRatio(detector.getScaleFactor());
			invalidate();
			return true;
			
		}
		
	}

	/**
	 * @param drawable
	 *            Object to display on the JImageView this object contains
	 * */
	public void setImageDrawable(Drawable drawable) {
		
		view.setImageDrawable(drawable);
		this.scrollTo(0, 0);
		
	}

	/**
	 * @return JBitmapDrawable currently displayed
	 * */
	public JBitmapDrawable getImageDrawable() {

		return (JBitmapDrawable) this.view.getDrawable();

	}
	
	@SuppressLint("DrawAllocation")
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			
        int viewWidth = view.getMeasuredWidth();
        int viewHeight = view.getMeasuredHeight();
        int scrollWidth = this.getMeasuredWidth();
        int scrollHeight = this.getMeasuredHeight();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(viewWidth,viewHeight);

        params.leftMargin = (scrollWidth > viewWidth) ? (scrollWidth - viewWidth) / 2 : 0;
        params.topMargin = (scrollHeight > viewHeight) ? (scrollHeight - viewHeight) / 2 : 0;

        view.setLayoutParams(params);
			

	}
	
}
