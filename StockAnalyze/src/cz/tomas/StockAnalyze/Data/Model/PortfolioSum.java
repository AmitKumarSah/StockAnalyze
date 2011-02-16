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
