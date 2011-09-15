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
 * Also initialize database - create all basic tables we need. 
 */
public class DataSqlHelper extends AbstractSqlHelper {
		
		private final static int DATABASE_VERSION_NUMBER = 15;
		
		private final static String DATABASE_FILE_NAME = "stocks.db";
		
		protected static final String STOCK_TABLE_NAME = "stock_item";
		protected static final String MARKET_TABLE_NAME = "market_item";
		protected static final String DAY_DATA_TABLE_NAME = "stock_day_data";
		protected static final String INTRADAY_DATA_TABLE_NAME = "stock_intraday_data";
		protected static final String PORTFOLIO_TABLE_NAME = "portfolio_item";
		
		private static final String STOCK_TABLE_CREATE =
	         "CREATE TABLE " + STOCK_TABLE_NAME + " (" +
	         "_id varchar(50) PRIMARY KEY," +
	         "ticker varchar(10) not null, " +
	         "is_favourite integer, " +
	         "is_index integer, " +
	         "market_id varchar(50), " + 
	         "name TEXT, " +
	         "FOREIGN KEY(market_id) REFERENCES " + MARKET_TABLE_NAME + "(id)" +
	         ");";
		
		private static final String MARKET_TABLE_CREATE =
	         "CREATE TABLE " + MARKET_TABLE_NAME + " (" +
	         "_id varchar(50) PRIMARY KEY," +
	         "name varchar(50) not null, " +
	         "currency varchar(10) not null, " +
	         "ui_order integer, " +
	         "description TEXT);";
		
		private static final String INTRADAY_DATA_TABLE_CREATE = 
			"CREATE TABLE " + INTRADAY_DATA_TABLE_NAME + " (" +
	         "_id integer PRIMARY KEY AUTOINCREMENT," +
	         "stock_id varchar(50)," +
	         "change real not null," +
	         "date integer not null, " +				//long - miliseconds
	         "price real not null," +
	         "volume real not null," +
	         "FOREIGN KEY(stock_id) REFERENCES " + STOCK_TABLE_NAME + "(id)" +
	         ");";
		
		private static final String DAY_DATA_TABLE_CREATE = 
			"CREATE TABLE " + DAY_DATA_TABLE_NAME + " (" +
	         "_id integer PRIMARY KEY AUTOINCREMENT," +
	         "stock_id varchar(50)," +
	         "year_min real," +
	         "year_max real," +
	         "change real not null," +
	         "date integer not null, " +				//long - miliseconds
	         "last_update integer not null, " +				//long - miliseconds
	         "price real not null," +
	         "volume real not null," +
	         "FOREIGN KEY(stock_id) REFERENCES " + STOCK_TABLE_NAME + "(id)" +
	         ");";

		private static final String PORTFOLIO_TABLE_CREATE = 
			"CREATE TABLE " + PORTFOLIO_TABLE_NAME + " (" +
	         "_id integer PRIMARY KEY AUTOINCREMENT," +
	         "stock_id varchar(50)," +
	         "buy_date integer not null," +
	         "sell_date integer," +
	         "count integer not null," +
	         "buy_price real not null," +
	         "sell_price real not null," +
	         "buy_fee real," +
	         "sell_fee real," +
	         "name varchar(20)," +
	         "market_id varchar(50)," +
	         "FOREIGN KEY(stock_id) REFERENCES " + STOCK_TABLE_NAME + "(id)" +
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
				Log.d(Utils.LOG_TAG, "creating intraday data table!");
				db.execSQL(INTRADAY_DATA_TABLE_CREATE);
				Log.d(Utils.LOG_TAG, "creating portfolio table!");
				db.execSQL(PORTFOLIO_TABLE_CREATE);				
			} catch (SQLException e) {
				Log.e(Utils.LOG_TAG, "Failed to create database!\n" + e.getMessage(), e);
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d(Utils.LOG_TAG, "droping tables!");
			db.execSQL(TABLE_DROP + INTRADAY_DATA_TABLE_NAME);
			db.execSQL(TABLE_DROP + DAY_DATA_TABLE_NAME);
			db.execSQL(TABLE_DROP + STOCK_TABLE_NAME);
			db.execSQL(TABLE_DROP + PORTFOLIO_TABLE_NAME);
			db.execSQL(TABLE_DROP + MARKET_TABLE_NAME);
			onCreate(db);
		}
	}
