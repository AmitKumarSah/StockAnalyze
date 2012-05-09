package cz.tomas.StockAnalyze.ui.widgets;

import android.content.Context;
import android.graphics.*;
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
		void onDragComplete(Object data, DragTarget target);
	}

	public static class DragTarget {

		public final int id;
		public final int color;
		public final int colorActive;
		public final String text;

		private Rect rect;
		private float textHalfWidth;

		public DragTarget(int id, int color, int colorActive, String text) {
			this.id = id;
			this.color = color;
			this.colorActive = colorActive;
			this.text = text;
		}
	}

	private final int BOTTOM_HEIGHT;
	
	private float dragX;
	private float dragY;
	private boolean isDragging;
	private int offsetX;
	private int offsetY;

	private int currentBottomBarHeight;
	private final Handler animHandler;
	
	private Paint targetPaint;
	private Paint bmpPaint;
	private Paint textPaint;
	private Paint borderPaint;

	private Bitmap bmp;

	private IDragListener listener;
	private Object data;
	private Rect bottomRect;

	private DragTarget[] targets;

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

		final float density = getResources().getDisplayMetrics().density;

		this.bmpPaint = new Paint();
		this.bmpPaint.setColor(0xEE000000);
		this.targetPaint = new Paint();
		this.targetPaint.setStyle(Paint.Style.FILL);

		this.borderPaint = new Paint();
		this.borderPaint.setColor(getResources().getColor(R.color.drag_container_border));
		this.borderPaint.setStyle(Paint.Style.STROKE);
		this.borderPaint.setStrokeWidth(1f * density);

		this.textPaint = new TextPaint();
		this.textPaint.setTextSize(18 * density);
		this.textPaint.setColor(Color.BLACK);
		BOTTOM_HEIGHT = (int) (density * 72);
		this.animHandler = new Handler();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		this.bottomRect = new Rect(0, getBottom() - currentBottomBarHeight, getWidth(), getHeight());
		if (this.targets != null) {
			this.prepareTargets();
		}
	}

	public void setListener(IDragListener listener) {
		this.listener = listener;
	}
	
	public void startDragging(View view, int left, int top, int offsetX, int offsetY, Object data, DragTarget... targets) {
		if (view == null) {
			throw new IllegalArgumentException("view cannot be null");
		}
		if (isDragging()) {
			throw new IllegalStateException("can't start dragging because we are already dragging");
		}
		if (targets == null || targets.length == 0) {
			throw new IllegalArgumentException("there must be at least one drag target");
		}
		this.targets = targets;
		this.prepareTargets();
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

	private void prepareTargets() {
		final int step = getWidth() / targets.length;
		int leftX = 0;
		int rightX = step;

		for (DragTarget target : targets) {
			target.textHalfWidth = this.textPaint.measureText(target.text) / 2;

			target.rect = new Rect(leftX, getBottom() - currentBottomBarHeight, rightX, getHeight());
			leftX += step;
			rightX += step;
		}
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

			invalidate();
			return true;
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			if (this.dragY > getBottom() - BOTTOM_HEIGHT && this.listener != null) {
				DragTarget target = null;
				for (DragTarget dragTarget : targets) {
					if (dragTarget.rect.contains((int) this.dragX, (int) this.dragY)) {
						target = dragTarget;
						break;
					}
				}
				this.listener.onDragComplete(this.data, target);
			}
			this.endDragging();
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (isDragging) {
			canvas.drawBitmap(this.bmp, this.dragX - this.bmp.getWidth() / 2, this.dragY - this.bmp.getHeight(), this.bmpPaint);

			for (DragTarget target : targets) {
				final Rect rect = target.rect;
				rect.top = getBottom() - currentBottomBarHeight;
				boolean isActive = rect.contains((int) this.dragX, (int) this.dragY);

				targetPaint.setColor(isActive ? target.colorActive : target.color);
				canvas.drawRect(rect, targetPaint);
				this.textPaint.setFakeBoldText(isActive);
				canvas.drawText(target.text, rect.exactCenterX() - target.textHalfWidth,
						getHeight() - rect.height() / 2 + this.textPaint.getTextSize() / 2, this.textPaint);
			}
			bottomRect.top = getBottom() - currentBottomBarHeight;
			canvas.drawRect(bottomRect, borderPaint);
		}

		super.onDraw(canvas);
	}
}
