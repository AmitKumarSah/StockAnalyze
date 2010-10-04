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
	
	public static final String TABLE_NAME = "stock_data";
	private static final String TABLE_CREATE =
         "CREATE TABLE " + TABLE_NAME + " (" +
        // "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
         "ticker varchar(10), " +
         "date TEXT, " +				//ISO8601 strings "YYYY-MM-DD HH:MM:SS.SSS"
         "price real);";
	private static final String TABLE_DROP =
		"DROP TABLE IF EXISTS " + TABLE_NAME;
	 
	public StockDataSqlStore(Context context, String name,
			CursorFactory factory, int version) {
		super(context, null, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			Log.d("StockDataSqlStore", "Creating database!");
			Log.d("StockDataSqlStore", "droping table!");
			db.execSQL(TABLE_DROP);
			Log.d("StockDataSqlStore", "creating table!");
			db.execSQL(TABLE_CREATE);
		} catch (SQLException e) {
			e.printStackTrace();
			Log.d("StockDataSqlStore", "Failed to create database!\n" + e.getMessage());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
