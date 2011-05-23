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

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.R.id;
import cz.tomas.StockAnalyze.R.layout;
import cz.tomas.StockAnalyze.R.menu;
import cz.tomas.StockAnalyze.R.string;
import cz.tomas.StockAnalyze.charts.view.CompositeChartView;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * @author tomas
 *
 */
public final class StockDetailActivity extends ChartActivity {

	private TextView txtChartDescription;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.stock_detail);
		this.txtChartDescription = (TextView) this.findViewById(R.id.detail_label_chart_description);
		this.chartView = (CompositeChartView) findViewById(R.id.stockChartView);
		if (chartView != null) {
			this.registerForContextMenu(this.chartView);
			this.chartView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					goToChartDetail();
				}
			});
		} else
			Log.w(Utils.LOG_TAG, "Failed to initialize chart view");
		
		this.setChartActivityListener(new IChartActivityListener() {
			
			@Override
			public void onChartUpdateFinish() {
				int id = DAY_COUNT_MAP.get(chartDayCount);
				if (txtChartDescription != null)
					txtChartDescription.setText(getString(id));
			}
			
			@Override
			public void onChartUpdateBegin() {
				if (txtChartDescription != null)
					txtChartDescription.setText(R.string.loading);
			}
		});
		
		// compatibility
		if (this.getParent() instanceof TabActivity) {
			((TabActivity) this.getParent()).getTabHost().setOnTabChangedListener(new OnTabChangeListener() {
				
				@Override
				public void onTabChanged(String tabId) {
					try {
						if (tabId.equals("StockDetail")) {
							Intent intent = StockDetailActivity.this.getParent().getIntent();
							if (readData(intent))
								fill();
						}
					} catch (Exception e) {
						Log.e(Utils.LOG_TAG, "failed to get data from intent", e);
						Toast toast = Toast.makeText(StockDetailActivity.this, R.string.InvalidData, Toast.LENGTH_LONG);
						if (e.getMessage() != null) {
							Log.d("StockDetailActivity", e.getMessage());
							toast.setText(getString(R.string.InvalidData) + ": " + e.getMessage());
						}
						toast.show();
					}
				}
			});
		}
		else {
			Intent intent = this.getIntent();
			try {
				if (readData(intent)) {
					this.fill();
				} else 
					this.showWarning();
			} catch (Exception e) {
				Log.e(Utils.LOG_TAG, "failed to get data from intent", e);
				Toast toast = Toast.makeText(StockDetailActivity.this, R.string.InvalidData, Toast.LENGTH_LONG);
				if (e.getMessage() != null) {
					Log.d("StockDetailActivity", e.getMessage());
					toast.setText(getString(R.string.InvalidData) + ": " + e.getMessage());
				}
				toast.show();
			}
		}
	}
		

	private void fill() throws NullPointerException, IOException {
		this.updateChart();
		this.updateCurrentStock();
	}
	
	private void goToChartDetail() {
		Intent intent = new Intent(StockDetailActivity.this,
				StockChartActivity.class);
		intent.putExtras(getIntent());
		intent.putExtra(EXTRA_CHART_DAY_COUNT, this.chartDayCount);
		startActivity(intent);
	}


	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
//		if (this.chartTask != null && this.chartTask.getStatus() != Status.FINISHED) {
//			Log.w(Utils.LOG_TAG, "canceling chart update task!");
//			this.chartTask.cancel(true);
//		}
		super.onPause();
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

	/* 
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
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

	private void updateCurrentStock() throws NullPointerException, IOException {
		if (this.stockItem == null)
			throw new NullPointerException("stockItem must be defined");
		if (this.stockItem.getMarket() == null)
			throw new NullPointerException("market must be defined");
		
		TextView txtHeader = (TextView) this.findViewById(R.id.txtDetailHeader);
		TextView txtDate = (TextView) this.findViewById(R.id.txtDetailDate);
		TextView txtName = (TextView) this.findViewById(R.id.txtDetailName);
		TextView txtPrice = (TextView) this.findViewById(R.id.txtDetailClosingPrice);
		TextView txtChange = (TextView) this.findViewById(R.id.txtDetailChange);
		TextView txtVolume = (TextView) this.findViewById(R.id.txtDetailVolume);
		TextView txtMax = (TextView) this.findViewById(R.id.txtDetailMax);
		TextView txtMin = (TextView) this.findViewById(R.id.txtDetailMin);
		
		final DataManager manager = DataManager.getInstance(this);
		
		if (txtPrice != null) {
			txtPrice.setText(R.string.loading);
		}
		if (this.dayData == null) {
			Log.w(Utils.LOG_TAG, "data incoming to detail don't contain DayData. Loading them from db...");
			this.dayData = manager.getLastValue(stockItem);
		}
		
		NumberFormat priceFormat = FormattingUtils.getPriceFormat(stockItem.getMarket().getCurrency());
		NumberFormat percentFormat = FormattingUtils.getPercentFormat();
		//NumberFormat volumeFormat = FormattingUtils.getVolumeFormat();

		if (this.dayData == null)
			throw new NullPointerException("Day data is null!");
		
		if (txtHeader != null)
			txtHeader.setText(stockItem.getTicker() + " - " + stockItem.getId());
		if (txtDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(this.dayData.getLastUpdate());
			
			txtDate.setText(FormattingUtils.formatStockDate(cal));
		}
		if (txtVolume != null) {
			String strVolume = priceFormat.format(this.dayData.getVolume());
			txtVolume.setText(strVolume);
		}
		if (txtMax != null)
			txtMax.setText(String.valueOf(this.dayData.getYearMaximum()));
		if (txtMin != null)
			txtMin.setText(String.valueOf(this.dayData.getYearMinimum()));
		if (txtName != null)
			txtName.setText(stockItem.getName());
		if (txtPrice != null) {
			String strPrice = priceFormat.format(this.dayData.getPrice());
			txtPrice.setText(strPrice);
			//txtPrice.setText(String.format("%s (%s%%)", strPrice, strChange));
		}
		if (txtChange != null) {
			String strChange = percentFormat.format(this.dayData.getChange());
			String strAbsChange = percentFormat.format(this.dayData.getAbsChange());
			txtChange.setText(String.format("%s (%s%%)", strAbsChange, strChange));
			
			if (this.dayData.getChange() > 0f)
				txtChange.setTextColor(Color.GREEN);
			else if (this.dayData.getChange() < 0f)
				txtChange.setTextColor(Color.RED);
		}
	}

	private void showWarning() {
		TextView txtName = (TextView) this.findViewById(R.id.txtDetailName);
		
		if (txtName != null)
			txtName.setText(R.string.NoStockSelected);
	}	
}
