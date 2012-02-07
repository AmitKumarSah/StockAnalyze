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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.utils.Utils;

import java.util.*;
import java.util.Map.Entry;


/**
 * db helper for {@link StockItem} data and {@link DayData} data
 * @author tomas
 *
 */
public class StockDataSqlStore extends DataSqlHelper {
	
	private static StockDataSqlStore instance;

	private static final int FLAG_REMOVED = 1;
	private static final String FLAG_REMOVED_STRING = "1";

	public static StockDataSqlStore getInstance(Context context) {
		if (instance == null) {
			instance = new StockDataSqlStore(context);
		}
		return instance;
	}
	
	private StockDataSqlStore(Context context) {
		super(context);
	}
	
	/**	
	 * mark stock item as deleted - we can't actually delete stock item,
	 * because of portfolio
	 * @param stockId id of stock we want mark as deleted
	 * @param db db to work with
	 * @return count of affected stock items
	 */
	private int deleteStockItem(String stockId, SQLiteDatabase db) {
		try {
			Log.d(Utils.LOG_TAG, "marking stockItem as deleted - " + stockId);

			ContentValues values = new ContentValues();
			values.put("flag", FLAG_REMOVED);

			return db.update(DAY_DATA_TABLE_NAME, values, "_id = ?", new String[]{stockId});
		} catch (SQLException e) {
			Log.e(Utils.LOG_TAG, "failed to delete data.", e);
		}
		return 0;
	}
	
	/**
	 * insert new stock or update current record to database
	 * @param item stock item to insert
	 */
	public void insertStockItem(StockItem item) {
		try {
			SQLiteDatabase db = this.getWritableDatabase();

			if (updateStockItem(item, db) > 0) {
				return;
			}
			if (DEBUG) Log.d(Utils.LOG_TAG, "inserting stockItem " + item.toString());
			
			ContentValues values = new ContentValues();
			values.put(StockColumns._ID, item.getId());
			values.put(StockColumns.TICKER, item.getTicker());
			values.put(StockColumns.NAME, item.getName());
			values.put(StockColumns.IS_INDEX, item.isIndex());
			if (item.getMarket() != null) {
				values.put(StockColumns.MARKET_ID, item.getMarket().getId());
			} else {
				Log.w(Utils.LOG_TAG, " stock item is without market " + item);
			}
			db.insert(STOCK_TABLE_NAME, null, values);
		} catch (SQLException e) {
			Log.e(Utils.LOG_TAG, "failed to insert stock item.", e);
		} finally {
			this.close();
		}
	}
	
	/**
	 * update stock item
	 *
	 * @param item item to update
	 * @param db db to work with
	 * @return count of updated items
	 */
	private int updateStockItem(StockItem item, SQLiteDatabase db) {
		try {
			if (DEBUG) Log.d(Utils.LOG_TAG, "updating stockItem " + item.toString());

			ContentValues values = new ContentValues();
			values.put(StockColumns.TICKER, item.getTicker());
			values.put(StockColumns.NAME, item.getName());
			values.put(StockColumns.IS_INDEX, item.isIndex());
			if (item.getMarket() != null) {
				values.put(StockColumns.MARKET_ID, item.getMarket().getId());
			} else {
				Log.w(Utils.LOG_TAG, " stock item is without market " + item);
			}

			return db.update(STOCK_TABLE_NAME, values, StockColumns._ID + "=?", new String[] {item.getId()});
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, "failed to update stock item.", e);
		}
		return 0;
	}

	public void insertDayData(String stockId, DayData newData) {
		try {
			SQLiteDatabase db = this.getWritableDatabase();

			if (updateDayData(newData, stockId, db) > 0) {
				return;
			}
			ContentValues values = new ContentValues();
	
			values.put(DayDataColumns.DATE, Utils.createDateOnlyCalendar(newData.getDate()).getTimeInMillis());
			values.put(DayDataColumns.LAST_UPDATE, newData.getLastUpdate());
			values.put(DayDataColumns.PRICE, newData.getPrice());
			values.put(DayDataColumns.CHANGE, newData.getChange());
			values.put(DayDataColumns.STOCK_ID, stockId);
			values.put(DayDataColumns.YEAR_MAX, newData.getYearMaximum());
			values.put(DayDataColumns.YEAR_MIN, newData.getYearMinimum());
			values.put(DayDataColumns.VOLUME, newData.getVolume());
			
			db.insert(DAY_DATA_TABLE_NAME, null, values);
		} catch (SQLException e) {
			Log.e(Utils.LOG_TAG, "Failed to INSERT stock item.", e);
		} finally {
			this.close();
		}
	}

	/**
	 * insert or update set of data in one transaction
	 *
	 * @param receivedData set of data to insert or update
	 */
	public void insertDayDataSet(Map<String, DayData> receivedData) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.beginTransaction();
		try {
			for (Entry<String, DayData> entry : receivedData.entrySet()) {
				this.insertDayData(entry.getKey(), entry.getValue());
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, "failed to insert day data from dataset, transaction is going to roll back", e);
		} finally {
			db.endTransaction();
		}
	}
	
	private int updateDayData(DayData newData, String stockId, SQLiteDatabase db) {
		try {
			ContentValues values = new ContentValues();

			if (DEBUG) Log.d(Utils.LOG_TAG, "updating day data to " + newData.toString());

			values.put(DayDataColumns.DATE ,Utils.createDateOnlyCalendar(newData.getDate()).getTimeInMillis());
			values.put(DayDataColumns.LAST_UPDATE, newData.getLastUpdate());
			values.put(DayDataColumns.PRICE, newData.getPrice());
			values.put(DayDataColumns.CHANGE, newData.getChange());
			if (newData.getVolume() > 0) {
				values.put(DayDataColumns.VOLUME, newData.getVolume());
			}
			if (newData.getYearMaximum() > 0) {
				values.put(DayDataColumns.YEAR_MAX, newData.getYearMaximum());
			}
			if (newData.getYearMinimum() > 0) {
				values.put(DayDataColumns.YEAR_MIN, newData.getYearMinimum());
			}

			return db.update(DAY_DATA_TABLE_NAME, values, DayDataColumns.STOCK_ID + "=?", new String[] { stockId });
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, "Failed to UPDATE stock item.", e);
		}
		return 0;
	}


	public Map<String, StockItem> getStockItems(Market market, String orderBy) {
		if (market == null) {
			throw new IllegalArgumentException("market cannot be null");
		}
		// LinkedHashMap preserve order of added items
		final Map<String, StockItem> items = new LinkedHashMap<String, StockItem>();
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = null;
			try {
				c = db.query(STOCK_TABLE_NAME, StockColumns.PROJECTION,
						String.format("%s=? AND %s != ?", StockColumns.MARKET_ID, StockColumns.FLAG),
						new String[] { market.getId(), FLAG_REMOVED_STRING },
						null, null, orderBy);

				if (c.moveToFirst()) {
					do {
						String id = c.getString(c.getColumnIndex(StockColumns._ID));
						String ticker = c.getString(c.getColumnIndex(StockColumns.TICKER));
						String name = c.getString(c.getColumnIndex(StockColumns.NAME));
						//String marketId = c.getString(c.getColumnIndex(StockColumns.MARKET_ID));
						StockItem item = new StockItem(ticker, id, name, market);
						items.put(id, item);
					} while (c.moveToNext());
				}
				
			} catch (SQLException e) {
				Log.e(Utils.LOG_TAG, "failed to read stock items from db", e);
			} finally {
				if (c != null) {
					c.close();
				}
			}
		} catch (SQLException e) {
			Log.e(Utils.LOG_TAG, "failed to get stock item.",e);
		} finally {
			this.close();
		}
		
		return items;
	}

	/**
	 * get stock item by id from database
	 *
	 * @param id stock id
	 * @return null if nothing is found
	 */
	public StockItem getStockItem(String id) {
		StockItem item = null;
		try {
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor c = null;
			try {
				c = db.query(STOCK_TABLE_NAME, StockColumns.PROJECTION, StockColumns._ID + "=?", new String[] { id }, null, null, null);
				if (c.moveToFirst()) {
					String ticker = c.getString(1);
					String name = c.getString(2);
					String marketId = c.getString(3);
					
					final Market market = getMarket(marketId, db);
					item = new StockItem(ticker, id, name, market);
				}
				
			} catch (SQLException e) {
				Log.e(Utils.LOG_TAG, "failed to read stock item from db", e);
			} finally {
				if (c != null) {
					c.close();
				}
			}
		} catch (SQLException e) {
			Log.e(Utils.LOG_TAG, "failed to get stock item." + e.getMessage(), e);
		} finally {
			this.close();
		}
		
		return item;
	}

	/**
	 * get day data item from stock_day_data table
	 * for given stock item and day in year represented by Calendar.
	 *
	 * @param stockId stock item id we want data for
	 * @return null if nothing is found
	 */
	public DayData getDayData(String stockId) {
		DayData data = null;

		try {
			SQLiteDatabase db = this.getWritableDatabase();
			Cursor c = null;
			try {
				c = db.query(DAY_DATA_TABLE_NAME, DayDataColumns.PROJECTION,
						DayDataColumns.STOCK_ID + "=?", new String[] { stockId }, null, null, null);

				if (c.moveToFirst()) {
					float price = c.getFloat(0);
					float change = c.getFloat(1);
					float max = c.getFloat(2);
					float min = c.getFloat(3);
					long milliseconds = c.getLong(4);
					float volume = c.getFloat(5);
					long id = c.getLong(6);
					long lastUpdate = c.getLong(7);
					
					Date date = new Date(milliseconds);
					data = new DayData(price, change, date, volume, max, min, lastUpdate, id);
				}
			} catch (SQLException e) {
				Log.e(Utils.LOG_TAG, e.toString());
			} finally {
				if (c != null) {
					c.close();
				}
			}
		} catch (SQLException e) {
			Log.e(Utils.LOG_TAG, "failed to get data.", e);
		} finally {
			this.close();
		}
		
		return data;
	}

	/**
	 * get day datas for given stock items
	 * @param stockItems stock items we want data for
	 * @param orderBy order clause (without ORDER BY)
	 * @return last available day data
	 */
	public Map<StockItem, DayData> getLastDataSet(Map<String, StockItem> stockItems, String orderBy) {
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
						selectionBuilder.append(DayDataColumns.STOCK_ID + "=?");
						whereArgs[index] = stockItem.getKey();
						index++;
					}
				}

				if (orderBy == null || orderBy.length() <= 0) {
					orderBy = DayDataColumns.DEFAULT_SORT;
				}

				// data is grouped by stock-id
				c = db.query(DAY_DATA_TABLE_NAME, DayDataColumns.PROJECTION,
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
				Log.e(Utils.LOG_TAG, "sql exception occurred", e);
			} catch (IllegalStateException e) {
				Log.e(Utils.LOG_TAG, "database is in an illegal state!", e);
			} finally {
				if (c != null) {
					c.close();
				}
			}
		} catch (SQLException e) {
			Log.e(Utils.LOG_TAG, "failed to get data.", e);
		} finally {
			this.close();
		}
		
		return dbData;
	}

	/**
	 * update stocks in db with new one and deleting old one, not present in given list
	 *
	 * @param market market the the stocks to update belongs to
	 * @param stocks list of stocks to update
	 * @return updated map of stocks and datas
	 */
	public Map<String, StockItem>  updateStockList(List<StockItem> stocks, Market market) {
		Map<String, StockItem> items = new LinkedHashMap<String, StockItem>();
		Log.i(Utils.LOG_TAG, "storing stock items to db ... " + items.size());
		
		SQLiteDatabase db = this.getWritableDatabase();
		try {
			db.beginTransaction();
			Map<String, StockItem> currentItems = this.getStockItems(market, null);
			for (StockItem stockItem : stocks) {
				items.put(stockItem.getId(), stockItem);
				this.insertStockItem(stockItem);
				if (currentItems != null) {
					currentItems.remove(stockItem.getId());
				}
			}
			// delete items that weren't downloaded
			if (currentItems != null) {
				for (Entry<String, StockItem> entry : currentItems.entrySet()) {
					this.deleteStockItem(entry.getKey(), db);
				}
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		return items;
	}
	
	public Map<String, Market> getMarkets() {
		Map<String, Market> markets = null;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = null;
		try {
			cursor = db.query(MARKET_TABLE_NAME, MarketColumns.PROJECTION, null, null, null, null, MarketColumns.UI_ORDER);
			if (cursor.moveToFirst()) {
				markets = new LinkedHashMap<String, Market>(cursor.getCount());
				do {
					String id = cursor.getString(cursor.getColumnIndex(MarketColumns._ID));
					String name = cursor.getString(cursor.getColumnIndex(MarketColumns.NAME));
					String desc = cursor.getString(cursor.getColumnIndex(MarketColumns.DESCRIPTION));
					String country = cursor.getString(cursor.getColumnIndex(MarketColumns.COUNTRY));
					String currency = cursor.getString(cursor.getColumnIndex(MarketColumns.CURRENCY));
					//int order = cursor.getInt(cursor.getColumnIndex(MarketColumns.UI_ORDER));
					long openFrom = cursor.getLong(cursor.getColumnIndex(MarketColumns.OPEN_FROM));
					long openTo = cursor.getLong(cursor.getColumnIndex(MarketColumns.OPEN_TO));
					double feeMax = cursor.getDouble(cursor.getColumnIndex(MarketColumns.FEE_MAX));
					double feeMin = cursor.getDouble(cursor.getColumnIndex(MarketColumns.FEE_MIN));
					double feePerc = cursor.getDouble(cursor.getColumnIndex(MarketColumns.FEE_PERC));

					Market market = new Market(name, id,currency,desc, country, feePerc, feeMax, feeMin, openTo, openFrom);
					markets.put(id, market);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, "failed tp read markets", e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			this.close();
		}
		return markets;
	}
	
	private Market getMarket(String marketId, SQLiteDatabase db) {
		if (TextUtils.isEmpty(marketId)) {
			throw new IllegalArgumentException("market id cannot be empty");
		}
		Cursor cursor = null;
		try {
			cursor = db.query(MARKET_TABLE_NAME, MarketColumns.PROJECTION, MarketColumns._ID + "=?",
					new String[] {marketId}, null, null, null);
			if (cursor.moveToFirst()) {
				String id = cursor.getString(cursor.getColumnIndex(MarketColumns._ID));
				String name = cursor.getString(cursor.getColumnIndex(MarketColumns.NAME));
				String desc = cursor.getString(cursor.getColumnIndex(MarketColumns.DESCRIPTION));
				String country = cursor.getString(cursor.getColumnIndex(MarketColumns.COUNTRY));
				String currency = cursor.getString(cursor.getColumnIndex(MarketColumns.CURRENCY));
				//int order = cursor.getInt(cursor.getColumnIndex(MarketColumns.UI_ORDER));
				long openFrom = cursor.getLong(cursor.getColumnIndex(MarketColumns.OPEN_FROM));
				long openTo = cursor.getLong(cursor.getColumnIndex(MarketColumns.OPEN_TO));
				double feeMax = cursor.getDouble(cursor.getColumnIndex(MarketColumns.FEE_MAX));
				double feeMin = cursor.getDouble(cursor.getColumnIndex(MarketColumns.FEE_MIN));
				double feePerc = cursor.getDouble(cursor.getColumnIndex(MarketColumns.FEE_PERC));

				return new Market(name, id,currency,desc, country, feePerc, feeMax, feeMin, openTo, openFrom);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}

	public void updateMarkets(Map<String, Market> markets) {
		SQLiteDatabase db = this.getWritableDatabase();

		try{
			db.beginTransaction();
			db.delete(MARKET_TABLE_NAME, null, null);

			for (Market market : markets.values()) {
				ContentValues values = new ContentValues();
				values.put(MarketColumns.NAME, market.getName());
				values.put(MarketColumns.COUNTRY, market.getCountry());
				values.put(MarketColumns.CURRENCY, market.getCurrencyCode());
				values.put(MarketColumns.DESCRIPTION, market.getDescription());
				values.put(MarketColumns.FEE_MAX, market.getFeeMax());
				values.put(MarketColumns.FEE_MIN, market.getFeeMin());
				values.put(MarketColumns.FEE_PERC, market.getFeePerc());
				values.put(MarketColumns.OPEN_FROM, market.getOpenFrom());
				values.put(MarketColumns.OPEN_TO, market.getOpenTo());

				int count = db.update(MARKET_TABLE_NAME, values, MarketColumns._ID+ "=?", new String[] { market.getId() });
				if (count == 0) {
					Log.d(Utils.LOG_TAG, "inserting market " + market);
					values.put(MarketColumns._ID, market.getId());
					db.insert(MARKET_TABLE_NAME, null, values);
				}
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			this.close();
		}
	}

	public Map<StockItem, DayData> getLastDataSet(Market market, String orderBy) {
		Map<String, StockItem> stocks = getStockItems(market, null);
		
		return this.getLastDataSet(stocks, orderBy);
	}
}
