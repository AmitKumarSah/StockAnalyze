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
package cz.tomas.StockAnalyze.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * helper class with various helper methods and string const names
 * @author tomas
 *
 */
public class Utils {
	
	public static final String LOG_TAG = "StockAnalyze";
	public static final boolean DEBUG = true;
	public static final String PREF_NAME = "StockAnalyzePreferences";
	public static final String PREF_UPDATE_NOTIF = "prefUpdateNotif";
	public static final String PREF_PERMANENT_NOTIF = "prefPermanentNotif";
	public static final String PREF_ENABLE_BACKGROUND_UPDATE = "prefEnableBackgroundUpdate";
	public static final String PREF_INTERVAL_BACKGROUND_UPDATE = "prefIntervalBackgroundUpdate";
	public static final String PREF_PORTFOLIO_INCLUDE_FEE = "prefPortfolioIncludeFee";
	public static final String PREF_LAST_UPDATE_TIME = "prefLastUpdateTime";
	public static final String PREF_LAST_STOCK_LIST_UPDATE_TIME = "cz.tomas.StockAnalyze:StockListUpdateTime";
	public static final String PREF_LAST_MARKET_LIST_UPDATE_TIME = "cz.tomas.StockAnalyze:MarketListUpdateTime";
	public static final String PREF_CHART_TIME_PERIOD = "prefChartTimePeriod";
	public static final String PREF_HOME_CHART_TICKER = "prefHomeChartTicker";
	public static final String PREF_HOME_CHART_MARKET_ID = "prefHomeChartMarketId";
	public static final String PREF_URLS_TIME = "prefGaeUrlUpdate";
	public static final String PREF_FULL_ARTICLE = "prefNewsFullArticle";
	public static final String PREF_STOCKS_POSITION = "prefStocksPosition";

	public static final boolean PREF_DEF_PERMANENT_NOTIF = false;
	public static final boolean PREF_DEF_UPDATE_NOTIF = true;
	public static final boolean PREF_DEF_ENABLE_BACKGROUND_UPDATE = true;
	public static final boolean PREF_DEF_FULL_ARTICLE = false;

	public static final String PREF_COOKIE = "cookie";
	
	public static final TimeZone PRAGUE_TIME_ZONE = TimeZone.getTimeZone("Europe/Prague");
	
	public static final String EXTRA_STOCK_ITEM = "cz.tomas.StockAnalyze:StockItem";
	public static final String EXTRA_DAY_DATA = "cz.tomas.StockAnalyze:DayData";
	public static final String EXTRA_MARKET_ID = "cz.tomas.StockAnalyze:Market_ID";
	public static final String EXTRA_SOURCE = "cz.tomas.StockAnalyze:Source";
	
	public static final String FLURRY_KEY = "DZTBW5JWI9WLE92D1QBX";
	
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
			//calendar.roll(Calendar.DAY_OF_WEEK, -(dayInWeek - 5));
			calendar.set(Calendar.DAY_OF_MONTH, dayInMonth);
		}
		
		return calendar;
	}
	
	/**
	 * get next nearest day that is trading day, or given day, if it is the trading day
	 * 
	 * @param calendar
	 * @return
	 */
	public static Calendar getNextValidDate(Calendar calendar) {
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == Calendar.SUNDAY) {
			calendar.add(Calendar.DAY_OF_YEAR, 1);
		} else if (dayOfWeek == Calendar.SATURDAY) {
			calendar.add(Calendar.DAY_OF_YEAR, 2);
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

	public static StringBuilder certToString(X509Certificate c) {
		final StringBuilder hexString = new StringBuilder();
		try {
			MessageDigest md = MessageDigest.getInstance("SHA1");
			byte[] publicKey = md.digest(c.getPublicKey().getEncoded());

			for (byte b : publicKey) {
				String appendString = Integer.toHexString(0xFF & b);
				if (appendString.length() == 1) {
					hexString.append("0");
				}
				hexString.append(appendString);
			}
		} catch (NoSuchAlgorithmException ignored) {
		}
		return hexString;
	}
}
