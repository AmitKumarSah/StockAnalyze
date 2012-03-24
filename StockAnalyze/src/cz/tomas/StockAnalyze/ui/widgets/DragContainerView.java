package cz.tomas.StockAnalyze.ui.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * @author tomas
 */
public class DragContainerView extends FrameLayout {

	public interface IDragListener {
		void onDragComplete(Object data);
	}

	private final int BOTTOM_HEIGHT;
	
	private float dragX;
	private float dragY;
	private boolean isDragging;
	private int offsetX;
	private int offsetY;

	private int currentBottomBarHeight;
	private final Handler animHandler;
	
	private Paint containerPaint;
	private Paint bmpPaint;
	private Paint textPaint;
	
	private final int colorBottomDefault;
	private final int colorBottomActive;
	
	private final String bottomText;
	private final float textHalfWidth;

	private Bitmap bmp;

	private IDragListener listener;
	private Object data;

	private Runnable updateBottomBarHeightRunnable = new Runnable() {
		@Override
		public void run() {
			currentBottomBarHeight += BOTTOM_HEIGHT / 4;
			if (currentBottomBarHeight > BOTTOM_HEIGHT) {
				currentBottomBarHeight = BOTTOM_HEIGHT;
			} else {
				animHandler.postDelayed(this, 50);
			}
			invalidate();
		}
	};

	@SuppressWarnings(value = "unused")
	public DragContainerView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DragContainerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		this.colorBottomDefault = 0xBBFFFFFA;
		this.colorBottomActive = 0xDDFFFFFA;

		this.bmpPaint = new Paint();
		this.bmpPaint.setColor(0xEE000000);
		this.containerPaint = new Paint();
		this.containerPaint.setColor(colorBottomDefault);
		
		this.textPaint = new TextPaint();
		this.textPaint.setTextSize(18 * getResources().getDisplayMetrics().density);
		this.textPaint.setColor(Color.BLACK);
		BOTTOM_HEIGHT = (int) (getResources().getDisplayMetrics().density * 72);
		
		this.bottomText = context.getString(R.string.homeMyPortfolio);
		this.textHalfWidth = this.textPaint.measureText(bottomText) / 2;

		this.animHandler = new Handler();
	}

	public void setListener(IDragListener listener) {
		this.listener = listener;
	}
	
	public void startDragging(View view, int left, int top, int offsetX, int offsetY, Object data) {
		if (view == null) {
			throw new IllegalArgumentException("view cannot be null");
		}
		if (isDragging()) {
			throw new IllegalStateException("can't start dragging because we are already dragging");
		}
		this.data = data;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.dragX = left;
		this.dragY = top;
		this.setVisibility(View.VISIBLE);
		this.isDragging = true;

		bmp = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		view.draw(canvas);
		this.animHandler.post(this.updateBottomBarHeightRunnable);

		if (Utils.DEBUG) Log.d(Utils.LOG_TAG, String.format("start drag from %d, %d; Offset: %d,%d", left, top, offsetX, offsetY));
	}
	
	private void endDragging() {
		this.setVisibility(View.GONE);
		this.isDragging = false;
		this.currentBottomBarHeight = 0;
		if (Utils.DEBUG) Log.d(Utils.LOG_TAG, "end drag");
	}
	
	public boolean isDragging() {
		return  this.isDragging;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			this.dragX = event.getX() - offsetX;
			this.dragY = event.getY() - offsetY;

			if (dragY > getBottom() - BOTTOM_HEIGHT) {
				this.containerPaint.setColor(colorBottomActive);
			} else {
				this.containerPaint.setColor(colorBottomDefault);
			}
			invalidate();
			return true;
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			if (this.dragY > getBottom() - BOTTOM_HEIGHT && this.listener != null) {
				this.listener.onDragComplete(this.data);
			}
			this.endDragging();
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (isDragging) {
			//canvas.drawRect(this.dragX, this.dragY, this.dragX + OBJECT_HEIGHT, this.dragY + OBJECT_HEIGHT, this.containerPaint);
			canvas.drawBitmap(this.bmp, this.dragX - this.bmp.getWidth() / 2, this.dragY - this.bmp.getHeight(), this.bmpPaint);

			canvas.drawRect(0, getBottom() - currentBottomBarHeight, getWidth(), getHeight(), containerPaint);
			canvas.drawText(this.bottomText, getWidth() / 2 - this.textHalfWidth,
					this.getHeight() - currentBottomBarHeight / 2 + this.textPaint.getTextSize() / 2, this.textPaint);
		}

		super.onDraw(canvas);
	}
}
