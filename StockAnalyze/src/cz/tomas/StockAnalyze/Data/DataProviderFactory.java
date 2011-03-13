/**
 * 
 */
package cz.tomas.StockAnalyze.Data;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import cz.tomas.StockAnalyze.Data.Model.Market;

import android.util.Log;

/**
 * @author tomas
 * factory for data providers
 */
public class DataProviderFactory {

	private static final Map<String, IStockDataProvider> providers = new HashMap<String, IStockDataProvider>();
	
	public static void registerDataProvider(IStockDataProvider provider) {
		providers.put(provider.getId(), provider);
	}
	
	/*
	 * get data provider that knows the stock based on ticker,
	 * if no provider knows the ticker, null is returned
	 */
	public static IStockDataProvider getDataProvider(String ticker) {
		if (ticker.toUpperCase().startsWith("BAA") || 
				ticker.toUpperCase().equals("PX")) {
			return providers.get("PSE_PATRIA");
		}
		
		return null;
	}
	
	/*
	 * get default DataProvider
	 */
	public static IStockDataProvider getDataProvider() {
		return providers.get("PSE_PATRIA");
	}
	
	/*
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
	
	/*
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
	
	/*
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
	
	/*
	 * get real time data provider for market specified
	 * if no provider fits the condition, null is returned
	 */
	public static IStockDataProvider getHistoricalDataProvider(Market market) {
		
		for (Entry<String, IStockDataProvider> provider : providers.entrySet()) {
			DataProviderAdviser providerAdviser =  provider.getValue().getAdviser();
			
			if (! providerAdviser.isRealTime() && providerAdviser.getMarkets().contains(market))
				return provider.getValue();
		}
		
		return null;
	}
	
	/* 
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
