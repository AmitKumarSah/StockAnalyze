/**
 * 
 */
package cz.tomas.StockAnalyze.Data;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;

/**
 * @author tomas
 *
 */
public interface IStockDataProvider {
	DayData getLastData(String ticker) throws IOException;
	DayData getDayData(String ticker, Calendar date) throws IOException;
	DayData[] getIntraDayData(String ticker, Date date, int minuteInterval);
	List<StockItem> getAvailableStockList();
	String getId();
	String getDescriptiveName();
}
