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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.utils.Utils;


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
	
	public void insertStockItem(StockItem item) {
		try {
			Log.d("StockDataSqlStore", "inserting stockItem " + item.toString());
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("id", item.getId());
			values.put("ticker", item.getTicker());
			values.put("name", item.getName());
			
			db.insert(STOCK_TABLE_NAME, null, values);
		} catch (SQLException e) {
			Log.e(Utils.LOG_TAG, "failed to insert data.", e);
		} finally {
			this.close();
		}
	}
	
	public void insertDayData(StockItem item, DayData newdata) {
		try {
			Calendar cal = new GregorianCalendar(Utils.PRAGUE_TIME_ZONE);
			cal.setTime(newdata.getDate());
			DayData currentData = getDayData(cal, item);
			if (currentData != null) {
				updateDateData(item, newdata, currentData);
				return;
			}
			Log.d(Utils.LOG_TAG, "inserting day data for " + item.getTicker() + " to db (" + newdata.getDate().toString() + ")");
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
			values.put("year_min", newdata.getYearMinimum());
			values.put("volume", newdata.getVolume());
			
			db.insert(DAY_DATA_TABLE_NAME, null, values);
		} catch (SQLException e) {
			String message =  "Failed to INSERT stock item.";
			if (e.getMessage() != null)
				 message += e.getMessage();
			Log.e(Utils.LOG_TAG, message, e);
		} finally {
			this.close();
		}
	}

	/**
	 * insert set of data in one transaction
	 * @param receivedData
	 */
	public void insertDayDataSet(Map<StockItem, DayData> receivedData) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.beginTransaction();
		try {
			for (Entry<StockItem, DayData> entry : receivedData.entrySet()) {
				this.insertDayData(entry.getKey(), entry.getValue());
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, "failed to insert day data from dataset, transaction is going to roll back", e);
		} finally {
			db.endTransaction();
		}
	}
	
	private void updateDateData(StockItem item, DayData newData, DayData currentData) {
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues values = new ContentValues();

			Log.d(Utils.LOG_TAG, "updating day data for " + item.getTicker() + " to db");
			values.put("date" ,Utils.createDateOnlyCalendar(newData.getDate()).getTimeInMillis());
			values.put("last_update", newData.getLastUpdate());
			values.put("price", newData.getPrice());
			values.put("change", newData.getChange());
			if (newData.getVolume() > 0)
				values.put("volume", newData.getVolume());
			if (newData.getYearMaximum() > 0)
				values.put("year_max", newData.getYearMaximum());
			if (newData.getYearMinimum() > 0)
				values.put("year_min", newData.getYearMinimum());
			
			db.update(DAY_DATA_TABLE_NAME, values,"id=?", new String[] { String.valueOf(currentData.getId()) });
		} catch (Exception e) {
			String message =  "Failed to UPDATE stock item.";
			if (e.getMessage() != null)
				 message += e.getMessage();
			Log.e(Utils.LOG_TAG, message, e);
		} finally {
			this.close();
		}
	}


	public Map<String, StockItem> getStockItems(Market market, String orderBy) {
		// LinkedHashMap preserve order of added items
		Map<String, StockItem> items = new LinkedHashMap<String, StockItem>();
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = null;
			try {
				c = db.query(STOCK_TABLE_NAME, new String[] { "id", "ticker", "name" }, null, null, null, null, orderBy);
				if (c.moveToFirst()) {
					do {
						String id = c.getString(0);
						String ticker = c.getString(1);
						String name = c.getString(2);
						// FIXME market table
						StockItem item = new StockItem(ticker, id, name, MarketFactory.getMarket("cz"));
						items.put(id, item);
					} while (c.moveToNext());
				}
				
			} catch (SQLException e) {
				Log.e(Utils.LOG_TAG, e.toString());
			} finally {
				// close cursor
				if (c != null)
					c.close();
			}
		} catch (SQLException e) {
			Log.e(Utils.LOG_TAG, "failed to get stock item.",e);
		} finally {
			this.close();
		}
		
		return items;
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
				Log.e(Utils.LOG_TAG, e.toString());
			} finally {
				// close cursor
				if (c != null)
					c.close();
			}
		} catch (SQLException e) {
			Log.d(Utils.LOG_TAG, "failed to get stock item." + e.getMessage());
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
		return this.getDayData(calendar, item.getId());
	}
	
	/*
	 * get day data item from stock_day_data table
	 * for given stock item and day in year represented by Calendar.
	 * returns null if nothing is found
	 */
	public DayData getDayData(Calendar calendar, String stockId) {
		DayData data = null;

		try {
			Calendar cal = Utils.createDateOnlyCalendar(calendar);
			long miliseconds =  cal.getTimeInMillis();
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = null;
			try {
				c = db.query(DAY_DATA_TABLE_NAME, new String[] {
						"price", "change", "year_max", "year_min", "date", "volume", "id", "last_update" }, 
						"stock_id=? AND date=?", new String[] { stockId, String.valueOf(miliseconds) }, null, null, "date");

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
				Log.e(Utils.LOG_TAG, e.toString());
			} finally {
				if (c != null)
					c.close();
			}
		} catch (SQLException e) {
			Log.d(Utils.LOG_TAG, "failed to get data." + e.getMessage());
			e.printStackTrace();
		} finally {
			this.close();
		}
		
		return data;
	}

	/*
	 * get day data item from stock_day_data table
	 * for given stock.
	 * returns null if nothing is found
	 */
	public DayData getLastAvailableDayData(StockItem item) {
		return this.getLastAvailableDayData(item.getId());
	}
	
	/*
	 * get day data item from stock_day_data table
	 * for given stock.
	 * returns null if nothing is found
	 */
	public DayData getLastAvailableDayData(String stockId) {
		DayData data = null;

		try {
			//Calendar cal = Utils.createDateOnlyCalendar(calendar);
			//long milliseconds =  cal.getTimeInMillis();
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = null;
			try {
				c = db.query(DAY_DATA_TABLE_NAME, new String[] {
						"price", "change", "year_max", "year_min", "date", "volume", "id", "last_update" }, 
						"stock_id=?", new String[] { stockId }, null, null, "date");

				if (c.moveToLast()) {
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
				Log.e(Utils.LOG_TAG, e.toString());
			} finally {
				if (c != null)
					c.close();
			}
		} catch (SQLException e) {
			Log.e(Utils.LOG_TAG, "failed to get last available data.", e);
		} finally {
			this.close();
		}
		
		return data;
	}

	public Map<StockItem, DayData> getLastDataSet(Map<String, StockItem> stockItems, Market market, String orderBy) {
		Map<StockItem, DayData> dbData = new LinkedHashMap<StockItem, DayData>();
		
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = null;
			try {
				StringBuilder selectionBuilder = new StringBuilder();
				String[] whereArgs = new String[stockItems.size()];
				int index = 0;
				for (Entry<String, StockItem> stockItem : stockItems.entrySet()) {
					if (stockItem.getKey() != null) {
						if (selectionBuilder.length() > 1)
							selectionBuilder.append(" or ");
						selectionBuilder.append("stock_id=?");
						whereArgs[index] = stockItem.getKey();
						index++;
					}
				}
				
				// order by given column, if any, and by date, so we get last results 
				if (orderBy != null && orderBy.length() > 0)
					orderBy += ", date DESC";
				else
					orderBy = "date DESC";
				
				// data is grouped by stock-id and sorted by date, 
				// so we get last result for all stock items
				c = db.query(DAY_DATA_TABLE_NAME, new String[] {
						"price", "change", "year_max", "year_min", "date", "volume", "id", "last_update", "stock_id" }, 
						selectionBuilder.toString(), whereArgs, null, null, orderBy, String.valueOf(stockItems.size()));
				
				if (c.moveToFirst()) {
					do {
						float price = c.getFloat(0);
						float change = c.getFloat(1);
						float max = c.getFloat(2);
						float min = c.getFloat(3);
						long millisecs = c.getLong(4);
						float volume = c.getFloat(5);
						long id = c.getLong(6);
						long lastUpdate = c.getLong(7);
						String stockId = c.getString(8);
						
						Date date = new Date(millisecs);
						DayData data = new DayData(price, change, date, volume, max, min, lastUpdate, id);
						StockItem stock = stockItems.get(stockId);
						dbData.put(stock, data);
					} while (c.moveToNext());
				}
			} catch (SQLException e) {
				Log.e(Utils.LOG_TAG, "sql exception occured", e);
			} catch (IllegalStateException e) {
				Log.e(Utils.LOG_TAG, "database is in an illegal state!", e);
			} finally {
				if (c != null)
					c.close();
			}
		} catch (SQLException e) {
			Log.e(Utils.LOG_TAG, "failed to get data.", e);
		} finally {
			this.close();
		}
		
		return dbData;
	}
}
