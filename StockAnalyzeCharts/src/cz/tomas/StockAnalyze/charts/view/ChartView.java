/**
 * 
 */
package cz.tomas.StockAnalyze.charts.view;

import cz.tomas.StockAnalyze.charts.Utils;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * @author tomas
 *
 */
public class ChartView extends View {

	private float[] data;
	private float[] preparedData;
	private float max;
	private float min;
	
	private Paint paint;
	private Paint chartPaint;
	
	// Convert the dps to pixels
	private final float scale = getContext().getResources().getDisplayMetrics().density;
	
	public ChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.paint = new Paint();
		this.paint.setStrokeWidth(2*scale);
		this.paint.setColor(Color.BLUE);
		this.paint.setAntiAlias(true);
		
		this.chartPaint = new Paint(this.paint);
		this.chartPaint.setColor(Color.GREEN);
	}

	
	
//	/* (non-Javadoc)
//	 * @see android.view.View#onMeasure(int, int)
//	 */
//	@Override
//	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		//super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//		
//		setMeasuredDimension(widthMeasureSpec, widthMeasureSpec);
//	}



	/*
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		//super.onDraw(canvas);
		int offset = (int) (10 * this.scale + 0.5f);
		float originX = offset;
		float originY = this.getHeight() - offset;
		float chartWidth = this.getWidth() - 2 * offset;
		float chartHeight = this.getHeight() - 2 * offset;
		
		drawAxes(canvas, offset, originX, originY, chartWidth);
		
		if (this.data != null && this.data.length > 1)
			this.drawData(canvas, offset, originX, originY, chartWidth, chartHeight);
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
		//return (in / ((oldMax - oldMin) / (newMax - newMin))) + newMin;
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
	private void drawAxes(Canvas canvas, float offset, float originX, float originY, float chartWidth) {
		// draw x axis
		canvas.drawLine(originX - offset/2, originY, chartWidth, originY, paint);
		
		//canvas.drawLine(originX, originY, chartWidth, 0 + offset, paint);
		// draw y axis
		canvas.drawLine(originX, originY + originY/2, originX, 0 + offset, paint);
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
}
