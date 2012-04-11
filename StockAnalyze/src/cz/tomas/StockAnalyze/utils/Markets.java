package cz.tomas.StockAnalyze.utils;

import cz.tomas.StockAnalyze.Data.Model.Market;

public class Markets {

//	public static final Market CZ = new Market("PX", "PSE", "CZK", "Prague Stock Exchange, SPAD market", "cz", feePercent, feeMax, feeMin, feeMin, feeMin, openTo, openFrom);
//	public static final Market DE = new Market("DAX", "XETRA", "EUR", "Deutsche Borse", "de", feePercent, feeMax, feeMin, feeMin, feeMin, openTo, openFrom);
//	public static final Market US_NYSE = new Market("Dow Jones Industrial Avrg.", "NYSE", "USD", "Dow Jones", "us", feePercent, feeMax, feeMin, feeMin, feeMin, openTo, openFrom);
//	public static final Market US_NASDAQ = new Market("US - Nasdaq - Composite", "NASDAQ", "USD", "NASDAQ", "us", feePercent, feeMax, feeMin, feeMin, feeMin, openTo, openFrom);
//	public static final Market EU = new Market("EU", "EU", "EUR", "Euro Stocks", "eu", feePercent, feeMax, feeMin, feeMin, feeMin, openTo, openFrom);
//	public static final Market HU = new Market("Budapest Stock Exchange", "BSE", "HUF", "Budapest Stock Exchange", "hu", feePercent, feeMax, feeMin, feeMin, feeMin, openTo, openFrom);
//	public static final Market PL = new Market("Warsaw Stock Exchange", "WSE", "PLN", "Warsaw Stock Exchange", "pl", feePercent, feeMax, feeMin, feeMin, feeMin, openTo, openFrom);
//	public static final Market RUS = new Market("Russian Trading System", "RTS", "RUB", "Russian Trading System Stock Exchange", "rus", feePercent, feeMax, feeMin, feeMin, feeMin, openTo, openFrom);
//	public static final Market JP = new Market("Tokio Stock Exchange", "TSE", "JPY", "Tokio Stock Exchange", "jp", feePercent, feeMax, feeMin, feeMin, feeMin, openTo, openFrom);
//	public static final Market SW = new Market("Swiss Exchange", "SIX", "CHF", "SIX Swiss Exchange", "sw", feePercent, feeMax, feeMin, feeMin, feeMin, openTo, openFrom);
//	public static final Market GB = new Market("London Stock Exchange", "LSE", "GBP", "London Stock Exchange", "gb", feePercent, feeMax, feeMin, feeMin, feeMin, openTo, openFrom);
	
	/**
	 * fake market used for global indeces
	 */
	public static final Market GLOBAL = new Market("Global", "GLOBAL", "", "Indices", "world", 0, 0, 0, 0, 0, Market.TYPE_FULL);
}
