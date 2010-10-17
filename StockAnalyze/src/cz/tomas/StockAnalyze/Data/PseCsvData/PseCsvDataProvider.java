package cz.tomas.StockAnalyze.Data.PseCsvData;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cz.tomas.StockAnalyze.Data.DownloadService;
import cz.tomas.StockAnalyze.Data.IStockDataProvider;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;

import android.util.Log;

/**
 * Data provider for Prague Stock Exchange
 * 
 * @author tomas
 * 
 */
public class PseCsvDataProvider implements IStockDataProvider {

	@SuppressWarnings("unused")
	private final String PSE_DATA_ROOT_FTP_URL = "ftp://ftp.pse.cz/Results.ak/";
	private final String PSE_DATA_ROOT_URL = "http://ftp.pse.cz/Results.ak/";
	
	private final String REMOTE_LAST_DATA_NAME = "ak.csv";
	
	PseCsvParser parser;
	
	/*
	 * first key is a date as "yyyy.MM.dd"
	 * under this key is a map of CsvRows for that day
	 * */
	private static Map<String, Map<String, CsvDataRow>> dataCache = new HashMap<String, Map<String,CsvDataRow>>();
	

	public PseCsvDataProvider() {
		this.parser = new PseCsvParser();
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
			Log.d("PseCsvDataProvider", "reading data for " + ticker + " from cache");
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
		byte[] byteArray = DownloadService.GetInstance().DownloadFromUrl(
				PSE_DATA_ROOT_URL + remoteFileName);
		String data = new String(byteArray, "CP-1250");
		
		Map<String, CsvDataRow> rows = this.parser.parse(data);
		CsvDataRow rowData = null;
		
		return rows;
	}

	/*
	 * build a file name according to desired date, this file can be downloaded
	 * */
	private String buildRemoteFileName(Calendar calendar) {
		//Calendar calendar = Calendar.getInstance(new Locale("cs"));
		
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

	@Override
	public DayData getLastData(String ticker) throws IOException {

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
			
			assert !dataCache.containsKey(buildCacheKey(now));
			dataCache.put(this.buildCacheKey(now), rows);
			if (rows.containsKey(ticker))
				row = rows.get(ticker);
		}
		
		DayData data = new DayData(row);
		return data;
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
	
	@Override
	public DayData getDayData(String ticker, Calendar date) throws IOException {
		String remoteFileName = this.buildRemoteFileName(date);
		
		Map<String, CsvDataRow> rows = getDataFromRemoteFile(remoteFileName);
		CsvDataRow row = null;
		if (rows.containsKey(ticker))
			row = rows.get(ticker);
		return new DayData(row);
	}

	@Override
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
			StockItem stockItem = new StockItem(row.ticker, row.code, row.name, row.market);
			stocks.add(stockItem);
		}
			
		return stocks;
	}

	@Override
	public String getId() {
		return "PSE";
	}

	@Override
	public DayData[] getIntraDayData(String ticker, Date date, int minuteInterval) {
		return null;
	}

	@Override
	public String getDescriptiveName() {
		return "Prague Stock Exchange";
	}
}