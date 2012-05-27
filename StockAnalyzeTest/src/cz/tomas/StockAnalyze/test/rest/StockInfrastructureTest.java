package cz.tomas.StockAnalyze.test.rest;

import android.test.AndroidTestCase;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.rest.Infrastructure;
import cz.tomas.StockAnalyze.test.Markets;

import java.io.IOException;
import java.util.Collection;

/**
 * @author tomas
 */
public class StockInfrastructureTest extends AndroidTestCase {

	private final Infrastructure infrastructure;

	public StockInfrastructureTest() {
		this.infrastructure = new Infrastructure();
	}

	public void testStock() throws IOException {
		StockItem stock = this.infrastructure.getStock("AAPL");
		assertNotNull(stock);
		assertEquals(stock.getTicker(), "AAPL");
		assertEquals(stock.getName(), "Apple Inc.");
	}

	public void testStockListCZ() throws IOException {
		testStockList(Markets.CZ);
	}

	public void testStockListDE() throws IOException {
		testStockList(Markets.DE);
	}

	public void testStockListPL() throws IOException {
		testStockList(Markets.PL);
	}

	private void testStockList(Market market) throws IOException {
		Collection<StockItem> stocks = this.infrastructure.getStockList(market);

		assertNotNull(stocks);
		assertTrue(stocks.size() > 0);

		for (StockItem stock : stocks) {
			assertNotNull(stock);
			assertNotNull(stock.getId());
			assertNotNull(stock.getMarket());
			assertEquals(stock.getMarket().getId(), market.getId());
			assertEquals(stock.getMarket().getCurrencyCode(), market.getCurrencyCode());
			assertNotNull(stock.getName());
			assertNotNull(stock.getTicker());
		}
	}
}
