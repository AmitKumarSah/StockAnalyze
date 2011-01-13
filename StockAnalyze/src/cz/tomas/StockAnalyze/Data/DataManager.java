/**
 * 
 */
package cz.tomas.StockAnalyze.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cz.tomas.StockAnalyze.NotificationSupervisor;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.Data.IStockDataProvider;
import cz.tomas.StockAnalyze.Data.Interfaces.IStockDataListener;
import cz.tomas.StockAnalyze.Data.Interfaces.IUpdateDateChangedListener;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.Data.PseCsvData.PseCsvDataAdapter;
import cz.tomas.StockAnalyze.Data.PsePatriaData.PsePatriaDataAdapter;
import cz.tomas.StockAnalyze.Data.exceptions.FailedToGetDataException;

import android.app.NotificationManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * @author tomas
 *
 */
public class DataManager implements IStockDataListener {
		
	StockDataSqlStore sqlStore;
	
	List<IUpdateDateChangedListener> updateDateChangedListeners;
	List<IStockDataListener> updateStockDataListeners;
	
	Context context;
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
		
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean backgroundData = connectivity.getBackgroundDataSetting();
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
	
	public StockItem getStockItem(String id, Market market) throws NullPointerException {
		IStockDataProvider provider = DataProviderFactory.getDataProvider(market);
		List<StockItem> stocks = provider.getAvailableStockList();
		
		StockItem item = this.sqlStore.getStockItem(id);
		
		if (item == null) {
			for (int i = 0; i < stocks.size(); i++) {
				if (stocks.get(i).getId().equals(id)) {
					item = stocks.get(i);
					break;
				}
			}
		}
		return item; 
	}
	
	public DayData getLastValue(StockItem item) throws IOException, NullPointerException {
		float val = -1;
		DayData data = null;
		Calendar now = Calendar.getInstance();
		//if (this.sqlStore.checkForData(item, now))
		
		data = this.sqlStore.getDayData(now, item);
		// we still can be without data from db - so we need to download it
		if (data == null) {
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
		return data;
	}	

	public boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.isConnectedOrConnecting();
	}

	public boolean refresh() throws Exception {
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
			this.sqlStore.insertDayData(item, sender.getLastData(item.getTicker()));
		}
		this.fireUpdateStockDataListenerUpdate(sender);
	}

	@Override
	public void OnStockDataUpdateBegin(IStockDataProvider sender) {

	}
}
