package cz.tomas.StockAnalyze.ui.widgets;

import cz.tomas.StockAnalyze.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public final class CirclesView extends LinearLayout {

	private static final int CIRCLE_SIZE = 8;
	private static final int CIRCLE_MARGIN = 2;
	 
	private final float SCALE = getContext().getResources().getDisplayMetrics().density;

	private final LayoutParams layoutParams;
	
	private View[] circles;
	private int lastSelected = -1;
	
	public CirclesView(Context context) {
		this(context, null);
	}
	
	public CirclesView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setOrientation(LinearLayout.HORIZONTAL);
		
		if (this.isInEditMode()) {
			this.setCircles(6);
		}

		layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layoutParams.width = (int) (CIRCLE_SIZE * SCALE);
		layoutParams.height = (int) (CIRCLE_SIZE * SCALE);
		layoutParams.leftMargin = (int) (CIRCLE_MARGIN * SCALE);
		layoutParams.rightMargin = (int) (CIRCLE_MARGIN * SCALE);
		layoutParams.topMargin = (int) (CIRCLE_MARGIN * SCALE);
		layoutParams.bottomMargin = (int) (CIRCLE_MARGIN * SCALE);
	}
	
	public void setSelected(int position) {
		if (this.circles == null) {
			throw new NullPointerException("cirlces views are not initialized, have you called setCirlces()?");
		}
		if (position >= this.circles.length) {
			throw new IndexOutOfBoundsException("selected position can't be larger than circles view count " + this.circles.length);
		}
		
		this.circles[position].setSelected(true);
		if (this.lastSelected >= 0 && this.lastSelected < this.circles.length) {
			this.circles[lastSelected].setSelected(false);
		}
		this.lastSelected = position;
	}
	
	public void setCircles(int count) {
		if (count <= 0) {
			this.circles = null;
			return;
		}
		this.removeAllViews();
		View[] circles = new View[count];
		for (int i = 0; i < count; i++) {
			View view = this.createCircleView();
			circles[i] = view;
			this.addView(view);
		}
		this.circles = circles;
		
	}
	
	private View createCircleView() {
		ImageView view = new ImageView(getContext());
		view.setLayoutParams(layoutParams);
		view.setImageResource(R.drawable.circle_selector);
		return view;
		
	}

}
