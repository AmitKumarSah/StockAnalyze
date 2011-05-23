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
package cz.tomas.StockAnalyze.Data.PsePatriaData;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.tomas.StockAnalyze.utils.Utils;

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
	
	private void createMarketData() throws Exception {
		List<PsePatriaDataItem> items = null;
		try {
			// download and parse xml file
			items = this.xmlParser.parse();
		} catch (Exception e) {
			String message = "Failed to process patria data xml! ";
			if (e.getMessage() != null)
				message += e.getMessage();
			Log.d(Utils.LOG_TAG, message);
			e.printStackTrace();
			throw e;
		}
		
		this.lastUpdate = this.xmlParser.getDate().getTimeInMillis();
		this.isClosePhase = this.xmlParser.isClosePhase();
		
		for (PsePatriaDataItem item : items) {
			String name = item.getName();
			if (name == null || ! this.patriaTickerMapping.containsKey(name.toUpperCase())) {
				Log.d(Utils.LOG_TAG, "Failed to find info about stockItem: " + ( item != null ? item.toString() : "NULL"));
				continue;
			}
			String ticker = this.patriaTickerMapping.get(name.toUpperCase());
			
			if (ticker != null)
				this.stocksData.put(ticker, item);
			else
				Log.d(Utils.LOG_TAG, "Failed to find ticker info for stockItem: " + ( item != null ? item.toString() : "NULL"));
		}
	}
	
	public void update() throws Exception {
		this.createMarketData();
	}
	
	/**
	 * @return the stocks
	 * @throws Exception 
	 */
	public Map<String, PsePatriaDataItem> getStocks() throws Exception {
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

	public PsePatriaDataItem getStock(String ticker) throws Exception {
		if (this.stocksData == null || this.stocksData.size() == 0)
			this.update();
		if (! this.stocksData.containsKey(ticker))
			return null;
		else
			return this.stocksData.get(ticker);
	}
}
