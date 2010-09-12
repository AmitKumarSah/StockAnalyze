/**
 * 
 */
package cz.tomas.StockAnalyze.Data;

import java.io.IOException;
import java.util.Date;

import android.content.Context;

/**
 * @author tomas
 *
 */
public class DataManager {
	
	Context context;
	public DataManager(Context context) {
		this.context = context;
	}

	public float getLastValue(String ticker) throws IOException, NullPointerException {
		float val = -1;
		try {
			val = this.getDataProvider(ticker).getDayData(ticker, new Date());
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw e;
		}
		return val;
	}	
	
	private IStockDataProvider getDataProvider(String ticker) {
		IStockDataProvider dataProvider = null;
		if (ticker.startsWith("BAA")) {
			dataProvider = new PseCsvDataProvider();
		}
		
		return dataProvider;
	}
}
