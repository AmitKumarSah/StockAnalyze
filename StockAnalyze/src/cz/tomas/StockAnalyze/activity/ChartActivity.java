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
import java.lang.ref.SoftReference;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.flurry.android.FlurryAgent;

import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.activity.base.BaseActivity;
import cz.tomas.StockAnalyze.charts.interfaces.IChartTextFormatter;
import cz.tomas.StockAnalyze.charts.view.CompositeChartView;
import cz.tomas.StockAnalyze.utils.Consts;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.NavUtils;
import cz.tomas.StockAnalyze.utils.Utils;
import android.content.Intent;	
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Toast;

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
	
	private class ChartDataCache {
		Map<Long, Float> dataSet;
		int timePeriod;
		long creationTime;
		
		/**
		 * @param dataSet
		 * @param timePeriod
		 */
		public ChartDataCache(Map<Long, Float> dataSet, int timePeriod) {
			super();
			this.dataSet = dataSet;
			this.timePeriod = timePeriod;
			this.creationTime = SystemClock.elapsedRealtime(); 
		}
		
	}
	
	protected static final String EXTRA_CHART_DAY_COUNT = "cz.tomas.StockAnalyze.chart_day_count";
	protected static final int MAX_CACHE_SIZE = 8;
	
	protected StockItem stockItem;
	protected DataManager dataManager;
	protected int timePeriod = DataManager.TIME_PERIOD_MONTH;
	protected CompositeChartView chartView;
	protected DrawChartTask chartTask;
	protected DayData dayData;
	
	private final static Map<String, SoftReference<ChartDataCache>> chartCacheDataSet = new LinkedHashMap<String, SoftReference<ChartDataCache>>();
	private final static Map<String, SoftReference<ChartDataCache>> chartIntradayCacheDataSet = new LinkedHashMap<String, SoftReference<ChartDataCache>>();
	
	public static final long CACHE_INTRADAY_EXPIRATION = 8 * 60 * 1000;
	
	protected SharedPreferences prefs;
	
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
			DAY_COUNT_MAP.put(DataManager.TIME_PERIOD_DAY, R.string.chartDay);
			DAY_COUNT_MAP.put(DataManager.TIME_PERIOD_WEEK, R.string.chart5days);
			DAY_COUNT_MAP.put(DataManager.TIME_PERIOD_MONTH, R.string.chartMonth);
			DAY_COUNT_MAP.put(DataManager.TIME_PERIOD_QUARTER, R.string.chart3months);
			DAY_COUNT_MAP.put(DataManager.TIME_PERIOD_HALF_YEAR, R.string.chart6months);
			DAY_COUNT_MAP.put(DataManager.TIME_PERIOD_YEAR, R.string.chartYear);
		}

		this.prefs = this.getSharedPreferences(Utils.PREF_NAME, 0);
		
		if (savedInstanceState != null) {
			this.timePeriod = savedInstanceState.getInt(EXTRA_CHART_DAY_COUNT);
		} else {
			this.timePeriod = this.prefs.getInt(Utils.PREF_CHART_TIME_PERIOD, DataManager.TIME_PERIOD_MONTH);
		}
		
		this.dataManager = DataManager.getInstance(this);		
	}
	
	/* (non-Javadoc)
	 * @see cz.tomas.StockAnalyze.activity.base.BaseActivity#onStop()
	 */
	@Override
	public void onStop() {
		super.onStop();
		
		if (this.chartTask != null) {
			this.chartTask.cancel(false);
		}
	}

	/**
	 * clear chart data caches
	 */
	public static void clearCache() {
		Log.i(Utils.LOG_TAG, "freeing chart activity cache...");
		if (chartCacheDataSet != null)
			chartCacheDataSet.clear();
		if (chartIntradayCacheDataSet != null) 
			chartIntradayCacheDataSet.clear();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putInt(EXTRA_CHART_DAY_COUNT, this.timePeriod);
	}


	/** 
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
		int timePeriod = this.getDayCountByResource(item.getItemId());
		if (timePeriod != 0) {
			this.timePeriod = timePeriod;
			Editor ed = this.prefs.edit();
			ed.putInt(Utils.PREF_CHART_TIME_PERIOD, timePeriod);
			ed.commit();
			this.updateChart();
			Map<String, String> pars = new HashMap<String, String>(2);
			pars.put(Consts.FLURRY_KEY_CHART_TIME_PERIOD, String.valueOf(timePeriod));
			pars.put(Consts.FLURRY_KEY_CHART_TIME_SOURCE, getClass().getName());
			FlurryAgent.onEvent(Consts.FLURRY_EVENT_CHART_TIME_PERIOD, pars);
			return true;
		} else
			return super.onContextItemSelected(item);
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
		//int dayCount = 0;
		int timePeriod = 0;
		// day counts are work days only
		switch (id) {
		case R.id.chartDay:
			timePeriod = DataManager.TIME_PERIOD_DAY;
			break;
		case R.id.chart3months:
			timePeriod = DataManager.TIME_PERIOD_QUARTER;
			break;
		case R.id.chart5days:
			timePeriod = DataManager.TIME_PERIOD_WEEK;
			break;
		case R.id.chart6months:
			timePeriod =  DataManager.TIME_PERIOD_HALF_YEAR;
			break;
		case R.id.chartMonth:
			timePeriod = DataManager.TIME_PERIOD_MONTH;
			break;
		case R.id.chartYear:
			timePeriod = DataManager.TIME_PERIOD_YEAR;
			break;
		default:
			break;
		}
		return timePeriod;
	}

	public void setChartActivityListener(IChartActivityListener listener) {
		this.listener = listener;
	}
	
	private Map<String, SoftReference<ChartDataCache>> getCacheMap(int timePeriod) {
		if (timePeriod != DataManager.TIME_PERIOD_DAY) {
			return ChartActivity.chartCacheDataSet;
		} else {
			return ChartActivity.chartIntradayCacheDataSet;
		}
	}

	private static final IChartTextFormatter<Long> FORMATTER_DATE = new IChartTextFormatter<Long>() {
		
		@Override
		public String formatAxeText(Long val) {
			if (val != null) {
				final Calendar cal = new GregorianCalendar(Utils.PRAGUE_TIME_ZONE);
				cal.setTimeInMillis(val);
				return FormattingUtils.formatStockShortDate(cal);
			} 
			return "";
		}
	};
	
	private static final IChartTextFormatter<Long> FORMATTER_TIME = new IChartTextFormatter<Long>() {
		
		@Override
		public String formatAxeText(Long val) {
			if (val != null) {
				final Calendar cal = new GregorianCalendar(Utils.PRAGUE_TIME_ZONE);
				cal.setTimeInMillis(val);
				return FormattingUtils.formatStockShortTime(cal);
			} 
			return "";
		}
	};
	
	final protected class DrawChartTask extends AsyncTask<StockItem, Integer, Void> {
		
		private Throwable ex;
		
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
		protected Void doInBackground(StockItem... params) {
			if (params.length == 0) {
				return null;
			}
			final StockItem stockItem = params[0];

			Map<Long, Float> dataSet = null;
			
			// either take data from local cache, or load them:
			final String key = String.format("%s-%d", stockItem.getId(), timePeriod);	// key to cache

			ChartDataCache chartCache = getCacheData(key);
			if (chartCache != null) {
				dataSet = chartCache.dataSet;
			} else {
				try {
					dataSet = dataManager.getDayDataSet(stockItem, timePeriod, true);

					if (dataSet != null) {
						chartCache = new ChartDataCache(dataSet, timePeriod);
						Map<String, SoftReference<ChartDataCache>> cacheMap = getCacheMap(timePeriod);
						if (cacheMap.size() >= MAX_CACHE_SIZE) {
							String firstKey = cacheMap.keySet().iterator().next();
							cacheMap.remove(firstKey);
						}
						cacheMap.put(key, new SoftReference<ChartActivity.ChartDataCache>(chartCache));
					} else {
						Log.w(Utils.LOG_TAG, "dataset for chart is null");
					}
				} catch (Exception e) {
					Log.e(Utils.LOG_TAG, "failed to get data", e);
					this.ex = e;
				} catch (OutOfMemoryError error) {
					Log.i(Utils.LOG_TAG, "allocation: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
					Log.e(Utils.LOG_TAG, "Out of memory! Failed to get data", error);
					this.ex = error;
				}
			}
			if (dataSet != null) {
				final float[] dataPoints = new float[dataSet.size()];
				final Long[] xAxisPoints = new Long[dataSet.size()];
				//Map<Date, Float> chartData = new LinkedHashMap<Date, Float>();
				float max = 0;
				float min = Float.MAX_VALUE;

				float price = -1f;
				long time = -1l;
				//for (int i = 0; i < dataSet.length; i++) {
				int index = 0;
				for (Entry<Long, Float> entry : dataSet.entrySet()) {
					if (entry.getValue() != 0f) {
						price = entry.getValue();
						time = entry.getKey();
					} else {
						Log.w(Utils.LOG_TAG, "day data with index " + index + " are not available");
//						if (i > 0 && dataSet[i-1] != 0f) {
//							// try to get previous one
//							price = dataSet[i-1];
//							time = timeSet[i];
//						}
					}
					if (price >= 0 && time >= 0) {
						dataPoints[index] = price;
						xAxisPoints[index] = time;
						//					chartData.put(time, price);

						if (price > max)
							max = price;
						if (price < min)
							min = price;
					}
					index++;
				}

				if (chartView != null && chartView.getVisibility() == View.VISIBLE) {
					IChartTextFormatter<Long> formatter = timePeriod == DataManager.TIME_PERIOD_DAY ? 
							FORMATTER_TIME : FORMATTER_DATE;
					chartView.setAxisX(xAxisPoints, formatter);
					chartView.setData(dataPoints, max, min);
				}
			}
			return null;
		}

		/**
		 * @param key
		 * @param chartData
		 * @return
		 */
		private ChartDataCache getCacheData(String key) {
			final long currentTime = SystemClock.elapsedRealtime();
			
			SoftReference<ChartDataCache> chartDataRef = null;
			ChartDataCache chartData = null;

			Map<String, SoftReference<ChartDataCache>> cacheMap = getCacheMap(timePeriod);
			chartDataRef = cacheMap.get(key);
			
			if (chartDataRef != null) {
				chartData = chartDataRef.get();
				// intra day data has some expiration time
				if (chartData != null && timePeriod == DataManager.TIME_PERIOD_DAY 
						&& currentTime - chartData.creationTime > CACHE_INTRADAY_EXPIRATION) {
					chartData.dataSet.clear();
					cacheMap.remove(key);
					chartData = null;
				}
			}
			return chartData;
		}

		@Override
		protected void onPostExecute(Void result) {	
			if (this.ex != null) {
				Toast.makeText(ChartActivity.this, R.string.failedGetChart, Toast.LENGTH_LONG).show();
			}
			// change ui to show that update is done
			if (chartView != null) {
				chartView.setLoading(false);
				chartView.invalidate();
			} else {
				Log.w(Utils.LOG_TAG, "ChartView is null! Can not set data to chart!");
			}
			
			if (listener != null)
				listener.onChartUpdateFinish();
		}
		
	}
}
