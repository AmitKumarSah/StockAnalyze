/**
 * 
 */
package cz.tomas.StockAnalyze.Data;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author tomas
 *
 */
public interface IStockDataProvider {
	DayData getLastData(String ticker) throws IOException;
	DayData getDayData(String ticker, Date date) throws IOException;
	List<String> getAvailableStockList();
}
