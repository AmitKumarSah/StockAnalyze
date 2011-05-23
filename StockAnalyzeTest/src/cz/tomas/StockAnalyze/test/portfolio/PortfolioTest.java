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
package cz.tomas.StockAnalyze.test.portfolio;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

import cz.tomas.StockAnalyze.Data.Model.PortfolioItem;
import cz.tomas.StockAnalyze.Portfolio.Portfolio;
import android.content.Context;
import android.test.AndroidTestCase;
import android.test.IsolatedContext;
import android.test.mock.MockContentResolver;

/**
 * @author tomas
 *
 */
public class PortfolioTest extends AndroidTestCase {

	private Portfolio portfolio;
	private Context context;
	
	/* (non-Javadoc)
	 * @see android.test.AndroidTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		this.context = new IsolatedContext(new MockContentResolver(), getContext());
		this.portfolio = new Portfolio(this.context);
	}

	/* 
	 * @see android.test.AndroidTestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
		// clear portfolio
		List<PortfolioItem> items = this.portfolio.getPortfolioItems();
		for (PortfolioItem portfolioItem : items) {
			this.portfolio.removeFromPortfolio(portfolioItem.getId());
		}
	}

	public void testAddToPortfolio() throws SQLException {
		String stockId = "moje_stock_id";
		String portfoliName = "default";
		int count = 1000;
		float price = 100.0f;
		String marketId = "muj_market";
		
		PortfolioItem item = new PortfolioItem(stockId, portfoliName, count, price, 
				Calendar.getInstance().getTimeInMillis(), marketId);
		
		this.portfolio.addToPortfolio(item);
		List<PortfolioItem> items = this.portfolio.getPortfolioItems();
		
		assertEquals(1, items.size());
		
		PortfolioItem actualItem = items.get(0);
		
		assertEquals(stockId, actualItem.getStockId());
		assertEquals(portfoliName, actualItem.getPortfolioName());
		assertEquals(count, actualItem.getStockCount());
		assertEquals(price, actualItem.getBuyPrice());
		assertEquals(marketId, actualItem.getMarketId());
	}
	
	public void testPortfolioPositiveGrouping() throws SQLException {
		String stockId = "moje_stock_id";
		String portfoliName = "default";
		int count = 1000;
		float price = 100.0f;
		String marketId = "muj_market";
		
		PortfolioItem item = new PortfolioItem(stockId, portfoliName, count, price, 
				Calendar.getInstance().getTimeInMillis(), marketId);
		
		this.portfolio.addToPortfolio(item);
		this.portfolio.addToPortfolio(item);
		
		List<PortfolioItem> items = this.portfolio.getGroupedPortfolioItems();
		assertEquals(1, items.size());
		
		PortfolioItem actualItem = items.get(0);
		
		assertEquals(count * 2, actualItem.getStockCount());
	}
	
	public void testPortfolioNegativeGrouping() throws SQLException {
		String stockId = "moje_stock_id";
		String portfoliName = "default";
		int count = 1000;
		float price = 100.0f;
		String marketId = "muj_market";
		
		PortfolioItem item1 = new PortfolioItem(stockId, portfoliName, count, price, 
				Calendar.getInstance().getTimeInMillis(), marketId);
		PortfolioItem item2 = new PortfolioItem(stockId, portfoliName, count, -price, 
				Calendar.getInstance().getTimeInMillis(), marketId);
		
		this.portfolio.addToPortfolio(item1);
		this.portfolio.addToPortfolio(item2);
		
		List<PortfolioItem> items = this.portfolio.getGroupedPortfolioItems();
		assertEquals(1, items.size());
		
		PortfolioItem actualItem = items.get(0);
		
		assertEquals(0, actualItem.getStockCount());
	}
	
	public void testPortfolioPriceGrouping() throws SQLException {
		String stockId = "moje_stock_id";
		String portfoliName = "default";
		int count = 1000;
		float price = 100.0f;
		String marketId = "muj_market";
		
		PortfolioItem item1 = new PortfolioItem(stockId, portfoliName, count, price, 
				Calendar.getInstance().getTimeInMillis(), marketId);
		PortfolioItem item2 = new PortfolioItem(stockId, portfoliName, count, 2 * price, 
				Calendar.getInstance().getTimeInMillis(), marketId);
		
		this.portfolio.addToPortfolio(item1);
		this.portfolio.addToPortfolio(item2);
		
		List<PortfolioItem> items = this.portfolio.getGroupedPortfolioItems();
		assertEquals(1, items.size());
		
		PortfolioItem actualItem = items.get(0);
		
		assertEquals(price + price/2, actualItem.getBuyPrice());
		assertEquals(0.0f, actualItem.getSellPrice());
	}
}
