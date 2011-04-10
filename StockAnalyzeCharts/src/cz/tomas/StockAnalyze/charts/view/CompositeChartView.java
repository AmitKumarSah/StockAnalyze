/**
 * 
 */
package cz.tomas.StockAnalyze.charts.view;

import java.util.Map;

import cz.tomas.StockAnalyze.charts.R;
import cz.tomas.StockAnalyze.charts.Utils;
import cz.tomas.StockAnalyze.charts.interfaces.IChartTextFormatter;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.widget.RelativeLayout;

/**
 * @author tomas
 *
 */
public class CompositeChartView extends RelativeLayout {

	MenuInflater inflater;
	ChartView chart;
	
	public CompositeChartView(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.inflater = new MenuInflater(getContext());
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.composite_chart_view_layout, this);
		this.chart = (ChartView) this.findViewById(R.id.chart);
	}
	
//	public <T> void setData(Map<T, Float> dataSet, float max, float min) {
//		
//	}
	
	public void setData(float[] dataSet, float max, float min) {
		if (this.chart != null)
			this.chart.setData(dataSet, max, min);
		else
			Log.w(Utils.LOG_TAG, "chart in CompositeChartView is null! Can't set data.");
	}
	
	public <T> void setAxisX(T[] xAxisPoints, IChartTextFormatter<T> formatter) {
		if (this.chart != null)
			this.chart.setAxisX(xAxisPoints, formatter);
		else
			Log.w(Utils.LOG_TAG, "chart in CompositeChartView is null! Can't set axis data.");
	}
}
