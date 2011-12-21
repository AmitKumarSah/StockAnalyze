package cz.tomas.StockAnalyze.StockList;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.IStockDataProvider;
import cz.tomas.StockAnalyze.Data.Interfaces.IStockDataListener;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.utils.Utils;
import android.content.Context;
import android.os.SystemClock;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

/**
 * Loader for {@link StockItem} and {@link DayData} that can be shown via {@link StockListAdapter}
 * @author tomas
 *
 */
public final class StocksLoader extends AsyncTaskLoader<Map<StockItem, DayData>> implements IStockDataListener {

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
		super.deliverResult(data);
	}

	@Override
	public Map<StockItem, DayData> loadInBackground() {
		Map<StockItem, DayData> items = null;
		try {
			if (Utils.DEBUG) Log.d(Utils.LOG_TAG, "adapter's semaphopre waiting queue: " + semaphore.getQueueLength());
			semaphore.acquire();
			long startTime = 0;
			if (Utils.DEBUG) {
				startTime = SystemClock.elapsedRealtime();
			}
			
			dataManager.acquireDb(this.getClass().getName());
			Map<String,StockItem> stocks = null;
			// first, get all stock items we need
			try {
				stocks = dataManager.getStockItems(market, includeIndeces);
				if (Utils.DEBUG) {
					long diff = SystemClock.elapsedRealtime() - startTime;
					Log.d(Utils.LOG_TAG, "stock loader - stocks read time: " + diff);
				}
			} catch (Exception e) {
				Log.e(Utils.LOG_TAG, "Failed to get stock list. ", e);
			}
			try {
				if (stocks != null) {
					Map<StockItem, DayData> datas = dataManager.getLastDataSet(stocks);
					items = new LinkedHashMap<StockItem, DayData>(datas.size());
					if (Utils.DEBUG) {
						long diff = SystemClock.elapsedRealtime() - startTime;
						Log.d(Utils.LOG_TAG, "stock loader - day data read time: " + diff);
					}
					// we need items sorted the same way as stocks are
					StockItem key = null;
					for (Entry<String, StockItem> entry : stocks.entrySet()) {
						key = entry.getValue();
						items.put(key, datas.get(key));
					}
					Log.d(Utils.LOG_TAG, "StockList: loaded data from database: " + items.size());
				}
			} catch (Exception e) {
				Log.e(Utils.LOG_TAG, "Failed to get stock day data. ", e);
			}

			if (Utils.DEBUG) {
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
		this.cachedData = null;
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
		this.cachedData = null;
		this.startLoading();
	}

	@Override
	public void OnStockDataUpdateBegin(IStockDataProvider sender) {
	}

	@Override
	public void OnStockDataNoUpdate(IStockDataProvider sender) {
	}

	
}
