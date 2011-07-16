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
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.activity.base.BaseActivity;
import cz.tomas.StockAnalyze.charts.interfaces.IChartTextFormatter;
import cz.tomas.StockAnalyze.charts.view.CompositeChartView;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.NavUtils;
import cz.tomas.StockAnalyze.utils.Utils;
import android.content.Intent;	
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * Base class for activities containing chart view,
 * chartView must be initialized in subclass
 * 
 * @author tomas
 *
 */
public abstract class ChartActivity extends BaseActivity {
	
	interface IChartActivityListener {
		void onChartUpdateBegin();
		void onChartUpdateFinish();
	}
	
	protected static final String EXTRA_CHART_DAY_COUNT = "cz.tomas.StockAnalyze.chart_day_count";
	protected static final int MAX_CACHE_SIZE = 4;
	
	protected StockItem stockItem;
	protected DataManager dataManager;
	protected int chartDayCount = 10;
	protected CompositeChartView chartView;
	protected DrawChartTask chartTask;
	protected DayData dayData;
	
	private final static Map<String, DayData[]> chartCacheDataSet = new LinkedHashMap<String, DayData[]>();
	
	private IChartActivityListener listener;
	/**
	 * string resource ids mapped to day count
	 */
	protected static Map<Integer, Integer> DAY_COUNT_MAP;
	/** 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (DAY_COUNT_MAP == null) {
			DAY_COUNT_MAP = new HashMap<Integer, Integer>();
			DAY_COUNT_MAP.put(5, R.string.chart5days);
			DAY_COUNT_MAP.put(10, R.string.chart10days);
			DAY_COUNT_MAP.put(21, R.string.chartMonth);
			DAY_COUNT_MAP.put(63, R.string.chart3months);
			DAY_COUNT_MAP.put(126, R.string.chart6months);
			DAY_COUNT_MAP.put(252, R.string.chartYear);
		}

		if (savedInstanceState != null) {
			this.chartDayCount = savedInstanceState.getInt(EXTRA_CHART_DAY_COUNT);
		}
		if (chartCacheDataSet.size() > MAX_CACHE_SIZE) {
			clearCache();
		}
		this.dataManager = DataManager.getInstance(this);		
	}

	/**
	 * 
	 */
	public static void clearCache() {
		Log.i(Utils.LOG_TAG, "freeing chart activity cache...");
		// TODO better mechanism
		if (chartCacheDataSet != null)
			chartCacheDataSet.clear();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putInt(EXTRA_CHART_DAY_COUNT, this.chartDayCount);
	}


	/**
	 * read data from input intent
	 * @param intent
	 * @throws NullPointerException
	 * @throws IOException
	 */
	protected boolean readData(Intent intent) throws NullPointerException,
			IOException {
		if (intent.hasExtra(NavUtils.STOCK_ITEM_OBJECT)) {
			this.stockItem = intent.getExtras().getParcelable(NavUtils.STOCK_ITEM_OBJECT);
			this.dayData = intent.getExtras().getParcelable(NavUtils.DAY_DATA_OBJECT);
			//Market market = (Market) intent.getExtras().getSerializable("market_id");
			
			return true;
		}
		else {
			return false;
		}
	}
	

	/**
	 * @param stockItem
	 */
	protected void updateChart() {
		this.chartTask = new DrawChartTask();
		chartTask.execute(this.stockItem);
	}
	
	protected boolean isChartUpdating() {
		if (this.chartTask != null && this.chartTask.getStatus() == AsyncTask.Status.RUNNING || 
				this.chartTask.getStatus() == AsyncTask.Status.PENDING)
			return true;
		else
			return false;
	}


	/**
	 * by resource id get day count, this method is used to translate selected
	 * menu item to day count
	 * @param item
	 * @return
	 */
	protected int getDayCountByResource(int id) {
		int dayCount = 0;
		// day counts are work days only
		switch (id) {
		case R.id.chart10days:
			dayCount = 10;
			break;
		case R.id.chart3months:
			dayCount = 63;
			break;
		case R.id.chart5days:
			dayCount = 5;
			break;
		case R.id.chart6months:
			dayCount =  126;
			break;
		case R.id.chartMonth:
			dayCount = 21;
			break;
		case R.id.chartYear:
			dayCount = 252;
			break;
		default:
			break;
		}
		return dayCount;
	}

	public void setChartActivityListener(IChartActivityListener listener) {
		this.listener = listener;
	}

	final protected class DrawChartTask extends AsyncTask<StockItem, Integer, DayData[]> {
		
		/* 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (listener != null)
				listener.onChartUpdateBegin();
			if (chartView != null)
				chartView.setLoading(true);
		}

		@Override
		protected DayData[] doInBackground(StockItem... params) {
			if (params.length == 0)
				return null;
			StockItem stockItem = params[0];

			DayData[] dataSet = null;
			// either take data from local variable, or load them
			
			if (ChartActivity.chartCacheDataSet.containsKey(stockItem.getId()) && 
					ChartActivity.chartCacheDataSet.get(stockItem.getId()).length == chartDayCount) {
				dataSet = ChartActivity.chartCacheDataSet.get(stockItem.getId());
			} else {
				try {
					dataSet = dataManager.getDayDataSet(stockItem, Calendar.getInstance(), chartDayCount, true);

					// save loaded data to cache
					if (ChartActivity.chartCacheDataSet.containsKey(stockItem.getId())) {
						ChartActivity.chartCacheDataSet.remove(stockItem.getId());
					}
					ChartActivity.chartCacheDataSet.put(stockItem.getId(), dataSet);
				} catch (Exception e) {
					Log.e(Utils.LOG_TAG, "failed to get data", e);
				}
			}
			if (dataSet != null) {
				float[] dataPoints = new float[dataSet.length];
				Long[] xAxisPoints = new Long[dataSet.length];
				//Map<Date, Float> chartData = new LinkedHashMap<Date, Float>();
				float max = 0;
				float min = Float.MAX_VALUE;

				float price = -1f;
				long time = -1l;
				for (int i = 0; i < dataSet.length; i++) {
					if (dataSet[i] != null) {
						price = dataSet[i].getPrice();
						time = dataSet[i].getDate().getTime();
					} else {
						Log.w(Utils.LOG_TAG, "day data with index " + i + " are not available");
						if (i > 0 && dataSet[i-1] != null) {
							// try to get previous one
							price = dataSet[i-1].getPrice();
							time = dataSet[i-1].getDate().getTime();
						}
					}
					if (price >= 0 && time >= 0) {
						dataPoints[i] = price;
						xAxisPoints[i] = time;
						//					chartData.put(time, price);

						if (price > max)
							max = price;
						if (price < min)
							min = price;
					}
				}

				if (chartView != null && chartView.getVisibility() == View.VISIBLE) {
					final Calendar cal = Calendar.getInstance();
					chartView.setAxisX(xAxisPoints, new IChartTextFormatter<Long>() {

						@Override
						public String formatAxeText(Long val) {
							if (val != null) {
								cal.setTimeInMillis(val);
								return FormattingUtils.formatStockShortDate(cal);
							} 
							return "";
						}
					});
					chartView.setData(dataPoints, max, min);
				}
			}
			return dataSet;
		}

		@Override
		protected void onPostExecute(DayData[] result) {		
			// change ui to show that update is done
			if (chartView != null)
				chartView.setLoading(false);
			else
				Log.w(Utils.LOG_TAG, "ChartView is null! Can not set data to chart!");
			
			if (listener != null)
				listener.onChartUpdateFinish();
		}
		
	}
}
