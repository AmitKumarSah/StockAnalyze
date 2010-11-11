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
	
	
}
