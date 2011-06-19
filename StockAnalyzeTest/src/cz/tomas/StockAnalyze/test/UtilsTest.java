package cz.tomas.StockAnalyze.test;

import java.util.Calendar;

import cz.tomas.StockAnalyze.utils.Utils;

import android.test.AndroidTestCase;

public class UtilsTest extends AndroidTestCase {

	public void testPrevoiusDaySunday() {
		Calendar cal = Calendar.getInstance(Utils.PRAGUE_TIME_ZONE);
		// 19.5.2011 is sunday
		cal.set(Calendar.DAY_OF_MONTH, 19);
		cal.set(Calendar.MONTH, Calendar.JUNE);
		cal.set(Calendar.YEAR, 2011);
		
		cal = Utils.getLastValidDate(cal);
		
		assertEquals(Calendar.FRIDAY, cal.get(Calendar.DAY_OF_WEEK));
		assertEquals(Calendar.JUNE, cal.get(Calendar.MONTH));
	}
	
	public void testNextDaySunday() {
		Calendar cal = Calendar.getInstance(Utils.PRAGUE_TIME_ZONE);
		// 19.5.2011 is sunday
		cal.set(Calendar.DAY_OF_MONTH, 19);
		cal.set(Calendar.MONTH, Calendar.JUNE);
		cal.set(Calendar.YEAR, 2011);
		
		cal = Utils.getNextValidDate(cal);
		
		assertEquals(Calendar.MONDAY, cal.get(Calendar.DAY_OF_WEEK));
		assertEquals(20, cal.get(Calendar.DAY_OF_MONTH));
		assertEquals(Calendar.JUNE, cal.get(Calendar.MONTH));
	}
	
	public void testPreviousDayWholeYear() {
		Calendar cal = Calendar.getInstance(Utils.PRAGUE_TIME_ZONE);
		cal.set(Calendar.YEAR, 2011);
		cal.set(Calendar.MONTH, 1);
		
		for (int i = 1; i < 366; i++) {
			cal.set(Calendar.DAY_OF_YEAR, i);
			
			cal = Utils.getLastValidDate(cal);
			assertTrue("day " + i + " is sunday", cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY);
			assertTrue("day " + i + " is saturday", cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY);
			// first two days of the year are Saturday and Sunday
			if ( i < 3) {
				assertEquals("day " + i, 2010, cal.get(Calendar.YEAR));
				cal.set(Calendar.YEAR, 2011);
			} else 
				assertEquals("day " + i, 2011, cal.get(Calendar.YEAR));
		}
	}
}
