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

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.RadioGroup.OnCheckedChangeListener;
import com.flurry.android.FlurryAgent;
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.activity.base.BaseActivity;
import cz.tomas.StockAnalyze.charts.interfaces.IChartTextFormatter;
import cz.tomas.StockAnalyze.charts.view.CompositeChartView;
import cz.tomas.StockAnalyze.utils.Consts;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.NavUtils;
import cz.tomas.StockAnalyze.utils.Utils;

import java.lang.ref.SoftReference;
import java.util.*;
import java.util.Map.Entry;

/**
 * Base class for activities containing chart view,
 * chartView must be initialized in subclass
 * 
 * @author tomas
 *
 */
public abstract class ChartActivity extends BaseActivity {

	protected static final int DIALOG_PICK_STOCK = 0;

	private class ChartDataCache {
		final Map<Long, Float> dataSet;
		final long creationTime;

		public ChartDataCache(Map<Long, Float> dataSet) {
			this.dataSet = dataSet;
			this.creationTime = SystemClock.elapsedRealtime(); 
		}
		
	}
	
	protected static final String EXTRA_CHART_DAY_COUNT = "cz.tomas.StockAnalyze.chart_day_count";
	protected static final int MAX_CACHE_SIZE = 8;
	
	protected StockItem stockItem;
	protected DataManager dataManager;
	protected int timePeriod = DataManager.TIME_PERIOD_MONTH;
	
	protected CompositeChartView chartView;
	private Button btnRetry;
	
	protected DrawChartTask chartTask;
	protected DayData dayData;
	
	private final static Map<String, SoftReference<ChartDataCache>> chartCacheDataSet = new LinkedHashMap<String, SoftReference<ChartDataCache>>();
	private final static Map<String, SoftReference<ChartDataCache>> chartIntradayCacheDataSet = new LinkedHashMap<String, SoftReference<ChartDataCache>>();
	
	public static final long CACHE_INTRADAY_EXPIRATION = 8 * 60 * 1000;
	
	protected SharedPreferences prefs;
	
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

		this.dataManager = (DataManager) getApplicationContext().getSystemService(Application.DATA_MANAGER_SERVICE);
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		RadioGroup depthGroup = (RadioGroup) this.findViewById(R.id.chartDepthGroup);
		if (depthGroup != null) {
			depthGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					int timePeriod = getDayCountById(checkedId);
					updateTimePeriod(timePeriod, false);
				}
			});
		}
		this.btnRetry = (Button) this.findViewById(R.id.chartRetryButton);
		if (this.btnRetry != null) {
			this.btnRetry.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					updateChart();
					btnRetry.setVisibility(View.GONE);
					chartView.setVisibility(View.VISIBLE);
				}
			});
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		final int id = getIdByDayCount(this.timePeriod);
		View v = findViewById(id);
		if (v != null) {
			((Checkable) v).setChecked(true);
		}
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
		if (chartCacheDataSet != null) {
			chartCacheDataSet.clear();
		}
		if (chartIntradayCacheDataSet != null) {
			chartIntradayCacheDataSet.clear();
		}
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
		int timePeriod = this.getDayCountById(item.getItemId());
		if (timePeriod != 0) {
			updateTimePeriod(timePeriod, false);
			return true;
		} else
			return super.onContextItemSelected(item);
	}

	/**
	 * update time period and redraw the chart
	 * @param timePeriod see {@link DataManager} constants
	 * @param updateUi true to manually select proper radio button
	 */
	protected void updateTimePeriod(int timePeriod, boolean updateUi) {
		this.timePeriod = timePeriod;
		final Editor ed = this.prefs.edit();
		ed.putInt(Utils.PREF_CHART_TIME_PERIOD, timePeriod);
		ed.commit();
		
		if (updateUi) {
			final int id = getIdByDayCount(this.timePeriod);
			View v = findViewById(id);
			if (v != null) {
				((Checkable) v).setChecked(true);
			}
		}
		
		this.updateChart();
		Map<String, String> pars = new HashMap<String, String>(2);
		pars.put(Consts.FLURRY_KEY_CHART_TIME_PERIOD, String.valueOf(timePeriod));
		pars.put(Consts.FLURRY_KEY_CHART_TIME_SOURCE, getClass().getName());
		FlurryAgent.onEvent(Consts.FLURRY_EVENT_CHART_TIME_PERIOD, pars);
	}

	/**
	 * read data from input intent
	 * @param intent intent to read data from
	 * @return true if data was read
	 */
	protected boolean readData(Intent intent) {
		if (intent.hasExtra(NavUtils.STOCK_ITEM_OBJECT)) {
			this.stockItem = intent.getExtras().getParcelable(NavUtils.STOCK_ITEM_OBJECT);
			this.dayData = intent.getExtras().getParcelable(NavUtils.DAY_DATA_OBJECT);			
			return true;
		} else {
			return false;
		}
	}

	protected void updateChart() {
		this.chartTask = new DrawChartTask();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			chartTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this.stockItem);
		} else {
			chartTask.execute(this.stockItem);
		}
	}
	
	protected boolean isChartUpdating() {
        return this.chartTask != null && (
		        this.chartTask.getStatus() == AsyncTask.Status.RUNNING ||
                this.chartTask.getStatus() == AsyncTask.Status.PENDING);
	}


	/**
	 * by resource id get day count, this method is used to translate selected
	 * menu item/radio button to day count
	 * @param id of view in radio group of chart periods
	 * @return number of days
	 */
	protected int getDayCountById(int id) {
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
		case R.id.chart1month:
			timePeriod = DataManager.TIME_PERIOD_MONTH;
			break;
		case R.id.chart1year:
			timePeriod = DataManager.TIME_PERIOD_YEAR;
			break;
		default:
			break;
		}
		return timePeriod;
	}
	
	protected int getIdByDayCount(int timePeriod) {
		int id = 0;
		switch (timePeriod) {
		case DataManager.TIME_PERIOD_DAY:
			id = R.id.chartDay;
			break;
		case DataManager.TIME_PERIOD_QUARTER:
			id = R.id.chart3months;
			break;
		case DataManager.TIME_PERIOD_WEEK:
			id = R.id.chart5days;
			break;
		case DataManager.TIME_PERIOD_HALF_YEAR:
			id = R.id.chart6months;
			break;
		case DataManager.TIME_PERIOD_MONTH:
			id = R.id.chart1month;
			break;
		case DataManager.TIME_PERIOD_YEAR:
			id = R.id.chart1year;
			break;
		default:
			break;
		}
		return id;
	}


	protected void onChartUpdateBegin() {
	}
	
	protected void onChartUpdateFinish() {
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
			onChartUpdateBegin();
			
			if (chartView != null) {
				chartView.setLoading(true);
			}
		}

		@Override
		protected Void doInBackground(StockItem... params) {
			if (params.length == 0 || params[0] == null) {
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
						Log.d(Utils.LOG_TAG, "downloaded chart dataset " + dataSet.size());
						chartCache = new ChartDataCache(dataSet);
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
					}
					if (price >= 0 && time >= 0) {
						dataPoints[index] = price;
						xAxisPoints[index] = time;
						//					chartData.put(time, price);

						if (price > max) {
							max = price;
						}
						if (price < min) {
							min = price;
						}
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
		 * @param key key to cache
		 * @return cached data or null
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
				Toast.makeText(ChartActivity.this, R.string.failedGetChart, Toast.LENGTH_SHORT).show();
				if (btnRetry != null && chartView != null) {
					btnRetry.setVisibility(View.VISIBLE);
					chartView.setVisibility(View.GONE);
				}
			} else {
				if (btnRetry != null && chartView != null) {
					btnRetry.setVisibility(View.GONE);
					chartView.setVisibility(View.VISIBLE);
				}
			}
			// change ui to show that update is done
			if (chartView != null) {
				chartView.setLoading(false);
				chartView.invalidate();
			} else {
				Log.w(Utils.LOG_TAG, "ChartView is null! Can not set data to chart!");
			}

			onChartUpdateFinish();
		}
		
	}
}
