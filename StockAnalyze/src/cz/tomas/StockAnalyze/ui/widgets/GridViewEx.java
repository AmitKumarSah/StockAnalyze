package cz.tomas.StockAnalyze.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * @author tomas
 */
public class GridViewEx extends GridView {

	private View touchReceiverView;
	
	public GridViewEx(Context context) {
		super(context);
	}

	public GridViewEx(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GridViewEx(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void setTouchReceiver(View v) {
		this.touchReceiverView = v;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		Log.d(Utils.LOG_TAG, "grid touch " + ev);
		if (touchReceiverView != null) {
			return touchReceiverView.onTouchEvent(ev);
		} else {
			return super.dispatchTouchEvent(ev);
		}
	}
}
