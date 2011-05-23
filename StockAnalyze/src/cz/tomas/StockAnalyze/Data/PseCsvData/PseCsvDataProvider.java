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
package cz.tomas.StockAnalyze.Data.PseCsvData;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import cz.tomas.StockAnalyze.Data.DataProviderAdviser;
import cz.tomas.StockAnalyze.Data.IStockDataProvider;
import cz.tomas.StockAnalyze.Data.Interfaces.IStockDataListener;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.utils.DownloadService;

import android.util.Log;

/**
 * Data provider for Prague Stock Exchange
 * 
 * @author tomas
 * 
 */
class PseCsvDataProvider {

	@SuppressWarnings("unused")
	private final String PSE_DATA_ROOT_FTP_URL = "ftp://ftp.pse.cz/Results.ak/";
	private final String PSE_DATA_ROOT_URL = "http://ftp.pse.cz/Results.ak/";
	
	//index px history
	//http://ftp.pse.cz/Info.bas/Cz/PX.csv
	
	private final String REMOTE_LAST_DATA_NAME = "ak.csv";
	
	PseCsvParser parser;

	long lasUpdateTime;
	Market market;
	
	IDownloadListener downloadListener;
	
	/*
	 * first key is a date as "yyyy.MM.dd"
	 * under this key is a map of CsvRows for that day
	 * */
	private static Map<String, Map<String, CsvDataRow>> dataCache = new HashMap<String, Map<String,CsvDataRow>>();
	
	interface IDownloadListener {
		void DownloadStart();
		void DownloadFinished(Map<String, CsvDataRow> data);
	}

	public PseCsvDataProvider() {
		this.parser = new PseCsvParser();
		
		//this.updateTimes = new HashMap<String, Long>();
		this.lasUpdateTime = 0;
		market = new Market("PSE", "XPRA", "CZK", this.getDescriptiveName());
	}

	void setDownloadListener(IDownloadListener listener) {
		this.downloadListener = listener;
	}
	
	public Market getMarket() {
		return this.market;
	}

	/**
	 * try to look in cache for ticker data in selected date
	 * @param cal date to search for
	 * @param ticker ticker name
	 * @return
	 */
	private CsvDataRow checkInCache(String ticker, Calendar cal) {
		CsvDataRow row = null;
		String key = buildCacheKey(cal);
		
		if (dataCache.containsKey(key)) {
//			Log.d("PseCsvDataProvider", "reading data for " + ticker + " from cache");
			Map<String, CsvDataRow> desiredMap = dataCache.get(key);
			if (desiredMap.containsKey(ticker));
				row = desiredMap.get(ticker);
		}
		return row;
	}

	/**
	 * @param ticker stock ticker
	 * @param remoteFileName remote csv file to download
	 * @return parsed price
	 * @throws IOException
	 */
	private Map<String, CsvDataRow> getDataFromRemoteFile(String remoteFileName)
			throws IOException {
		Map<String, CsvDataRow> rows = null;
		if (this.downloadListener != null)
			this.downloadListener.DownloadStart();
		try {
			byte[] byteArray = DownloadService.GetInstance().DownloadFromUrl(
					PSE_DATA_ROOT_URL + remoteFileName, true);
			String data = new String(byteArray, "CP-1250");
			
			rows = this.parser.parse(data);
			
//		if (this.updateTimes.containsKey(remoteFileName))
//			this.updateTimes.remove(remoteFileName);
//		this.updateTimes.put(remoteFileName, Calendar.getInstance().getTimeInMillis());
			this.lasUpdateTime = Calendar.getInstance().getTimeInMillis();
		} finally {
			if (this.downloadListener != null) {
				this.downloadListener.DownloadFinished(rows);
			}
		}

		return rows;
	}

	/*
	 * build a file name according to desired date, this file can be downloaded
	 * */
	private String buildRemoteFileName(Calendar calendar) {		
		String name = null;
		NumberFormat format = DecimalFormat.getInstance();

		if (format instanceof DecimalFormat) {
			DecimalFormat decFormat = (DecimalFormat) format;
			decFormat.applyPattern("00");
			String year = decFormat.format(calendar.get(Calendar.YEAR) - 2000);
			String month = decFormat.format(calendar.get(Calendar.MONTH) + 1);
			
			int dayInMonth = calendar.get(Calendar.DAY_OF_MONTH);
			int dayInWeek = (7 + calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY) % 7 + 1;
			// there are no data for weekends
			if (dayInWeek > 5)
				dayInMonth -= (dayInWeek - 5);
			String day = decFormat.format(dayInMonth);

			name = String.format("AK%s%s%s.csv", year, month, day);
			// if we wanted data from previous year, we would also want to
			// include year folder in path (see structure of bcpp ftp server)
			int desiredYear = calendar.get(Calendar.YEAR);
			if (desiredYear < Calendar.getInstance().get(Calendar.YEAR)) {
				name = desiredYear + "//" + name;
			}
		}

		return name;
	}
	
	/**
	 * Build the key to map, that represents the data cache
	 * @param cal Calendar object with date we want to look for
	 * @return built key
	 */
	private String buildCacheKey(Calendar cal) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.US);
		
		String key = dateFormat.format(cal.getTime());
		return key;
	}

	public CsvDataRow getLastData(String ticker) throws IOException {

		Calendar now = Calendar.getInstance();
		CsvDataRow row = checkInCache(ticker, now);
				
		// we didn't find in cache
		if (row == null) {
			Map<String, CsvDataRow> rows;
			try {
				rows = getDataFromRemoteFile(this.REMOTE_LAST_DATA_NAME);
			} catch (IOException e) {
				// if failed to download last data, try yesterday's data
				rows = getYesterdayRemoteData();
			}
			
			dataCache.put(this.buildCacheKey(now), rows);
			if (rows.containsKey(ticker))
				row = rows.get(ticker);
		}
		
		return row;
	}

	/**
	 * Download yesterday data, uses buildRemoteFileName with yesterday's date 
	 * @return
	 * @throws IOException
	 */
	private Map<String, CsvDataRow> getYesterdayRemoteData() throws IOException {
		Map<String, CsvDataRow> rows;
		Calendar yesterday = Calendar.getInstance();
		yesterday.add(Calendar.DAY_OF_YEAR, -1);
		rows = getDataFromRemoteFile(this.buildRemoteFileName(yesterday));
		return rows;
	}
	
	public CsvDataRow getDayData(String ticker, Calendar date) throws IOException {
		String remoteFileName = this.buildRemoteFileName(date);
		
		Map<String, CsvDataRow> rows = null;
		try {
			rows = getDataFromRemoteFile(remoteFileName);
		} catch (IOException e) {
			rows = getYesterdayRemoteData();
		}
		
		CsvDataRow row = null;
		if (rows.containsKey(ticker))
			row = rows.get(ticker);
		return row;
	}

	public List<StockItem> getAvailableStockList() {
		if (PseCsvDataProvider.dataCache.size() == 0) {
			// if there is nothing in cache, download last data
			Map<String, CsvDataRow> rows = null;
			Calendar cal = Calendar.getInstance();	// will serve as a key to cache
			try {
				rows = getDataFromRemoteFile(this.REMOTE_LAST_DATA_NAME);
			} catch (IOException e) {
				// if failed to download last data, try yesterday's data
				try {
					rows = this.getYesterdayRemoteData();
					cal.add(Calendar.DAY_OF_YEAR, -1);
				} catch (IOException e1) {
					e1.printStackTrace();
					return null;
				}
			}
			PseCsvDataProvider.dataCache.put(this.buildCacheKey(cal), rows);
		}
		List<StockItem> stocks = new ArrayList<StockItem>();
		// we can take any item from cache
		Map <String, CsvDataRow> rows = PseCsvDataProvider.dataCache.values().iterator().next();
		for (CsvDataRow row : rows.values()) {
			StockItem stockItem = new StockItem(row.ticker, row.code, row.name, this.market);
			stocks.add(stockItem);
		}
			
		return stocks;
	}

	public boolean refresh() {
		boolean result = false;	// update performed?
		Calendar now = Calendar.getInstance();
		String cacheKey = buildCacheKey(now);
		
		if (this.lasUpdateTime != 0 &&
				PseCsvDataProvider.dataCache.containsKey(cacheKey)) {
			//long time = this.updateTimes.get(REMOTE_LAST_DATA_NAME);
			long diff = now.getTimeInMillis() - this.lasUpdateTime;
			long diffHours = diff / (60 * 60 * 1000);

			// TODO limit diff time should be taken from settings
			if (diffHours >= 1 ) {
				Log.d("cz.tomas.StockAnalyze.Data.PseCsvData.PseCsvDataProvider", "Clearing data cache for " + cacheKey);
				result = true;
				PseCsvDataProvider.dataCache.remove(cacheKey);			
			}
		}
		else if (this.lasUpdateTime == 0) {
			// we don't know las update time, so update it
			PseCsvDataProvider.dataCache.remove(cacheKey);
			result = true;
		}
		
		return result;
	}
	
	String getDescriptiveName() {
		return "Prague Stock Exchange";
	}
}
