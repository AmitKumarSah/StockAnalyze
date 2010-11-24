/**
 * 
 */
package cz.tomas.StockAnalyze.Data.PsePatriaData;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

/**
 * @author tomas
 *
 * contains current stock data, market phase, last update time. this class gets data from PsePatriaXmlParser
 * 
 */
public class PsePatriaDataMarketItem {

	private final String SOURCE_URL = "http://www.patria.cz/dataexport/VistaGadget.ashx?guid=D88B6094-E9C7-11DF-A5A1-05E4DED72085";
	/*
	 * mapping between ticker and name used in xml
	 * */
	private Map<String, String> patriaTickerMapping;
	
	/*
	 * stocks with they ticker as key
	 */
	Map<String, PsePatriaDataItem> stocksData;
	long lastUpdate;
	boolean isClosePhase;

	PsePatriaXmlParser xmlParser;

	public PsePatriaDataMarketItem() {
		this.xmlParser = new PsePatriaXmlParser(SOURCE_URL);
		this.stocksData = new HashMap<String, PsePatriaDataItem>();
		
		this.patriaTickerMapping = new HashMap<String, String>();
		this.patriaTickerMapping.put("PX", "PX");
		this.patriaTickerMapping.put("AAA", "BAAAAA");
		this.patriaTickerMapping.put("CETV", "BAACETV");
		this.patriaTickerMapping.put("ČEZ", "BAACEZ");
		this.patriaTickerMapping.put("ECM", "BAAECM");
		this.patriaTickerMapping.put("ERSTE", "BAAERBAG");
		this.patriaTickerMapping.put("FORTUNA", "BAAFOREG");
		this.patriaTickerMapping.put("KB", "BAAKOMB");
		this.patriaTickerMapping.put("KITD", "BAAKITDG");
		this.patriaTickerMapping.put("NWR", "BAANWR");
		this.patriaTickerMapping.put("ORCO", "BAAORCO");
		this.patriaTickerMapping.put("PEGAS", "BAAPEGAS");
		this.patriaTickerMapping.put("PM", "BAATABAK");
		this.patriaTickerMapping.put("TEL. O2", "BAATELEC");
		this.patriaTickerMapping.put("UNI", "BAAUNIPE");
		this.patriaTickerMapping.put("VIG", "BAAVIG");
	}
	
	private void createMarketData() {
		List<PsePatriaDataItem> items = null;
		try {
			items = this.xmlParser.parse();
		} catch (Exception e) {
			String message = "Failed to process patria data xml! ";
			if (e.getMessage() != null)
				message += e.getMessage();
			Log.d("PsePatriaDataMarket", message);
			e.printStackTrace();
			return;
		}
		
		//this.lastUpdate = Calendar.getInstance().getTimeInMillis();
		this.lastUpdate = this.xmlParser.getDate().getTimeInMillis();
		
		for (PsePatriaDataItem item : items) {
			if (! this.patriaTickerMapping.containsKey(item.getName()))
				continue;
			String ticker = this.patriaTickerMapping.get(item.getName());
			
			this.stocksData.put(ticker, item);
		}
	}
	
	public void update() {
		this.createMarketData();
	}
	
	/**
	 * @return the stocks
	 */
	public Map<String, PsePatriaDataItem> getStocks() {
		if (this.stocksData == null || this.stocksData.size() == 0)
			this.update();
		return stocksData;
	}

	/**
	 * @return the lastUpdate
	 */
	public long getLastUpdate() {
		return lastUpdate;
	}

	/**
	 * @return the isClosePhase
	 */
	public boolean isClosePhase() {
		return isClosePhase;
	}

	public PsePatriaDataItem getStock(String ticker) {
		if (this.stocksData == null || this.stocksData.size() == 0)
			this.update();
		if (! this.stocksData.containsKey(ticker))
			return null;
		else
			return this.stocksData.get(ticker);
	}
}