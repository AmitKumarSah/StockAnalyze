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
package cz.tomas.StockAnalyze.StockList;

import java.io.IOException;
import java.util.Comparator;

import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;

/**
 * @author tomas
 *
 */
public class StockComparator implements Comparator<StockItem> {
	
	StockCompareTypes type;
	DataManager dataManager;
	
	public StockComparator(StockCompareTypes type, DataManager dataManager) {
		this.type = type;
		this.dataManager = dataManager;
	}
	
	@Override
	public int compare(StockItem stock1, StockItem stock2) {
		int result = 0;
		DayData data1 = null;
		DayData data2 = null;
		if (this.type != StockCompareTypes.Name 
			&& this.type != StockCompareTypes.Ticker ) {
			try {
				data1 = this.dataManager.getLastOfflineValue(stock1.getId());
				data2 = this.dataManager.getLastOfflineValue(stock2.getId());
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		}
		switch (this.type) {
		case Change:
			result = (data1.getChange() < data2.getChange() ? 1 : -1);
			break;
		case Name:
			result = stock1.getName().compareToIgnoreCase(stock2.getName());
			break;
		case Price:
			result = (data1.getPrice() < data2.getPrice() ? 1 : -1);
			break;
		case Ticker:
			result = stock1.getTicker().compareToIgnoreCase(stock2.getTicker());
			break;
		case Volume:
			result = (data1.getVolume() < data2.getVolume() ? 1 : -1);
			break;
		case TradedPieces:
			result = (data1.getTradedPieaces() < data2.getTradedPieaces() ? 1 : -1);
			break;
		default:
			break;
		}
		return result;
	}
	
}
