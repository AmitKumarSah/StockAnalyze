package cz.tomas.StockAnalyze.utils;

public class Consts {

	public static final String FLURRY_EVENT_REFRESH = "refresh";
	public static final String FLURRY_KEY_REFRESH_TARGET = "target";
	public static final String FLURRY_KEY_REFRESH_SOURCE = "source";
	
	public static final String FLURRY_EVENT_PORTFOLIO_REMOVE = "portfolio-remove";
	public static final String FLURRY_EVENT_PORTFOLIO_NEW = "portfolio-new";
	public static final String FLURRY_KEY_PORTFOLIO_NEW_SOURCE = "source";
	public static final String FLURRY_KEY_PORTFOLIO_NEW_OPERATOIN = "operation";
	
	public static final String FLURRY_EVENT_CHART_TIME_PERIOD = "chart-time-period-change";
	public static final String FLURRY_KEY_CHART_TIME_PERIOD = "time-period";
	public static final String FLURRY_KEY_CHART_TIME_SOURCE = "source";

	public static final String FLURRY_EVENT_SCHEDULED_UPDATE = "scheduled-update";
	public static final String FLURRY_KEY_SCHEDULED_UPDATE_DAY = "day";
}