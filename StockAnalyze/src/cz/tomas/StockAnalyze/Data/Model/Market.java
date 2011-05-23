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
 * @author tomas
 * 
 * represents market (e.g. NYSE, PSE,...)
 * is serializable so it can be pu in an Intent
 * 
 * StockItem has a reference to the Market
 */
public class Market implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	String name;
	String id;
	Currency currency;
	String description;

	public Market(String name, String id, String currencyCode, String description) {
		super();
		this.name = name;
		this.id = id;
		this.currency = Currency.getInstance(currencyCode);
		this.description = description;
	}
	
	public Market(String name, String id, Currency currency, String description) {
		super();
		this.name = name;
		this.id = id;
		this.currency = currency;
		this.description = description;
	}
	/**
	 * name of market (e.g. New York Stock Exchange)
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * set name of market
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * unique code of the market
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * set unique code of the market
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * Currency used for trading on the market
	 * @return the currency
	 */
	public Currency getCurrency() {
		return currency;
	}
	/**
	 * set Currency used for trading on the market
	 * @param currency the currency to set
	 */
	public void setCurrency(Currency currency) {
		this.currency = currency;
	}
	/**
	 * market description, it's optional
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	

	
	/* 
	 * hash code of this market object
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((currency == null) ? 0 : currency.hashCode());
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((currency == null) ? 0 : currency.getCurrencyCode().hashCode());
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
		Market other = (Market) obj;
		if (currency == null) {
			if (other.currency != null)
				return false;
		} else if (!currency.equals(other.currency))
			return false;
//		if (description == null) {
//			if (other.description != null)
//				return false;
//		} else if (!description.equals(other.description))
//			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	/* string representation
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Market [name=" + name + ", id=" + id + ", currency=" + currency
				+ ", description=" + description + "]";
	}
}
