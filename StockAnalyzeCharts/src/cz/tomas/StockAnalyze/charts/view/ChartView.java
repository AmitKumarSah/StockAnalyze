/*******************************************************************************
 * StockAnalyze for Android
 *     Copyright (C)  2011 Tomas Vondracek.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
/**
 * 
 */
package cz.tomas.StockAnalyze.charts.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import cz.tomas.StockAnalyze.charts.R;
import cz.tomas.StockAnalyze.charts.Utils;
import cz.tomas.StockAnalyze.charts.interfaces.IChartTextFormatter;

/**
 * @author tomas
 *
 */
public class ChartView extends View {

	private static final boolean DEBUG = false;

	/**
	 *  Convert the dps to pixels
	 */
	private final float SCALE = getContext().getResources().getDisplayMetrics().density;
	/**
	 * offset for whole chart (padding)
	 */
	private final float OFFSET = 14 * SCALE;
	
	/**
	 * pixels between axis text and axis itself
	 */
	private final int AXIS_TEXT_PADDING = (int) (4 * SCALE);
	
	private final int GRID_HORIZONTAL_LINES = 5;
	private final int GRID_VERTICAL_LINES = 5;
	
	private final float MOVE_TRESHOLD = 8 * SCALE;
	
	private byte STATE_IN_CLICK = 1;
	private byte STATE_IN_MOVE = 4;
	//private byte STATE_IN_PATH = 8;
	
	private float[] data;
	private float[] preparedData;
	
	private Number[] axisX;
	private IChartTextFormatter<Number> formatter;
	
	private Path currentPaintingPath;
	
	/**
	 * data maximum value
	 */
	private float max;
	
	/**
	 * data minimum value
	 */
	private float min;
	
	private final Paint paint;
	private final Paint paintingPaint;
	private final Paint chartPaint;
	private final Paint gridPaint;
	private final Paint gridFillPaint;
	private final TextPaint textPaint;
	
	private Bitmap chartBitmap;
	private boolean isChartBitmapDirty;
	
	private boolean disableRedraw = false;
	private boolean drawTracking = false;
	private boolean drawPainting = false;
	
	private float trackingValueX = -1;
	private float lastTrackingValue = -1;
	private float lastPaintingX = -1;
	private float lastPaintingY = -1;
	
	private byte touchState;
	
	private final Bitmap.Config bmpConfig;
	
	public ChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.paint = new Paint();
		this.paint.setStrokeWidth(2*SCALE);
		this.paint.setColor(getResources().getColor(R.color.chartGrid));
		this.paint.setAntiAlias(true);
		
		this.paintingPaint = new Paint(paint);
		this.paintingPaint.setColor(getResources().getColor(R.color.chartPainting));
		this.paintingPaint.setStyle(Style.STROKE);
		this.paintingPaint.setStrokeWidth(2*SCALE);
		
		this.chartPaint = new Paint(this.paint);
		this.chartPaint.setColor(getResources().getColor(R.color.chartLine));
		this.gridPaint = new Paint(this.paint);
		this.gridPaint.setStrokeWidth(0.4f*SCALE);
		this.gridPaint.setPathEffect(new DashPathEffect( new float[] {5f,5f}, 5f));
		
		this.textPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
		this.textPaint.setTypeface(Typeface.DEFAULT_BOLD);
		this.textPaint.setTextAlign(Align.LEFT);
		this.textPaint.setTextSize(SCALE * 8f);
		this.textPaint.setColor(getResources().getColor(R.color.chartPainting));
		
		this.gridFillPaint = new Paint();
		this.gridFillPaint.setStyle(Style.FILL_AND_STROKE);
		this.gridFillPaint.setColor(getResources().getColor(R.color.chartFill));
		
		// detect pixel format of our window
		final WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		final int format = manager.getDefaultDisplay().getPixelFormat();
		if (format == PixelFormat.RGBA_4444) {
			bmpConfig = Config.ARGB_4444;
		} else {
			bmpConfig = Config.ARGB_8888;
		}
	}

	/**
	 * @param disableRedraw the disableRedraw to set
	 */
	void setDisableRedraw(boolean disableRedraw) {
		this.disableRedraw = disableRedraw;
	}


	/*
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		if (this.disableRedraw) {
			return;
		}
		int offsetBelowXAxis, offsetNextToYAxis;									// offset caused by text descriptions of axis

		offsetBelowXAxis = calculateXAxisDescriptionOffset();
		offsetNextToYAxis = calculateYAxisDescriptionOffset();
		
		float originX = OFFSET + offsetNextToYAxis;								// x coord where the chart starts
		float originY = this.getHeight() - OFFSET - offsetBelowXAxis;			// y coord where the chart starts
		// originX & originY give us the start point of the chart,
		// it is the lower left corner with value (0,0)
		
		final float chartWidth = this.getWidth() - 2 * OFFSET -offsetNextToYAxis ;
		final float chartHeight = this.getHeight() - 2 * OFFSET - offsetBelowXAxis;
		
		if (this.chartBitmap == null || this.isChartBitmapDirty) {
			if (this.chartBitmap == null) {
				// cache bitmap with full chart
				this.chartBitmap = Bitmap.createBitmap(getWidth(), getHeight(), this.bmpConfig);
			} else {
				this.chartBitmap.eraseColor(Color.TRANSPARENT);
				this.isChartBitmapDirty = false;
			}
			final Canvas cacheCanvas = new Canvas(this.chartBitmap);
			drawAxis(cacheCanvas, originX, originY, chartWidth);
			drawAxisDescription(cacheCanvas, chartWidth, chartHeight, originX, originY);
			
			drawGrid(cacheCanvas,originX, originY, chartWidth, chartHeight);
			
			if (this.data != null && this.data.length > 1) {
				this.drawData(cacheCanvas, originX, originY, chartWidth, chartHeight);
			}
		}

		canvas.drawBitmap(this.chartBitmap, 0, 0, this.chartPaint);
		
		if (this.data != null && this.data.length > 1) {
			// tracking and paintings go right to the canvas
			if (this.drawTracking) {
				this.drawTracking(canvas, originX, originY, chartWidth, chartHeight);
			}
			if (this.drawPainting) {
				this.drawPaintingPaths(canvas);
			}
		}
	}

	private void drawPaintingPaths(Canvas canvas) {
		if (this.currentPaintingPath != null && ! this.currentPaintingPath.isEmpty()) {
			canvas.drawPath(this.currentPaintingPath, this.paintingPaint);
		}
	}

	private void drawTracking(Canvas canvas, float originX, float originY, float chartWidth, float chartHeight) {
		if (this.trackingValueX > originX && this.trackingValueX - originX < chartWidth && this.preparedData != null) {
			// vertical line
			canvas.drawLine(this.trackingValueX, OFFSET, this.trackingValueX, originY, this.paintingPaint);
			
			final float step = chartWidth / (float) (this.data.length -1);
			final int index = (int) ((this.trackingValueX - originX) / step);
			final float yValue = chartHeight - preparedData[index] + OFFSET;
			// horizontal line
			canvas.drawLine(originX, yValue, this.getWidth() - OFFSET, yValue, this.paintingPaint);
			
			final String value = String.valueOf(this.data[index]);
			final String text = String.format("%s - %s", getFormattedValue(this.axisX[index]), value);
			if (this.trackingValueX - originX < chartWidth / 2) {
				// draw text on right to the line
				canvas.drawText(text, this.trackingValueX + AXIS_TEXT_PADDING, 
						yValue - AXIS_TEXT_PADDING, this.textPaint);
			} else {
				// draw text to left of the line
				float textWidth = textPaint.measureText(text);
				canvas.drawText(text, this.trackingValueX - AXIS_TEXT_PADDING - textWidth,
						yValue - AXIS_TEXT_PADDING, this.textPaint);
			}
		}
	}


	private int calculateYAxisDescriptionOffset() {
		if (this.data != null && this.data.length > 0) {
			float startWidth = this.textPaint.measureText(String.valueOf(this.data[0]));
			float endWidth = this.textPaint.measureText(String.valueOf(this.data[this.data.length - 1]));
			
			return (int) (Math.max(startWidth, endWidth) + 0.5f) + AXIS_TEXT_PADDING;
		}
		return 4;
	}

	private int calculateXAxisDescriptionOffset() {
		if (this.axisX != null && this.axisX.length > 0) {
			return (int) (this.textPaint.getTextSize() + AXIS_TEXT_PADDING);
		}
		return 4;
	}


	private void drawGrid(Canvas canvas, float originX, float originY,
			float chartWidth, float chartHeight) {
		float stepY = chartHeight / GRID_HORIZONTAL_LINES;
		float stepX = chartWidth / GRID_VERTICAL_LINES;
		
		// draw horizontal lines
		for (int i = 1; i < GRID_HORIZONTAL_LINES; i++) {
			float posY =  originY - stepY * i;
			canvas.drawLine(originX, posY, originX + chartWidth, posY, this.gridPaint);
		}
		
		// draw vertical lines on x axis
		for (int i = 1; i < GRID_VERTICAL_LINES; i++) {
			float posX = originX + stepX * i;
			canvas.drawLine(posX, originY, posX, originY - chartHeight, this.gridPaint);
		}
	}

	private void drawData(Canvas canvas, float originX,
			float originY, float chartWidth, float chartHeight) {

		final float step = chartWidth / (float) (this.data.length -1);
		if (this.preparedData == null || this.preparedData.length == 0)
			this.preparedData = prepareDataValues(chartHeight);
		// 
		// for one line we need 4 points
		// startX, startY, stopX, stopY
		//
		final float[] points = new float[this.data.length * 4];
//		Log.d(Utils.LOG_TAG, "drawing chart data " + this.data.length + " with step " + step + " in chart width " + chartWidth + 
//				" from origin " + originX + "; " + originY);
		// first value
		points[0] = originX;
		points[1] = chartHeight - preparedData[0] + OFFSET;
		points[2] = originX;
		points[3] = chartHeight - preparedData[0] + OFFSET;

		final Path path = new Path();
		path.moveTo(originX, originX);
		path.lineTo(points[0], points[1]);
		
		for (int i = 1; i < data.length; i++) {
			final float value = preparedData[i];

			final float x = step * (float)i + originX;
			final float y = chartHeight - value + OFFSET;
			
			points[i * 4] = points[i * 4 - 2];
			points[i * 4 + 1] = points[i * 4 - 1];
			
			points[i * 4 + 2] = x;
			points[i * 4 + 3] = y;
		
			path.lineTo(x, y);
		}
		path.lineTo(originX + chartWidth, originY);
		path.lineTo(originX, originY);
		
		canvas.drawLines(points, this.chartPaint);
		if (this.gridFillPaint.getShader() == null) {
			int color = this.gridFillPaint.getColor();
			int opaque = color & 0x92ffffff;
			int transparent = color & 0x10ffffff;
			
			this.gridFillPaint.setShader(new LinearGradient(0, 0, 0, getHeight(), opaque, transparent, TileMode.CLAMP));
		}
		canvas.drawPath(path, this.gridFillPaint);

		//Log.d(Utils.LOG_TAG, "finished drawing chart, last point: " + (step*(data.length -1)));
	}

	private float[] prepareDataValues(float chartHeight) {
		if (DEBUG) Log.d(Utils.LOG_TAG, "preparing chart data..");
		float[] preparedData = new float[this.data.length];
		//StringBuilder builder = new StringBuilder("Prepared Data: ");
		
		for (int i = 0; i < this.data.length; i++) {
			preparedData[i] = this.scaleRange(this.data[i], this.min, this.max, 0, chartHeight);
			//builder.append(String.valueOf(preparedData[i]) + "; ");
		}
		//Log.d(Utils.LOG_TAG, builder.toString());
		return preparedData;
	}
	
	private float scaleRange(float in, float oldMin, float oldMax, float newMin, float newMax)
	{
		return ( ((newMax - newMin) * (in - oldMin)) / (oldMax - oldMin) ) + newMin;
	}
	
//    		(b-a)(x - min)
//    f(x) = --------------  + a
//              max - min

	private void drawAxis(Canvas canvas, float originX, float originY, float chartWidth) {
		// the lines are crossing with overlap = offset/2
		// draw x axis
		canvas.drawLine(OFFSET/2, originY, originX + chartWidth, originY, this.paint);
		
		// draw y axis
		canvas.drawLine(originX, this.getHeight() - OFFSET/2, originX, 0 + OFFSET, this.paint);
	}

	private void drawAxisDescription(Canvas canvas, float chartWidth, float chartHeight, 
			float originX, float originY) {
		// description next to y axis
		final float textSize = this.textPaint.getTextSize();
		if (this.data != null && this.data.length > 0) {
			// bottom text is right above x=0 value to the left of y axis
			canvas.drawText(String.valueOf(this.min), OFFSET, 
					originY - textSize, this.textPaint);
			
			final String lastTickText = String.valueOf(max);
			canvas.drawText(lastTickText, OFFSET, OFFSET + textSize,
					this.textPaint);
		}

		// description under the x axis
		if (this.axisX != null && this.axisX.length > 0) {
			String text = this.getFormattedValue(this.axisX[0]);
			canvas.drawText(text, originX + AXIS_TEXT_PADDING, 
					originY + textSize + AXIS_TEXT_PADDING, this.textPaint);
			
			text = this.getFormattedValue(this.axisX[this.axisX.length - 1]);
			float textWidth = this.textPaint.measureText(text);
			canvas.drawText(text, originX + chartWidth - textWidth, 
					originY + textSize + AXIS_TEXT_PADDING, this.textPaint);
		}
	}
	
	private <T extends Number> String getFormattedValue(T val) {
		if (this.formatter != null)
			return this.formatter.formatAxeText(val);
		else
			return val.toString();
	}
	
	void setEnableTracking(boolean enabled) {
		this.drawTracking = enabled;
	}

	void setEnablePainting(boolean enabled) {
		this.drawPainting = enabled;
		if (! enabled && this.currentPaintingPath != null) {
			this.currentPaintingPath.reset();
		}
	}
	
	void setData(float[] data, float max, float min) {
		this.max = max;
		this.min = min;
		this.data = data;
		this.preparedData = null;
		this.isChartBitmapDirty = true;
		this.trackingValueX = 0;
		
		this.postInvalidate();
		if (DEBUG) {
			StringBuilder builder = new StringBuilder("Chart Data: ");
			for (int i = 0; i < data.length; i++) {
				builder.append(String.valueOf(data[i]) + "; ");
			}
			Log.d(Utils.LOG_TAG, builder.toString());
		}
	}
	
	@SuppressWarnings("unchecked")
	<T extends Number>void setAxisX(T[] xAxisPoints, IChartTextFormatter<T> formatter) {
		this.axisX = xAxisPoints;
		this.formatter = (IChartTextFormatter<Number>) formatter;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final float x = event.getX();
		if (this.preparedData != null) {
			if (this.drawTracking && this.trackingValueX != x) {
				return onTrackingTouch(event);
			} else if (this.drawPainting) {
				return onPaintingTouch(event);
			}
		}
		return super.onTouchEvent(event);
	}

	private boolean onPaintingTouch(MotionEvent event) {
		final float x = event.getX();
		final float y = event.getY();
		
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (this.currentPaintingPath == null) {
				this.currentPaintingPath = new Path();
			}
			this.currentPaintingPath.moveTo(x, y);
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			final float dx = Math.abs(x - this.lastPaintingX);
			final float dy = Math.abs(y - this.lastPaintingY);
			if (dx > MOVE_TRESHOLD || dy > MOVE_TRESHOLD) {
				this.currentPaintingPath.lineTo(x, y);
				this.lastPaintingX = x;
				this.lastPaintingY = y;
			}
			this.invalidate();
		} else if (event.getAction() == MotionEvent.ACTION_UP &&
				! this.currentPaintingPath.isEmpty()) {
			//this.paintingPaths.add(this.currentPaintingPath);
		}
		return true;
	}

	protected boolean onTrackingTouch(final MotionEvent event) {
		final float x = event.getX();
		final float moveLength = x - this.lastTrackingValue;
		
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			this.touchState = STATE_IN_CLICK;
			if (trackingValueX < 0) {
				// initial touch
				this.trackingValueX = x;
				if (DEBUG) Log.d(Utils.LOG_TAG, "chart initial touch");
			}
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			this.trackingValueX += moveLength;
			if (Math.abs(moveLength) > MOVE_TRESHOLD) {
				this.touchState &= ~STATE_IN_CLICK;
				this.touchState |= STATE_IN_MOVE;
			}
			if (DEBUG) Log.d(Utils.LOG_TAG, "chart move touch " + moveLength + " state " + this.touchState);
		} else if (event.getAction() == MotionEvent.ACTION_UP && 
				(this.touchState & STATE_IN_CLICK) == STATE_IN_CLICK) {
			this.trackingValueX = x;
			if (DEBUG) Log.d(Utils.LOG_TAG, "chart click touch " + this.touchState);
		}
		this.lastTrackingValue = x;
		this.invalidate();
		return true;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (this.chartBitmap != null) {
			this.chartBitmap.recycle();
			this.chartBitmap = null;
		}
	}

	public void clear() {
		if (this.currentPaintingPath != null) {
			this.currentPaintingPath.reset();
		}
		this.lastTrackingValue = -1;
		this.trackingValueX = -1;
		this.invalidate();
	}

	public boolean isDataLoaded() {
		return this.data != null;
	}
}