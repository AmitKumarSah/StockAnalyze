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
				data1 = this.dataManager.getLastValue(stock1);
				data2 = this.dataManager.getLastValue(stock2);
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