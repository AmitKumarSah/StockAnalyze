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
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import cz.tomas.StockAnalyze.Data.Model.PortfolioItem;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * class wrapping all portfolio functionality
 * @author tomas
 *
 */
public class Portfolio {

	private PortfolioSqlHelper sqlHelper;
	
	public Portfolio(Context context) {
		this.sqlHelper = new PortfolioSqlHelper(context);
	}
	
	/**
	 * add new Portfolio item to database
	 */
	public void addToPortfolio(PortfolioItem item) throws SQLException {
		try {
			this.sqlHelper.acquireDb(this);
			this.sqlHelper.addPortfolioItem(item);
		} finally {
			this.sqlHelper.releaseDb(true, this);
		}
	}
	
	/**
	 * get portfolio items grouped by stock id - sums up positions. So for each stock, that is in 
	 * portfolio, it will find all portfolio items and group them to get total count and average buy/sell prices
	 * @return
	 */
	public List<PortfolioItem> getGroupedPortfolioItems() {
		this.sqlHelper.acquireDb(this);
		Map<String, PortfolioItem> bought;
		Map<String, PortfolioItem> sold;
		try {
			bought = this.sqlHelper.getGroupedPortfolioItems(true);
			sold = this.sqlHelper.getGroupedPortfolioItems(false);
		} finally {
			this.sqlHelper.releaseDb(true, this);
		}
		List<PortfolioItem> result = new ArrayList<PortfolioItem>();
		
		// match bought and sold items together
		for (Entry<String, PortfolioItem> entry : bought.entrySet()) {
			PortfolioItem item = entry.getValue();
			if (sold.containsKey(entry.getKey())) {
				PortfolioItem soldItem = sold.get(entry.getKey());
				item.setSellFee(soldItem.getSellFee());
				item.setSellPrice(soldItem.getSellPrice());
				item.setSellDate(soldItem.getSellDate());
				item.setSoldStockCount(soldItem.getSoldStockCount());
				//item.setBoughtStockCount(soldItem.getBoughtStockCount());
				
				sold.remove(entry.getKey());
			}
			
			result.add(item);
		}
		
		// add the rest of sold items to result list
		for (Entry<String, PortfolioItem> entry : sold.entrySet()) {
			result.add(entry.getValue());
		}
		
		return result;
	}
	
	/**
	 * get every single portfolio item in database for given stock
	 * @return
	 */
	public List<PortfolioItem> getPortfolioItems(String stockId) {
		try {
			this.sqlHelper.acquireDb(this);
			return this.sqlHelper.getPortfolioItems(stockId);
		} finally {
			this.sqlHelper.releaseDb(true, this);
		}
	}
	
	/**
	 * get all portfolio items
	 * (test and debug purposes so far)
	 */
	public List<PortfolioItem> getPortfolioItems() {
		return this.sqlHelper.getPortfolioItems();
	}
	
	/**
	 * remove one portfolio record from db
	 */
	public void removeFromPortfolio(int id) {
		if (id != -1) {
			this.sqlHelper.removeItem(id);
		}
	}
	
	/**
	 * remove all portfolio items for given stock
	 */
	public void removeFromPortfolio(String stockId) {
		if (! TextUtils.isEmpty(stockId)) {
			
			try {
				this.sqlHelper.acquireDb(this);
				SQLiteDatabase db = this.sqlHelper.getWritableDatabase();
				db.beginTransaction();
				List<PortfolioItem> items = this.sqlHelper.getPortfolioItems(stockId);
				for (PortfolioItem portfolioItem : items) {
					this.sqlHelper.removeItem(portfolioItem.getId());
				}
				db.setTransactionSuccessful();
			} finally {
				try {
					this.sqlHelper.getWritableDatabase().endTransaction();
				} catch (Exception e) {
					Log.e(Utils.LOG_TAG, "faild to end transaction", e);
				}
				this.sqlHelper.releaseDb(true, this);
			}
		} else {
			throw new NullPointerException("stock id cannot be null, can't remove associated portfolio items");
		}
	}
}
