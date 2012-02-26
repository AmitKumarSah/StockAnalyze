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

import cz.tomas.StockAnalyze.Data.Interfaces.IObservableDataProvider;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.Data.exceptions.FailedToGetDataException;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author tomas
 *
 */
public interface IStockDataProvider extends IObservableDataProvider {
	/**
	 * get last available data for stock
	 * @param ticker
	 * @return
	 * @throws FailedToGetDataException
	 */
	DayData getLastData(String ticker) throws FailedToGetDataException;
	/**
	 * get data for stock in given date
	 * @param ticker
	 * @param date
	 * @return
	 * @throws IOException
	 * @throws FailedToGetDataException
	 */
	DayData getDayData(String ticker, Calendar date) throws IOException, FailedToGetDataException;
	
	/**
	 * get array of historical data
	 * 
	 * @param timePeriod see {@link DataManager} constants for valid time periods
	 * @param timeSet an array to get filled by time data
	 * @return
	 */
	Map<Long, Float> getHistoricalPriceSet(String ticker, int timePeriod);
	Map<Long, Float> getIntraDayData(String ticker, Date date);
	List<StockItem> getAvailableStockList(Market market) throws FailedToGetDataException;
	String getId();
	String getDescriptiveName();
	boolean refresh(Market market);
	DataProviderAdviser getAdviser();
	void enable(boolean enabled);
}
