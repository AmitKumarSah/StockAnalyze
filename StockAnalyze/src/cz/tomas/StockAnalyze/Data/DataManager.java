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
package cz.tomas.StockAnalyze.Data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.Data.GaeData.GaeIndecesDataAdapter;
import cz.tomas.StockAnalyze.Data.GaeData.GaePseDataAdapter;
import cz.tomas.StockAnalyze.Data.GaeData.GaeXetraAdapter;
import cz.tomas.StockAnalyze.Data.Interfaces.IMarketListener;
import cz.tomas.StockAnalyze.Data.Interfaces.IStockDataListener;
import cz.tomas.StockAnalyze.Data.Interfaces.IUpdateDateChangedListener;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.Data.exceptions.FailedToGetDataException;
import cz.tomas.StockAnalyze.utils.Markets;
import cz.tomas.StockAnalyze.utils.Utils;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * main class for managing stock data, 
 * this class is singleton and accessible from 
 * {@link Application#getSystemService(String)}
 * 
 * @author tomas
 *
 */
public class DataManager implements IStockDataListener {

	private static final int DAY_IN_MILLISECONDS = 1000 * 60 * 60 * 24;

	private static final int MARKET_LIST_EXPIRATION_DAYS = 2;

	public static final int TIME_PERIOD_DAY = 1;
	public static final int TIME_PERIOD_WEEK = 2;
	public static final int TIME_PERIOD_MONTH = 3;
	public static final int TIME_PERIOD_QUARTER = 4;
	public static final int TIME_PERIOD_HALF_YEAR = 5;
	public static final int TIME_PERIOD_YEAR = 6;
	
	private final StockDataSqlStore sqlStore;
		
	private List<IUpdateDateChangedListener> updateDateChangedListeners;
	private List<IStockDataListener> updateStockDataListeners;
	private Map<String, Market> markets;
	
	private final Context context;
	private static DataManager instance;
	private final List<IMarketListener> marketListeners;
	private boolean isMarketUpdateRunning;

	private Handler handler;

	public static DataManager getInstance(Context context) {
		if (instance == null) {
			instance = new DataManager(context.getApplicationContext());
		}
		
		return instance;
	}
	
	private DataManager(Context context) {
		this.context = context;
		this.handler = new Handler();
		this.marketListeners = new ArrayList<IMarketListener>();
		
		this.sqlStore = StockDataSqlStore.getInstance(context);

		IStockDataProvider gaePse = new GaePseDataAdapter(context);
		IStockDataProvider gaeIndices = new GaeIndecesDataAdapter(context);
		IStockDataProvider gaeXetra = new GaeXetraAdapter(context);

		DataProviderFactory.registerDataProvider(gaePse);
		DataProviderFactory.registerDataProvider(gaeIndices);
		DataProviderFactory.registerDataProvider(gaeXetra);
		
		this.updateDateChangedListeners = new ArrayList<IUpdateDateChangedListener>();
		this.updateStockDataListeners = new ArrayList<IStockDataListener>();

		gaeXetra.enable(true);
		gaeXetra.addListener(this);
		
		gaePse.enable(true);
		gaePse.addListener(this);
		
		gaeIndices.enable(true);
		gaeIndices.addListener(this);

		// load markets, this is essential for whole application
		Thread marketsThread = new Thread(new Runnable() {
			@Override
			public void run() {
				loadMarkets();
			}
		});
		marketsThread.start();
	}
	
	public static boolean isInitialized() {
		return instance != null;
	}

	public void addMarketListener(IMarketListener marketListener) {
		this.marketListeners.add(marketListener);
	}
	
	public boolean removeMarketListener(IMarketListener marketListener) {
		return this.marketListeners.remove(marketListener);
	}

	/**
	 * get supported markets
	 * @return collection of markets or null if they are not yet available
	 */
	public Collection<Market> getMarkets() {
		if (this.markets == null || this.markets.size() == 0) {
			// if we aren't already loading markets, start so
			if (! isMarketUpdateRunning) {
				this.loadMarkets();
			}
			return null;
		}
		return this.markets.values();
	}

	/**
	 * initial data load for whole application,
	 * load or download markets, refresh them if necessary
	 * and also check for stock items
	 */
	private void loadMarkets() {
		isMarketUpdateRunning = true;
		this.sqlStore.acquireDb(this);
		this.markets = this.sqlStore.getMarkets();
		boolean isDirty = isMarketListDirty();
		if (this.markets == null || this.markets.size() == 0 || isDirty) {
			Log.d(Utils.LOG_TAG, "downloading markets, current: " + this.markets);
 			MarketProvider provider = new MarketProvider();
			try {
				this.markets = provider.getMarkets(this.context);
				this.sqlStore.updateMarkets(this.markets);

				for (Market market : markets.values()) {
					this.downloadStockItems(market);
				}
				this.downloadStockItems(Markets.GLOBAL);
				SharedPreferences preferences = this.context.getSharedPreferences(Utils.PREF_NAME, 0);
				preferences.edit()
						.putLong(Utils.PREF_LAST_STOCK_LIST_UPDATE_TIME, System.currentTimeMillis())
						.putLong(Utils.PREF_LAST_MARKET_LIST_UPDATE_TIME, System.currentTimeMillis())
						.commit();
			} catch (Exception e) {
				Log.e(Utils.LOG_TAG, "failed to read markets", e);
			}
		}
		this.sqlStore.releaseDb(true, this);

		this.handler.post(new Runnable() {
			@Override
			public void run() {
				fireMarketReady();
				isMarketUpdateRunning = false;
			}
		});
	}

	private void fireMarketReady() {
		for (IMarketListener listener : marketListeners) {
			if (this.markets == null) {
				listener.onMarketsAvailable(null);
			} else {
				final Market[] allMarkets = new Market[this.markets.size() + 1];
				this.markets.values().toArray(allMarkets);
				allMarkets[allMarkets.length -1] = Markets.GLOBAL;

				listener.onMarketsAvailable(allMarkets);
			}
		}
	}

	/**
	 * get all stock items from database for given Market,
	 * if stock items weren't found, would try to download them
	 *
	 * @param market market of stocks we want
	 * @return map stock id vs StockItem, ordered by stock ticker
	 */
	public synchronized Map<String, StockItem> getStockItems(Market market) {
		Map<String, StockItem> items = this.sqlStore.getStockItems(market, DataSqlHelper.StockColumns.NAME);
			
		return items;
	}

//	/**
//	 * check if stock list was downloaded and isn't too old
//	 *
//	 * @return true if stock list needs to be refreshed
//	 */
//	private boolean isStockListDirty() {
//		SharedPreferences preferences = this.context.getSharedPreferences(Utils.PREF_NAME, 0);
//
//		long lastUpdate = preferences.getLong(Utils.PREF_LAST_STOCK_LIST_UPDATE_TIME, 0);
//		long diff = System.currentTimeMillis() - lastUpdate;
//		long dayDiff = diff / DAY_IN_MILLISECONDS;
//		boolean isStockListDirty = dayDiff > STOCK_LIST_EXPIRATION_DAYS;
//
//		return isStockListDirty;
//	}

	private boolean isMarketListDirty() {
		SharedPreferences preferences = this.context.getSharedPreferences(Utils.PREF_NAME, 0);

		long lastUpdate = preferences.getLong(Utils.PREF_LAST_MARKET_LIST_UPDATE_TIME, 0);
		long diff = System.currentTimeMillis() - lastUpdate;
		long dayDiff = diff / DAY_IN_MILLISECONDS;
		boolean isMarketListDirty = dayDiff > MARKET_LIST_EXPIRATION_DAYS;

		return isMarketListDirty;
	}

	/**
	 * download stock items using StockProvider
	 * and store them in database
	 * @param market market to download stocks for
	 * @return downloaded stock items
	 */
	private Map<String, StockItem> downloadStockItems(Market market) {
		Log.d(Utils.LOG_TAG, "downloading stock item list for " + market);
		if (market == null) {
			throw new NullPointerException("market can't be null to get stock list");
		}
		IStockDataProvider provider = DataProviderFactory.getDataProvider(market);
		List<StockItem> stocks = provider.getAvailableStockList();
		
		Map<String, StockItem> items = null;
		if (stocks != null && stocks.size() > 0) {
			items = this.sqlStore.updateStockList(stocks, market);
		}
		return items;
	}
	
	/**
	 * get single stock item based on its id
	 * 
	 * @param id stick id to query stock item in db
	 * @return found sotck item or null
	 * @throws NullPointerException
	 */
	public StockItem getStockItem(String id) throws NullPointerException {
		StockItem item = this.sqlStore.getStockItem(id);
		return item;
	}

	/**
	 * return last data from database, not even trying to go online
	 * @param stockId id of stock
	 * @return data for given stock or null if not found
	 */
	public synchronized DayData getLastOfflineValue(String stockId) {
		DayData data = this.sqlStore.getDayData(stockId);
		return data;
	}

	public Map<StockItem,DayData> getLastDataSet(Market market) {
		Map<StockItem,DayData> dbData = this.sqlStore.getLastDataSet(market, null);
		return dbData;
	}
	
	/**
	 * get historical or intraday data
	 * 
	 * @param item see {@link DataManager} constant fields
	 * @param timePeriod chart time period, see {@link}
	 * @param includeToday
	 * @return
	 * @throws FailedToGetDataException
	 * @throws IOException
	 */
	public Map<Long, Float> getDayDataSet(StockItem item, int timePeriod, boolean includeToday) throws FailedToGetDataException, IOException {
		Calendar currentCal = Utils.getLastValidDate(Calendar.getInstance(Utils.PRAGUE_TIME_ZONE));
		if (! includeToday) {
			currentCal.roll(Calendar.DAY_OF_YEAR, false);
			currentCal = Utils.getLastValidDate(currentCal);
		}
		
		IStockDataProvider provider = DataProviderFactory.getHistoricalDataProvider(item);
		
		Map<Long, Float> dataSet = null;
		if (timePeriod != TIME_PERIOD_DAY) {
			dataSet = provider.getHistoricalPriceSet(item.getTicker(), timePeriod);
		} else {
			dataSet = provider.getIntraDayData(item.getTicker(), null);
		}
		return dataSet;
	}
	
	/**
	 * get last data for stock item,
	 * check in db for todays data, try to download new one or check in db for older one  
	 */
	public synchronized DayData getLastValue(StockItem item) throws IOException, NullPointerException {
		float val = -1;
		DayData data = null;
		//Calendar now = Calendar.getInstance();
		
		data = this.sqlStore.getDayData(item.getId());
		// we still can be without data from db - so we need to download it
		// of try to search for older from database
		if (data == null && Utils.isOnline(this.context)) {
			try {
				IStockDataProvider provider = DataProviderFactory.getDataProvider(item.getMarket());
				if (provider != null) {
					data = provider.getLastData(item.getTicker());
					val = data.getPrice();
				} else {
					throw new NullPointerException("Can't find appropriate data provider for " + item.toString());
				}
			} catch (NullPointerException e) {
				Log.e(Utils.LOG_TAG, "failed to get day dat for " + item, e);
				throw e;
			}
			if (val > 0) {
				this.sqlStore.insertDayData(item.getId(), data);
			}
		}
		if (data == null ) {
			data = this.getLastOfflineValue(item.getId());
		} else if (data.getPrice() == 0) {
			// this is special case when the data is downloaded, but the price is not valid,
			// so we take old data's price and set it to the output DayData object
			// (it happens in patria provider and zero is reported
			// if there wasn't any trade yet)
			data = createDataWithPrice(item, data);
		}
		return data;
	}

	/**
	 * mix last available off-line data with new one, 
	 * the purpose is to get reasonable data if there is no price
	 * from data provider - it may happen if there wasn't any trade yet
	 * and provider reports price as a zero
	 * 
	 * @param item stock item to get data for
	 * @param data data from provider
	 * @return data from provider with price of last off-line data
	 */
	private DayData createDataWithPrice(StockItem item, DayData data) {
		DayData oldData = this.getLastOfflineValue(item.getId());
		if (oldData != null) {
			data = new DayData(oldData.getPrice(), data.getChange(), data.getDate(), data.getVolume(), data.getYearMaximum(), data.getYearMinimum(),
					data.getLastUpdate(), data.getId());
		}
		return data;
	}	
	
	private void fireUpdateDateChanged(long timeInMillis) {
		for (IUpdateDateChangedListener handler : this.updateDateChangedListeners) {
			handler.OnLastUpdateDateChanged(timeInMillis);
		}
	}
	
	private void fireUpdateStockDataListenerUpdate(IStockDataProvider sender, Map<String, DayData> dataMap) {
		//this.lastUpdateTime = SystemClock.elapsedRealtime();
		for (IStockDataListener listener : this.updateStockDataListeners) {
			listener.OnStockDataUpdated(sender, dataMap);
		}
	}

	/**
	 * register new listener for last update date changed, 
	 * don't forget to call removeUpdateListener when yuo don't need it anymore
	 * @param listener
	 */
	public void addUpdateChangedListener(IUpdateDateChangedListener listener) {
		this.updateDateChangedListeners.add(listener);
	}
	
	/**
	 * remove update date changed listener
	 * @param listener
	 * @return true if listener was removed
	 */
	public boolean removeUpdateChangedListener(IUpdateDateChangedListener listener) {
		return this.updateDateChangedListeners.remove(listener);
	}
	
	/**
	 * add listener to stock data updates
	 * @param listener
	 */
	public void addStockDataListener(IStockDataListener listener) {
		this.updateStockDataListeners.add(listener);
	}
	
	/**
	 * remove stock data listener
	 * @param listener
	 * @return true if listener was removed
	 */
	public boolean removeStockDataListener(IStockDataListener listener) {
		return this.updateStockDataListeners.remove(listener);
	}

	/**
	 * event from data provider, store data to db and fire events
	 */
	@Override
	public void OnStockDataUpdated(IStockDataProvider sender, Map<String, DayData> dataMap) {
		Log.i(Utils.LOG_TAG, "received stock data update event from " + sender.getId());
		this.sqlStore.acquireDb(sender.getId());
		try {
			if (dataMap == null || dataMap.size() == 0) {
				Map<String, DayData> receivedData = new HashMap<String, DayData>();
				for (StockItem item : sender.getAvailableStockList()) {
					DayData data = sender.getLastData(item.getTicker());
					if (data.getPrice() == 0) {
						data = this.createDataWithPrice(item, data);
					}
					receivedData.put(item.getId(), data);
				}

				this.sqlStore.insertDayDataSet(receivedData);
			} else {
				for (Entry<String, DayData> entry : dataMap.entrySet()) {
					this.sqlStore.insertDayData(entry.getKey(), entry.getValue());
				}
			}
			this.fireUpdateDateChanged(Calendar.getInstance().getTimeInMillis());
			this.fireUpdateStockDataListenerUpdate(sender, dataMap);
		} finally {
			this.sqlStore.releaseDb(true, sender.getId());
		}
	}
	
	/**
	 * tell the database to not close until method releaseDb is called
	 */
	public void acquireDb(Object applicant) {
		this.sqlStore.acquireDb(applicant);
	}
	
	/**
	 * release lock created by acquireDb() calling
	 */
	public void releaseDb(boolean close, Object applicant) {
		this.sqlStore.releaseDb(close, applicant);
	}

	@Override
	public void OnStockDataUpdateBegin(IStockDataProvider sender) {
	}
	@Override
	public void OnStockDataNoUpdate(IStockDataProvider sender) {
	}
}
