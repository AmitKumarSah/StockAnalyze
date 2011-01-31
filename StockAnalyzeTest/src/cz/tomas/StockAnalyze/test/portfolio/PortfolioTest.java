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
	
}
