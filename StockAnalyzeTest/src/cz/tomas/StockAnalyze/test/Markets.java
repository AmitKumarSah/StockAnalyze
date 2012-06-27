package cz.tomas.StockAnalyze.test;

import cz.tomas.StockAnalyze.Data.Model.Market;


public class Markets {

	private static int uiOrder = 0;
	public static final Market CZ = new Market("Prague Stock Exch.", "PSE", "CZK", "Prague Stock Exchange", "cz", 5, 0, 0, 0, 0, Market.TYPE_FULL, uiOrder);
	public static final Market DE = new Market("Deutsche Borse", "XETRA", "EUR", "Deutsche Borse", "de",  5, 0, 0, 0, 0, Market.TYPE_FULL, uiOrder);
	public static final Market US_NYSE = new Market("New York Stock Exch.", "NYSE", "USD", "Dow Jones", "us",  5, 0, 0, 0, 0, Market.TYPE_SELECTIVE, uiOrder);
	public static final Market US_NASDAQ = new Market("Nasdaq", "NASDAQ", "USD", "NASDAQ", "us",  5, 0, 0, 0, 0, Market.TYPE_SELECTIVE, uiOrder);
	public static final Market EU = new Market("EU", "EU", "EUR", "Euro Stocks", "eu",  5, 0, 0, 0, 0, Market.TYPE_FULL, uiOrder);
	public static final Market HU = new Market("Budapest Stock Exch.", "BSE", "HUF", "Budapest Stock Exchange", "hu",  5, 0, 0, 0, 0, Market.TYPE_FULL, uiOrder);
	public static final Market PL = new Market("Warsaw Stock Exch.", "WSE", "PLN", "Warsaw Stock Exchange", "pl",  5, 0, 0, 0, 0, Market.TYPE_FULL, uiOrder);
	public static final Market RUS = new Market("Russian Trading Sys.", "RTS", "RUB", "Russian Trading System Stock Exchange", "rus",  5, 0, 0, 0, 0, Market.TYPE_FULL, uiOrder);
	public static final Market JP = new Market("Tokio Stock Exch.", "TSE", "JPY", "Tokio Stock Exchange", "jp",  5, 0, 0, 0, 0, Market.TYPE_FULL, uiOrder);
	public static final Market SW = new Market("Swiss Exchange", "SIX", "CHF", "SIX Swiss Exchange", "sw",  5, 0, 0, 0, 0, Market.TYPE_FULL, uiOrder);
	public static final Market GB = new Market("London Stock Exch.", "LSE", "GBP", "London Stock Exchange", "gb",  5, 0, 0, 0, 0, Market.TYPE_FULL, uiOrder);
	
	/**
	 * fake market used for global indices
	 */
	public static final Market GLOBAL = new Market("Global", "GLOBAL", "", "Indeces", "world", 5, 0, 0, 0, 0, Market.TYPE_FULL, uiOrder);
}
