/*******************************************************************************
 * Copyright (c) 2011 Tomas Vondracek.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Tomas Vondracek
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
