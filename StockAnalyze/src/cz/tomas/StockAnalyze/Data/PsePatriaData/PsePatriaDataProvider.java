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
