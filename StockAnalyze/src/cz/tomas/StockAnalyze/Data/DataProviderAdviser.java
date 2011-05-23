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
package cz.tomas.StockAnalyze.Data;

import java.util.ArrayList;
import java.util.List;

import cz.tomas.StockAnalyze.Data.Model.Market;

/**
 * @author tomas
 * class advising about data provider, it is used in DataProviderFactory 
 * to determine which concrete provider to use
 */
public class DataProviderAdviser {

	boolean isRealTime;
	boolean supportHistorical;
	boolean supportStatistics;
	List<Market> markets;
	
	
	public DataProviderAdviser(boolean isRealTime, boolean supportHistorical,
			boolean supportStatistics, List<Market> markets) {
		this.isRealTime = isRealTime;
		this.supportHistorical = supportHistorical;
		this.supportStatistics = supportStatistics;
		this.markets = markets;
	}
	
	public DataProviderAdviser(boolean isRealTime, boolean supportHistorical,
			boolean supportStatistics, Market market) {
		this.isRealTime = isRealTime;
		this.supportHistorical = supportHistorical;
		this.supportStatistics = supportStatistics;
		this.markets = new ArrayList<Market>();
		this.markets.add(market);
	}

	/**
	 * true if provider supports real time data (including delayed)
	 * @return the isRealTime
	 */
	public boolean isRealTime() {
		return isRealTime;
	}

	/**
	 * true if provider supports historical data
	 * @return the supportHistorical
	 */
	public boolean supportHistorical() {
		return supportHistorical;
	}

	/**
	 * true if provider supports statistical information (year max/min, ...)
	 * @return the supportStatistics
	 */
	public boolean supportStatistics() {
		return supportStatistics;
	}

	/**
	 * Markets the provider covers
	 * @return the market
	 */
	public List<Market> getMarkets() {
		return markets;
	}

	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isRealTime ? 1231 : 1237);
		result = prime * result + ((markets == null) ? 0 : markets.hashCode());
		result = prime * result + (supportHistorical ? 1231 : 1237);
		result = prime * result + (supportStatistics ? 1231 : 1237);
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
		DataProviderAdviser other = (DataProviderAdviser) obj;
		if (isRealTime != other.isRealTime)
			return false;
		if (markets == null) {
			if (other.markets != null)
				return false;
		} else if (!markets.equals(other.markets))
			return false;
		if (supportHistorical != other.supportHistorical)
			return false;
		if (supportStatistics != other.supportStatistics)
			return false;
		return true;
	}

	/* 
	 * string representation
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DataProviderAdviser [isRealTime=" + isRealTime
				+ ", supportHistorical=" + supportHistorical
				+ ", supportStatistics=" + supportStatistics + ", market count="
				+ markets.size() + "]";
	}
	
	
}
