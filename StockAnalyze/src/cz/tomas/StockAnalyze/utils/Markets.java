package cz.tomas.StockAnalyze.utils;

import cz.tomas.StockAnalyze.Data.Model.Market;


public class Markets {

	public static final Market CZ = new Market("PX", "PSE", "CZK", "Prague Stock Exchange, SPAD market", "cz");
	public static final Market DE = new Market("DAX", "XETRA", "EUR", "Deutsche Borse", "de");
	public static final Market US_NYSE = new Market("Dow Jones Industrial Avrg.", "NYSE", "USD", "Dow Jones", "us");
	public static final Market US_NASDAQ = new Market("US - Nasdaq - Composite", "NASDAQ", "USD", "NASDAQ", "us");
	public static final Market EU = new Market("EU", "EU", "EUR", "Euro Stocks", "eu");
	public static final Market HU = new Market("Budapest Stock Exchange", "BSE", "HUF", "Budapest Stock Exchange", "hu");
	public static final Market PL = new Market("Warsaw Stock Exchange", "WSE", "PLN", "Warsaw Stock Exchange", "pl");
	public static final Market RUS = new Market("Russian Trading System", "RTS", "RUB", "Russian Trading System Stock Exchange", "rus");
	public static final Market JP = new Market("Tokio Stock Exchange", "TSE", "JPY", "Tokio Stock Exchange", "jp");
	public static final Market SW = new Market("Swiss Exchange", "SIX", "CHF", "SIX Swiss Exchange", "sw");
	public static final Market GB = new Market("London Stock Exchange", "LSE", "GBP", "London Stock Exchange", "gb");
	
	/**
	 * fake market used for global indeces
	 */
	public static final Market GLOBAL = new Market("Global", "GLOBAL", "", "Indeces", "world");
	
	/**
	 * get market based on the id
	 * @param id
	 * @return
	 */
	public static Market getMarket(String id) {
		if (id == null) {
			throw new NullPointerException("market id can't be null");
		}
		id = id.toLowerCase();
		if (id.equals("pse")) {
			return CZ;
		} else if (id.equals("global")) {
			return GLOBAL;
		} else if (id.equals("eu")) {
			return EU;
		} else if (id.equals("bse")) {
			return HU;
		} else if (id.equals("nyse")) {
			return US_NYSE;
		} else if (id.equals("nasdaq")) {
			return US_NASDAQ;
		} else if (id.equals("wse")) {
			return PL;
		} else if (id.equals("rts")) {
			return RUS;
		} else if (id.equals("tse")) {
			return JP;
		} else if (id.equals("six")) {
			return SW;
		} else if (id.equals("lse")) {
			return GB;
		} else if (id.equals("xetra")) {
			return DE;
		} else {
			return null;
		}
	}
}
