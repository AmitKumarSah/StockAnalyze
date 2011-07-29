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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import cz.tomas.StockAnalyze.NotificationSupervisor;
import cz.tomas.StockAnalyze.Data.Interfaces.IStockDataListener;
import cz.tomas.StockAnalyze.Data.Interfaces.IUpdateDateChangedListener;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.Data.PseCsvData.PseCsvDataAdapter;
import cz.tomas.StockAnalyze.Data.PsePatriaData.PsePatriaDataAdapter;
import cz.tomas.StockAnalyze.Data.exceptions.FailedToGetDataException;
import cz.tomas.StockAnalyze.StockList.StockComparator;
import cz.tomas.StockAnalyze.StockList.StockCompareTypes;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * main class for managing stock data, 
 * this class is singleton
 * 
 * @author tomas
 *
 */
public class DataManager implements IStockDataListener {

	private int STOCK_LIST_EXPIRATION_DAYS = 30;
	
	private StockDataSqlStore sqlStore;
		
	private List<IUpdateDateChangedListener> updateDateChangedListeners;
	private List<IStockDataListener> updateStockDataListeners;
	
	private Context context;
	private static DataManager instance;
	
	public static DataManager getInstance(Context context) {
		if (instance == null)
			instance = new DataManager(context.getApplicationContext());
		
		return instance;
	}
	
	private DataManager(Context context) {
		this.context = context;
		
		this.sqlStore = new StockDataSqlStore(context);
		
		IStockDataProvider pse = new PseCsvDataAdapter();
		IStockDataProvider patriaPse = new PsePatriaDataAdapter();
		
		DataProviderFactory.registerDataProvider(pse);
		DataProviderFactory.registerDataProvider(patriaPse);
		
		this.updateDateChangedListeners = new ArrayList<IUpdateDateChangedListener>();
		this.updateStockDataListeners = new ArrayList<IStockDataListener>();
		
		NotificationSupervisor supervisor = new NotificationSupervisor(context);
		patriaPse.enable(true);
		patriaPse.addListener(this);
		patriaPse.addListener(supervisor);
		
		pse.enable(true);
		pse.addListener(this);
		//pse.addListener(supervisor);
	}
	
	public static boolean isInitialized() {
		return instance != null;
	}

	/**
	 * search for stocks with pattern in name or in ticker,
	 * consider only stocks from given market
	 * use star (*) for all stock items 
	 */
	public List<StockItem> search(String pattern, Market market) throws NullPointerException, FailedToGetDataException {
		// FIXME
		IStockDataProvider provider = DataProviderFactory.getRealTimeDataProvider(market);
		if (provider == null)
			throw new NullPointerException("failed to find appropriate data provider to search for stock");
		List<StockItem> stocks = provider.getAvailableStockList();
		if (stocks == null)
			throw new NullPointerException("can't get list of available stock items");
		List<StockItem> results = new ArrayList<StockItem>();
		
		if (pattern.equals("*"))
			results = stocks;
		else {
			for (StockItem stock : stocks) {
				if (stock != null && stock.getTicker() != null
						&& stock.getName() != null) {
					// search for pattern
					if (stock.getTicker().contains(pattern.toUpperCase())
							|| stock.getName().contains(pattern.toUpperCase()))
						results.add(stock);
				}
			}
		}
		return results;
	}
	
	/**
	 * get all stock items from database for given Market,
	 * if stock items weren't found, would try to download them
	 * @returns map stock id vs StockOte,
	 */
	public synchronized Map<String, StockItem> getStockItems(Market market) {
		SharedPreferences prefs = this.context.getSharedPreferences(Utils.PREF_NAME, 0);
		
		long lastUpdate = prefs.getLong(Utils.PREF_LAST_STOCK_LIST_UPDATE_TIME, 0);
		long diff = System.currentTimeMillis() - lastUpdate; 
		long dayDiff = diff / (1000 * 60 * 60 * 24); 
		Map<String, StockItem> items = this.sqlStore.getStockItems(market, "ticker");
		if (items == null || items.size() == 0 || dayDiff > STOCK_LIST_EXPIRATION_DAYS) {
			items = downloadStockItems(market);
			prefs.edit().putLong(Utils.PREF_LAST_STOCK_LIST_UPDATE_TIME, System.currentTimeMillis()).commit();
		}
			
		return items;
	}

	/**
	 * download stock items using StockProvider
	 * @param market
	 * @return
	 */
	private Map<String, StockItem> downloadStockItems(Market market) {
		Log.d(Utils.LOG_TAG, "downloading stock item list");
		Map<String, StockItem> items;
		IStockDataProvider provider = DataProviderFactory.getDataProvider(market);
		List<StockItem> stocks = provider.getAvailableStockList();
		Collections.sort(stocks, new StockComparator(StockCompareTypes.Ticker, null));
		
		items = new LinkedHashMap<String, StockItem>();
		for (StockItem stockItem : stocks) {
			items.put(stockItem.getId(), stockItem);
		}
		return items;
	}
	
	/**
	 * get single stock item based on its id
	 * 
	 * @param id
	 * @return
	 * @throws NullPointerException
	 */
	public StockItem getStockItem(String id) throws NullPointerException {
		return getStockItem(id, null);
	}
	
	public StockItem getStockItem(String id, Market market) throws NullPointerException {
		StockItem item = this.sqlStore.getStockItem(id);
		
		if (item == null) {
			Map<String, StockItem> items = downloadStockItems(market);
			item = items.get(id);
		}
		return item; 
	}
	
	public synchronized DayData getLastOfflineValue(String stockId) {
		DayData data = this.sqlStore.getLastAvailableDayData(stockId);
		return data;
	}
	
	/**
	 * get last day data for set of stock items
	 * Map of StockId: DayData
	 */
	public synchronized Map<StockItem,DayData>  getLastDataSet(Map<String, StockItem> stockItems) {
		Map<StockItem,DayData> dbData = this.sqlStore.getLastDataSet(stockItems, null, null);
		return dbData;
	}
	
	public synchronized DayData[] getDayDataSet(StockItem item, Calendar cal, int count, boolean includeToday) throws FailedToGetDataException, IOException {
		DayData[] dataSet = new DayData[count];
		
		Calendar currentCal = Utils.getLastValidDate(cal);
		if (! includeToday) {
			currentCal.roll(Calendar.DAY_OF_YEAR, false);
			currentCal = Utils.getLastValidDate(currentCal);
		}
		this.acquireDb(this);
		try {
			for (int i = dataSet.length - 1; i >= 0; i--) {
				try {
					dataSet[i] = this.getDayData(item, currentCal);
				} catch (Exception e) {
					Log.e(Utils.LOG_TAG, "failed to get data for "
							+ FormattingUtils.formatStockDate(currentCal), e);
				}
				currentCal.roll(Calendar.DAY_OF_YEAR, false);
				currentCal = Utils.getLastValidDate(currentCal);
			}
		} finally {
			this.releaseDb(true, this);
		}
		return dataSet;
	}
	
	public synchronized DayData getDayData(StockItem item, Calendar cal) throws FailedToGetDataException, IOException {
		DayData data = this.sqlStore.getDayData(cal, item);
		// if data is null, we will have to download them
		if (data == null) {
			Log.d(Utils.LOG_TAG, "day data for " + FormattingUtils.formatStockDate(cal) + " wasn't found in db, downloading...");
			IStockDataProvider provider = DataProviderFactory.getHistoricalDataProvider(MarketFactory.getCzechMarket());
			data = provider.getDayData(item.getTicker(), cal);
		}
		
		return data;
	}
	
	/**
	 * get last data for stock item,
	 * check in db for todays data, try to download new one or check in db for older one  
	 */
	public synchronized DayData getLastValue(StockItem item) throws IOException, NullPointerException {
		float val = -1;
		DayData data = null;
		Calendar now = Calendar.getInstance();
		
		data = this.sqlStore.getDayData(now, item);
		// we still can be without data from db - so we need to download it
		// of try to search for older from database
		if (data == null && Utils.isOnline(this.context)) {
			try {
				IStockDataProvider provider = DataProviderFactory.getDataProvider(item.getTicker());
				if (provider != null) {
					data = provider.getLastData(item.getTicker());
					val = data.getPrice();
				}
				else
					throw new NullPointerException("Can't find appropriate data provider for " + item.toString());
			} catch (NullPointerException e) {
				e.printStackTrace();
				throw e;
			}
			if (val > 0) {
				this.sqlStore.insertDayData(item, data);
			}
		}
		if (data == null ) {
			data = this.getLastOfflineValue(item.getId());
		} else if (data.getPrice() == 0) {
			// this is special case when the data is downloaded, but the price is not valid,
			// so we take old data's price and set it to the output DayData object
			data = createDataWithPrice(item, data);
		}
		return data;
	}

	/**
	 * mix last available off-line data with new one, 
	 * the purpose is to get reasonable data if there is no price
	 * from data provider 
	 * @param item stock item to get data for
	 * @param data data from provider
	 * @return data from provider with price of last off-line data
	 */
	private DayData createDataWithPrice(StockItem item, DayData data) {
		DayData oldData = this.getLastOfflineValue(item.getId());
		if (oldData != null)
			data = new DayData(oldData.getPrice(), data.getChange(), data.getDate(), data.getVolume(), data.getYearMaximum(), data.getYearMinimum(),
					data.getLastUpdate(), data.getId());
		return data;
	}	

//	/**
//	 * refresh all enabled data providers
//	 */
//	public synchronized boolean refresh() throws Exception {
//		boolean result = DataProviderFactory.refreshAll();
//
////		if (result) {
////			fireUpdateDateChanged(Calendar.getInstance().getTimeInMillis());
////		}
//		return result;
//	}
	
	private void fireUpdateDateChanged(long timeInMillis) {
		for (IUpdateDateChangedListener handler : this.updateDateChangedListeners) {
			handler.OnLastUpdateDateChanged(timeInMillis);
		}
	}
	
	private void fireUpdateStockDataListenerUpdate(IStockDataProvider sender, Map<StockItem, DayData> dataMap) {
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
	public void OnStockDataUpdated(IStockDataProvider sender, Map<StockItem,DayData> dataMap) {
		Log.i(Utils.LOG_TAG, "received stock data update event from " + sender.getId());
		this.acquireDb(sender.getId());
		try {
			if (dataMap == null || dataMap.size() == 0) {
				Map<StockItem, DayData> receivedData = new HashMap<StockItem, DayData>();
				for (StockItem item : sender.getAvailableStockList()) {
					DayData data = sender.getLastData(item.getTicker());
					if (data.getPrice() == 0)
						data = this.createDataWithPrice(item, data);
					receivedData.put(item, data);
				}

				this.sqlStore.insertDayDataSet(receivedData);
			} else {
				for (Entry<StockItem, DayData> entry : dataMap.entrySet()) {
					this.sqlStore.insertDayData(entry.getKey(), entry.getValue());
				}
			}
			this.fireUpdateDateChanged(Calendar.getInstance().getTimeInMillis());
			this.fireUpdateStockDataListenerUpdate(sender, dataMap);
		} finally {
			this.releaseDb(true, sender.getId());
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
