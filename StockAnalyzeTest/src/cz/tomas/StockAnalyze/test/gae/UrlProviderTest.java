package cz.tomas.StockAnalyze.test.gae;

import cz.tomas.StockAnalyze.Data.GaeData.UrlProvider;
import android.test.AndroidTestCase;

/**
 * tests for {@link UrlProvider} and its capability to
 * compose and provide urls
 * @author tomas
 *
 */
public final class UrlProviderTest extends AndroidTestCase {

	private UrlProvider provider;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.provider = UrlProvider.getInstance(getContext());
	}

	public void testDDataUrl1() {
		String url = this.provider.getUrl("DDATA", "marketCode");
		
		assertNotNull(url);
		assertTrue(url.contains("?marketCode=%s"));
	}
	
	public void testDDataUrl2() {
		String url = this.provider.getUrl("DDATA", "stockId");
		
		assertNotNull(url);
		assertTrue(url.contains("?stockId=%s"));
	}
	
	public void testInvalidUrl() {
		String url = this.provider.getUrl("blabla", "");
		
		assertNull(url);
	}
	
	public void testIDataUrl() {
		String url = this.provider.getUrl("IDATA", "stockId");
		
		assertNotNull(url);
		assertTrue(url.contains("?stockId=%s"));
	}
	
	public void testHDataUrl1() {
		String url = this.provider.getUrl("HDATA", "stockId");
		
		assertNotNull(url);
		assertTrue(url.contains("?stockId=%s"));
	}
	
	public void testHDataUrl2() {
		String url = this.provider.getUrl("HDATA", "stockId", "timePeriod");
		
		assertNotNull(url);
		assertTrue(url.contains("?stockId=%s&timePeriod=%s"));
	}
	
	public void testIndDataUrl1() {
		String url = this.provider.getUrl("INDATA", "indList");
		
		assertNotNull(url);
		assertTrue(url.contains("?indList=%s"));
	}
}
