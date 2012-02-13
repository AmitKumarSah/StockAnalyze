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
package cz.tomas.StockAnalyze.Data;

import cz.tomas.StockAnalyze.utils.Utils;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Provides access to acquirable database.
 * Also initialize database - create all basic tables we need for stocks.
 */
public class DataSqlHelper extends AbstractSqlHelper {

	protected static final boolean DEBUG = Utils.DEBUG;
	protected static final boolean VERBOSE = DEBUG && false;

	private final static int DATABASE_VERSION_NUMBER = 21;

	private final static String DATABASE_FILE_NAME = "stocks.db";

	protected static final String STOCK_TABLE_NAME = "stock_item";
	protected static final String MARKET_TABLE_NAME = "market_item";
	protected static final String DAY_DATA_TABLE_NAME = "stock_day_data";
	protected static final String PORTFOLIO_TABLE_NAME = "portfolio_item";

	private static final String STOCK_TABLE_CREATE =
         "CREATE TABLE " + STOCK_TABLE_NAME + " (" +
		         StockColumns._ID + " varchar(50) PRIMARY KEY," +
		         StockColumns.TICKER + " varchar(10) not null, " +
		         StockColumns.IS_FAVOURITE + " integer, " +
		         StockColumns.IS_INDEX + " integer, " +
		         StockColumns.MARKET_ID + " varchar(50), " +
		         StockColumns.NAME + " TEXT, " +
		         StockColumns.FLAG + " integer default 0, " +
		         "FOREIGN KEY(" + StockColumns.MARKET_ID + ") REFERENCES " + MARKET_TABLE_NAME + "(" + MarketColumns._ID + ")" +
         ");";

	private static final String MARKET_TABLE_CREATE =
         "CREATE TABLE " + MARKET_TABLE_NAME + " (" +
		         MarketColumns._ID + " text PRIMARY KEY," +
		         MarketColumns.NAME + " text not null, " +
		         MarketColumns.CURRENCY + " text not null, " +
		         MarketColumns.COUNTRY + " text not null, " +
		         MarketColumns.UI_ORDER + " integer, " +
		         MarketColumns.DESCRIPTION + " text," +
		         MarketColumns.OPEN_FROM + " integer," +
		         MarketColumns.OPEN_TO + " integer," +
		         MarketColumns.FEE_MIN + " real," +
		         MarketColumns.FEE_MAX + " real," +
		         MarketColumns.INDEX_ID + " text," +
		         MarketColumns.FEE_PERC + " real" +
		         ");";

	private static final String DAY_DATA_TABLE_CREATE =
		"CREATE TABLE " + DAY_DATA_TABLE_NAME + " (" +
				DayDataColumns._ID + " integer PRIMARY KEY AUTOINCREMENT," +
				DayDataColumns.STOCK_ID + " text," +
				DayDataColumns.YEAR_MIN + " real," +
				DayDataColumns.YEAR_MAX + " real," +
				DayDataColumns.CHANGE + " real not null," +
				DayDataColumns.DATE + " integer not null, " +				//long - miliseconds
				DayDataColumns.LAST_UPDATE + " integer not null, " +				//long - miliseconds
				DayDataColumns.PRICE + " real not null," +
				DayDataColumns.VOLUME + " real not null," +
				"FOREIGN KEY(" + DayDataColumns.STOCK_ID + ") REFERENCES " + STOCK_TABLE_NAME + "(" + StockColumns._ID + ")" +
         ");";

	private static final String PORTFOLIO_TABLE_CREATE =
		"CREATE TABLE " + PORTFOLIO_TABLE_NAME + " (" +
				PortfolioColumns._ID + " integer PRIMARY KEY AUTOINCREMENT," +
				PortfolioColumns.STOCK_ID + " varchar(50)," +
				PortfolioColumns.BUY_DATE + " integer not null," +
				PortfolioColumns.SELL_DATE + " integer," +
				PortfolioColumns.COUNT + " integer not null," +
				PortfolioColumns.BUY_PRICE + " real not null," +
				PortfolioColumns.SELL_PRICE + " real not null," +
				PortfolioColumns.BUY_FEE + " real," +
				PortfolioColumns.SELL_FEE + " real," +
				PortfolioColumns.NAME + " text," +
				PortfolioColumns.MARKET_ID + " text," +
				"FOREIGN KEY(" + PortfolioColumns.STOCK_ID + ") REFERENCES " + STOCK_TABLE_NAME + "(" + StockColumns._ID + ")," +
				"FOREIGN KEY(" + PortfolioColumns.MARKET_ID + ") REFERENCES " + MARKET_TABLE_NAME + "(" + MarketColumns._ID + ")" +
         ");";

	public DataSqlHelper(Context context) {
		super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION_NUMBER);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			Log.d(Utils.LOG_TAG, "creating market table!");
			db.execSQL(MARKET_TABLE_CREATE);
			Log.d(Utils.LOG_TAG, "creating stock table!");
			db.execSQL(STOCK_TABLE_CREATE);
			Log.d(Utils.LOG_TAG, "creating day data table!");
			db.execSQL(DAY_DATA_TABLE_CREATE);
			Log.d(Utils.LOG_TAG, "creating portfolio table!");
			db.execSQL(PORTFOLIO_TABLE_CREATE);
		} catch (SQLException e) {
			Log.e(Utils.LOG_TAG, "Failed to create database!\n", e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(Utils.LOG_TAG, "droping tables!");
		db.execSQL(TABLE_DROP + DAY_DATA_TABLE_NAME);
		db.execSQL(TABLE_DROP + STOCK_TABLE_NAME);
		db.execSQL(TABLE_DROP + PORTFOLIO_TABLE_NAME);
		db.execSQL(TABLE_DROP + MARKET_TABLE_NAME);
		onCreate(db);
	}

	public static class StockColumns {

		public static final String _ID = "_id";
		public static final String TICKER = "ticker";
		public static final String IS_FAVOURITE = "is_favourite";
		public static final String IS_INDEX = "is_index";
		public static final String MARKET_ID = "market_id";
		public static final String NAME = "name";
		public static final String FLAG = "flag";

		public static final String DEFAULT_SORT = NAME + " ASC";

		public static final String[] PROJECTION = new String[] {"_id", "ticker", "name", "market_id"};
		public static final String PROJECTION_JOIN_STRING =
				"stock._id, stock.ticker, stock.name, stock.market_id";
	}

	public static class MarketColumns {

		public static final String _ID = "_id";
		public static final String NAME = "name";
		public static final String CURRENCY = "currency";
		public static final String COUNTRY = "country";
		public static final String UI_ORDER = "ui_order";
		public static final String DESCRIPTION = "description";
		public static final String OPEN_FROM = "open_from";
		public static final String OPEN_TO = "open_top";
		public static final String FEE_MIN = "fee_min";
		public static final String FEE_MAX = "fee_max";
		public static final String FEE_PERC = "fee_perc";
		public static final String INDEX_ID = "index_id";

		public static final String[] PROJECTION = new String[]{ _ID, NAME, CURRENCY, COUNTRY, UI_ORDER, DESCRIPTION,
													OPEN_FROM, OPEN_TO, FEE_MAX, FEE_MIN, FEE_PERC };
	}

	public static class DayDataColumns {

		public static final String _ID = "_id";
		public static final String STOCK_ID = "stock_id";
		public static final String YEAR_MIN = "year_min";
		public static final String YEAR_MAX = "year_max";
		public static final String CHANGE = "change";
		public static final String DATE = "date";
		public static final String LAST_UPDATE = "last_update";
		public static final String PRICE = "price";
		public static final String VOLUME = "volume";
		public static final String[] PROJECTION = new String[] {
				"_id", "price", "change", "year_max", "year_min", "date", "volume", "last_update", "stock_id" };
		public static final String PROJECTION_JOIN_STRING =
				"data._id, data.price, data.change, data.year_max, data.year_min, data.date, data.volume, data.last_update, data.stock_id";
		public static final String DEFAULT_SORT = "date DESC";
	}

	public static class PortfolioColumns {

		public static final String _ID = "_id";
		public static final String NAME = "name";
		public static final String STOCK_ID = "stock_id";
		public static final String BUY_DATE = "buy_date";
		public static final String SELL_DATE = "sell_date";
		public static final String COUNT = "count";
		public static final String BUY_PRICE = "buy_price";
		public static final String SELL_PRICE = "sell_price";
		public static final String BUY_FEE = "buy_fee";
		public static final String SELL_FEE = "sell_fee";
		public static final String MARKET_ID = "market_id";
	}
}
