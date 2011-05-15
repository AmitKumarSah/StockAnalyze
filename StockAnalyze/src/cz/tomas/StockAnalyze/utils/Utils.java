/**
 * 
 */
package cz.tomas.StockAnalyze.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * helper class with various helper methods string const names
 * @author tomas
 *
 */
public class Utils {
	
	public static final String LOG_TAG = "StockAnalyze";
	public static final String PREF_NAME = "StockAnalyzePreferences";
	public static final String PREF_UPDATE_NOTIF = "prefUpdateNotif";
	public static final String PREF_PERMANENT_NOTIF = "prefPermanentNotif";
	public static final String PREF_ENABLE_BACKGROUND_UPDATE = "prefEnableBackgroundUpdate";
	public static final String PREF_INTERVAL_BACKGROUND_UPDATE = "prefIntervalBackgroundUpdate";
	public static final String PREF_PORTFOLIO_INCLUDE_FEE = "prefPortfolioIncludeFee";
	public static final String PREF_LAST_UPDATE_TIME = "prefLastUpdateTime";
	
	public static final TimeZone PRAGUE_TIME_ZONE = TimeZone.getTimeZone("Europe/Prague");
	
	private static ConnectivityManager connectivityManager;

//	private final static String HOLIDAYS = {
//	        "Den obnovy samostatneho ceskeho statu" => "01.01.",
//	        "Svatek prace" => "01.05.",
//	        "Den osvobozeni" => "08.05.",
//	        "Den slovanskych verozvestu Cyrila a Metodeje" => "05.07.",
//	        "Den upaleni mistra Jana Husa" => "06.07.",
//	        "Den ceske statnosti" => "28.09.",
//	        "Den vzniku samostatneho ceskoslovenskeho statu" => "28.10.",
//	        "Den boje za svobodu a demokracii" => "17.11.",
//	        "Stedry den" => "24.12.",
//	        "1. svatek vanocni" => "25.12.",
//	        "2. svatek vanocni" => "26.12."
//	        };
	
	/**
	 * construct calendar that contains only date info from general Calendar object
	 */
	public static Calendar createDateOnlyCalendar(Calendar calendar) {
		Calendar noTimeCal = Calendar.getInstance(TimeZone.getDefault());
		noTimeCal.set(calendar.get(Calendar.YEAR), 
					calendar.get(Calendar.MONTH),
					calendar.get(Calendar.DAY_OF_MONTH), 
					0, 0, 0);
		noTimeCal.set(Calendar.MILLISECOND, 0);
//		noTimeCal.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
//		noTimeCal.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
//		noTimeCal.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
		
		return noTimeCal;
	}
	
	/**
	 * construct calendar that contains only date info from general Calendar object
	 */
	public static Calendar createDateOnlyCalendar(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date.getTime());
		
		return createDateOnlyCalendar(calendar);
	}
	
	/**
	 * get nearest previous day that was a trading day or given day, if it is the trading day
	 * - it will exclude Saturdays & Sundays
	 */
	public static Calendar getLastValidDate(Calendar calendar) {		
		int dayInMonth = calendar.get(Calendar.DAY_OF_MONTH);
		int dayInWeek = (7 + calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY) % 7 + 1;
		// there are no data for weekends
		if (dayInWeek > 5) {
			dayInMonth -= (dayInWeek - 5);	// go back by one or two days

			calendar.set(Calendar.DAY_OF_MONTH, dayInMonth);
		}
		
		return calendar;
	}
	
	/**
	 * ask ConnectivityManager if the device is connected
	 */
	public static boolean isOnline(Context context) {
		try {
			if (connectivityManager == null)
				connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = connectivityManager.getActiveNetworkInfo();
			return info != null && info.isConnectedOrConnecting();
		} catch (Exception e) {
			return false;
		}
	}
}
