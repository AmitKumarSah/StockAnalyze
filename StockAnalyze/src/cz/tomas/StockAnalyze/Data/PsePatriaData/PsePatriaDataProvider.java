/*******************************************************************************
 * Copyright (c) 2011 Tomas Vondracek.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Tomas Vondracek
 ******************************************************************************/
/**
 * 
 */
package cz.tomas.StockAnalyze.Data.PsePatriaData;

import java.util.Map;

import cz.tomas.StockAnalyze.utils.Utils;

import android.util.Log;

/**
 * @author tomas
 *
 */
public class PsePatriaDataProvider {

	private PsePatriaDataMarketItem currentMarketData;
	private boolean isClosedInternal = false;
	
	public PsePatriaDataProvider() {
		this.currentMarketData = new PsePatriaDataMarketItem();
		
	}
	
	public PsePatriaDataItem getLastData(String ticker) throws Exception {
		//this.currentMarketData.update();
		PsePatriaDataItem stockDataItem = this.currentMarketData.getStock(ticker);
		
		return stockDataItem;
	}
	
	public long getLastUpdateTime() {
		return this.currentMarketData.getLastUpdate();
	}

	public Map<String, PsePatriaDataItem> getAvailableStockMap() throws Exception {
		Map<String, PsePatriaDataItem> stocks = this.currentMarketData.getStocks();
		
		return stocks;
	}

	/*
	 * download new data, if market is closed, only once the refresh returns true, 
	 * next attempts of refresh on will return false until the market is open again 
	 */
	public boolean refresh() throws Exception {
		this.currentMarketData.update();
		boolean retValue = ! this.currentMarketData.isClosePhase() || ! this.isClosedInternal;
		this.isClosedInternal = this.currentMarketData.isClosePhase();
		
		Log.d(Utils.LOG_TAG, "refresh performed on Patria data provider with result: " + retValue);
		return retValue;
	}
}
