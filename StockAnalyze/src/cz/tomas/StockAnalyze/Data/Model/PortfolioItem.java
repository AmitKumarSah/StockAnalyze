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

/**
 * represents one portfolio item -
 * particular stock bought at particular price in particular number of pieces
 * 
 * @author tomas
 *
 */
public class PortfolioItem {
	
	/*
	 * id from db
	 */
	int id;
	
	String stockId;
	String portfolioName;
	int boughtCount;
	int soldCount;
	
	double buyPrice;
	double sellPrice;
	
	long buyDate;
	long sellDate;
	
	double buyFee;
	double sellFee;
	
	String marketId;
	
	
	
	/**
	 * create new portfolio item only with buy data
	 * @param stockId
	 * @param portfolioName
	 * @param stockCount
	 * @param buyPrice
	 * @param buyDate
	 * @param marketId
	 */
	public PortfolioItem(String stockId, String portfolioName, int stockCount,
			double buyPrice, double sellPrice, long buyDate, long sellDate, String marketId) {
		this(-1, stockId, portfolioName, stockCount, buyPrice, sellPrice, buyDate, sellDate, 0, 0, marketId);
	}

	public PortfolioItem(int id, String stockId, String portfolioName, int stockCount,
			double buyPrice, double sellPrice, long buyDate, long sellDate,
			double buyFee, double sellFee, String marketId) {
		this.id = id;
		this.stockId = stockId;
		this.portfolioName = portfolioName;
		if (stockCount > 0) {
			this.boughtCount = stockCount;
		} else {
			this.soldCount = stockCount;
		}
		this.buyPrice = buyPrice;
		this.buyDate = buyDate;
		this.marketId = marketId;
		this.sellPrice = sellPrice;
		this.sellDate = sellDate;
		this.buyFee = buyFee;
		this.sellFee = sellFee;
	}
	
	/*
	 * id from database, 
	 * in case this item wasn't created from database, it is -1
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * id of market where to stock was bought
	 * @return the marketId
	 */
	public String getMarketId() {
		return marketId;
	}

	/**
	 * fee to broker for buying the stock
	 * @return the buyFee
	 */
	public double getBuyFee() {
		return buyFee;
	}

	/**
	 * fee to broker for buying the stock
	 * @param fee the buyFee to set
	 */
	public void setBuyFee(double fee) {
		this.buyFee = fee;
	}

	/**
	 * fee to broker for potential selling the stock
	 * @return the sellFee
	 */
	public double getSellFee() {
		return sellFee;
	}

	/**
	 * fee to broker for selling the stock
	 * @param sellFee the sellFee to set
	 */
	public void setSellFee(double sellFee) {
		this.sellFee = sellFee;
	}

	/**
	 * stock id for existing StockItem
	 * @return the stockId
	 */
	public String getStockId() {
		return stockId;
	}

	/**
	 * @return the portfolioName
	 */
	public String getPortfolioName() {
		return portfolioName;
	}

	/**
	 * bought count - sold count
	 * @return
	 */
	public int getCurrentStockCount() {
		return this.boughtCount + this.soldCount;
	}

	/**
	 * number of stock pieces for this particular portfolio item
	 * @return the stockCount
	 */
	public int getBoughtStockCount() {
		return boughtCount;
	}
	
	public void setSoldStockCount(int count) {
		this.soldCount = count;
	}
	
	public int getSoldStockCount() {
		return soldCount;
	}

	/**
	 * price of stock item in time of buying
	 * @return the buyPrice
	 */
	public double getBuyPrice() {
		return buyPrice;
	}

	/**
	 * price of stock item in time of selling
	 * @return the sellPrice, returns negative number if stock item hasn't been sold yet
	 */
	public double getSellPrice() {
		return sellPrice;
	}

	/**
	 * date of buying (in ms)
	 * @return the buyDate
	 */
	public long getBuyDate() {
		return buyDate;
	}

	/**
	 * date of selling (in ms)
	 * @return the sellDate
	 */
	public long getSellDate() {
		return sellDate;
	}
	
	/**
	 * @param stockCount the stock count to set
	 */
	public void setBoughtStockCount(int stockCount) {
		this.boughtCount = stockCount;
	}

	/**
	 * @param sellPrice the sell price to set
	 */
	public void setSellPrice(double sellPrice) {
		this.sellPrice = sellPrice;
	}

	/**
	 * @param sellDate the sell date to set
	 */
	public void setSellDate(long sellDate) {
		this.sellDate = sellDate;
	}

	/** 
	 * full string representation of portfolio item
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PortfolioItem [stockId=" + stockId + ", portfolioName="
				+ portfolioName + ", stockCount=" + boughtCount + ", buyPrice="
				+ buyPrice + ", sellPrice=" + sellPrice + ", buyDate="
				+ buyDate + ", sellDate=" + sellDate + "]";
	}

	public double getInvestedValue(boolean includeFees) {
		double soldItemsValue = this.soldCount * this.sellPrice;
		double boughtItemsValue = this.boughtCount * this.buyPrice;
		
		soldItemsValue = Math.abs(soldItemsValue);
		if (includeFees) {
			boughtItemsValue += this.buyFee;	// how much money user paid
			soldItemsValue -= this.sellFee;		// how much money did user get
		}
		
		if (boughtItemsValue > soldItemsValue) {
			// portfolio is long
			return boughtItemsValue;
		} else {
			// portfolio is short
			return soldItemsValue;
		}
	}
	
	/**
	 * calculate absolute and relative changes according value and current price
	 * 
	 * @param stockPrice current stock price
	 * @param includeFees true if calculation should include fees
	 * @param output 2 element array that will be filled with change values.
	 * at index 0 is relative change in % and at index 1 is absolute change
	 */
	public void calculateChanges(double stockPrice, boolean includeFees, double[] output) {
		final double currentMarketValue = this.getCurrentStockCount() * stockPrice;
    	// value of sold items, this is negative number
		double soldItemsValue = this.soldCount * this.sellPrice;
		double boughtItemsValue = this.boughtCount * this.buyPrice;
		
		soldItemsValue = Math.abs(soldItemsValue);
		if (includeFees) {
			boughtItemsValue += this.buyFee;	// how much money user paid
			soldItemsValue -= this.sellFee;		// how much money did user get
		}
		double change = 0f;
		double absChange = 0f;

		if (boughtItemsValue > soldItemsValue) {
			// portfolio is long
			absChange = soldItemsValue + currentMarketValue - boughtItemsValue;
			change = 100 * absChange / boughtItemsValue;
		} else if (boughtItemsValue < soldItemsValue) {
			// portfolio is short
			absChange = soldItemsValue - boughtItemsValue + currentMarketValue;
			change = 100 * absChange / soldItemsValue;
		}
		output[0] = change;
		output[1] = absChange;
	}
	
}
