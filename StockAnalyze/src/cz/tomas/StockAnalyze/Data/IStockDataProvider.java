/**
 * 
 */
package cz.tomas.StockAnalyze.Data;

import java.io.IOException;
import java.util.Date;

/**
 * @author tomas
 *
 */
public interface IStockDataProvider {
	float getDayData(String ticker, Date date) throws IOException;
}
