package cz.tomas.StockAnalyze.test.rest;

import android.test.AndroidTestCase;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.rest.Infrastructure;
import cz.tomas.StockAnalyze.test.Markets;

import java.io.IOException;
import java.util.Map;

/**
 * @author tomas
 */
public class DayDataInfrastructureTest extends AndroidTestCase {

	private final Infrastructure infrastructure;

	public DayDataInfrastructureTest() {
		this.infrastructure = new Infrastructure();
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
