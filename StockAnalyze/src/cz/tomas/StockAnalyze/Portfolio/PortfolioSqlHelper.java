/**
 * 
 */
package cz.tomas.StockAnalyze.Portfolio;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import cz.tomas.StockAnalyze.Data.DataSqlHelper;
import cz.tomas.StockAnalyze.Data.Model.PortfolioItem;
import cz.tomas.StockAnalyze.Data.Model.PortfolioSum;

/*
 * 
 * "CREATE TABLE " + PORTFOLIO_TABLE_NAME + " (" +
 * "id integer PRIMARY KEY AUTOINCREMENT," +
 * "stock_id varchar(50)," +
 * "buy_date integer not null" +
 * "sell_date integer" +
 * "count integer not null" +
 * "buy_price real not null" +
 * "sell_price real not null" +
 * "buy_fee real" +
 * "sell_fee real" +
 * "name varchar(20)," +
 * "market_id varchar(50)" +
 * "FOREIGN KEY(stock_id) REFERENCES " + STOCK_TABLE_NAME + "(id)" +
 * ");";
*/

/**
 * @author tomas
 *
 */
public class PortfolioSqlHelper extends DataSqlHelper {

	PortfolioSqlHelper(Context context) {
		super(context);
	}

	void addPortfolioItem(PortfolioItem item) throws SQLException {
		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("stock_id", item.getStockId());
			values.put("buy_date", item.getBuyDate());
			values.put("sell_date", item.getSellDate());
			values.put("count", item.getStockCount());
			values.put("buy_price", item.getBuyPrice());
			values.put("sell_price", item.getSellPrice());
			values.put("buy_fee", item.getBuyFee());
			values.put("sell_fee", item.getSellFee());
			values.put("name", item.getPortfolioName());
			values.put("market_id", item.getMarketId());
			
			long result = db.insert(PORTFOLIO_TABLE_NAME, null, values);
			if (result == -1)
				throw new SQLException("Failed to insert portfolio item!");
		} finally {
			if (db != null)
				db.close();
		}
	}

	/*
	 * get portfolio items grouped by stock id - sums up positions
	 */
	public List<PortfolioItem> getGroupedPortfolioItems() {
		List<PortfolioItem> items = new ArrayList<PortfolioItem>();
		Cursor c = null;
		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
			
			c = db.query(PORTFOLIO_TABLE_NAME, new String [] {"stock_id", "SUM(count)", "AVG(buy_price)", "AVG(sell_price)",
					"buy_date", "sell_date", "name", "SUM(buy_fee)", "SUM(sell_fee)", "market_id", "id" },
					null, null, "stock_id", null, "buy_date");
			if (c.moveToFirst())
				do {
					String stockId = c.getString(0);
					int count = c.getInt(1);
					float buyPrice = c.getFloat(2);
					float sellPrice = c.getFloat(3);
					long buyDate = c.getLong(4);
					long sellDate = c.getLong(5);
					String name = c.getString(6);
					float buyFee = c.getFloat(7);
					float sellFee = c.getFloat(8);
					String marketId = c.getString(9);
					int id = c.getInt(10);
					PortfolioItem item = new PortfolioItem(id, stockId, name, count, buyPrice, sellPrice, 
							buyDate, sellDate, buyFee, sellFee, marketId);
					items.add(item);
				} while(c.moveToNext());
		} finally {
			if (c != null)
				c.close();
			if (db != null)
				db.close();
		}
		return items;
	}
	
	/*
	 * get all portfolio items in database
	 */
	public List<PortfolioItem> getPortfolioItems() {
		List<PortfolioItem> items = new ArrayList<PortfolioItem>();
		Cursor c = null;
		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
			
			c = db.query(PORTFOLIO_TABLE_NAME, new String [] {"stock_id", "count", "buy_price", "sell_price",
					"buy_date", "sell_date", "name", "buy_fee", "sell_fee", "market_id", "id" },
					null, null, null, null, null);
			if (c.moveToFirst())
				do {
					String stockId = c.getString(0);
					int count = c.getInt(1);
					float buyPrice = c.getFloat(2);
					float sellPrice = c.getFloat(3);
					long buyDate = c.getLong(4);
					long sellDate = c.getLong(5);
					String name = c.getString(6);
					float buyFee = c.getFloat(7);
					float sellFee = c.getFloat(8);
					String marketId = c.getString(9);
					int id = c.getInt(10);
					PortfolioItem item = new PortfolioItem(id, stockId, name, count, buyPrice, sellPrice, 
							buyDate, sellDate, buyFee, sellFee, marketId);
					items.add(item);
				} while(c.moveToNext());
		} finally {
			if (c != null)
				c.close();
			if (db != null)
				db.close();
		}
		return items;
		
	}

	public void removeItem(int id) {
		SQLiteDatabase db = null;
		db = this.getWritableDatabase();
		db.delete(PORTFOLIO_TABLE_NAME, "id=?", new String[] { String.valueOf(id) });
		db.close();
	}

}
