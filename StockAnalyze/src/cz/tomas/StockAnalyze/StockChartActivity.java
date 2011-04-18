/**
 * 
 */
package cz.tomas.StockAnalyze;

import java.io.IOException;

import cz.tomas.StockAnalyze.charts.view.CompositeChartView;
import cz.tomas.StockAnalyze.utils.Utils;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Toast;

/**
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
		
		try {
			if (this.readData(this.getIntent())) {
				this.updateChart();
				this.updateTitle();
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

	

	private void updateTitle() {
		int id = ChartActivity.DAY_COUNT_MAP.get(this.chartDayCount);
		String title = this.getString(R.string.activityStockChart);
		if (this.stockItem != null) {
			title += ": " + this.stockItem.getTicker();
		}
		title += " (" + this.getString(id) + ")";
		this.setTitle(title);
	}
	
	/* 
	 * @see cz.tomas.StockAnalyze.ChartActivity#readData(android.content.Intent)
	 */
	@Override
	protected boolean readData(Intent intent) throws NullPointerException,
			IOException {
		if (super.readData(intent)) {
			this.chartDayCount = intent.getIntExtra(EXTRA_CHART_DAY_COUNT, 10);
			
			return true;
		}
		return false;
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
