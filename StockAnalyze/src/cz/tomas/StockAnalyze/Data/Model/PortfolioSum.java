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

/**
 * portfolio summarization info
 * 
 * @author tomas
 *
 */
public class PortfolioSum {
	
	private float totalValue;
	private float totalAbsChange;
	private float totalPercChange;
	
	public PortfolioSum(float totalValue, float totalAbsChange,
			float totalPercChange) {
		this.totalValue = totalValue;
		this.totalAbsChange = totalAbsChange;
		this.totalPercChange = totalPercChange;
	}
	
	/*
	 * get total absolute value of all portfolio items
	 */
	public float getTotalValue() {
		return totalValue;
	}
	
	/*
	 * get total absolute value of change of your portfolio, 
	 * that is, sum of all changes among all portfolio items.
	 */
	public float getTotalAbsChange() {
		return totalAbsChange;
	}
	
	/*
	 * get total percentual change of all portfolio items
	 */
	public float getTotalPercChange() {
		return totalPercChange;
	}
	
	
}
