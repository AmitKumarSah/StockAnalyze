package cz.tomas.StockAnalyze.test.rest;

import android.test.AndroidTestCase;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.rest.Infrastructure;
import cz.tomas.StockAnalyze.test.Markets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author tomas
 */
public class DayDataInfrastructureTest extends AndroidTestCase {

	private final Infrastructure infrastructure;

	public DayDataInfrastructureTest() {
		this.infrastructure = new Infrastructure();
	}

	public void testDayDataByStocks() {
		StockItem item1 = new StockItem("AAPL", "AAPL", "Apple Inc.", Markets.US_NASDAQ);
		StockItem item2 = new StockItem("MSFT", "MSFT", "Microsoft Inc.", Markets.US_NASDAQ);
		List<StockItem> stocks = new ArrayList<StockItem>(2);
		stocks.add(item1);
		stocks.add(item2);

		Map<String, DayData> dataSet = this.infrastructure.getDataSet(stocks);
		assertNotNull(dataSet);
		assertEquals(dataSet.size(), 2);
		assertNotNull(dataSet.values().toArray()[0]);
		assertNotNull(dataSet.values().toArray()[1]);
	}

	public void testDayDataCZ() throws IOException {
		testDayData(Markets.CZ);
	}

	public void testStockListDE() throws IOException {
		testDayData(Markets.DE);
	}

	public void testStockListPL() throws IOException {
		testDayData(Markets.PL);
	}

	private void testDayData(Market market) throws IOException {
		Map<String, DayData> dataSet = this.infrastructure.getDataSet(market);

		assertNotNull(dataSet);
		assertTrue(dataSet.size() > 0);

		for (Map.Entry<String, DayData> entry : dataSet.entrySet()) {
			assertNotNull(entry.getKey());
			final DayData data = entry.getValue();
			assertNotNull(data);
			assertTrue(data.getDate() > 0);
			assertTrue(data.getLastUpdate() > 0);
			assertTrue(data.getPrice() >= 0);
		}
	}
}
