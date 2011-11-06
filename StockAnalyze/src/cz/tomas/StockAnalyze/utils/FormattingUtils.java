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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

import android.util.Log;

/**
 * @author tomas
 *
 * Utils for formatting dates and other
 */
public final class FormattingUtils {
	// e.g. "Thu, 4 Nov 2010 16:00:13 +0100"
	static DateFormat frmCz = null;
	// e.g. 04 Jan 2011 13:31:00 +0100
	static DateFormat frmEn = null;
	/*
	 * remember last successful format to use it as a primary format
	 */
	static DateFormat lastSuccessfulDateFormat;
	
	static NumberFormat priceFormatCzk = null;
	static NumberFormat percentFormat = null;
	static NumberFormat volumeFormat = null;
	
	private static DateFormat getCzechFormatter() {
		if (frmCz == null)
			frmCz = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
		return frmCz;
	}
	
	private static DateFormat getEnFormatter() {
		if (frmEn == null)
			frmEn = new SimpleDateFormat("dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
		return frmEn;
	}
	
	
	public static NumberFormat getVolumeFormat() {
		if (volumeFormat == null) {
			volumeFormat = DecimalFormat.getNumberInstance();
			volumeFormat.setGroupingUsed(true);
		}
		return volumeFormat;
	}
	
	public static NumberFormat getPercentFormat() {
		if (percentFormat == null) {
			percentFormat = DecimalFormat.getNumberInstance();
			percentFormat.setMaximumFractionDigits(2);
		}
		return percentFormat;
	}
	
	public static NumberFormat getPriceFormatCzk() {
		if (priceFormatCzk == null) {
			priceFormatCzk = DecimalFormat.getCurrencyInstance();
			priceFormatCzk.setCurrency(Currency.getInstance("CZK"));
		}
		
		return priceFormatCzk;
	}
	
	public static NumberFormat getPriceFormat(Currency cur) {
		NumberFormat priceFormat = null;
		if (cur.getCurrencyCode().equals("CZK")) {
			priceFormat = getPriceFormatCzk();
		}
		else {
			priceFormat = DecimalFormat.getCurrencyInstance();
			priceFormat.setCurrency(cur);
		}
		return priceFormat;
	}
	
	/**
	 * parse date string with predefined parser, preferring English parser
	 * @return parsed Date or null if failed
	 */
	public static Date parse(String date) throws ParseException {
		Date result = null;
		if (lastSuccessfulDateFormat != null) {
			try {
				result = lastSuccessfulDateFormat.parse(date);
			} catch (Exception e) {
				lastSuccessfulDateFormat = null;
			}
		}
		if (result == null) {
			try {
				result = getEnFormatter().parse(date);
				lastSuccessfulDateFormat = getEnFormatter();
			} catch (Exception e) {
				try {
					result = getCzechFormatter().parse(date);
					lastSuccessfulDateFormat = getCzechFormatter();
				} catch (Exception e1) {
					Log.d(Utils.LOG_TAG, "failed to parse date " + date
							+ " with czech or english parse format");
				}
			}
		}
		return result;
	}
	
	public static String formatStockShortDate(long ms) {
		Calendar cal = Calendar.getInstance(Utils.PRAGUE_TIME_ZONE);
		cal.setTimeInMillis(ms);
		DateFormat formatter = null;
		formatter = DateFormat.getDateInstance(DateFormat.SHORT);
		return formatter.format(cal.getTime());
	}
	
	public static String formatStockShortDate(Calendar cal) {
		DateFormat formatter = null;
		formatter = DateFormat.getDateInstance(DateFormat.SHORT);
		return formatter.format(cal.getTime());
	}
	
	public static String formatStockShortTime(Calendar cal) {
		DateFormat formatter = null;
		formatter = DateFormat.getTimeInstance(DateFormat.SHORT);
		return formatter.format(cal.getTime());
	}

	public static String formatStockDate(Calendar cal) {
		DateFormat formatter = null;
//		if (cal.get(Calendar.HOUR_OF_DAY) != 0 && cal.get(Calendar.MINUTE) != 0)
//			formatter = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM);
//		else
//			formatter = DateFormat.getDateInstance(DateFormat.LONG);
		formatter = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM);
		return formatter.format(cal.getTime());
	}
	
	public static String formatDate(Calendar cal) {
		DateFormat formatter = null;
		formatter = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM);
		return formatter.format(cal.getTime());
	}
}
