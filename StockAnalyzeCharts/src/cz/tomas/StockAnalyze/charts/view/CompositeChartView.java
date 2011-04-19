/**
 * 
 */
package cz.tomas.StockAnalyze.charts.view;

import cz.tomas.StockAnalyze.charts.R;
import cz.tomas.StockAnalyze.charts.Utils;
import cz.tomas.StockAnalyze.charts.interfaces.IChartTextFormatter;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * @author tomas
 *
 */
public class CompositeChartView extends RelativeLayout {

	MenuInflater inflater;
	ChartView chart;
	View progressBar;
	View background;
	
	public CompositeChartView(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.inflater = new MenuInflater(getContext());
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.composite_chart_view_layout, this);
		this.chart = (ChartView) this.findViewById(R.id.chart);
		this.progressBar = this.findViewById(R.id.chartProgressBar);
		this.background = this.findViewById(R.id.chartBackground);
	}
	
	
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

	public void setLoading(boolean loading) {
		if (this.progressBar != null)
			this.progressBar.setVisibility(loading ? View.VISIBLE: View.GONE);
		
		if (this.background != null)
			this.background.setVisibility(loading ? View.VISIBLE: View.GONE);
		
		if (this.chart == null)
			this.chart.setDisableRedraw(loading);
	}
}
