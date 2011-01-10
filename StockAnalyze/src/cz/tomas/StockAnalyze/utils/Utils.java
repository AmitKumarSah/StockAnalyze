/**
 * 
 */
package cz.tomas.StockAnalyze.utils;

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
	
}
