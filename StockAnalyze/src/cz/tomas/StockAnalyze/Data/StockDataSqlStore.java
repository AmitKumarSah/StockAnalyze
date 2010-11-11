/**
 * 
 */
package cz.tomas.StockAnalyze.Data;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

/**
 * @author tomas
 *
 */
public class StockDataSqlStore extends SQLiteOpenHelper {
	
	private final String DATABASE_NAME = "cz.tomas.StockAnalyze.Data";
	
	private final static int DATABASE_VERSION_NUMBER = 1;
	
	private final static String DATABASE_FILE_NAME = "cz.tomas.StockAnalyze.Data.db";
	
	public static final String STOCK_TABLE_NAME = "stock_data";
	private static final String STOCK_TABLE_CREATE =
         "CREATE TABLE " + STOCK_TABLE_NAME + " (" +
        // "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
         "ticker varchar(10), " +
         "date TEXT, " +				//ISO8601 strings "YYYY-MM-DD HH:MM:SS.SSS"
         "price real);";
	private static final String TABLE_DROP =
		"DROP TABLE IF EXISTS " + STOCK_TABLE_NAME;
	
	private static final String CREATE_TABLE_FEEDS = "create table feeds (feed_id integer primary key autoincrement, "
			+ "title text not null, url text not null, country text not null);";

	private static final String CREATE_TABLE_ARTICLES = "create table articles (article_id integer primary key autoincrement, "
			+ "feed_id int not null, title text not null, description text, url text not null, date int not null);";

	protected static final String FEEDS_TABLE = "feeds";
	protected static final String ARTICLES_TABLE = "articles";
	
	public StockDataSqlStore(Context context) {
		super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION_NUMBER);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			Log.d("StockDataSqlStore", "droping table!");
			db.execSQL(TABLE_DROP);
			Log.d("StockDataSqlStore", "creating table!");
			db.execSQL(STOCK_TABLE_CREATE);
			Log.d("StockDataSqlStore", "creating Feeds table!");
			db.execSQL(CREATE_TABLE_FEEDS);
			Log.d("StockDataSqlStore", "creating Articles table!");
			db.execSQL(CREATE_TABLE_ARTICLES);
			
		} catch (SQLException e) {
			e.printStackTrace();
			Log.d("StockDataSqlStore", "Failed to create database!\n" + e.getMessage());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}

}
