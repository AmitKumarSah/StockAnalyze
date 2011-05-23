package cz.tomas.StockAnalyze.Data.Interfaces;

import java.util.Map;

import cz.tomas.StockAnalyze.Data.IStockDataProvider;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;

public interface IStockDataListener {
	void OnStockDataUpdated(IStockDataProvider sender, Map<StockItem,DayData> dataMap);
	void OnStockDataUpdateBegin(IStockDataProvider sender);
	void OnStockDataNoUpdate(IStockDataProvider sender);
}
