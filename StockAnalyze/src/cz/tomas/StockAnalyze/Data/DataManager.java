/**
 * 
 */
package cz.tomas.StockAnalyze.Data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.Data.PseCsvData.PseCsvDataProvider;

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
	
	private final String DATABASE_NAME = "cz.tomas.StockAnalyze.Data";
	private final int DATABASE_VERSION_NUMBER = 1;
	private final String DATABASE_FILE_NAME = "cz.tomas.StockAnalyze.Data.db";
	
	StockDataSqlStore sqlStore;
	
	Context context;
	public DataManager(Context context) {
		this.context = context;
		
		this.sqlStore = new StockDataSqlStore(context, DATABASE_FILE_NAME, null, DATABASE_VERSION_NUMBER);
	}

	public List<StockItem> search(String pattern) {
		IStockDataProvider provider = new PseCsvDataProvider();
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
	
	public StockItem getStockItem(String id) {
		IStockDataProvider provider = new PseCsvDataProvider();
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
			IStockDataProvider provider = this.getDataProvider(ticker);
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
	
	private IStockDataProvider getDataProvider(String ticker) {
		IStockDataProvider dataProvider = null;
		if (ticker.startsWith("BAA")) {
			dataProvider = new PseCsvDataProvider();
		}
		
		return dataProvider;
	}

	public boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.isConnectedOrConnecting();
	}
}
