/**
 * 
 */
package cz.tomas.StockAnalyze.Data.Model;

/**
 * represents one portfolio item -
 * particular stock bought at particular price in particular number of pieces
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
	int stockCount;
	
	float buyPrice;
	float sellPrice;
	
	long buyDate;
	long sellDate;
	
	float buyFee;
	float sellFee;
	
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
			float buyPrice, long buyDate, String marketId) {
		this(-1, stockId, portfolioName, stockCount, buyPrice, 0, buyDate, 0, 0, 0, marketId);
	}

	public PortfolioItem(int id, String stockId, String portfolioName, int stockCount,
			float buyPrice, float sellPrice, long buyDate, long sellDate,
			float buyFee, float sellFee, String marketId) {
		this.id = id;
		this.stockId = stockId;
		this.portfolioName = portfolioName;
		this.stockCount = stockCount;
		this.buyPrice = buyPrice;
		this.buyDate = buyDate;
		this.marketId = marketId;
		this.sellPrice = sellPrice;
		this.sellDate = sellDate;
		this.buyFee = buyFee;
		this.sellFee = sellFee;
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
	public float getBuyFee() {
		return buyFee;
	}

	/**
	 * fee to broker for buying the stock
	 * @param buyFee the buyFee to set
	 */
	public void setBuyFee(float buyFee) {
		this.buyFee = buyFee;
	}

	/**
	 * fee to broker for potential selling the stock
	 * @return the sellFee
	 */
	public float getSellFee() {
		return sellFee;
	}

	/**
	 * fee to broker for selling the stock
	 * @param sellFee the sellFee to set
	 */
	public void setSellFee(float sellFee) {
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
	 * number of stock pieces for this particular portfolio item
	 * @return the stockCount
	 */
	public int getStockCount() {
		return stockCount;
	}

	/**
	 * price of stock item in time of buying
	 * @return the buyPrice
	 */
	public float getBuyPrice() {
		return buyPrice;
	}

	/**
	 * price of stock item in time of selling
	 * @return the sellPrice, returns negative number if stock item hasn't been sold yet
	 */
	public float getSellPrice() {
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


	/* 
	 * full string representation of portfolio item
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PortfolioItem [stockId=" + stockId + ", portfolioName="
				+ portfolioName + ", stockCount=" + stockCount + ", buyPrice="
				+ buyPrice + ", sellPrice=" + sellPrice + ", buyDate="
				+ buyDate + ", sellDate=" + sellDate + "]";
	}
	
	
}
