/**
 * 
 */
package cz.tomas.StockAnalyze.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

/**
 * @author tomas
 *
 */
public class DataManager implements IStockDataListener {
		
	private StockDataSqlStore sqlStore;
	
	private ConnectivityManager connectivityManager = null;
	
	private List<IUpdateDateChangedListener> updateDateChangedListeners;
	private List<IStockDataListener> updateStockDataListeners;
	
	private Context context;
	private static DataManager instance;
	
	public static DataManager getInstance(Context context) {
		if (instance == null)
			instance = new DataManager(context);
		
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
		
		patriaPse.enable(true);
		patriaPse.addListener(this);
		patriaPse.addListener(new NotificationSupervisor(context));
		
		try {
			ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			boolean backgroundData = connectivity.getBackgroundDataSetting();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO
	}

	/*
	 * search for stocks with pattern in name or in ticker,
	 * consider only stocks from given market
	 * use star (*) for all stock items 
	 */
	public List<StockItem> search(String pattern, Market market) throws NullPointerException, FailedToGetDataException {
		// FIXME
		IStockDataProvider provider = DataProviderFactory.getRealTimeDataProvider(market);
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
	
	/*
	 * get all stock items from database for given Market instance
	 */
	public synchronized List<StockItem> getStockItems(Market market) {
		return this.sqlStore.getStockItems(market);
	}
	
	public StockItem getStockItem(String id) throws NullPointerException {
		return getStockItem(id, null);
	}
	public synchronized StockItem getStockItem(String id, Market market) throws NullPointerException {
		StockItem item = this.sqlStore.getStockItem(id);
		
		if (item == null && market != null) {
			IStockDataProvider provider = DataProviderFactory.getDataProvider(market);
			List<StockItem> stocks = provider.getAvailableStockList();
			
			for (int i = 0; i < stocks.size(); i++) {
				if (stocks.get(i).getId().equals(id)) {
					item = stocks.get(i);
					break;
				}
			}
		}
		return item; 
	}
	
	public synchronized DayData getLastOfflineValue(String stockId) {
		DayData data = this.sqlStore.getLastAvailableDayData(stockId);
		return data;
	}
	
	/*
	 * get last data for stock item,
	 * check in db for todays data, try to download new one or ceck in db for older one  
	 */
	public synchronized DayData getLastValue(StockItem item) throws IOException, NullPointerException {
		float val = -1;
		DayData data = null;
		Calendar now = Calendar.getInstance();
		
		data = this.sqlStore.getDayData(now, item);
		// we still can be without data from db - so we need to download it
		// of try to search for older from database
		if (data == null && this.isOnline(this.context)) {
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
		else if (data == null) {
			data = this.getLastOfflineValue(item.getId());
		}
		return data;
	}	

	public boolean isOnline(Context context) {
		try {
			if (this.connectivityManager == null)
				this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = this.connectivityManager.getActiveNetworkInfo();
			return info != null && info.isConnectedOrConnecting();
		} catch (Exception e) {
			return false;
		}
	}

	public synchronized boolean refresh() throws Exception {
		boolean result = DataProviderFactory.refreshAll();

		if (result) {
			fireUpdateDateChanged(Calendar.getInstance().getTimeInMillis());
		}
		return result;
	}
	
	private void fireUpdateDateChanged(long timeInMillis) {
		for (IUpdateDateChangedListener handler : this.updateDateChangedListeners) {
			handler.OnLastUpdateDateChanged(timeInMillis);
		}
	}
	
	private void fireUpdateStockDataListenerUpdate(IStockDataProvider sender) {
		for (IStockDataListener listener : this.updateStockDataListeners) {
			listener.OnStockDataUpdated(sender);
		}
	}

	public void addUpdateChangedListener(IUpdateDateChangedListener listener) {
		this.updateDateChangedListeners.add(listener);
	}
	
	public void addStockDataListener(IStockDataListener listener) {
		this.updateStockDataListeners.add(listener);
	}

	@Override
	public void OnStockDataUpdated(IStockDataProvider sender) {
		Log.d("cz.tomas.StockAnalyze.Data.DataManger", "received stock data update event from " + sender.getId());
		for (StockItem item : sender.getAvailableStockList()) {
			DayData data = sender.getLastData(item.getTicker());
			if (data.getPrice() != 0)
				this.sqlStore.insertDayData(item, data);
		}
		this.fireUpdateStockDataListenerUpdate(sender);
	}

	@Override
	public void OnStockDataUpdateBegin(IStockDataProvider sender) {

	}
	@Override
	public void OnStockDataNoUpdate(IStockDataProvider sender) {

	}
}
