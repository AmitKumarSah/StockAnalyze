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

import java.util.Collection;
import java.util.HashSet;

/**
 * @author tomas
 * class advising about data provider, it is used in DataProviderFactory 
 * to determine which concrete provider to use
 */
public class DataProviderAdviser {

	private final boolean isRealTime;
	private final boolean supportHistorical;
	private final boolean supportStatistics;
	private final HashSet<String> marketCodes;
	
	public DataProviderAdviser(boolean isRealTime, boolean supportHistorical,
			boolean supportStatistics, String marketCode) {
		this.isRealTime = isRealTime;
		this.supportHistorical = supportHistorical;
		this.supportStatistics = supportStatistics;
		this.marketCodes = new HashSet<String>(1);
		this.marketCodes.add(marketCode);
	}

	public DataProviderAdviser(boolean isRealTime, boolean supportHistorical,
	                           boolean supportStatistics, HashSet<String> marketCodes) {
		this.isRealTime = isRealTime;
		this.supportHistorical = supportHistorical;
		this.supportStatistics = supportStatistics;
		this.marketCodes = marketCodes;
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

	/* 
	 * string representation
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DataProviderAdviser [isRealTime=" + isRealTime
				+ ", supportHistorical=" + supportHistorical
				+ ", supportStatistics=" + supportStatistics + ", marketCode="
				+ marketCodes + "]";
	}

	public Collection<String> getMarketCode() {
		return this.marketCodes;
	}
	
	
}
