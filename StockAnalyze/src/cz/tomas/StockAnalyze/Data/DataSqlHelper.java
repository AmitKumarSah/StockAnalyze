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
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Provides access to acquirable database.
 * Also initialize database - create all basic tables we need. 
 */
public class DataSqlHelper extends SQLiteOpenHelper {
		
		//private final String DATABASE_NAME = "cz.tomas.StockAnalyze.Data";
		
		private final static int DATABASE_VERSION_NUMBER = 13;
		
		private final static String DATABASE_FILE_NAME = "cz.tomas.StockAnalyze.Data.db";
		
		protected static final String STOCK_TABLE_NAME = "stock_item";
		protected static final String MARKET_TABLE_NAME = "market_item";
		protected static final String DAY_DATA_TABLE_NAME = "stock_day_data";
		protected static final String INTRADAY_DATA_TABLE_NAME = "stock_intraday_data";
		protected static final String PORTFOLIO_TABLE_NAME = "portfolio_item";
		
		private static final String STOCK_TABLE_CREATE =
	         "CREATE TABLE " + STOCK_TABLE_NAME + " (" +
	         "id varchar(50) PRIMARY KEY," +
	         "ticker varchar(10) not null, " +
	         "is_favourite integer, " +
	         "is_index integer, " +
	         "market_id varchar(50), " + 
	         "name TEXT, " +
	         "FOREIGN KEY(market_id) REFERENCES " + MARKET_TABLE_NAME + "(id)" +
	         ");";
		
		private static final String MARKET_TABLE_CREATE =
	         "CREATE TABLE " + MARKET_TABLE_NAME + " (" +
	         "id varchar(50) PRIMARY KEY," +
	         "name varchar(50) not null, " +
	         "currency varchar(10) not null, " +
	         "description TEXT);";
		
		private static final String INTRADAY_DATA_TABLE_CREATE = 
			"CREATE TABLE " + INTRADAY_DATA_TABLE_NAME + " (" +
	         "id integer PRIMARY KEY AUTOINCREMENT," +
	         "stock_id varchar(50)," +
	         "change real not null," +
	         "date integer not null, " +				//long - miliseconds
	         "price real not null," +
	         "volume real not null," +
	         "FOREIGN KEY(stock_id) REFERENCES " + STOCK_TABLE_NAME + "(id)" +
	         ");";
		
		private static final String DAY_DATA_TABLE_CREATE = 
			"CREATE TABLE " + DAY_DATA_TABLE_NAME + " (" +
	         "id integer PRIMARY KEY AUTOINCREMENT," +
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
	         "id integer PRIMARY KEY AUTOINCREMENT," +
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
		
		private static final String TABLE_DROP =
			"DROP TABLE IF EXISTS ";
		
		private static final String CREATE_TABLE_FEEDS = "create table feeds (feed_id integer primary key autoincrement, "
				+ "title text not null, url text not null, country text not null);";

		private static final String CREATE_TABLE_ARTICLES = "create table articles (article_id integer primary key autoincrement, "
				+ "feed_id int not null, title text not null, description text, tags text, url text not null, date int not null, read int);";

		protected static final String FEEDS_TABLE_NAME = "feeds";
		protected static final String ARTICLES_TABLE_NAME = "articles";
		
		/**
		 * db counter - only for diag purpose so far
		 */
		private static int acquireCounter = 0;
		
		private static boolean keepDbOpen = false;
		
		/**
		 * determine deffered db close
		 * if set to true, in next successful db release, 
		 * will close the db
		 */
		private static boolean closeDb = false;
		
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
				
				Log.d(Utils.LOG_TAG, "creating Feeds table!");
				db.execSQL(CREATE_TABLE_FEEDS);
				Log.d(Utils.LOG_TAG, "creating Articles table!");
				db.execSQL(CREATE_TABLE_ARTICLES);
				
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
			db.execSQL(TABLE_DROP + ARTICLES_TABLE_NAME);
			db.execSQL(TABLE_DROP + FEEDS_TABLE_NAME);
			db.execSQL(TABLE_DROP + MARKET_TABLE_NAME);
			onCreate(db);
		}


		/**
		 * Close any open database object, if database isn't acquired
		 */
		@Override
		public synchronized void close() {
			if (! keepDbOpen) {
				Log.d(Utils.LOG_TAG, "Closing database...");
				super.close();
			}
		}

		/**
		 * calling this method will cause the database connection to be open until 
		 * releaseDb() will be called
		 * The purpose of this method is to allow service to access database and not get 
		 * interrupted - closed - by user interaction in UI.
		 * This calling won't open the connection.
		 * 
		 * @param applicant object acquiring the database - only informative purpose
		 */
		public synchronized void acquireDb(Object applicant) {
			keepDbOpen = true;
			acquireCounter++;
			if (applicant == null)
				applicant = "unknown";
			Log.d(Utils.LOG_TAG, String.format("database acquired by %s ... %d", applicant.toString(), acquireCounter));
		}
		
		/**
		 * Release previously acquired database. If database wasn't acquired,
		 * nothing would happen
		 * @param close true to invoke close method
		 * @param applicant object releasing database - only informative
		 */
		public synchronized void releaseDb(boolean close, Object applicant) {
			acquireCounter--;
			if (applicant == null)
				applicant = "unknown";
			if (acquireCounter == 0){
				Log.d(Utils.LOG_TAG, "database released by " + applicant.toString());
				keepDbOpen = false;

				if (close || closeDb) {
					this.close();
					closeDb = false;
				}
			} else {
				Log.d(Utils.LOG_TAG, String.format("request from %s: can NOT release db, still acquired... %d", applicant.toString(), acquireCounter));
				closeDb |= close;
			}
			
		}

		@Override
		public synchronized SQLiteDatabase getReadableDatabase() {
			Log.d(Utils.LOG_TAG, "db open R request, counter: ");
			return super.getReadableDatabase();
		}


		@Override
		public synchronized SQLiteDatabase getWritableDatabase() {
			//Log.d(Utils.LOG_TAG, "db open RW request, counter: ");
			return super.getWritableDatabase();
		}
	}
