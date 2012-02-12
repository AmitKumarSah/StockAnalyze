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
package cz.tomas.StockAnalyze.test.portfolio;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.IsolatedContext;
import android.test.mock.MockContentResolver;
import cz.tomas.StockAnalyze.Data.Model.PortfolioItem;
import cz.tomas.StockAnalyze.Portfolio.Portfolio;
import cz.tomas.StockAnalyze.utils.Markets;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

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
		
		PortfolioItem item = new PortfolioItem(stockId, portfoliName, count, price, 0,  
				Calendar.getInstance().getTimeInMillis(), 0, marketId);
		
		this.portfolio.addToPortfolio(item);
		List<PortfolioItem> items = this.portfolio.getPortfolioItems();
		
		assertEquals(1, items.size());
		
		PortfolioItem actualItem = items.get(0);
		
		assertEquals(stockId, actualItem.getStockId());
		assertEquals(portfoliName, actualItem.getPortfolioName());
		assertEquals(count, actualItem.getBoughtStockCount());
		assertEquals(price, actualItem.getBuyPrice());
		assertEquals(marketId, actualItem.getMarketId());
	}
	
	public void testPortfolioPositiveGrouping() throws SQLException {
		String stockId = "moje_stock_id";
		String portfoliName = "default";
		int count = 1000;
		float price = 100.0f;
		String marketId = Markets.GLOBAL.getId();
		
		PortfolioItem item = new PortfolioItem(stockId, portfoliName, count, price, 0,
				Calendar.getInstance().getTimeInMillis(), 0, marketId);
		
		this.portfolio.addToPortfolio(item);
		this.portfolio.addToPortfolio(item);
		
		List<PortfolioItem> items = this.portfolio.getGroupedPortfolioItems(Markets.GLOBAL);
		assertEquals(1, items.size());
		
		PortfolioItem actualItem = items.get(0);
		
		assertEquals(count * 2, actualItem.getCurrentStockCount());
	}
	
	public void testPortfolioNegativeGrouping() throws SQLException {
		String stockId = "moje_stock_id";
		String portfoliName = "default";
		int count = 1000;
		float price = 100.0f;
		String marketId = Markets.GLOBAL.getId();
		
		PortfolioItem item1 = new PortfolioItem(stockId, portfoliName, count, price, 0,
				Calendar.getInstance().getTimeInMillis(), 0, marketId);
		PortfolioItem item2 = new PortfolioItem(stockId, portfoliName, -count, 0, price,  
				Calendar.getInstance().getTimeInMillis(), 0, marketId);
		
		this.portfolio.addToPortfolio(item1);
		this.portfolio.addToPortfolio(item2);
		
		List<PortfolioItem> items = this.portfolio.getGroupedPortfolioItems(Markets.GLOBAL);
		assertEquals(1, items.size());
		
		PortfolioItem actualItem = items.get(0);
		
		assertEquals(0, actualItem.getCurrentStockCount());
	}
	
	public void testPortfolioPriceGrouping() throws SQLException {
		String stockId = "moje_stock_id";
		String portfoliName = "default";
		int count = 1000;
		float price = 100.0f;
		String marketId = Markets.GLOBAL.getId();
		
		PortfolioItem item1 = new PortfolioItem(stockId, portfoliName, count, price, 0, 
				Calendar.getInstance().getTimeInMillis(), 0, marketId);
		PortfolioItem item2 = new PortfolioItem(stockId, portfoliName, count, 2 * price, 0, 
				Calendar.getInstance().getTimeInMillis(), 0, marketId);
		
		this.portfolio.addToPortfolio(item1);
		this.portfolio.addToPortfolio(item2);
		
		List<PortfolioItem> items = this.portfolio.getGroupedPortfolioItems(Markets.GLOBAL);
		assertEquals(1, items.size());
		
		PortfolioItem actualItem = items.get(0);
		
		assertEquals(price + price/2, actualItem.getBuyPrice());
		assertEquals(0.0f, actualItem.getSellPrice());
		assertEquals(count * 2, actualItem.getCurrentStockCount());
	}
}
