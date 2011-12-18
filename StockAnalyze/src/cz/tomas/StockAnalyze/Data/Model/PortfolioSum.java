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

/**
 * portfolio summarization info
 * 
 * @author tomas
 *
 */
@SuppressWarnings("serial")
public class PortfolioSum implements Serializable{
	
	private final double totalValue;
	private final double totalAbsChange;
	private final double totalPercChange;
	
	public PortfolioSum(double totalValueSum, double totalAbsChangeSum,
			double totalInvestedSum) {
		this.totalValue = totalValueSum;
		this.totalAbsChange = totalAbsChangeSum;
		this.totalPercChange = totalInvestedSum;
	}
	
	/**
	 * get total absolute value of all portfolio items
	 */
	public double getTotalValue() {
		return totalValue;
	}
	
	/**
	 * get total absolute value of change of your portfolio, 
	 * that is, sum of all changes among all portfolio items.
	 */
	public double getTotalAbsChange() {
		return totalAbsChange;
	}
	
	/**
	 * get total percentual change of all portfolio items
	 */
	public double getTotalPercChange() {
		return totalPercChange;
	}
	
	
}
