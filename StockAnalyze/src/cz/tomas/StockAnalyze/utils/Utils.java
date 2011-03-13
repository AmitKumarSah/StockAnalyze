/**
 * 
 */
package cz.tomas.StockAnalyze.utils;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * helper class with various helper methods
 * @author tomas
 *
 */
public class Utils {
	
	public static String LOG_TAG = "StockAnalyze";
	public static String PREF_NAME = "StockAnalyzePreferences";
	public static String PREF_UPDATE_NOTIF = "prefUpdateNotif";
	public static String PREF_PERMANENT_NOTIF = "prefPermanentNotif";

	/*
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
	
	/*
	 * construct calendar that contains only date info from general Calendar object
	 */
	public static Calendar createDateOnlyCalendar(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date.getTime());
		
		return createDateOnlyCalendar(calendar);
	}
	
	/*
	 * get nearest previous day that was a trading day 
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
}
