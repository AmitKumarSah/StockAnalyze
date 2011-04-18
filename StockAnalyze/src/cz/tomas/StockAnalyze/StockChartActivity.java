/**
 * 
 */
package cz.tomas.StockAnalyze;

import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
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

	/* 
	 * create context menu for chart
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		// in case the task is running, don't create context menu
		if (this.isChartUpdating())
			return;
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.chart_context_menu, menu);
	}


	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int dayCount = this.getDayCountByResource(item.getItemId());
		if (dayCount != 0) {
			this.chartDayCount = dayCount;
			this.updateChart();
			return true;
		} else
			return super.onContextItemSelected(item);
	}
}
