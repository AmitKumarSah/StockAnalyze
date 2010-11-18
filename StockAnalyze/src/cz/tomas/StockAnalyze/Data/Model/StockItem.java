package cz.tomas.StockAnalyze.Data.Model;

public class StockItem {
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
	

	public StockItem(String ticker, String id, String name, Market market) {
		this.ticker = ticker;
		this.id = id;
		this.name = name;
		this.market = market;
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
	
	
}
