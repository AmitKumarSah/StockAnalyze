package cz.tomas.StockAnalyze.Data;

import cz.tomas.StockAnalyze.utils.Utils;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/*
 * initialize database - create all tables we need 
 */
public class DataSqlHelper extends SQLiteOpenHelper {
		
		private final String DATABASE_NAME = "cz.tomas.StockAnalyze.Data";
		
		private final static int DATABASE_VERSION_NUMBER = 10;
		
		private final static String DATABASE_FILE_NAME = "cz.tomas.StockAnalyze.Data.db";
		
		protected static final String STOCK_TABLE_NAME = "stock_item";
		protected static final String DAY_DATA_TABLE_NAME = "stock_day_data";
		protected static final String INTRADAY_DATA_TABLE_NAME = "stock_intraday_data";
		protected static final String PORTFOLIO_TABLE_NAME = "portfolio_item";
		
		private static final String STOCK_TABLE_CREATE =
	         "CREATE TABLE " + STOCK_TABLE_NAME + " (" +
	         "id varchar(50) PRIMARY KEY," +
	         "ticker varchar(10) not null, " +
	         "is_favourite integer, " +
	         "name TEXT);";
		
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
				+ "feed_id int not null, title text not null, description text, url text not null, date int not null, read int);";

		protected static final String FEEDS_TABLE_NAME = "feeds";
		protected static final String ARTICLES_TABLE_NAME = "articles";
		
		public DataSqlHelper(Context context) {
			super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION_NUMBER);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
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
				e.printStackTrace();
				Log.d(Utils.LOG_TAG, "Failed to create database!\n" + e.getMessage());
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
			onCreate(db);
		}

	}
