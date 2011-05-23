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
