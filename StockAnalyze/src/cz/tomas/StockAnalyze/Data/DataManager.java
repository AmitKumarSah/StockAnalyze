/**
 * 
 */
package cz.tomas.StockAnalyze.Data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.tomas.StockAnalyze.Data.IStockDataProvider;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.Data.PseCsvData.PseCsvDataProvider;
import cz.tomas.StockAnalyze.Data.PsePatriaData.PsePatriaDataAdapter;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * @author tomas
 *
 */
public class DataManager {
		
	StockDataSqlStore sqlStore;
	
	List<IUpdateDateChangedHandler> updateDateChangedListeners;
	
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
		
		IStockDataProvider pse = new PseCsvDataProvider();
		IStockDataProvider patriaPse = new PsePatriaDataAdapter();
		
		DataProviderFactory.registerDataProvider(pse);
		DataProviderFactory.registerDataProvider(patriaPse);
		
		this.updateDateChangedListeners = new ArrayList<IUpdateDateChangedHandler>();
	}

	public List<StockItem> search(String pattern) {
		// FIXME
		IStockDataProvider provider = DataProviderFactory.getRealTimeDataProvider(new Market("PSE", "XPRA", "CZK", null));
		List<StockItem> stocks = provider.getAvailableStockList();
		if (stocks == null)
			throw new NullPointerException("can't get list of available stock items");
		List<StockItem> results = new ArrayList<StockItem>();
		
		for (StockItem stock : stocks) {
			if (stock.getTicker().contains(pattern.toUpperCase()) ||
					stock.getName().contains(pattern.toUpperCase()))
				results.add(stock);
		}
		return results;
	}
	
	public StockItem getStockItem(String id, Market market) {
		IStockDataProvider provider = DataProviderFactory.getDataProvider(market);
		List<StockItem> stocks = provider.getAvailableStockList();
		
		// TODO find in db
		for (int i = 0; i < stocks.size(); i++) {
			if (stocks.get(i).getId().equals(id))
				return stocks.get(i);
		}
		return null; 
	}
	
	public DayData getLastValue(String ticker) throws IOException, NullPointerException {
		float val = -1;
		DayData data = null;
		try {
			IStockDataProvider provider = DataProviderFactory.getDataProvider(ticker);
			if (provider != null) {
				data = provider.getLastData(ticker);
				val = data.getPrice();
			}
			else
				throw new NullPointerException("Can't find appropriate data provider for " + ticker);
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw e;
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			throw e;
		}
		
//		if (val > 0) {
//			try {
//				SQLiteDatabase db = this.sqlStore.getWritableDatabase();
//				db.execSQL("INSERT INTO " + StockDataSqlStore.TABLE_NAME + " values('"+ ticker + "', date('now'), " + val + ");");
//			} catch (SQLException e) {
//				Log.d("StockDataSqlStore", "failed to insert data." + e.getMessage());
//				e.printStackTrace();
//			} finally {
//				if (this.sqlStore != null)
//					this.sqlStore.close();
//			}
//		}
		return data;
	}	

	public boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.isConnectedOrConnecting();
	}

	public boolean refresh() throws Exception {
		boolean result = DataProviderFactory.refreshAll();

		if (result)
			fireUpdateDateChanged(Calendar.getInstance().getTimeInMillis());
		return result;
	}
	
	private void fireUpdateDateChanged(long timeInMillis) {
		for (IUpdateDateChangedHandler handler : this.updateDateChangedListeners) {
			handler.OnLastUpdateDateChanged(timeInMillis);
		}
	}

	public void addUpdateChangedListener(IUpdateDateChangedHandler handler) {
		this.updateDateChangedListeners.add(handler);
	}
	
	public interface IUpdateDateChangedHandler {
		public void OnLastUpdateDateChanged(long updateTime);
	}
}
