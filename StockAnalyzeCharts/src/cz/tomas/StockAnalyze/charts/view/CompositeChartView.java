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

import cz.tomas.StockAnalyze.charts.R;
import cz.tomas.StockAnalyze.charts.Utils;
import cz.tomas.StockAnalyze.charts.interfaces.IChartTextFormatter;
import android.content.Context;
import android.os.Build;
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
		
		// we have some problems with drawing in accelerated layer
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
	}
	
	
	public void setData(float[] dataSet, float max, float min, IChartTextFormatter<Float> formatter) {
		if (this.chart != null)
			this.chart.setData(dataSet, max, min, formatter);
		else
			Log.w(Utils.LOG_TAG, "chart in CompositeChartView is null! Can't set data.");
	}
	
	public <T extends Number> void setAxisX(T[] xAxisPoints, IChartTextFormatter<T> formatter) {
		if (this.chart != null) {
			this.chart.setAxisX(xAxisPoints, formatter);
		} else {
			Log.w(Utils.LOG_TAG, "chart in CompositeChartView is null! Can't set axis data.");
		}
	}
	
	public void setEnableTracking(boolean enabled) {
		if (this.chart != null) {
			this.chart.setEnableTracking(enabled);
		}
	}
	
	public void setEnablePainting(boolean enabled) {
		if (this.chart != null) {
			this.chart.setEnablePainting(enabled);
		}
	}
	
	public void clear() {
		if (this.chart != null) {
			this.chart.clear();
		}
	}

	public void setLoading(boolean loading) {
		if (this.progressBar != null) {
			this.progressBar.setVisibility(loading ? View.VISIBLE: View.GONE);
		}
		
		if (this.background != null) {
			this.background.setVisibility(loading ? View.VISIBLE: View.GONE);
		}
		
		if (this.chart != null) {
			this.chart.setVisibility(loading ? View.INVISIBLE: View.VISIBLE);
			this.chart.setDisableRedraw(loading);
		}
	}

	public boolean isDataLoaded() {
		return this.chart.isDataLoaded();
	}
}
