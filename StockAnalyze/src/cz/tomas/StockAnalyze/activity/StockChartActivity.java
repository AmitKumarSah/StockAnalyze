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
package cz.tomas.StockAnalyze.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.charts.view.CompositeChartView;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * activity showing stock chart as the only content 
 * 
 * @author tomas
 *
 */
public class StockChartActivity extends ChartActivity {
	
	/* 
	 * @see cz.tomas.StockAnalyze.ChartActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.chartView = new CompositeChartView(this, null);
		this.setContentView(this.chartView);
		this.registerForContextMenu(this.chartView);
		
		// if we aren't resuming, load day count from intent (first run)
		if (savedInstanceState == null || !savedInstanceState.containsKey(EXTRA_CHART_DAY_COUNT))
			this.chartDayCount = this.getIntent().getIntExtra(EXTRA_CHART_DAY_COUNT, 10);
		
		this.setChartActivityListener(new IChartActivityListener() {
			
			@Override
			public void onChartUpdateFinish() {
				updateTitle(false);
			}
			
			@Override
			public void onChartUpdateBegin() {
				updateTitle(true);
			}
		});
		
		try {
			if (this.readData(this.getIntent())) {
				this.updateChart();
			}
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, "Failed to load data or update chart.", e);
			Toast toast = Toast.makeText(this, R.string.InvalidData, Toast.LENGTH_LONG);
			if (e.getMessage() != null) {
				toast.setText(getString(R.string.InvalidData) + ": " + e.getMessage());
			}
			toast.show();
		}
	}

	
	/**
	 * build activity title according to content
	 * @param loading true if chart is just loading
	 */
	private void updateTitle(boolean loading) {
		int id = ChartActivity.DAY_COUNT_MAP.get(this.chartDayCount);
		String title = this.getString(R.string.activityStockChart);
		if (this.stockItem != null) {
			title += ": " + this.stockItem.getTicker();
		}
		title += " (" + this.getString(id) + ")";
		if (loading)
			title += " - " + this.getString(R.string.loading);
		this.setTitle(title);
	}
}
