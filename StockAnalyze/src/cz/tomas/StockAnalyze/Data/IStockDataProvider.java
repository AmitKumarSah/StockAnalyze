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
