/*******************************************************************************
 * Copyright (c) 2011 Tomas Vondracek.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Tomas Vondracek
 ******************************************************************************/
/**
 * 
 */
package cz.tomas.StockAnalyze.Data.Model;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * class representing one price value for one day of one stock
 * 
 * @author tomas
 */
public class DayData implements Parcelable {
	private long id;

	private float price;
	private float change;
	private float absChange;
	private Date date;
	private long lastUpdate;

	private float volume;
	private int tradedPieces;
	
	private float yearMaximum;
	private float yearMinimum;
	
	public static final Parcelable.Creator<DayData> CREATOR = new Parcelable.Creator<DayData>() {
		public DayData createFromParcel(Parcel in) {
			return new DayData(in);
		}

		public DayData[] newArray(int size) {
			return new DayData[size];
		}
	};
	
	public DayData(float price, float change, Date date, float volume, float yearMax, float yearMin, long updateTime, long id) {
		this(price, change, date, volume, yearMax, yearMin, updateTime);
		this.id = id;
	}	
	public DayData(float price, float change, Date date, float volume, float yearMax, float yearMin, long updateTime) {
		this.price = price;
		this.change = change;
		this.date = date;
		this.volume = volume;
		this.yearMaximum = yearMax;
		this.yearMinimum = yearMin;
		this.lastUpdate = updateTime;
		
		this.absChange = this.price * this.change / 100.0f;
	}

	public DayData(Parcel in) {
		this.readParcel(in);
	}

	public long getId() {
		return id;
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
	 * last update time (in miliseconds)
	 */
	public long getLastUpdate() {
		return lastUpdate;
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
	@Override
	public int describeContents() {
		return 0;
	}
	
	private void readParcel(Parcel in) {
		this.id = in.readLong();
		this.price = in.readFloat();
		this.change = in.readFloat();
		this.absChange = in.readFloat();
		this.date = new Date(in.readLong());
		this.lastUpdate = in.readLong();
		this.volume = in.readFloat();
		this.tradedPieces = in.readInt();
		this.yearMaximum = in.readFloat();
		this.yearMinimum = in.readFloat();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(this.id);
		dest.writeFloat(this.price);
		dest.writeFloat(this.change);
		dest.writeFloat(this.absChange);
		dest.writeLong(this.date.getTime());
		dest.writeLong(this.lastUpdate);
		dest.writeFloat(this.volume);
		dest.writeInt(this.tradedPieces);
		dest.writeFloat(this.yearMaximum);
		dest.writeFloat(this.yearMinimum);
	}
}
