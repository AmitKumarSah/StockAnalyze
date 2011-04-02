/**
 * 
 */
package cz.tomas.StockAnalyze.charts.view;

import java.util.Map;

import cz.tomas.StockAnalyze.charts.R;
import cz.tomas.StockAnalyze.charts.Utils;
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

//	@Override
//	public void onCreateContextMenu(ContextMenu menu) {
//	  super.onCreateContextMenu(menu);
//	  inflater.inflate(R.menu.chart_context_menu, menu);
//	  
//	}
	
	public void setData(Map<Object, Float> dataSet, float max, float min) {
		
	}
	
	public void setData(float[] dataSet, float max, float min) {
		if (this.chart != null)
			this.chart.setData(dataSet, max, min);
		else
			Log.w(Utils.LOG_TAG, "chart in CompositeChartView is null! Can't set data.");
	}
	
	public <T> void setAxisX(T[] xAxisPoints) {
		// TODO Auto-generated method stub
	}
}
