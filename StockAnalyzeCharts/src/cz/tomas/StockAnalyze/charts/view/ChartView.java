/**
 * 
 */
package cz.tomas.StockAnalyze.charts.view;

import cz.tomas.StockAnalyze.charts.Utils;
import cz.tomas.StockAnalyze.charts.interfaces.IChartTextFormatter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * @author tomas
 *
 */
public class ChartView<T> extends View {

	private float[] data;
	private float[] preparedData;
	
	private T[] axisX;
	private IChartTextFormatter<T> formatter;
	
	private float max;
	private float min;
	
	private Paint paint;
	private Paint chartPaint;
	private TextPaint textPaint;
	
	/**
	 *  Convert the dps to pixels
	 */
	private final float SCALE = getContext().getResources().getDisplayMetrics().density;
	/**
	 * offset for whole chart (padding)
	 */
	private final int OFFSET = 8;
	
	/**
	 * pixels between axis text and axis itself
	 */
	private final int AXIS_TEXT_PADDING = 2;
	
	public ChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.paint = new Paint();
		this.paint.setStrokeWidth(2*SCALE);
		this.paint.setColor(Color.BLUE);
		this.paint.setAntiAlias(true);
		
		this.chartPaint = new Paint(this.paint);
		this.chartPaint.setColor(Color.GREEN);
		this.textPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
		this.textPaint.setTypeface(Typeface.DEFAULT_BOLD);
		this.textPaint.setTextAlign(Align.LEFT);
	}


	/*
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		//super.onDraw(canvas);
		int offsetBelowXAxis, offsetNextToYAxis;									// offset caused by text descriptions of axis

		offsetBelowXAxis = calculateXAxisDescriptionOffset();
		offsetNextToYAxis = calculateYAxisDescriptionOffset();
		
		float originX = OFFSET + offsetNextToYAxis;								// x coord where the chart starts
		float originY = this.getHeight() - OFFSET - offsetBelowXAxis;			// y coord where the chart starts
		// originX & originY give us the start point of the chart,
		// it is the lower left corner
		
		float chartWidth = this.getWidth() - 2 * OFFSET - offsetBelowXAxis;
		float chartHeight = this.getHeight() - 2 * OFFSET - offsetNextToYAxis;
		
		drawAxis(canvas, OFFSET, originX, originY, chartWidth);
		drawAxisDescription(canvas, offsetBelowXAxis, offsetNextToYAxis, chartWidth, chartHeight);
		
		if (this.data != null && this.data.length > 1)
			this.drawData(canvas, OFFSET, originX, originY, chartWidth, chartHeight);
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
			return (int) (2 * this.textPaint.getTextSize() + AXIS_TEXT_PADDING);
		}
		return 4;
	}


	private void drawData(Canvas canvas, float offset, float originX,
			float originY, float chartWidth, float chartHeight) {
		
		float step = chartWidth / this.data.length;
		if (this.preparedData == null || this.preparedData.length == 0)
			this.preparedData = prepareDataValues(chartHeight);
		// for one line we need 4 points
		// startX, startY, stopX, stopY
		float[] points = new float[this.data.length * 4];
		
		// first value
		points[0] = originX;
		points[1] = chartHeight - preparedData[0] + offset;
		points[2] = originX;
		points[3] = chartHeight - preparedData[0] + offset;
		
		for (int i = 1; i < data.length; i++) {
			float value = preparedData[i];
			
			points[i * 4] = points[i * 4 - 2];
			points[i * 4 +1] = points[i * 4 - 1];
			points[i * 4 + 2] = step * i + originX;
			points[i * 4 + 3] = chartHeight - value + offset;
			
		}
		canvas.drawLines(points, chartPaint);
	}

	private float[] prepareDataValues(float chartHeight) {
		Log.d(Utils.LOG_TAG, "preparing chart data..");
		float[] preparedData = new float[this.data.length];
		float heightMaxScale = chartHeight / max;
		float heightMinScale = chartHeight / min;
		float minMax = heightMaxScale / heightMinScale;
		StringBuilder builder = new StringBuilder("Prepared Data: ");
		
		for (int i = 0; i < this.data.length; i++) {
			//preparedData[i] = this.data[i] * heightMaxScale * minMax;
			preparedData[i] = this.scaleRange(this.data[i], this.min, this.max, 0, chartHeight);
			builder.append(String.valueOf(preparedData[i]) + "; ");
		}
		Log.d(Utils.LOG_TAG, builder.toString());
		return preparedData;
	}
	float scaleRange(float in, float oldMin, float oldMax, float newMin, float newMax)
	{
		return ( ((newMax - newMin) * (in - oldMin)) / (oldMax - oldMin) ) + newMin;
	}
	
//    		(b-a)(x - min)
//    f(x) = --------------  + a
//              max - min
	

	/**
	 * @param canvas
	 * @param offset
	 * @param originX
	 * @param originY
	 * @param chartWidth
	 */
	private void drawAxis(Canvas canvas, float offset, float originX, float originY, float chartWidth) {
		// the lines are crossing with overlap = offset/2
		// draw x axis
		canvas.drawLine(originX - offset/2, originY, chartWidth, originY, this.paint);
		
		// draw y axis
		canvas.drawLine(originX, originY + originY/2, originX, 0 + offset, this.paint);
	}

	private void drawAxisDescription(Canvas canvas, int offsetBelowXAxis, int offsetNextToYAxis, float chartWidth, float chartHeight) {
		// description next to y axis
		if (this.data != null && this.data.length > 0) {
			// bottom text is right above x=0 value to the left of y axis
			canvas.drawText(String.valueOf(this.min), OFFSET, 
					this.getHeight() - OFFSET - offsetBelowXAxis, this.textPaint);
			
			String lastTickText = String.valueOf(max);
			canvas.drawText(lastTickText, OFFSET, OFFSET + this.textPaint.getTextSize(),
					this.textPaint);
		}

		// description under the x axis
		if (this.axisX != null && this.axisX.length > 0) {
			String text = this.getFormattedValue(this.axisX[0]);
			canvas.drawText(text, OFFSET + offsetNextToYAxis, 
					this.getHeight() - OFFSET - this.textPaint.getTextSize(), this.textPaint);
			
			text = this.getFormattedValue(this.axisX[this.axisX.length - 1]);
			float textWidth = this.textPaint.measureText(text);
			canvas.drawText(text, 2*OFFSET + chartWidth - textWidth, 
					this.getHeight() - OFFSET - this.textPaint.getTextSize(), this.textPaint);
		}
	}
	
	private String getFormattedValue(T val) {
		if (this.formatter != null)
			return this.formatter.formatAxeText(val);
		else
			return val.toString();
	}
	
	public void setData(float[] data, float max, float min) {
		this.max = max;
		this.min = min;
		this.data = data;
		this.preparedData = null;
		
		this.postInvalidate();
		StringBuilder builder = new StringBuilder("Chart Data: ");
		for (int i = 0; i < data.length; i++) {
			builder.append(String.valueOf(data[i]) + "; ");
		}
		Log.d(Utils.LOG_TAG, builder.toString());
	}
	
	public void setAxisX(T[] xAxisPoints, IChartTextFormatter<T> formatter) {
		this.axisX = xAxisPoints;
		this.formatter = formatter;
	}
}
