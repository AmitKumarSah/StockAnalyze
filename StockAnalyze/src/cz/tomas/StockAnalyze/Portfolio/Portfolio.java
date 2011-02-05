/**
 * 
 */
package cz.tomas.StockAnalyze.Portfolio;

import java.sql.SQLException;
import java.util.List;

import cz.tomas.StockAnalyze.Data.Model.PortfolioItem;
import android.content.Context;

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
	
	public void removeFromPortfolio(int id) {
		if (id != -1)
			this.sqlHelper.removeItem(id);
	}
}
