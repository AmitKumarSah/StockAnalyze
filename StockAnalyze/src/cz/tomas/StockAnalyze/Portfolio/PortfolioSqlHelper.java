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

	/**
	 * add new portfolio item to database
	 * @param item
	 * @throws SQLException
	 */
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
			this.close();
		}
	}

	/**
	 * get portfolio items grouped by stock id - sums up positions.
	 * So for each stock, that is in portfolio, it will find all portfolio items
	 * and group them to get total count and average buy/sell prices
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
			this.close();
		}
		return items;
	}
	
	
	/**
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
			this.close();
		}
		return items;
		
	}
	
	/**
	 * get all portfolio items in database for one stock
	 */
	public List<PortfolioItem> getPortfolioItems(String stockId) {
		List<PortfolioItem> items = new ArrayList<PortfolioItem>();
		Cursor c = null;
		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
			
			c = db.query(PORTFOLIO_TABLE_NAME, new String [] {"count", "buy_price", "sell_price",
					"buy_date", "sell_date", "name", "buy_fee", "sell_fee", "market_id", "id" },
					"stock_id=?", new String[] { stockId }, null, null, null);
			if (c.moveToFirst())
				do {
					int count = c.getInt(0);
					float buyPrice = c.getFloat(1);
					float sellPrice = c.getFloat(2);
					long buyDate = c.getLong(3);
					long sellDate = c.getLong(4);
					String name = c.getString(5);
					float buyFee = c.getFloat(6);
					float sellFee = c.getFloat(7);
					String marketId = c.getString(8);
					int id = c.getInt(9);
					PortfolioItem item = new PortfolioItem(id, stockId, name, count, buyPrice, sellPrice, 
							buyDate, sellDate, buyFee, sellFee, marketId);
					items.add(item);
				} while(c.moveToNext());
		} finally {
			if (c != null)
				c.close();
			this.close();
		}
		return items;
		
	}


	public void removeItem(int id) {
		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
			db.delete(PORTFOLIO_TABLE_NAME, "id=?", new String[] { String.valueOf(id) });
		} finally {
			this.close();
		}
	}

}
