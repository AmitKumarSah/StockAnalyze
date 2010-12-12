/**
 * 
 */
package cz.tomas.StockAnalyze.Data.Model;

import java.util.Date;

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
	int tradedPieces;
	
	float yearMaximum;
	float yearMinimum;
		
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
		return tradedPieces;
	}

	/* 
	 * string representation with selected fields
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DayData [price=" + price + ", change=" + change
				+ ", absChange=" + absChange + ", date=" + date + ", volume="
				+ volume + "]";
	}
}
