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
	Date date;
	float volume;
	
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
	}
	
	public DayData(float price, float change, Date date, float volume) {
		this.price = price;
		this.change = change;
		this.date = date;
		this.volume = volume;
	}
	public float getPrice() {
		return price;
	}
	public float getChange() {
		return change;
	}
	public Date getDate() {
		return date;
	}
	public float getVolume() {
		return volume;
	}
}
