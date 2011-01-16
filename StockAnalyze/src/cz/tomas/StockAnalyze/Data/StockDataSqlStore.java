/**
 * 
 */
package cz.tomas.StockAnalyze.Data;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.News.Article;
import cz.tomas.StockAnalyze.utils.Utils;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;


/*
 	Stock table:
		private static final String STOCK_TABLE_CREATE =
	         "CREATE TABLE " + STOCK_TABLE_NAME + " (" +
	         "id varchar(50) PRIMARY KEY," +
	         "ticker varchar(10) not null, " +
	         "name TEXT);";
	Day data table:	
		private static final String DAY_DATA_TABLE_CRETE = 
			"CREATE TABLE " + DAY_DATA_TABLE_NAME + " (" +
	         "id integer PRIMARY KEY AUTOINCREMENT," +
	         "stock_id varchar(50)," +
	         "FOREIGN KEY(stock_id) REFERENCES " + STOCK_TABLE_NAME + "(id));" +
	         "year_min real," +
	         "year_max real," +
	         "change real not null," +
	         "date TEXT not null, " +				//ISO8601 strings "YYYY-MM-DD HH:MM:SS.SSS"
	         "price real not null);";
 */



/**
 * @author tomas
 *
 */
public class StockDataSqlStore extends DataSqlHelper {
	
	public StockDataSqlStore(Context context) {
		super(context);
	}


	private boolean checkForStock(String id) {
		if (id == null || id.length() == 0)
			return false;

		boolean result = false;
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = null;
			try {
				//c = db.query(STOCK_TABLE_NAME, new String[] { "id" }, "id='"+ item.getId() +"'", null, null, null, null);
				c = db.query(STOCK_TABLE_NAME, new String[] { "id" }, "id=?", new String[] { id }, null, null, null);
				if (c.moveToFirst())
					result = true;
				
			} catch (SQLException e) {
				Log.e("StockDataSqlStore", e.toString());
			} finally {
				// close cursor
				if (c != null)
					c.close();
			}
		} catch (SQLException e) {
			Log.d("StockDataSqlStore", "failed to get stock item." + e.getMessage());
			e.printStackTrace();
		} finally {
			this.close();
		}
		return result;
	}
	
	private boolean checkForStock(StockItem item) {
		if (item == null || item.getId() == null)
			return false;
		String id = item.getId();
		
		return this.checkForStock(id);
	}
//	
//	public boolean checkForData(StockItem item, Calendar cal) {
//		if (item == null || item.getId() == null)
//			return false;
//		boolean result = false;
//		try {
//			SQLiteDatabase db = this.getWritableDatabase();
//			Cursor c = null;
//			try {
//				// TODO date!
//				//c = db.query(DAY_DATA_TABLE_NAME, new String[] { "id" }, "stock_id='"+ item.getId() +"'", null, null, null, null);
//				c = db.query(DAY_DATA_TABLE_NAME, new String[] { "id", "date" }, "stock_id=?", new String[] { item.getId() }, null, null, null);
//				if (c.moveToFirst()) {
//					long time = c.getLong(1);
//					Calendar calendar = Calendar.getInstance();
//					calendar.setTimeInMillis(time);
//					// if date fits to this day, then return true
//					if (calendar.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR) &&
//							calendar.get(Calendar.YEAR) == cal.get(Calendar.YEAR))
//						result = true;
//				}
//				
//			} catch (SQLException e) {
//				Log.e("StockDataSqlStore", e.toString());
//				e.printStackTrace();
//			} finally {
//				// close cursor
//				if (c != null)
//					c.close();
//			}
//		} catch (SQLException e) {
//			Log.d("StockDataSqlStore", "failed to get data." + e.getMessage());
//			e.printStackTrace();
//		} finally {
//			this.close();
//		}
//		return result;
//	}
	
	public void insertStockItem(StockItem item) {
		try {
			Log.d("StockDataSqlStore", "inserting stockItem " + item.toString());
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("id", item.getId());
			values.put("ticker", item.getTicker());
			values.put("name", item.getName());
			
			db.insert(STOCK_TABLE_NAME, null, values);
			//db.execSQL("INSERT INTO " + StockDataSqlStore.STOCK_TABLE_NAME + " values('"+ ticker + "', date('now'), " + data.getPrice() + ");");
		} catch (SQLException e) {
			Log.d("StockDataSqlStore", "failed to insert data." + e.getMessage());
			e.printStackTrace();
		} finally {
			this.close();
		}
	}
	
	public void insertDayData(StockItem item, DayData newdata) {
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(newdata.getDate());
			DayData currentData = getDayData(cal, item);
			if (currentData != null) {
				updateDateData(item, newdata, currentData);
				return;
			}
			Log.d("StockDataSqlStore", "inserting day data for " + item.getTicker() + " to db");
			if (! this.checkForStock(item))
				this.insertStockItem(item);
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues values = new ContentValues();

			values.put("date", Utils.createDateOnlyCalendar(newdata.getDate()).getTimeInMillis());
			values.put("last_update", newdata.getLastUpdate());
			values.put("price", newdata.getPrice());
			values.put("change", newdata.getChange());
			values.put("stock_id", item.getId());
			values.put("year_max", newdata.getYearMaximum());
			values.put("year_min", newdata.getYearMaximum());
			values.put("volume", newdata.getVolume());
			
			db.insert(DAY_DATA_TABLE_NAME, null, values);
			//db.execSQL("INSERT INTO " + StockDataSqlStore.STOCK_TABLE_NAME + " values('"+ ticker + "', date('now'), " + data.getPrice() + ");");
		} catch (SQLException e) {
			String message =  "Failed to INSERT stock item.";
			if (e.getMessage() != null)
				 message += e.getMessage();
			Log.d("StockDataSqlStore", message);
			e.printStackTrace();
		} finally {
			this.close();
		}
	}
	
	private void updateDateData(StockItem item, DayData newData, DayData currentData) {
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues values = new ContentValues();

			Log.d("StockDataSqlStore", "updating day data for " + item.getTicker() + " to db");
			values.put("date" ,Utils.createDateOnlyCalendar(newData.getDate()).getTimeInMillis());
			values.put("last_update", newData.getLastUpdate());
			values.put("price", newData.getPrice());
			values.put("change", newData.getChange());
			values.put("volume", newData.getVolume());
			
			db.update(DAY_DATA_TABLE_NAME, values,"id=?", new String[] { String.valueOf(currentData.getId()) });
		} catch (Exception e) {
			String message =  "Failed to UPDATE stock item.";
			if (e.getMessage() != null)
				 message += e.getMessage();
			Log.d("StockDataSqlStore", message);
			e.printStackTrace();
		} finally {
			this.close();
		}
	}


	/*
	 * get stock item by id from database
	 * returns null if nothing is found
	 */
	public StockItem getStockItem(String id) {
		StockItem item = null;
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = null;
			try {
				//c = db.query(STOCK_TABLE_NAME, new String[] { "id" }, "id='"+ item.getId() +"'", null, null, null, null);
				c = db.query(STOCK_TABLE_NAME, new String[] { "id", "ticker", "name" }, "id=?", new String[] { id }, null, null, null);
				if (c.moveToFirst()) {
					String ticker = c.getString(1);
					String name = c.getString(2);
					// FIXME market table
					item = new StockItem(ticker, id, name, MarketFactory.getMarket("cz"));
				}
				
			} catch (SQLException e) {
				Log.e("StockDataSqlStore", e.toString());
			} finally {
				// close cursor
				if (c != null)
					c.close();
			}
		} catch (SQLException e) {
			Log.d("StockDataSqlStore", "failed to get stock item." + e.getMessage());
			e.printStackTrace();
		} finally {
			this.close();
		}
		
		return item;
	}

	/*
	 * get day data item from stock_day_data table
	 * for given stock item and day in year represented by Calendar.
	 * returns null if nothing is found
	 */
	public DayData getDayData(Calendar calendar, StockItem item) {
		DayData data = null;

		try {
			Calendar cal = Utils.createDateOnlyCalendar(calendar);
			long miliseconds =  cal.getTimeInMillis();
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = null;
			try {
				c = db.query(DAY_DATA_TABLE_NAME, new String[] {
						"price", "change", "year_max", "year_min", "date", "volume", "id", "last_update" }, 
						"stock_id=? AND date=?", new String[] { item.getId(), String.valueOf(miliseconds) }, null, null, "date");

				if (c.moveToFirst()) {
					float price = c.getFloat(0);
					float change = c.getFloat(1);
					float max = c.getFloat(2);
					float min = c.getFloat(3);
					long millisecs = c.getLong(4);
					float volume = c.getFloat(5);
					long id = c.getLong(6);
					long lastUpdate = c.getLong(7);
					
					Date date = new Date(millisecs);
					data = new DayData(price, change, date, volume, max, min, lastUpdate, id);
				}
			} catch (SQLException e) {
				Log.e("StockDataSqlStore", e.toString());
			} finally {
				if (c != null)
					c.close();
			}
		} catch (SQLException e) {
			Log.d("StockDataSqlStore", "failed to get data." + e.getMessage());
			e.printStackTrace();
		} finally {
			this.close();
		}
		
		return data;
	}

	/*
	 * get day data item from stock_day_data table
	 * for given stock item and day in year represented by Calendar.
	 * returns null if nothing is found
	 */
	public DayData getLastAvailableDayData(StockItem item) {
		DayData data = null;

		try {
			//Calendar cal = Utils.createDateOnlyCalendar(calendar);
			//long milliseconds =  cal.getTimeInMillis();
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = null;
			try {
				c = db.query(DAY_DATA_TABLE_NAME, new String[] {
						"price", "change", "year_max", "year_min", "date", "volume", "id", "last_update" }, 
						"stock_id=?", new String[] { item.getId() }, null, null, "date");

				if (c.moveToFirst()) {
					float price = c.getFloat(0);
					float change = c.getFloat(1);
					float max = c.getFloat(2);
					float min = c.getFloat(3);
					long millisecs = c.getLong(4);
					float volume = c.getFloat(5);
					long id = c.getLong(6);
					long lastUpdate = c.getLong(7);
					
					Date date = new Date(millisecs);
					data = new DayData(price, change, date, volume, max, min, lastUpdate, id);
				}
			} catch (SQLException e) {
				Log.e("StockDataSqlStore", e.toString());
			} finally {
				if (c != null)
					c.close();
			}
		} catch (SQLException e) {
			Log.d("StockDataSqlStore", "failed to get data." + e.getMessage());
			e.printStackTrace();
		} finally {
			this.close();
		}
		
		return data;
	}

}
