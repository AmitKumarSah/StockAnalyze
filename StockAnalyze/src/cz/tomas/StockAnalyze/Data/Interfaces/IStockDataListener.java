package cz.tomas.StockAnalyze.Data.Interfaces;

import cz.tomas.StockAnalyze.Data.IStockDataProvider;

public interface IStockDataListener {
	void OnStockDataUpdated(IStockDataProvider sender);
	void OnStockDataUpdateBegin(IStockDataProvider sender);
}
