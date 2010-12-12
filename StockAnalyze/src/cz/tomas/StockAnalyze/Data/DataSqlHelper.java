package cz.tomas.StockAnalyze.Data;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataSqlHelper extends SQLiteOpenHelper {
		
		private final String DATABASE_NAME = "cz.tomas.StockAnalyze.Data";
		
		private final static int DATABASE_VERSION_NUMBER = 5;
		
		private final static String DATABASE_FILE_NAME = "cz.tomas.StockAnalyze.Data.db";
		
		protected static final String STOCK_TABLE_NAME = "stock_item";
		protected static final String DAY_DATA_TABLE_NAME = "stock_day_data";
		protected static final String INTRADAY_DATA_TABLE_NAME = "stock_intraday_data";
		protected static final String USER_STOCK_TABLE_NAME = "user_stocks";
		
		private static final String STOCK_TABLE_CREATE =
	         "CREATE TABLE " + STOCK_TABLE_NAME + " (" +
	         "id varchar(50) PRIMARY KEY," +
	         "ticker varchar(10) not null, " +
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
	         "price real not null," +
	         "volume real not null," +
	         "FOREIGN KEY(stock_id) REFERENCES " + STOCK_TABLE_NAME + "(id)" +
	         ");";

		private static final String USER_STOCK_TABLE_CRETE = 
			"CREATE TABLE " + USER_STOCK_TABLE_NAME + " (" +
	         "id integer PRIMARY KEY AUTOINCREMENT," +
	         "stock_id varchar(50)," +
	         "tag varchar(20)," +
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
				Log.d("DataSqlHelper", "creating stock table!");
				db.execSQL(STOCK_TABLE_CREATE);
				Log.d("DataSqlHelper", "creating day data table!");
				db.execSQL(DAY_DATA_TABLE_CREATE);
				Log.d("DataSqlHelper", "creating intraday data table!");
				db.execSQL(INTRADAY_DATA_TABLE_CREATE);
				Log.d("DataSqlHelper", "creating user stock table!");
				db.execSQL(USER_STOCK_TABLE_CRETE);
				
				Log.d("DataSqlHelper", "creating Feeds table!");
				db.execSQL(CREATE_TABLE_FEEDS);
				Log.d("DataSqlHelper", "creating Articles table!");
				db.execSQL(CREATE_TABLE_ARTICLES);
				
			} catch (SQLException e) {
				e.printStackTrace();
				Log.d("DataSqlHelper", "Failed to create database!\n" + e.getMessage());
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d("StockDataSqlStore", "droping tables!");
			db.execSQL(TABLE_DROP + INTRADAY_DATA_TABLE_NAME);
			db.execSQL(TABLE_DROP + DAY_DATA_TABLE_NAME);
			db.execSQL(TABLE_DROP + STOCK_TABLE_NAME);
			db.execSQL(TABLE_DROP + USER_STOCK_TABLE_NAME);
			db.execSQL(TABLE_DROP + ARTICLES_TABLE_NAME);
			db.execSQL(TABLE_DROP + FEEDS_TABLE_NAME);
			onCreate(db);
		}

	}
