/**
 * 
 */
package cz.tomas.StockAnalyze.Data;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.tomas.StockAnalyze.Data.Interfaces.IObservableDataProvider;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.Data.exceptions.FailedToGetDataException;

/**
 * @author tomas
 *
 */
public interface IStockDataProvider extends IObservableDataProvider {
	DayData getLastData(String ticker) throws FailedToGetDataException;
	DayData getDayData(String ticker, Calendar date) throws IOException, FailedToGetDataException;
	DayData[] getIntraDayData(String ticker, Date date, int minuteInterval);
	List<StockItem> getAvailableStockList() throws FailedToGetDataException;
	String getId();
	String getDescriptiveName();
	boolean refresh();
	DataProviderAdviser getAdviser();
	void enable(boolean enabled);
}
