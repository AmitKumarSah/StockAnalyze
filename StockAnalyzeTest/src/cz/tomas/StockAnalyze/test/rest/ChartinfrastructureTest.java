package cz.tomas.StockAnalyze.test.rest;

import android.test.AndroidTestCase;
import android.test.IsolatedContext;
import android.test.mock.MockContentResolver;
import cz.tomas.StockAnalyze.rest.Infrastructure;

import java.io.IOException;
import java.util.Map;

/**
 * @author tomas
 */
public class ChartInfrastructureTest extends AndroidTestCase {

	private Infrastructure infrastructure;

	public ChartInfrastructureTest() {
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (this.infrastructure == null) {
			this.infrastructure = new Infrastructure(new IsolatedContext(new MockContentResolver(), getContext()));
		}
	}

	public void testIntradayCZ() throws IOException {
		testChart("BAACEZ", "1D");
	}

	public void testWeekCZ() throws IOException {
		testChart("BAACEZ", "1W");
	}

	public void testIntradayDE() throws IOException {
		testChart("DBK:GR", "1W");
	}

	private void testChart(String ticker, String timePeriod) throws IOException {
		Map<Long, Float> data = this.infrastructure.getChartData(ticker, timePeriod);

		assertNotNull(data);
		assertTrue(data.size() > 0);

		for (Map.Entry<Long, Float> entry : data.entrySet()) {
			assertTrue(entry.getKey() > 0);
			assertTrue(entry.getValue() >= 0);
		}
	}
}
