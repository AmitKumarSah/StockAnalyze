/**
 * 
 */
package cz.tomas.StockAnalyze.Data;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.News.Article;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;


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

	public boolean checkForStock(StockItem item) {
		boolean result = false;
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			List<DayData> datas = new ArrayList<DayData>();
			Cursor c = null;
			try {
				c = db.query(STOCK_TABLE_NAME, new String[] { "id" }, null, null, null, null, null);
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
			Log.d("StockDataSqlStore", "failed to get data." + e.getMessage());
			e.printStackTrace();
		} finally {
			this.close();
		}
		return result;
	}
	
	public boolean checkForData(Calendar cal) {
		// TODO
		return false;
	}
	
	public void insertStockItem(StockItem item) {
		try {
			Log.d("StockDataSqlStore", "inserting stockItem " + item.toString());
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("id", item.getId());
			//values.put("date", "date('now')");
			values.put("ticker", item.getTicker());
			values.put("name", item.getName());
			
			db.insert(STOCK_TABLE_NAME, null, values);
			//db.execSQL("INSERT INTO " + StockDataSqlStore.STOCK_TABLE_NAME + " values('"+ ticker + "', date('now'), " + data.getPrice() + ");");
		} catch (SQLException e) {
			Log.d("StockDataSqlStore", "failed to insert data." + e.getMessage());
			e.printStackTrace();
		} finally {
			this.close();
		}
	}
	
	public void insertDayData(StockItem item, DayData data) {
		try {
			Log.d("StockDataSqlStore", "inserting day data for " + item.getTicker() + " to db");
			if (! this.checkForStock(item))
				this.insertStockItem(item);
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("date", "date('now')");
			values.put("price", data.getPrice());
			values.put("change", data.getChange());
			values.put("stock_id", item.getId());
			values.put("year_max", data.getYearMaximum());
			values.put("year_min", data.getYearMaximum());
			
			db.insert(DAY_DATA_TABLE_NAME, null, values);
			//db.execSQL("INSERT INTO " + StockDataSqlStore.STOCK_TABLE_NAME + " values('"+ ticker + "', date('now'), " + data.getPrice() + ");");
		} catch (SQLException e) {
			Log.d("StockDataSqlStore", "failed to insert stock item." + e.getMessage());
			e.printStackTrace();
		} finally {
			this.close();
		}
	}

	public DayData getDayData(Calendar now, StockItem item) {
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			List<DayData> datas = new ArrayList<DayData>();
			Cursor c = null;
			try {
				c = db.query(DAY_DATA_TABLE_NAME, new String[] {
						"price", "change", "year_max", "year_min", "date", "volume" }, "stock_id="
						+ item.getId(), null, null, null, "date");

				c.moveToFirst();
				do {
					float price = c.getFloat(0);
					float change = c.getFloat(1);
					float max = c.getFloat(2);
					float min = c.getFloat(3);
					String strDate = c.getString(4);
					float volume = c.getFloat(5);
					
					long d = Date.parse(strDate);
					DateFormat frm = DateFormat.getInstance();
					Date date = frm.parse(strDate);
					
					DayData data = new DayData(price, change, date, volume, max, min);
					datas.add(data);
				} while (c.moveToNext());
			} catch (SQLException e) {
				Log.e("StockDataSqlStore", e.toString());
			} catch (ParseException e) {
				Log.e("StockDataSqlStore", "Failed to parse date!\n" + e.toString());
			} finally {
				if (c != null)
					c.close();
			}
		} catch (SQLException e) {
			Log.d("StockDataSqlStore", "failed to get data." + e.getMessage());
			e.printStackTrace();
		} finally {
			this.close();
		}
		return null;
	}

}
