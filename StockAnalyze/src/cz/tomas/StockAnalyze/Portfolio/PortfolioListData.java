package cz.tomas.StockAnalyze.Portfolio;

import java.util.Map;

import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.PortfolioItem;
import cz.tomas.StockAnalyze.Data.Model.PortfolioSum;
import cz.tomas.StockAnalyze.Data.Model.StockItem;

public final class PortfolioListData {
	
	final Map<PortfolioItem, DayData> datas;
	/**
	 * array backed by the datas map
	 */
	final PortfolioItem[] portfolioItems;
	/**
	 * portfolio item id is a key for stock items
	 */
	final Map<String, StockItem> stockItems;
	
	public final PortfolioSum portfolioSummary;
	
	PortfolioListData(Map<PortfolioItem, DayData> datas, Map<String, StockItem> stockItems,
			PortfolioSum portfolioSummary) {
		this.datas = datas;
		PortfolioItem[] array = new PortfolioItem[datas.size()];
		this.portfolioItems = datas.keySet().toArray(array);
		this.stockItems = stockItems;
		this.portfolioSummary = portfolioSummary;
	}

	
}