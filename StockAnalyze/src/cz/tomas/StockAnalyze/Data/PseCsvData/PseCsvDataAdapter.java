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
package cz.tomas.StockAnalyze.Data.PseCsvData;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.util.Log;

import cz.tomas.StockAnalyze.Data.DataProviderAdviser;
import cz.tomas.StockAnalyze.Data.IStockDataProvider;
import cz.tomas.StockAnalyze.Data.Interfaces.IStockDataListener;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.Data.PseCsvData.PseCsvDataProvider.IDownloadListener;
import cz.tomas.StockAnalyze.Data.exceptions.FailedToGetDataException;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * Adapter for pse provider that can be registered in DataManager.
 * It adapts the data from pse provider to DataManager model
 * 
 * @author tomas
 *
 */
public class PseCsvDataAdapter implements IStockDataProvider {

	private PseCsvDataProvider provider;
	private Set<IStockDataListener> listeners;

	public PseCsvDataAdapter() {
		this.provider = new PseCsvDataProvider();
		this.listeners = new HashSet<IStockDataListener>();
		
		// adapt events from data provider to IStockDataListener events
		this.provider.setDownloadListener(new IDownloadListener() {
			
			@Override
			public void DownloadStart() {
				for (IStockDataListener listener : listeners) {
					listener.OnStockDataUpdateBegin(PseCsvDataAdapter.this);
				}
			}
			
			@Override
			public void DownloadFinished(Map<String, CsvDataRow> rows) {
				Map<StockItem,DayData> dataMap = new HashMap<StockItem, DayData>();
				for (CsvDataRow row : rows.values()) {
					StockItem stockItem = new StockItem(row.ticker, row.code, row.name, provider.getMarket());
					DayData data = createDayData(row);
					dataMap.put(stockItem, data);
				}
				for (IStockDataListener listener : listeners) {
					listener.OnStockDataUpdated(PseCsvDataAdapter.this, dataMap);
				}
			}
		});
	}

	/**
	 * register listener for updates
	 * @see cz.tomas.StockAnalyze.Data.Interfaces.IObservableDataProvider#addListener(cz.tomas.StockAnalyze.Data.Interfaces.IStockDataListener)
	 */
	@Override	
	public void addListener(IStockDataListener listener) {
		this.listeners.add(listener);
	}

	/**
	 * get last known available day data, that is current data
	 * @see cz.tomas.StockAnalyze.Data.IStockDataProvider#getLastData(java.lang.String)
	 */
	@Override
	public DayData getLastData(String ticker) throws FailedToGetDataException {
		CsvDataRow row;
		try {
			row = this.provider.getLastData(ticker);
		} catch (IOException e) {
			throw new FailedToGetDataException(e);
		}
		return this.createDayData(row);
	}

	/**
	 * get day data for particular stock and date
	 * @see cz.tomas.StockAnalyze.Data.IStockDataProvider#getDayData(java.lang.String, java.util.Calendar)
	 */
	@Override
	public DayData getDayData(String ticker, Calendar date) throws IOException {
		CsvDataRow row = this.provider.getDayData(ticker, date);
		return this.createDayData(row);
	}

	/**
	 * get all stocks that this provider supports
	 * @see cz.tomas.StockAnalyze.Data.IStockDataProvider#getAvailableStockList()
	 */
	@Override
	public List<StockItem> getAvailableStockList() {
		return this.provider.getAvailableStockList();
	}


	@Override
	public String getId() {
		return "PSE";
	}

	/**
	 * this provider doesn't support intraday data
	 * @see cz.tomas.StockAnalyze.Data.IStockDataProvider#getIntraDayData(java.lang.String, java.util.Date, int)
	 */
	@Override
	public DayData[] getIntraDayData(String ticker, Date date, int minuteInterval) {
		return null;
	}

	@Override
	public String getDescriptiveName() {
		return this.provider.getDescriptiveName();
	}

	/**
	 * refresh provider - ask remote server for data
	 * @see cz.tomas.StockAnalyze.Data.IStockDataProvider#refresh()
	 */
	@Override
	public boolean refresh() {
		return this.provider.refresh();
	}


	@Override
	public DataProviderAdviser getAdviser() {
		DataProviderAdviser adviser = new DataProviderAdviser(false, true, true, this.provider.getMarket());
		return adviser;
	}

	/** 
	 * not yet implemented
	 * @see cz.tomas.StockAnalyze.Data.IStockDataProvider#enable(boolean)
	 */
	@Override
	public void enable(boolean enabled) {
	}
	
	/**
	 * convert csv data row to DayData
	 */
	private DayData createDayData(CsvDataRow dataRow) {
		float price, change, volume, yearMinimum, yearMaximum;
		Date date;
		
		if (dataRow == null) {
			throw new NullPointerException("dataRow is null");
		}
		try {
			price = Float.parseFloat(dataRow.getClosePrice());
		} catch (Exception e) {
			price = -1;
			Log.e(Utils.LOG_TAG, "parse error", e);
		}
		try {
			change = Float.parseFloat(dataRow.getChange());
		} catch (Exception e) {
			change = 0;
			Log.e(Utils.LOG_TAG, "parse error", e);
		}
		try {
			date = new Date(Date.parse(dataRow.getDate()));
		} catch (Exception e) {
			date = Calendar.getInstance().getTime();
			Log.e(Utils.LOG_TAG, "parse error", e);
		}
		try {
			volume = Float.parseFloat(dataRow.getDayVolume());
		} catch (Throwable e) {
			volume = -1;
			Log.e(Utils.LOG_TAG, "parse error", e);
		}
		try {
			yearMaximum = Float.parseFloat(dataRow.getYearMax());
		} catch (NumberFormatException e) {
			yearMaximum = -1;
			Log.e(Utils.LOG_TAG, "parse error", e);
		}
		try {
			yearMinimum = Float.parseFloat(dataRow.getYearMin());
		} catch (NumberFormatException e) {
			yearMinimum = -1;
			Log.e(Utils.LOG_TAG, "parse error", e);
		}
		return new DayData(price, change, date, volume, yearMaximum, yearMinimum, Utils.createDateOnlyCalendar(date).getTimeInMillis());
	}

}
