package cz.tomas.StockAnalyze.Data;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.util.Log;

/**
 * Data provider for Prague Stock Exchange
 * 
 * @author tomas
 * 
 */
public class PseCsvDataProvider implements IStockDataProvider {

	private final String PSE_DATA_ROOT_FTP_URL = "ftp://ftp.pse.cz/Results.ak/";
	private final String PSE_DATA_ROOT_URL = "http://ftp.pse.cz/Results.ak/";

	public PseCsvDataProvider() {
		// TODO Auto-generated constructor stub
	}

	public float getDayData(String ticker, Date date) throws IOException {
		String remoteFileName = this.buildRemoteFileName(date);
		String localFileName = "/sdcard/downloads/" + remoteFileName;
		
		byte[] byteArray = DownloadService.GetInstance().DownloadFromUrl(
				PSE_DATA_ROOT_URL + remoteFileName);
		String data = new String(byteArray);
		//Log.d("PseCsvDataProvider", data);
		//TODO csv parser
		
		return 0;
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
}