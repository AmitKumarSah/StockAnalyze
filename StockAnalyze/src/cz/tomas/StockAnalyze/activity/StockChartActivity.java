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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.Data.DataManager;
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
		
		this.setContentView(R.layout.part_chart_with_header);
		this.chartView = (CompositeChartView) this.findViewById(R.id.stockChartView);
		this.chartView.setEnablePainting(true);
		this.chartView.setEnableTracking(false);
		
		// if we aren't resuming, load day count from intent (first run)
		if (savedInstanceState == null || !savedInstanceState.containsKey(EXTRA_CHART_DAY_COUNT)) {
			this.timePeriod = this.getIntent().getIntExtra(EXTRA_CHART_DAY_COUNT, DataManager.TIME_PERIOD_MONTH);
		}
		
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.stock_chart_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_chart_painting:
			this.chartView.setEnablePainting(true);
			this.chartView.setEnableTracking(false);
			this.chartView.clear();
			break;
		case R.id.menu_chart_tracking:
			this.chartView.setEnablePainting(false);
			this.chartView.setEnableTracking(true);
			this.chartView.clear();
			break;
		case R.id.menu_chart_clear:
			this.chartView.clear();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}



	@Override
	public void onChartUpdateFinish() {
		updateTitle(false);
	}
	
	@Override
	public void onChartUpdateBegin() {
		updateTitle(true);
	}
	
	/**
	 * build activity title according to content
	 * @param loading true if chart is just loading
	 */
	private void updateTitle(boolean loading) {
		int id = ChartActivity.DAY_COUNT_MAP.get(this.timePeriod);
		String title = this.getString(R.string.activityStockChart);
		if (this.stockItem != null) {
			title += ": " + this.stockItem.getTicker();
		}
		title += " (" + this.getString(id) + ")";
		if (loading) {
			title += " - " + this.getString(R.string.loading);
		}
		this.setTitle(title);
	}
	
	@Override
	protected void onNavigateUp() {
		this.finish();
	}
}
