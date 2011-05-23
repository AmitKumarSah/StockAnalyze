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
package cz.tomas.StockAnalyze.Data.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class StockItem implements Parcelable {
	/*
	 * stock ticker
	 * */
	String ticker;
	/*
	 * ISIN
	 * */
	String id;
	/*
	 * full name of stock
	 * */
	String name;
	/*
	 * market where is the stock traded, e.g. NYSE, RM-System,
	 * */
	Market market;
	
	public static final Parcelable.Creator<StockItem> CREATOR = new Parcelable.Creator<StockItem>() {
		public StockItem createFromParcel(Parcel in) {
			return new StockItem(in);
		}

		public StockItem[] newArray(int size) {
			return new StockItem[size];
		}
	};

	public StockItem(String ticker, String id, String name, Market market) {
		this.ticker = ticker;
		this.id = id;
		this.name = name;
		this.market = market;
	}
	
	public StockItem(Parcel in) {
		this.readParcel(in);
	}

	public String getTicker() {
		return ticker;
	}
	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public Market getMarket() {
		return this.market;
	}
	
	@Override
	public String toString() {
		return "StockItem [name=" + name + ", ticker=" + ticker + ", market=" + this.market.getName() + "]";
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StockItem other = (StockItem) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	private void readParcel(Parcel in) {
		this.ticker = in.readString();
		this.id = in.readString();
		this.name = in.readString();
		this.market = (Market) in.readSerializable();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.ticker);
		dest.writeString(this.id);
		dest.writeString(this.name);
		dest.writeSerializable(this.market);
	}
	
	
}
