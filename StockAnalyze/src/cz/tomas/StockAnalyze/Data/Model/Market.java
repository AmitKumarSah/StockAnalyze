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
package cz.tomas.StockAnalyze.Data.Model;

import java.io.Serializable;
import java.util.Currency;

/**
 * represents market (e.g. NYSE, PSE,...)
 * is serializable so it can be put in an Intent
 * 
 * {@link StockItem} has a reference to the Market
 * 
 * @author tomas
 */
public class Market implements Serializable {
	
	private final String name;
	private final String id;
	private final String currency;
	private final String description;
	private final String country;

	private final long openFrom;
	private final long openTo;
	private final double feeMin;
	private final double feeMax;
	private final double feePercent;

	public Market(String name, String id, String currencyCode, String description, String country,
	              double feePercent, double feeMax, double feeMin, long openTo, long openFrom) {
		this.name = name;
		this.id = id;
		this.currency = currencyCode;
		this.description = description;
		this.country = country;
		this.feePercent = feePercent;
		this.feeMax = feeMax;
		this.feeMin = feeMin;
		this.openTo = openTo;
		this.openFrom = openFrom;
	}
	
	/**
	 * name of market (e.g. New York Stock Exchange)
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * unique code of the market
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Currency used for trading on the market
	 * @return the currency
	 */
	public Currency getCurrency() {
		return Currency.getInstance(currency);
	}

	/**
	 * market description, it's optional
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * get country code
	 */
	public String getCountry() {
		return this.country;
	}

	public String getCurrencyCode() {
		return this.currency;
	}
	public long getOpenFrom() {
		return openFrom;
	}

	public long getOpenTo() {
		return openTo;
	}

	public double calculateFee(double value) {
		double fee = value * feePercent;
		fee = Math.max(this.feeMin, fee);
		fee = Math.min(this.feeMax, fee);

		return fee;
	}

	/*
	 * string representation
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Market [name=" + name + ", id=" + id + ", currency=" + currency
				+ ", description=" + description + "]";
	}

	public double getFeeMax() {
		return feeMax;
	}

	public double getFeeMin() {
		return feeMin;
	}

	public double getFeePerc() {
		return feePercent;
	}
}
