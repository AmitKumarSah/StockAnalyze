package cz.tomas.StockAnalyze.StockList;

import android.content.Context;
import android.os.SystemClock;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.IStockDataProvider;
import cz.tomas.StockAnalyze.Data.Interfaces.IStockDataListener;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.utils.Utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

/**
 * Loader for {@link StockItem} and {@link DayData} that can be shown via {@link StockListAdapter}
 * @author tomas
 *
 */
public final class StocksLoader extends AsyncTaskLoader<Map<StockItem, DayData>> implements IStockDataListener {

	public static final boolean DEBUG = Utils.DEBUG;
	private final Semaphore semaphore;
	private final DataManager dataManager;
	private final Market market;
	private final boolean includeIndeces;
	
	private Map<StockItem, DayData> cachedData;
	
	public StocksLoader(Context context, Market market, boolean includeIndeces) {
		super(context);
		this.includeIndeces = includeIndeces;
		this.semaphore = new Semaphore(1);
		this.dataManager = (DataManager) context.getApplicationContext().getSystemService(Application.DATA_MANAGER_SERVICE);
		this.market = market;
	}

	@Override
	public void deliverResult(Map<StockItem, DayData> data) {
		this.cachedData = data;
		if (this.isStarted()) {
			super.deliverResult(data);
		}
	}

	@Override
	public void onCanceled(Map<StockItem, DayData> data) {
		// TODO Auto-generated method stub
		super.onCanceled(data);
	}

	@Override
	public Map<StockItem, DayData> loadInBackground() {
		Map<StockItem, DayData> items = null;
		try {
			if (DEBUG) Log.d(Utils.LOG_TAG, "adapter's semaphore waiting queue: " + semaphore.getQueueLength());
			semaphore.acquire();
			long startTime;
			if (DEBUG) {
				startTime = SystemClock.elapsedRealtime();
			}
			
			dataManager.acquireDb(this.getClass().getName());
			Map<String,StockItem> stocks = null;
			// first, get all stock items we need
			try {
				stocks = dataManager.getStockItems(market, includeIndeces);
				if (DEBUG) {
					long diff = SystemClock.elapsedRealtime() - startTime;
					Log.d(Utils.LOG_TAG, "stock loader - stocks read time: " + diff);
				}
			} catch (Exception e) {
				Log.e(Utils.LOG_TAG, "Failed to get stock list. ", e);
			}
			try {
				if (stocks != null) {
					Map<StockItem, DayData> dataSets = dataManager.getLastDataSet(stocks);
					items = new LinkedHashMap<StockItem, DayData>(dataSets.size());
					if (DEBUG) {
						long diff = SystemClock.elapsedRealtime() - startTime;
						Log.d(Utils.LOG_TAG, "stock loader - day data read time: " + diff);
					}
					// we need items sorted the same way as stocks are
					StockItem key;
					for (Entry<String, StockItem> entry : stocks.entrySet()) {
						key = entry.getValue();
						items.put(key, dataSets.get(key));
					}
					Log.d(Utils.LOG_TAG, "StockList: loaded data from database: " + items.size());
				}
			} catch (Exception e) {
				Log.e(Utils.LOG_TAG, "Failed to get stock day data. ", e);
			}

			if (DEBUG) {
				long diff = SystemClock.elapsedRealtime() - startTime;
				Log.d(Utils.LOG_TAG, "stock loader time: " + diff);
			}
		} catch (InterruptedException e) {
			Log.e(Utils.LOG_TAG, "semaphore was interrupted", e);
		} finally {
			dataManager.releaseDb(true, this.getClass().getName());
			semaphore.release();
		}
		return items;
	}

	@Override
	protected void onStartLoading() {
		if (this.cachedData != null) {
			this.deliverResult(this.cachedData);
		} else {
			this.forceLoad();
		}
	}

	@Override
	protected void onStopLoading() {
		super.onStopLoading();
		this.cancelLoad();
	}

	@Override
	protected void onReset() {
		super.onReset();
		this.cachedData = null;

        onStopLoading();
	}

	@Override
	public void registerListener(int id, OnLoadCompleteListener<Map<StockItem, DayData>> listener) {
		super.registerListener(id, listener);
		this.dataManager.addStockDataListener(this);
	}

	@Override
	public void unregisterListener(OnLoadCompleteListener<Map<StockItem, DayData>> listener) {
		super.unregisterListener(listener);
		this.dataManager.removeStockDataListener(this);
	}

	@Override
	public void OnStockDataUpdated(IStockDataProvider sender,
			Map<String, DayData> dataMap) {
		if (! this.isAbandoned()) {
			this.cachedData = null;
			this.startLoading();
		}
	}

	@Override
	public void OnStockDataUpdateBegin(IStockDataProvider sender) {
	}

	@Override
	public void OnStockDataNoUpdate(IStockDataProvider sender) {
	}

	
}
