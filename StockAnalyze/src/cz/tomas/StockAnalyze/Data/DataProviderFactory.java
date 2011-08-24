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
package cz.tomas.StockAnalyze.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;
import cz.tomas.StockAnalyze.Data.GaeData.GaeDataAdapter;
import cz.tomas.StockAnalyze.Data.Model.Market;

/**
 * Factory for data providers
 * You can get data provider by your wish here:
 * by stock, by market, historical vs realtime.
 * 
 * @author tomas
 */
public class DataProviderFactory {

	private static final Map<String, IStockDataProvider> providers = new HashMap<String, IStockDataProvider>();
	
	public static void registerDataProvider(IStockDataProvider provider) {
		providers.put(provider.getId(), provider);
	}
	
	/**
	 * get data provider that knows the stock based on ticker,
	 * if no provider knows the ticker, null is returned
	 */
	public static IStockDataProvider getDataProvider(String ticker) {
		if (ticker.toUpperCase().startsWith("BAA") || 
				ticker.toUpperCase().equals("PX")) {
			return providers.get(GaeDataAdapter.ID);
		}
		
		return null;
	}
	
	/**
	 * get default DataProvider
	 */
	public static IStockDataProvider getDataProvider() {
		return providers.get(GaeDataAdapter.ID);
	}
	
	/**
	 * get real time data provider for market specified
	 * if no provider fits the condition, null is returned
	 */
	public static IStockDataProvider getDataProvider(Market market) {
		if (market == null)
			return getDataProvider();
		
		for (Entry<String, IStockDataProvider> provider : providers.entrySet()) {
			DataProviderAdviser providerAdviser =  provider.getValue().getAdviser();
			
			if (providerAdviser.getMarkets().contains(market))
				return provider.getValue();
		}
		
		return null;
	}
	
	/**
	 * get data provider based on adviser,
	 * if no provider fits the condition, null is returned
	 */
	public static IStockDataProvider getDataProvider(DataProviderAdviser adviser) {
		
		for (Entry<String, IStockDataProvider> provider : providers.entrySet()) {
			DataProviderAdviser providerAdviser =  provider.getValue().getAdviser();
			
			if (providerAdviser.equals(adviser))
				return provider.getValue();
		}
		
		return null;
	}
	
	/**
	 * get real time data provider for market specified
	 * if no provider fits the condition, null is returned
	 */
	public static IStockDataProvider getRealTimeDataProvider(Market market) {
		
		for (Entry<String, IStockDataProvider> provider : providers.entrySet()) {
			DataProviderAdviser providerAdviser =  provider.getValue().getAdviser();
			
			if (providerAdviser.isRealTime() && providerAdviser.getMarkets().contains(market))
				return provider.getValue();
		}
		
		return null;
	}
	
	/**
	 * get real time data provider for market specified
	 * if no provider fits the condition, null is returned
	 */
	public static IStockDataProvider getHistoricalDataProvider(Market market) {
		
		for (Entry<String, IStockDataProvider> provider : providers.entrySet()) {
			DataProviderAdviser providerAdviser =  provider.getValue().getAdviser();
			
			List<Market> markets = providerAdviser.getMarkets();
			if (providerAdviser.supportHistorical() && markets.contains(market))
				return provider.getValue();
		}
		
		return null;
	}
	
	/** 
	 * refresh all registered data providers
	 * @returns true if something was updated
	 */
	public static boolean refreshAll() throws Exception {
		boolean result = false;
		try {
			for(IStockDataProvider p : providers.values()) {
				result |= p.refresh();
			}
		} catch (Exception e) {
			Log.d("DataProviderFactory", "Failed to refresh data! " + e.getMessage());
			throw e;
		}
		return result;
	}
}
