package cz.tomas.StockAnalyze.Data.PseCsvData;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import cz.tomas.StockAnalyze.Data.DayData;
import cz.tomas.StockAnalyze.Data.DownloadService;
import cz.tomas.StockAnalyze.Data.IStockDataProvider;

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
	
	PseCsvParser parser;

	public PseCsvDataProvider() {
		this.parser = new PseCsvParser();
	}

	@Override
	public DayData getLastData(String ticker) throws IOException {
		String remoteFileName = "ak.csv";
		
		Map<String, CsvDataRow> rows = getDataFromRemoteFile(ticker, remoteFileName);
		CsvDataRow row = null;
		if (rows.containsKey(ticker))
			row = rows.get(ticker);
		
		DayData data = new DayData(row);
		return data;
	}
	
	public DayData getDayData(String ticker, Date date) throws IOException {
		String remoteFileName = this.buildRemoteFileName(date);
		
		Map<String, CsvDataRow> rows = getDataFromRemoteFile(ticker, remoteFileName);
		CsvDataRow row = null;
		if (rows.containsKey(ticker))
			row = rows.get(ticker);
		return new DayData(row);
	}

	/**
	 * @param ticker stock ticker
	 * @param remoteFileName remote csv file to download
	 * @return parsed price
	 * @throws IOException
	 */
	private Map<String, CsvDataRow> getDataFromRemoteFile(String ticker, String remoteFileName)
			throws IOException {
		byte[] byteArray = DownloadService.GetInstance().DownloadFromUrl(
				PSE_DATA_ROOT_URL + remoteFileName);
		String data = new String(byteArray);
		//Log.d("PseCsvDataProvider", data);
		
		Map<String, CsvDataRow> rows = this.parser.parse(data);
		CsvDataRow rowData = null;
		
		return rows;
	}

	private String buildRemoteFileName(Date date) {
		Calendar calendar = Calendar.getInstance(new Locale("cs"));
		
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

	public List<String> getAvailableStockList() {
		// TODO Auto-generated method stub
		return null;
	}
}