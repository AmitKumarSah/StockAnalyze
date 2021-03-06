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
	private final Calendar calendar;        // reused calendar object

	public static StockDataSqlStore getInstance(Context context) {
		if (instance == null) {
			instance = new StockDataSqlStore(context);
		}
		return instance;
	}
	
	private StockDataSqlStore(Context context) {
		super(context);
		calendar = new GregorianCalendar();
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

			synchronized (calendar) {
				calendar.setTimeInMillis(newData.getDate());
				values.put(DayDataColumns.DATE, Utils.createDateOnlyCalendar(calendar).getTimeInMillis());
			}
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
	
	private int updateDayData(DayData newData, String stockId, SQLiteDatabase db) {
		try {
			ContentValues values = new ContentValues();

			if (VERBOSE) Log.d(Utils.LOG_TAG, "updating day data to " + newData.toString());

			synchronized (calendar) {
				calendar.setTimeInMillis(newData.getDate());
				values.put(DayDataColumns.DATE, Utils.createDateOnlyCalendar(calendar).getTimeInMillis());
			}
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
						DayDataColumns.STOCK_ID.concat("=?"), new String[] { stockId }, null, null, null);

				if (c.moveToFirst()) {
					data = readDayData(c, c.getColumnIndex(DayDataColumns._ID));
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

	private DayData readDayData(Cursor c, int idColumn) {
		float price = c.getFloat(c.getColumnIndex(DayDataColumns.PRICE));
		float change = c.getFloat(c.getColumnIndex(DayDataColumns.CHANGE));
		float max = c.getFloat(c.getColumnIndex(DayDataColumns.YEAR_MAX));
		float min = c.getFloat(c.getColumnIndex(DayDataColumns.YEAR_MIN));
		long milliseconds = c.getLong(c.getColumnIndex(DayDataColumns.DATE));
		float volume = c.getFloat(c.getColumnIndex(DayDataColumns.VOLUME));
		long id = c.getLong(idColumn);
		long lastUpdate = c.getLong(c.getColumnIndex(DayDataColumns.LAST_UPDATE));

		return new DayData(price, change, milliseconds, volume, max, min, lastUpdate, id);
	}

	private StockItem readStockItem(Market market, Cursor c, int idColumn) {
		String id = c.getString(idColumn);
		String ticker = c.getString(c.getColumnIndex(StockColumns.TICKER));
		String name = c.getString(c.getColumnIndex(StockColumns.NAME));
		//String marketId = c.getString(c.getColumnIndex(StockColumns.MARKET_ID));
		return new StockItem(ticker, id, name, market);
	}

	/**
	 * update stocks in db with new one and deleting old one, not present in given list
	 *
	 * @param market market the the stocks to update belongs to
	 * @param stocks list of stocks to update
	 * @return updated map of stocks and datas
	 */
	public Map<String, StockItem>  updateStockList(Collection<StockItem> stocks, Market market) {
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

	/**
	 * get all markets currently present in database
	 * @return map of market's id and market
	 */
	public Map<String, Market> getMarkets() {
		return this.getMarkets(false);
	}

	/**
	 * get all markets in database
	 * @param groupByCurrency true to group markets by their currency
	 * @return map of market's id and market
	 */
	public Map<String, Market> getMarkets(boolean groupByCurrency) {
		Map<String, Market> markets = null;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = null;
		try {
			final String groupBy = groupByCurrency ? MarketColumns.CURRENCY : null;
			cursor = db.query(MARKET_TABLE_NAME, MarketColumns.PROJECTION, null, null, groupBy, null, MarketColumns.UI_ORDER);
			if (cursor.moveToFirst()) {
				markets = new LinkedHashMap<String, Market>(cursor.getCount());
				do {
					String id = cursor.getString(cursor.getColumnIndex(MarketColumns._ID));
					String name = cursor.getString(cursor.getColumnIndex(MarketColumns.NAME));
					String desc = cursor.getString(cursor.getColumnIndex(MarketColumns.DESCRIPTION));
					String country = cursor.getString(cursor.getColumnIndex(MarketColumns.COUNTRY));
					String currency = cursor.getString(cursor.getColumnIndex(MarketColumns.CURRENCY));
					int order = cursor.getInt(cursor.getColumnIndex(MarketColumns.UI_ORDER));
					long openFrom = cursor.getLong(cursor.getColumnIndex(MarketColumns.OPEN_FROM));
					long openTo = cursor.getLong(cursor.getColumnIndex(MarketColumns.OPEN_TO));
					double feeMax = cursor.getDouble(cursor.getColumnIndex(MarketColumns.FEE_MAX));
					double feeMin = cursor.getDouble(cursor.getColumnIndex(MarketColumns.FEE_MIN));
					double feePerc = cursor.getDouble(cursor.getColumnIndex(MarketColumns.FEE_PERC));
					int type = cursor.getInt(cursor.getColumnIndex(MarketColumns.TYPE));

					Market market = new Market(name, id,currency, desc, country, feePerc,
							feeMax, feeMin, openTo, openFrom, type, order);
					markets.put(id, market);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, "failed to read markets", e);
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
				int order = cursor.getInt(cursor.getColumnIndex(MarketColumns.UI_ORDER));
				long openFrom = cursor.getLong(cursor.getColumnIndex(MarketColumns.OPEN_FROM));
				long openTo = cursor.getLong(cursor.getColumnIndex(MarketColumns.OPEN_TO));
				double feeMax = cursor.getDouble(cursor.getColumnIndex(MarketColumns.FEE_MAX));
				double feeMin = cursor.getDouble(cursor.getColumnIndex(MarketColumns.FEE_MIN));
				double feePerc = cursor.getDouble(cursor.getColumnIndex(MarketColumns.FEE_PERC));
				int type = cursor.getInt(cursor.getColumnIndex(MarketColumns.TYPE));

				return new Market(name, id,currency,desc, country, feePerc, feeMax, feeMin, openTo, openFrom, type, order);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}

	/**
	 * update {@link MarketColumns#UI_ORDER} column with values from given markets
	 * @param markets markets to update their ui order
	 */
	public void updateMarketsUiOrder(Collection<Market> markets) {
		SQLiteDatabase db = this.getWritableDatabase();

		try{
			db.beginTransaction();

			ContentValues values = new ContentValues();
			for (Market market : markets) {
				values.put(MarketColumns.UI_ORDER, market.getUiOrder());
				int count = db.update(MARKET_TABLE_NAME, values, MarketColumns._ID+ "=?", new String[] { market.getId() });
				if (count == 0) {
					Log.w(Utils.LOG_TAG, "failed to update uiOrder for market " + market);
				}
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			this.close();
		}
	}

	/**
	 * Update current markets or insert new one. Old markets not present in the given
	 * collection will be marked as {@link Market#REMOVED}
	 * @param markets collection of all current markets to update/insert to db
	 */
	public void updateMarkets(Collection<Market> markets) {
		SQLiteDatabase db = this.getWritableDatabase();

		try{
			db.beginTransaction();
			Map<String, Market> currentMarkets = getMarkets();

			final ContentValues values = new ContentValues();
			for (Market market : markets) {
				values.put(MarketColumns.NAME, market.getName());
				values.put(MarketColumns.COUNTRY, market.getCountry());
				values.put(MarketColumns.CURRENCY, market.getCurrencyCode());
				values.put(MarketColumns.DESCRIPTION, market.getDescription());
				values.put(MarketColumns.FEE_MAX, market.getFeeMax());
				values.put(MarketColumns.FEE_MIN, market.getFeeMin());
				values.put(MarketColumns.FEE_PERC, market.getFeePerc());
				values.put(MarketColumns.OPEN_FROM, market.getOpenFrom());
				values.put(MarketColumns.OPEN_TO, market.getOpenTo());
				values.put(MarketColumns.TYPE, market.getType());

				int count = db.update(MARKET_TABLE_NAME, values, MarketColumns._ID+ "=?", new String[] { market.getId() });
				if (count == 0) {
					Log.d(Utils.LOG_TAG, "inserting market " + market);
					values.put(MarketColumns._ID, market.getId());
					db.insert(MARKET_TABLE_NAME, null, values);
				} else if (currentMarkets != null) {
					currentMarkets.remove(market.getId());
				}
				values.clear();
			}

			if (currentMarkets != null && currentMarkets.size() > 0) {
				for (Market market : currentMarkets.values()) {
					market.setUiOrder(Market.REMOVED);
				}
				this.updateMarketsUiOrder(currentMarkets.values());
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			this.close();
		}
	}

	public Map<StockItem, DayData> getLastDataSet(Market market, String orderBy) {
		Map<StockItem, DayData> dataSet = null;
		SQLiteDatabase db = this.getWritableDatabase();

		Cursor c = null;
		try {
			if (TextUtils.isEmpty(orderBy)) {
				orderBy = StockColumns.DEFAULT_SORT;
			}
			final String sql = String.format("SELECT %s, %s FROM %s stock INNER JOIN %s data on stock.%s = data.%s WHERE stock.%s=? ORDER BY stock.%s",
					DayDataColumns.PROJECTION_JOIN_STRING, StockColumns.PROJECTION_JOIN_STRING, STOCK_TABLE_NAME,
					DAY_DATA_TABLE_NAME, StockColumns._ID, DayDataColumns.STOCK_ID, StockColumns.MARKET_ID, orderBy);
			c = db.rawQuery(sql, new String[] {market.getId()});

			if (c.moveToFirst()) {
				dataSet = new LinkedHashMap<StockItem, DayData>(c.getCount());
				// there are _id columns in both tables, we need to get ids explicitly
				int stockIdIndex = DayDataColumns.PROJECTION.length;
				do {
					DayData dayData = readDayData(c, 0);
					StockItem item = readStockItem(market, c, stockIdIndex);
					dataSet.put(item, dayData);
				} while (c.moveToNext());
			}
		} finally {
			if (c != null) {
				c.close();
			}
			this.close();
		}
		return dataSet;
	}
}
