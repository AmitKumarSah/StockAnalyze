/**
 * 
 */
package cz.tomas.StockAnalyze.Data.Model;

import java.util.Calendar;
import java.util.Date;

import cz.tomas.StockAnalyze.Data.PseCsvData.CsvDataRow;

/**
 * @author tomas
 * class representing one price value for one day of one stock
 */
public class DayData {
	float price;
	float change;
	float absChange;
	Date date;
	float volume;
	int tradedPieaces;
	
	float yearMaximum;
	float yearMinimum;
	
	// TODO get rid of this ctor
	public DayData(CsvDataRow dataRow) {
		if (dataRow == null) {
			throw new NullPointerException("dataRow is null");
		}
		try {
			this.price = Float.parseFloat(dataRow.getClosePrice());
		} catch (Exception e) {
			this.price = -1;
			e.printStackTrace();
		}
		try {
			this.change = Float.parseFloat(dataRow.getChange());
		} catch (Exception e) {
			this.change = 0;
			e.printStackTrace();
		}
		try {
			this.date = new Date(Date.parse(dataRow.getDate()));
		} catch (Exception e) {
			this.date = Calendar.getInstance().getTime();
			e.printStackTrace();
		}
		try {
			this.volume = Float.parseFloat(dataRow.getDayVolume());
		} catch (Exception e) {
			this.volume = -1;
			e.printStackTrace();
		}
		try {
			this.yearMaximum = Float.parseFloat(dataRow.getYearMax());
		} catch (NumberFormatException e) {
			this.yearMaximum = -1;
			e.printStackTrace();
		}
		try {
			this.yearMinimum = Float.parseFloat(dataRow.getYearMin());
		} catch (NumberFormatException e) {
			this.yearMinimum = -1;
			e.printStackTrace();
		}
		try {
			this.tradedPieaces = Integer.parseInt(dataRow.getTradedPieces());
		} catch (NumberFormatException e) {
			this.tradedPieaces = -1;
			e.printStackTrace();
		}
	}
	
	public DayData(float price, float change, Date date, float volume, float yearMax, float yearMin) {
		this.price = price;
		this.change = change;
		this.date = date;
		this.volume = volume;
		this.yearMaximum = yearMax;
		this.yearMinimum = yearMin;
		
		this.absChange = this.price * this.change / 100.0f;
	}
	
	public float getPrice() {
		return price;
	}
	/*
	 * percentual change
	 */
	public float getChange() {
		return change;
	}
	
	/**
	 * absolute change in currency of the stock
	 * @return the absChange
	 */
	public float getAbsChange() {
		return absChange;
	}
	
	public Date getDate() {
		return date;
	}
	/*
	 * Day volume in stock's currency
	 * */
	public float getVolume() {
		return volume;
	}

	/**
	 * get year maximum price valid for this particular day
	 * @return the yearMaximum
	 */
	public float getYearMaximum() {
		return yearMaximum;
	}

	/**
	 * get year minimum price valid for this particular day
	 * @return the yearMinimum
	 */
	public float getYearMinimum() {
		return yearMinimum;
	}

	/**
	 * count of traded pieces
	 * @return the tradedPieaces
	 */
	public int getTradedPieaces() {
		return tradedPieaces;
	}
}
