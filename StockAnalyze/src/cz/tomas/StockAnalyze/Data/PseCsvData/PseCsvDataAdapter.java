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
 * @author tomas
 *
 */
public class PseCsvDataAdapter implements IStockDataProvider {

	private PseCsvDataProvider provider;
	Set<IStockDataListener> listeners;

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

	/* (non-Javadoc)
	 * @see cz.tomas.StockAnalyze.Data.Interfaces.IObservableDataProvider#addListener(cz.tomas.StockAnalyze.Data.Interfaces.IStockDataListener)
	 */
	@Override	
	public void addListener(IStockDataListener listener) {
		this.listeners.add(listener);
	}

	/* (non-Javadoc)
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

	/* (non-Javadoc)
	 * @see cz.tomas.StockAnalyze.Data.IStockDataProvider#getDayData(java.lang.String, java.util.Calendar)
	 */
	@Override
	public DayData getDayData(String ticker, Calendar date) throws IOException {
		CsvDataRow row = this.provider.getDayData(ticker, date);
		return this.createDayData(row);
	}

	/* (non-Javadoc)
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

	/*
	 * doesn't support
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

	/* (non-Javadoc)
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

	/* (non-Javadoc)
	 * @see cz.tomas.StockAnalyze.Data.IStockDataProvider#enable(boolean)
	 */
	@Override
	public void enable(boolean enabled) {
	}
	
	/*
	 * convert csv data row to DayData
	 */
	private DayData createDayData(CsvDataRow dataRow) {
		float price, change, volume, yearMinimum, yearMaximum;
		Date date;
		int tradedPieces;
		
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
		} catch (Exception e) {
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
		try {
			tradedPieces = (int) Float.parseFloat(dataRow.getTradedPieces());
		} catch (NumberFormatException e) {
			tradedPieces = -1;
			Log.e(Utils.LOG_TAG, "parse error", e);
		}
		return new DayData(price, change, date, volume, yearMaximum, yearMinimum, Utils.createDateOnlyCalendar(date).getTimeInMillis());
	}

}
