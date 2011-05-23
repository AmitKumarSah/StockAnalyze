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
import java.text.NumberFormat;
import java.util.List;

import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.PortfolioItem;
import cz.tomas.StockAnalyze.Data.Model.PortfolioSum;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import android.content.Context;

/**
 * class wrapping all portfolio functionality
 * @author tomas
 *
 */
public class Portfolio {

	private PortfolioSqlHelper sqlHelper;
	private DataManager dataManager;
	
	public Portfolio(Context context) {
		this.sqlHelper = new PortfolioSqlHelper(context);
		this.dataManager = DataManager.getInstance(context);
	}
	
	/*
	 * add new Portfolio item to database
	 */
	public void addToPortfolio(PortfolioItem item) throws SQLException {
		this.sqlHelper.addPortfolioItem(item);
	}
	
	public List<PortfolioItem> getGroupedPortfolioItems() {
		return this.sqlHelper.getGroupedPortfolioItems();
	}
	
	public List<PortfolioItem> getPortfolioItems() {
		return this.sqlHelper.getPortfolioItems();
	}
	
	/*
	 * remove all records records from given stock from db
	 */
	public void removeFromPortfolio(String stockId) {
		throw new RuntimeException("this is not yet implemented");
	}
	
	/*
	 * remove one record from db
	 */
	public void removeFromPortfolio(int id) {
		if (id != -1)
			this.sqlHelper.removeItem(id);
	}
}
