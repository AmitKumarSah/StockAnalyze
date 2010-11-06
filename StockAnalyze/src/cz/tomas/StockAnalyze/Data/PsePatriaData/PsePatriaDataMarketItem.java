/**
 * 
 */
package cz.tomas.StockAnalyze.Data.PsePatriaData;

import java.util.List;
import java.util.Map;

/**
 * @author tomas
 *
 */
public class PsePatriaDataMarketItem {

	private final String SOURCE_URL = "";
	
	Map<String, PsePatriaDataItem> stocks;
	long lastUpdate;
	boolean isClosePhase;
	
	PsePatriaXmlParser xmlParser;

	public PsePatriaDataMarketItem() {
		this.xmlParser = new PsePatriaXmlParser(SOURCE_URL);
		
		createMarketData();
	}
	
	private void createMarketData() {
		List<PsePatriaDataItem> items = this.xmlParser.parse();
	}
}
